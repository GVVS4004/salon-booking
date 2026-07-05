package com.salon.booking.exception;

/** Thrown when a slot is no longer available (double-booking). Maps to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
