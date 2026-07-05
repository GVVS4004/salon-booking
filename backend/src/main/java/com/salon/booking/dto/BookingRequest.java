package com.salon.booking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * A booking request. When the caller is an authenticated customer the guest fields are
 * ignored; otherwise guestName + guestEmail are required (guest fallback booking).
 */
public record BookingRequest(
        @NotNull Long serviceId,
        @NotNull Long staffId,
        @NotNull OffsetDateTime startTime,
        String notes,
        String guestName,
        String guestEmail,
        String guestPhone) {
}
