package com.salon.booking.repository;

import com.salon.booking.domain.ServiceOffering;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    List<ServiceOffering> findByActiveTrueOrderByName();
}
