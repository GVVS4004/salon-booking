package com.salon.booking.repository;

import com.salon.booking.domain.Appointment;
import com.salon.booking.domain.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Non-cancelled appointments for a stylist that overlap the [start, end) window. */
    @Query("""
            select a from Appointment a
            where a.staff.id = :staffId
              and a.status <> com.salon.booking.domain.AppointmentStatus.CANCELLED
              and a.startTime < :end
              and a.endTime > :start
            order by a.startTime""")
    List<Appointment> findActiveOverlapping(@Param("staffId") Long staffId,
                                            @Param("start") OffsetDateTime start,
                                            @Param("end") OffsetDateTime end);

    List<Appointment> findByCustomerIdOrderByStartTimeDesc(Long customerId);

    /** Appointments (any stylist) within a window — used by the admin schedule view. */
    List<Appointment> findByStartTimeBetweenOrderByStartTime(OffsetDateTime from, OffsetDateTime to);

    List<Appointment> findByStaffIdAndStartTimeBetweenOrderByStartTime(Long staffId,
                                                                       OffsetDateTime from,
                                                                       OffsetDateTime to);

    /** Used by the reminder scheduler. */
    List<Appointment> findByStatusAndReminderSentFalseAndStartTimeBetween(AppointmentStatus status,
                                                                          OffsetDateTime from,
                                                                          OffsetDateTime to);
}
