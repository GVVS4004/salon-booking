package com.salon.booking.service;

/**
 * Delivers a pre-built notification to the customer. Implementations are selected by the
 * {@code salon.notifications.mode} property (console vs email) and run asynchronously, so
 * delivery latency or failure never blocks or breaks the booking request.
 */
public interface NotificationService {

    void send(NotificationMessages.Message message);
}
