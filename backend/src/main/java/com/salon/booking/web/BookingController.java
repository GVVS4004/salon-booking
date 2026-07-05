package com.salon.booking.web;

import com.salon.booking.dto.AppointmentDto;
import com.salon.booking.dto.BookingRequest;
import com.salon.booking.exception.UnauthorizedException;
import com.salon.booking.security.AuthPrincipal;
import com.salon.booking.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Create a booking. Uses the signed-in customer if present, else guest details. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto book(@Valid @RequestBody BookingRequest req,
                               @AuthenticationPrincipal AuthPrincipal principal) {
        Long customerId = (principal != null && !principal.isAdmin()) ? principal.customerId() : null;
        return bookingService.book(req, customerId);
    }

    /** The signed-in customer's own appointments. */
    @GetMapping("/me")
    public List<AppointmentDto> myBookings(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null || principal.customerId() == null) {
            throw new UnauthorizedException("Please sign in to view your bookings.");
        }
        return bookingService.forCustomer(principal.customerId());
    }

    @PostMapping("/{id}/cancel")
    public AppointmentDto cancel(@PathVariable Long id,
                                 @AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Please sign in to cancel a booking.");
        }
        return bookingService.cancel(id, principal.customerId(), principal.isAdmin());
    }
}
