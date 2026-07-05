package com.salon.booking.web;

import com.salon.booking.dto.AvailabilityDto;
import com.salon.booking.dto.ServiceDto;
import com.salon.booking.dto.SlotDto;
import com.salon.booking.dto.StaffDto;
import com.salon.booking.service.AvailabilityService;
import com.salon.booking.service.CatalogService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only endpoints used to browse the catalogue and find open slots. */
@RestController
public class CatalogController {

    private final CatalogService catalog;
    private final AvailabilityService availability;

    public CatalogController(CatalogService catalog, AvailabilityService availability) {
        this.catalog = catalog;
        this.availability = availability;
    }

    @GetMapping("/api/services")
    public List<ServiceDto> services() {
        return catalog.activeServices();
    }

    /** Active stylists, optionally filtered to those who perform a given service. */
    @GetMapping("/api/staff")
    public List<StaffDto> staff(@RequestParam(required = false) Long serviceId) {
        if (serviceId != null) {
            return catalog.activeStaffForService(serviceId);
        }
        return catalog.allStaff().stream().filter(StaffDto::active).toList();
    }

    @GetMapping("/api/staff/{id}/availability")
    public List<AvailabilityDto> staffAvailability(@PathVariable Long id) {
        return catalog.availabilityFor(id);
    }

    /** Open slots for a stylist + service on a specific date. */
    @GetMapping("/api/availability")
    public List<SlotDto> slots(@RequestParam Long staffId,
                               @RequestParam Long serviceId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availability.availableSlots(staffId, serviceId, date);
    }
}
