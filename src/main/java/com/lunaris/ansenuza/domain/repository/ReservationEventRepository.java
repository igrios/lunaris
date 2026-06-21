package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;

@Repository
public interface ReservationEventRepository extends JpaRepository<ReservationEvent, UUID> {
    
    // Método clave para cuando armemos la pantalla /timeline de Martín
    List<ReservationEvent> findByReservationIdOrderByCreatedAtAsc(UUID reservationId);
}