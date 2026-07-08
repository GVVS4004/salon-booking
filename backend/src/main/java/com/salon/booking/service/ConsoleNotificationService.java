package com.salon.booking.service;

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

    @Override
    public void send(NotificationMessages.Message m) {
        log.info("\n--- NOTIFICATION (console mode) ---\nTo: {}\nSubject: {}\n{}-----------------------------------",
                m.to(), m.subject(), m.body());
    }
}
