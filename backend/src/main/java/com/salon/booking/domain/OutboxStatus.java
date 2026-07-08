package com.salon.booking.domain;

public enum OutboxStatus {
    /** Awaiting delivery (or a retry). */
    PENDING,
    /** Delivered successfully. */
    SENT,
    /** Gave up after the maximum number of attempts. */
    FAILED
}
