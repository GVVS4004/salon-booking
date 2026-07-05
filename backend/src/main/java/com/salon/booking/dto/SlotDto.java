package com.salon.booking.dto;

import java.time.OffsetDateTime;

/** An available appointment slot for a stylist + service. */
public record SlotDto(
        OffsetDateTime start,
        OffsetDateTime end) {
}
