package com.salon.booking.web;

import com.salon.booking.dto.AvailabilityDto;
import com.salon.booking.dto.AvailabilityRequest;
import com.salon.booking.dto.ServiceDto;
import com.salon.booking.dto.ServiceRequest;
import com.salon.booking.dto.StaffDto;
import com.salon.booking.dto.StaffRequest;
import com.salon.booking.service.CatalogAdminService;
import com.salon.booking.service.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Admin CRUD for services, stylists, and their availability. */
@RestController
@RequestMapping("/api/admin")
public class AdminCatalogController {

    private final CatalogService catalog;
    private final CatalogAdminService admin;

    public AdminCatalogController(CatalogService catalog, CatalogAdminService admin) {
        this.catalog = catalog;
        this.admin = admin;
    }

    // ----- Services -----

    @GetMapping("/services")
    public List<ServiceDto> services() {
        return catalog.allServices();
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDto createService(@Valid @RequestBody ServiceRequest req) {
        return admin.createService(req);
    }

    @PutMapping("/services/{id}")
    public ServiceDto updateService(@PathVariable Long id, @Valid @RequestBody ServiceRequest req) {
        return admin.updateService(id, req);
    }

    @DeleteMapping("/services/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable Long id) {
        admin.deactivateService(id);
    }

    // ----- Staff -----

    @GetMapping("/staff")
    public List<StaffDto> staff() {
        return catalog.allStaff();
    }

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public StaffDto createStaff(@Valid @RequestBody StaffRequest req) {
        return admin.createStaff(req);
    }

    @PutMapping("/staff/{id}")
    public StaffDto updateStaff(@PathVariable Long id, @Valid @RequestBody StaffRequest req) {
        return admin.updateStaff(id, req);
    }

    @DeleteMapping("/staff/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStaff(@PathVariable Long id) {
        admin.deactivateStaff(id);
    }

    // ----- Availability -----

    @GetMapping("/staff/{id}/availability")
    public List<AvailabilityDto> availability(@PathVariable Long id) {
        return catalog.availabilityFor(id);
    }

    @PutMapping("/staff/{id}/availability")
    public List<AvailabilityDto> setAvailability(@PathVariable Long id,
                                                 @Valid @RequestBody List<AvailabilityRequest> windows) {
        return admin.setAvailability(id, windows);
    }
}
