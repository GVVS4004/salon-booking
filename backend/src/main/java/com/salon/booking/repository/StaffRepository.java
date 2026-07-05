package com.salon.booking.repository;

import com.salon.booking.domain.Staff;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findByActiveTrueOrderByName();

    /** Active stylists who can perform the given service. */
    List<Staff> findByActiveTrueAndServices_IdOrderByName(Long serviceId);
}
