package com.salon.booking.service;

import com.salon.booking.domain.OutboxNotification;
import com.salon.booking.repository.OutboxNotificationRepository;
import org.springframework.stereotype.Service;

/**
 * Persists notifications to the outbox. Called from within a business transaction so the
 * notification is committed atomically with the booking that triggered it.
 */
@Service
public class OutboxService {

    private final OutboxNotificationRepository repo;

    public OutboxService(OutboxNotificationRepository repo) {
        this.repo = repo;
    }

    public void enqueue(NotificationMessages.Message message) {
        OutboxNotification n = new OutboxNotification();
        n.setRecipient(message.to());
        n.setSubject(message.subject());
        n.setBody(message.body());
        repo.save(n);
    }
}
