package com.lunaris.ansenuza.reservation.application.port.out;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findByPickupLocality(String locality);
}
