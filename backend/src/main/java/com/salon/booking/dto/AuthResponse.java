package com.salon.booking.dto;

/**
 * Result of a successful login. {@code customer} is populated for customer logins,
 * null for the admin.
 */
public record AuthResponse(
        String token,
        String role,
        String name,
        String email,
        CustomerDto customer) {
}
