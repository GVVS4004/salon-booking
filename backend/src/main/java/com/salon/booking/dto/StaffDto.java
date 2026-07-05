package com.salon.booking.dto;

import java.util.List;

public record StaffDto(
        Long id,
        String name,
        String email,
        boolean active,
        List<Long> serviceIds) {
}
