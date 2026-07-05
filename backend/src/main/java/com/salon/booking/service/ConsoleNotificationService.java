package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Appointment;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default notification transport: logs the message instead of sending it. Lets the whole
 * system run with no SMTP configured (dev / local testing).
 */
@Service
@ConditionalOnProperty(name = "salon.notifications.mode", havingValue = "console", matchIfMissing = true)
public class ConsoleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);

    private final ZoneId zone;

    public ConsoleNotificationService(SalonProperties props) {
        this.zone = ZoneId.of(props.getBooking().getTimezone());
    }

    private void logMessage(NotificationMessages.Message m) {
        log.info("\n--- NOTIFICATION (console mode) ---\nTo: {}\nSubject: {}\n{}-----------------------------------",
                m.to(), m.subject(), m.body());
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        logMessage(NotificationMessages.confirmation(appointment, zone));
    }

    @Override
    public void sendCancellation(Appointment appointment) {
        logMessage(NotificationMessages.cancellation(appointment, zone));
    }

    @Override
    public void sendReminder(Appointment appointment) {
        logMessage(NotificationMessages.reminder(appointment, zone));
    }
}
