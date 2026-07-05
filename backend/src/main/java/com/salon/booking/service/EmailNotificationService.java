package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Appointment;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends real email via the configured SMTP server. Enabled with
 * {@code salon.notifications.mode=email}.
 */
@Service
@ConditionalOnProperty(name = "salon.notifications.mode", havingValue = "email")
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final ZoneId zone;

    public EmailNotificationService(JavaMailSender mailSender, SalonProperties props) {
        this.mailSender = mailSender;
        this.from = props.getNotifications().getFrom();
        this.zone = ZoneId.of(props.getBooking().getTimezone());
    }

    private void send(NotificationMessages.Message m) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(m.to());
            mail.setSubject(m.subject());
            mail.setText(m.body());
            mailSender.send(mail);
            log.info("Sent email '{}' to {}", m.subject(), m.to());
        } catch (Exception e) {
            // Never let a delivery failure break the booking flow.
            log.error("Failed to send email '{}' to {}: {}", m.subject(), m.to(), e.getMessage());
        }
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        send(NotificationMessages.confirmation(appointment, zone));
    }

    @Override
    public void sendCancellation(Appointment appointment) {
        send(NotificationMessages.cancellation(appointment, zone));
    }

    @Override
    public void sendReminder(Appointment appointment) {
        send(NotificationMessages.reminder(appointment, zone));
    }
}
