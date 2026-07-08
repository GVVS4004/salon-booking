package com.salon.booking.service;

import com.salon.booking.domain.OutboxNotification;
import com.salon.booking.domain.OutboxStatus;
import com.salon.booking.repository.OutboxNotificationRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Drains the outbox: delivers PENDING notifications and, on failure, schedules a retry with
 * exponential backoff. After {@link #MAX_ATTEMPTS} tries a message is marked FAILED (dead-letter).
 *
 * <p>Runs on a single scheduler thread ({@code fixedDelay} waits for each run to finish, so
 * runs never overlap). If this is ever scaled to multiple instances, switch the fetch query to
 * {@code SELECT ... FOR UPDATE SKIP LOCKED} so instances don't process the same rows.
 */
@Component
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private static final int BATCH_SIZE = 10;
    private static final int MAX_ATTEMPTS = 6;
    private static final int MAX_BACKOFF_MINUTES = 60;
    private static final int MAX_ERROR_LENGTH = 1000;

    private final OutboxNotificationRepository repo;
    private final NotificationService notifications;

    public OutboxDispatcher(OutboxNotificationRepository repo, NotificationService notifications) {
        this.repo = repo;
        this.notifications = notifications;
    }

    @Scheduled(fixedDelay = 15_000, initialDelay = 10_000)
    @Transactional
    public void dispatch() {
        OffsetDateTime now = OffsetDateTime.now();
        List<OutboxNotification> due = repo.findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAt(
                OutboxStatus.PENDING, now, Limit.of(BATCH_SIZE));
        if (due.isEmpty()) {
            return;
        }

        for (OutboxNotification n : due) {
            try {
                notifications.send(new NotificationMessages.Message(n.getRecipient(), n.getSubject(), n.getBody()));
                n.setStatus(OutboxStatus.SENT);
                n.setSentAt(now);
                n.setLastError(null);
            } catch (Exception e) {
                int attempts = n.getAttempts() + 1;
                n.setAttempts(attempts);
                n.setLastError(truncate(e.getMessage()));
                if (attempts >= MAX_ATTEMPTS) {
                    n.setStatus(OutboxStatus.FAILED);
                    log.error("Notification permanently FAILED after {} attempts (to={}, subject='{}'): {}",
                            attempts, n.getRecipient(), n.getSubject(), e.getMessage());
                } else {
                    long delay = Math.min(MAX_BACKOFF_MINUTES, (long) Math.pow(2, attempts));
                    n.setNextAttemptAt(now.plusMinutes(delay));
                    log.warn("Notification send failed (attempt {}/{}), retrying in {} min (to={}): {}",
                            attempts, MAX_ATTEMPTS, delay, n.getRecipient(), e.getMessage());
                }
            }
        }
        repo.saveAll(due);
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= MAX_ERROR_LENGTH ? s : s.substring(0, MAX_ERROR_LENGTH);
    }
}
