package com.salon.booking.repository;

import com.salon.booking.domain.StaffAvailability;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAvailabilityRepository extends JpaRepository<StaffAvailability, Long> {

    List<StaffAvailability> findByStaffIdOrderByDayOfWeekAscStartTimeAsc(Long staffId);

    List<StaffAvailability> findByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);
}
