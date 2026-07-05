package com.salon.booking.web;

import com.salon.booking.dto.AppointmentDto;
import com.salon.booking.dto.StatusUpdateRequest;
import com.salon.booking.security.AuthPrincipal;
import com.salon.booking.service.BookingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin view of the schedule and appointment status management. */
@RestController
@RequestMapping("/api/admin/appointments")
public class AdminAppointmentController {

    private final BookingService bookingService;

    public AdminAppointmentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<AppointmentDto> schedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long staffId) {
        return bookingService.forScheduleDates(from, to, staffId);
    }

    @PostMapping("/{id}/status")
    public AppointmentDto updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return bookingService.setStatus(id, req.status());
    }

    @PostMapping("/{id}/cancel")
    public AppointmentDto cancel(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return bookingService.cancel(id, principal.customerId(), true);
    }
}
