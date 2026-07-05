package com.salon.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ServiceRequest(
        @NotBlank String name,
        String description,
        @Positive int durationMinutes,
        @PositiveOrZero int priceCents,
        Boolean active) {
}
