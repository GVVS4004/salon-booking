package com.salon.booking.service;

import com.salon.booking.dto.AvailabilityDto;
import com.salon.booking.dto.ServiceDto;
import com.salon.booking.dto.StaffDto;
import com.salon.booking.exception.NotFoundException;
import com.salon.booking.repository.ServiceOfferingRepository;
import com.salon.booking.repository.StaffAvailabilityRepository;
import com.salon.booking.repository.StaffRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side access to the catalogue (services, stylists, availability). */
@Service
public class CatalogService {

    private final ServiceOfferingRepository serviceRepo;
    private final StaffRepository staffRepo;
    private final StaffAvailabilityRepository availabilityRepo;

    public CatalogService(ServiceOfferingRepository serviceRepo,
                          StaffRepository staffRepo,
                          StaffAvailabilityRepository availabilityRepo) {
        this.serviceRepo = serviceRepo;
        this.staffRepo = staffRepo;
        this.availabilityRepo = availabilityRepo;
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> activeServices() {
        return serviceRepo.findByActiveTrueOrderByName().stream().map(Mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffDto> activeStaffForService(Long serviceId) {
        if (!serviceRepo.existsById(serviceId)) {
            throw new NotFoundException("Service not found: " + serviceId);
        }
        return staffRepo.findByActiveTrueAndServices_IdOrderByName(serviceId).stream()
                .map(Mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityDto> availabilityFor(Long staffId) {
        if (!staffRepo.existsById(staffId)) {
            throw new NotFoundException("Stylist not found: " + staffId);
        }
        return availabilityRepo.findByStaffIdOrderByDayOfWeekAscStartTimeAsc(staffId).stream()
                .map(Mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> allServices() {
        return serviceRepo.findAll().stream().map(Mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffDto> allStaff() {
        return staffRepo.findAll().stream().map(Mapper::toDto).toList();
    }
}
