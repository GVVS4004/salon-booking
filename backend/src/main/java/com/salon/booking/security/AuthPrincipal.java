package com.salon.booking.security;

/**
 * The authenticated caller, stored as the Spring Security principal.
 * {@code customerId} is null for the admin.
 */
public record AuthPrincipal(String role, Long customerId, String email, String name) {

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
