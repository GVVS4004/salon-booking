package com.salon.booking.dto;

import com.salon.booking.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull AppointmentStatus status) {
}
