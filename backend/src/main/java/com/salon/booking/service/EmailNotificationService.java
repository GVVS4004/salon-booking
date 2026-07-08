package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends real email via the configured SMTP server. Enabled with
 * {@code salon.notifications.mode=email}. Exceptions propagate to the outbox dispatcher,
 * which records the failure and schedules a retry.
 */
@Service
@ConditionalOnProperty(name = "salon.notifications.mode", havingValue = "email")
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailNotificationService(JavaMailSender mailSender, SalonProperties props) {
        this.mailSender = mailSender;
        this.from = props.getNotifications().getFrom();
    }

    @Override
    public void send(NotificationMessages.Message m) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(m.to());
        mail.setSubject(m.subject());
        mail.setText(m.body());
        mailSender.send(mail); // throws on failure -> dispatcher retries
        log.info("Sent email '{}' to {}", m.subject(), m.to());
    }
}
