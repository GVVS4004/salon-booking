package com.salon.booking.service;

import com.salon.booking.config.SalonProperties;
import com.salon.booking.domain.Appointment;
import com.salon.booking.domain.AppointmentStatus;
import com.salon.booking.repository.AppointmentRepository;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically sends reminders for appointments starting within the configured lead time.
 * Each appointment is reminded at most once (tracked by {@code reminder_sent}).
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final AppointmentRepository appointments;
    private final NotificationService notifications;
    private final ZoneId zone;
    private final int leadHours;

    public ReminderScheduler(AppointmentRepository appointments,
                             NotificationService notifications,
                             SalonProperties props) {
        this.appointments = appointments;
        this.notifications = notifications;
        this.zone = ZoneId.of(props.getBooking().getTimezone());
        this.leadHours = props.getNotifications().getReminderLeadHours();
    }

    /** Runs every 15 minutes (and 30s after startup). */
    @Scheduled(fixedDelay = 15 * 60 * 1000, initialDelay = 30 * 1000)
    @Transactional
    public void sendDueReminders() {
        OffsetDateTime now = OffsetDateTime.now(zone);
        OffsetDateTime until = now.plusHours(leadHours);

        List<Appointment> due = appointments
                .findByStatusAndReminderSentFalseAndStartTimeBetween(AppointmentStatus.BOOKED, now, until);
        if (due.isEmpty()) {
            return;
        }

        log.info("Sending {} appointment reminder(s)", due.size());
        for (Appointment appt : due) {
            try {
                notifications.sendReminder(appt);
                appt.setReminderSent(true);
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}: {}", appt.getId(), e.getMessage());
            }
        }
        appointments.saveAll(due);
    }
}
