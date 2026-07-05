package com.salon.booking.dto;

/** Public auth configuration the frontend needs to render the Google Sign-In button. */
public record AuthConfigResponse(
        boolean googleEnabled,
        String googleClientId) {
}
