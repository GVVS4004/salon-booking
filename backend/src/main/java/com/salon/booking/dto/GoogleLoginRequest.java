package com.salon.booking.dto;

import jakarta.validation.constraints.NotBlank;

/** The ID token (JWT credential) returned by Google Identity Services on the frontend. */
public record GoogleLoginRequest(
        @NotBlank String credential) {
}
