package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.repository;

import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByPickupLocalityIgnoreCase(String locality);
}
