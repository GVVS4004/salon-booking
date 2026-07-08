package com.salon.booking.repository;

import com.salon.booking.domain.OutboxNotification;
import com.salon.booking.domain.OutboxStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxNotificationRepository extends JpaRepository<OutboxNotification, Long> {

    /** Due notifications for the dispatcher: PENDING and past their next-attempt time. */
    List<OutboxNotification> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAt(
            OutboxStatus status, OffsetDateTime cutoff, Limit limit);
}
