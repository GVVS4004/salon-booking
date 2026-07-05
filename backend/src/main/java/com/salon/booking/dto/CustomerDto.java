package com.salon.booking.dto;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String phone) {
}
