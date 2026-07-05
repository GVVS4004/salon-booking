package com.salon.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record StaffRequest(
        @NotBlank String name,
        @Email String email,
        Boolean active,
        List<Long> serviceIds) {
}
