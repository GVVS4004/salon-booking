package com.salon.booking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed binding for all {@code salon.*} configuration.
 */
@ConfigurationProperties(prefix = "salon")
@Getter
@Setter
public class SalonProperties {

    private Booking booking = new Booking();
    private Auth auth = new Auth();
    private Google google = new Google();
    private Notifications notifications = new Notifications();
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Booking {
        private int slotIntervalMinutes = 15;
        private int maxAdvanceDays = 60;
        private String timezone = "Asia/Kolkata";
    }

    @Getter
    @Setter
    public static class Auth {
        private String jwtSecret;
        private long jwtTtlMinutes = 720;
        private String adminEmail;
        private String adminPassword;
    }

    @Getter
    @Setter
    public static class Google {
        /** OAuth client id. Blank disables Google Sign-In (guest booking still works). */
        private String clientId = "";

        public boolean isEnabled() {
            return clientId != null && !clientId.isBlank();
        }
    }

    @Getter
    @Setter
    public static class Notifications {
        /** {@code console} (log only) or {@code email} (send via SMTP). */
        private String mode = "console";
        private String from = "no-reply@salon.local";
        private int reminderLeadHours = 24;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }
}
