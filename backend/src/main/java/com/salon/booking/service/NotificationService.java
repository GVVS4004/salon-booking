package com.salon.booking.service;

import com.salon.booking.domain.Appointment;

/**
 * Sends transactional notifications to customers. Implementations are selected by the
 * {@code salon.notifications.mode} property (console vs email).
 */
public interface NotificationService {

    void sendBookingConfirmation(Appointment appointment);

    void sendCancellation(Appointment appointment);

    void sendReminder(Appointment appointment);
}
