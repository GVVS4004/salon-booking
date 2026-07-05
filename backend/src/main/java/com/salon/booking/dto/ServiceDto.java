package com.salon.booking.dto;

public record ServiceDto(
        Long id,
        String name,
        String description,
        int durationMinutes,
        int priceCents,
        boolean active) {
}
