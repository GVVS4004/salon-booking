package com.salon.booking.exception;

/** Thrown when authentication fails or is missing. Maps to HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
