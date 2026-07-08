package com.salon.booking.service;

import com.salon.booking.domain.Appointment;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Builds the subject/body text for customer notifications. */
public final class NotificationMessages {

    private static final DateTimeFormatter WHEN =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy 'at' h:mm a", Locale.ENGLISH);

    private NotificationMessages() {
    }

    /** An immutable, ready-to-send message — decoupled from JPA entities so it can be sent async. */
    public record Message(String to, String subject, String body) {
    }

    public static Message confirmation(Appointment a, ZoneId zone) {
        String when = a.getStartTime().atZoneSameInstant(zone).format(WHEN);
        String subject = "Your booking is confirmed - " + a.getService().getName();
        String body = """
                Hi %s,

                Your appointment is confirmed:

                  Service:  %s
                  Stylist:  %s
                  When:     %s
                  Duration: %d minutes

                See you soon!
                """.formatted(
                a.getCustomer().getName(),
                a.getService().getName(),
                a.getStaff().getName(),
                when,
                a.getService().getDurationMinutes());
        return new Message(a.getCustomer().getEmail(), subject, body);
    }

    public static Message cancellation(Appointment a, ZoneId zone) {
        String when = a.getStartTime().atZoneSameInstant(zone).format(WHEN);
        String subject = "Your booking was cancelled - " + a.getService().getName();
        String body = """
                Hi %s,

                Your appointment for %s with %s on %s has been cancelled.

                If this wasn't you, please contact the salon.
                """.formatted(
                a.getCustomer().getName(),
                a.getService().getName(),
                a.getStaff().getName(),
                when);
        return new Message(a.getCustomer().getEmail(), subject, body);
    }

    public static Message reminder(Appointment a, ZoneId zone) {
        String when = a.getStartTime().atZoneSameInstant(zone).format(WHEN);
        String subject = "Reminder: upcoming appointment - " + a.getService().getName();
        String body = """
                Hi %s,

                This is a reminder for your upcoming appointment:

                  Service: %s
                  Stylist: %s
                  When:    %s

                We look forward to seeing you!
                """.formatted(
                a.getCustomer().getName(),
                a.getService().getName(),
                a.getStaff().getName(),
                when);
        return new Message(a.getCustomer().getEmail(), subject, body);
    }
}
