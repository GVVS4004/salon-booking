package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Appointment;
import com.salon.booking.domain.ServiceOffering;
import com.salon.booking.domain.Staff;
import com.salon.booking.domain.StaffAvailability;
import com.salon.booking.dto.SlotDto;
import com.salon.booking.exception.NotFoundException;
import com.salon.booking.repository.AppointmentRepository;
import com.salon.booking.repository.ServiceOfferingRepository;
import com.salon.booking.repository.StaffAvailabilityRepository;
import com.salon.booking.repository.StaffRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes bookable time slots for a stylist + service on a given date, honouring the
 * stylist's weekly working hours and existing appointments.
 */
@Service
public class AvailabilityService {

    private final StaffRepository staffRepo;
    private final ServiceOfferingRepository serviceRepo;
    private final StaffAvailabilityRepository availabilityRepo;
    private final AppointmentRepository appointmentRepo;

    private final ZoneId zone;
    private final int intervalMinutes;
    private final int maxAdvanceDays;

    public AvailabilityService(StaffRepository staffRepo,
                               ServiceOfferingRepository serviceRepo,
                               StaffAvailabilityRepository availabilityRepo,
                               AppointmentRepository appointmentRepo,
                               SalonProperties props) {
        this.staffRepo = staffRepo;
        this.serviceRepo = serviceRepo;
        this.availabilityRepo = availabilityRepo;
        this.appointmentRepo = appointmentRepo;
        this.zone = ZoneId.of(props.getBooking().getTimezone());
        this.intervalMinutes = props.getBooking().getSlotIntervalMinutes();
        this.maxAdvanceDays = props.getBooking().getMaxAdvanceDays();
    }

    @Transactional(readOnly = true)
    public List<SlotDto> availableSlots(Long staffId, Long serviceId, LocalDate date) {
        ServiceOffering service = serviceRepo.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found: " + serviceId));
        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Stylist not found: " + staffId));

        // No slots if the stylist is inactive or doesn't offer this service.
        if (!staff.isActive() || !service.isActive()) {
            return List.of();
        }
        boolean canPerform = staff.getServices().stream()
                .anyMatch(s -> s.getId().equals(serviceId));
        if (!canPerform) {
            return List.of();
        }

        LocalDate today = LocalDate.now(zone);
        if (date.isBefore(today) || date.isAfter(today.plusDays(maxAdvanceDays))) {
            return List.of();
        }

        DayOfWeek dow = date.getDayOfWeek();
        List<StaffAvailability> windows = availabilityRepo.findByStaffIdAndDayOfWeek(staffId, dow);
        if (windows.isEmpty()) {
            return List.of();
        }

        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();
        List<Appointment> booked = appointmentRepo.findActiveOverlapping(staffId, dayStart, dayEnd);

        int duration = service.getDurationMinutes();
        OffsetDateTime now = OffsetDateTime.now(zone);

        List<SlotDto> slots = new ArrayList<>();
        for (StaffAvailability w : windows) {
            LocalTime cursor = w.getStartTime();
            while (true) {
                LocalTime slotEndLocal = cursor.plusMinutes(duration);
                // Stop if the slot would run past the window or wrap past midnight.
                if (!slotEndLocal.isAfter(cursor) || slotEndLocal.isAfter(w.getEndTime())) {
                    break;
                }

                OffsetDateTime start = date.atTime(cursor).atZone(zone).toOffsetDateTime();
                OffsetDateTime end = start.plusMinutes(duration);

                boolean inPast = start.isBefore(now);
                boolean overlaps = booked.stream()
                        .anyMatch(b -> start.isBefore(b.getEndTime()) && end.isAfter(b.getStartTime()));
                if (!inPast && !overlaps) {
                    slots.add(new SlotDto(start, end));
                }

                LocalTime next = cursor.plusMinutes(intervalMinutes);
                if (!next.isAfter(cursor)) {
                    break; // wrapped past midnight
                }
                cursor = next;
            }
        }

        slots.sort(Comparator.comparing(SlotDto::start));
        return slots;
    }
}
