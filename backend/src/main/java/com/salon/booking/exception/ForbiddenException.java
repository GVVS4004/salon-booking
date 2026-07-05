package com.salon.booking.exception;

/** Thrown when the caller may not act on a resource. Maps to HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
