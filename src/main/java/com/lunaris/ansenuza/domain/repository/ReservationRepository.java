package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository
        extends JpaRepository<Reservation, UUID> {


          List<Reservation> findByTravelDate(LocalDate travelDate);
}