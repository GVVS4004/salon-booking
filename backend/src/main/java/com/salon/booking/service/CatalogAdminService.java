package com.salon.booking.service;

import com.salon.booking.domain.ServiceOffering;
import com.salon.booking.domain.Staff;
import com.salon.booking.domain.StaffAvailability;
import com.salon.booking.dto.AvailabilityDto;
import com.salon.booking.dto.AvailabilityRequest;
import com.salon.booking.dto.ServiceDto;
import com.salon.booking.dto.ServiceRequest;
import com.salon.booking.dto.StaffDto;
import com.salon.booking.dto.StaffRequest;
import com.salon.booking.exception.BadRequestException;
import com.salon.booking.exception.NotFoundException;
import com.salon.booking.repository.ServiceOfferingRepository;
import com.salon.booking.repository.StaffAvailabilityRepository;
import com.salon.booking.repository.StaffRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin mutations for the catalogue. */
@Service
public class CatalogAdminService {

    private final ServiceOfferingRepository serviceRepo;
    private final StaffRepository staffRepo;
    private final StaffAvailabilityRepository availabilityRepo;

    public CatalogAdminService(ServiceOfferingRepository serviceRepo,
                               StaffRepository staffRepo,
                               StaffAvailabilityRepository availabilityRepo) {
        this.serviceRepo = serviceRepo;
        this.staffRepo = staffRepo;
        this.availabilityRepo = availabilityRepo;
    }

    // ----- Services -----

    @Transactional
    public ServiceDto createService(ServiceRequest req) {
        ServiceOffering s = new ServiceOffering();
        applyServiceFields(s, req);
        return Mapper.toDto(serviceRepo.save(s));
    }

    @Transactional
    public ServiceDto updateService(Long id, ServiceRequest req) {
        ServiceOffering s = serviceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found: " + id));
        applyServiceFields(s, req);
        return Mapper.toDto(serviceRepo.save(s));
    }

    /** Soft-delete: keeps historical appointments valid, just hides the service. */
    @Transactional
    public void deactivateService(Long id) {
        ServiceOffering s = serviceRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Service not found: " + id));
        s.setActive(false);
        serviceRepo.save(s);
    }

    private void applyServiceFields(ServiceOffering s, ServiceRequest req) {
        s.setName(req.name());
        s.setDescription(req.description());
        s.setDurationMinutes(req.durationMinutes());
        s.setPriceCents(req.priceCents());
        s.setActive(req.active() == null || req.active());
    }

    // ----- Staff -----

    @Transactional
    public StaffDto createStaff(StaffRequest req) {
        Staff staff = new Staff();
        applyStaffFields(staff, req);
        return Mapper.toDto(staffRepo.save(staff));
    }

    @Transactional
    public StaffDto updateStaff(Long id, StaffRequest req) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stylist not found: " + id));
        applyStaffFields(staff, req);
        return Mapper.toDto(staffRepo.save(staff));
    }

    @Transactional
    public void deactivateStaff(Long id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stylist not found: " + id));
        staff.setActive(false);
        staffRepo.save(staff);
    }

    private void applyStaffFields(Staff staff, StaffRequest req) {
        staff.setName(req.name());
        staff.setEmail(req.email());
        staff.setActive(req.active() == null || req.active());
        Set<ServiceOffering> services = new HashSet<>();
        if (req.serviceIds() != null) {
            for (Long sid : req.serviceIds()) {
                services.add(serviceRepo.findById(sid)
                        .orElseThrow(() -> new BadRequestException("Unknown service id: " + sid)));
            }
        }
        staff.setServices(services);
    }

    // ----- Availability -----

    /** Replaces a stylist's entire weekly availability with the supplied windows. */
    @Transactional
    public List<AvailabilityDto> setAvailability(Long staffId, List<AvailabilityRequest> windows) {
        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Stylist not found: " + staffId));

        List<StaffAvailability> existing = availabilityRepo.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staffId);
        availabilityRepo.deleteAll(existing);

        List<StaffAvailability> saved = new ArrayList<>();
        if (windows != null) {
            for (AvailabilityRequest w : windows) {
                if (!w.endTime().isAfter(w.startTime())) {
                    throw new BadRequestException("End time must be after start time for " + w.dayOfWeek());
                }
                StaffAvailability a = new StaffAvailability();
                a.setStaff(staff);
                a.setDayOfWeek(w.dayOfWeek());
                a.setStartTime(w.startTime());
                a.setEndTime(w.endTime());
                saved.add(availabilityRepo.save(a));
            }
        }
        return saved.stream().map(Mapper::toDto).toList();
    }
}
