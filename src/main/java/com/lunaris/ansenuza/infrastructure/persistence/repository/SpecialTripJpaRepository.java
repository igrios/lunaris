package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.SpecialTripEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialTripJpaRepository extends JpaRepository<SpecialTripEntity, Long> {
    List<SpecialTripEntity> findAllByOrderByStartDateAsc();
    List<SpecialTripEntity> findAllByActiveTrueOrderByStartDateAsc();
}
