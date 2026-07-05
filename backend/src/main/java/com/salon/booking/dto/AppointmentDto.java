package com.salon.booking.dto;

import java.time.OffsetDateTime;

/** Flat view of an appointment for both customer and admin views. */
public record AppointmentDto(
        Long id,
        Long serviceId,
        String serviceName,
        int durationMinutes,
        int priceCents,
        Long staffId,
        String staffName,
        Long customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String status,
        String notes) {
}
