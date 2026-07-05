package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Appointment;
import com.salon.booking.domain.AppointmentStatus;
import com.salon.booking.domain.Customer;
import com.salon.booking.domain.ServiceOffering;
import com.salon.booking.domain.Staff;
import com.salon.booking.dto.AppointmentDto;
import com.salon.booking.dto.BookingRequest;
import com.salon.booking.dto.SlotDto;
import com.salon.booking.exception.BadRequestException;
import com.salon.booking.exception.ConflictException;
import com.salon.booking.exception.ForbiddenException;
import com.salon.booking.exception.NotFoundException;
import com.salon.booking.repository.AppointmentRepository;
import com.salon.booking.repository.ServiceOfferingRepository;
import com.salon.booking.repository.StaffRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final AppointmentRepository appointments;
    private final StaffRepository staffRepo;
    private final ServiceOfferingRepository serviceRepo;
    private final AvailabilityService availabilityService;
    private final CustomerService customerService;
    private final NotificationService notifications;
    private final ZoneId zone;

    public BookingService(AppointmentRepository appointments,
                          StaffRepository staffRepo,
                          ServiceOfferingRepository serviceRepo,
                          AvailabilityService availabilityService,
                          CustomerService customerService,
                          NotificationService notifications,
                          SalonProperties props) {
        this.appointments = appointments;
        this.staffRepo = staffRepo;
        this.serviceRepo = serviceRepo;
        this.availabilityService = availabilityService;
        this.customerService = customerService;
        this.notifications = notifications;
        this.zone = ZoneId.of(props.getBooking().getTimezone());
    }

    /**
     * Creates a booking. If {@code authCustomerId} is non-null the booking belongs to that
     * signed-in customer; otherwise the request's guest details are used.
     */
    @Transactional
    public AppointmentDto book(BookingRequest req, Long authCustomerId) {
        ServiceOffering service = serviceRepo.findById(req.serviceId())
                .orElseThrow(() -> new NotFoundException("Service not found: " + req.serviceId()));
        Staff staff = staffRepo.findById(req.staffId())
                .orElseThrow(() -> new NotFoundException("Stylist not found: " + req.staffId()));

        if (!service.isActive()) {
            throw new BadRequestException("That service is not currently offered.");
        }
        if (!staff.isActive()) {
            throw new BadRequestException("That stylist is not currently available.");
        }
        boolean canPerform = staff.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
        if (!canPerform) {
            throw new BadRequestException("That stylist does not perform the selected service.");
        }

        OffsetDateTime start = req.startTime();
        OffsetDateTime end = start.plusMinutes(service.getDurationMinutes());

        // Re-validate against live availability (working hours, past times, interval, overlaps).
        LocalDate localDate = start.atZoneSameInstant(zone).toLocalDate();
        boolean available = availabilityService.availableSlots(staff.getId(), service.getId(), localDate)
                .stream()
                .anyMatch(s -> s.start().isEqual(start));
        if (!available) {
            throw new ConflictException("That time slot is no longer available. Please pick another.");
        }

        Customer customer = (authCustomerId != null)
                ? customerService.getById(authCustomerId)
                : customerService.findOrCreateGuest(req.guestEmail(), req.guestName(), req.guestPhone());

        Appointment appt = new Appointment();
        appt.setCustomer(customer);
        appt.setStaff(staff);
        appt.setService(service);
        appt.setStartTime(start);
        appt.setEndTime(end);
        appt.setStatus(AppointmentStatus.BOOKED);
        appt.setNotes(req.notes());

        try {
            // Flush now so the DB exclusion constraint fires inside this try/catch.
            appointments.saveAndFlush(appt);
        } catch (DataIntegrityViolationException e) {
            log.warn("Double-booking prevented for staff {} at {}", staff.getId(), start);
            throw new ConflictException("That time slot was just taken. Please pick another.");
        }

        sendAfterCommit(() -> notifications.sendBookingConfirmation(appt));
        return Mapper.toDto(appt);
    }

    @Transactional
    public AppointmentDto cancel(Long appointmentId, Long requesterCustomerId, boolean isAdmin) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found: " + appointmentId));

        if (!isAdmin && !appt.getCustomer().getId().equals(requesterCustomerId)) {
            throw new ForbiddenException("You can only cancel your own appointments.");
        }
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            return Mapper.toDto(appt); // idempotent
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appointments.saveAndFlush(appt);
        sendAfterCommit(() -> notifications.sendCancellation(appt));
        return Mapper.toDto(appt);
    }

    @Transactional
    public AppointmentDto setStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found: " + appointmentId));
        appt.setStatus(status);
        appointments.saveAndFlush(appt);
        if (status == AppointmentStatus.CANCELLED) {
            sendAfterCommit(() -> notifications.sendCancellation(appt));
        }
        return Mapper.toDto(appt);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> forCustomer(Long customerId) {
        return appointments.findByCustomerIdOrderByStartTimeDesc(customerId).stream()
                .map(Mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> forScheduleDates(LocalDate from, LocalDate to, Long staffId) {
        OffsetDateTime start = from.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime end = to.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        return forSchedule(start, end, staffId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> forSchedule(OffsetDateTime from, OffsetDateTime to, Long staffId) {
        List<Appointment> found = (staffId != null)
                ? appointments.findByStaffIdAndStartTimeBetweenOrderByStartTime(staffId, from, to)
                : appointments.findByStartTimeBetweenOrderByStartTime(from, to);
        return found.stream().map(Mapper::toDto).toList();
    }

    /** Runs the action after the current transaction commits (so notifications aren't sent on rollback). */
    private void sendAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (Exception e) {
                        log.error("Notification failed after commit: {}", e.getMessage());
                    }
                }
            });
        } else {
            action.run();
        }
    }
}
