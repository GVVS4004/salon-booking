package com.salon.booking.exception;

/** Thrown for invalid input the client can correct. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
