package com.salon.booking.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityDto(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime) {
}
