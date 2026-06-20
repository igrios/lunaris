package com.lunaris.ansenuza.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // 🔄 Dejamos este acá para que los otros 6 archivos compilen sin romperse
    List<Reservation> findByTravelDate(LocalDate travelDate);

    // 🆕 Este es el nuevo que vamos a usar solo para la tabla del Panel de Operaciones
    List<Reservation> findByTravelDateAndStatusNot(LocalDate travelDate, String status);

    List<Reservation> findByPassengerOrderByTravelDateAsc(Passenger passenger);

    List<Reservation> findByPassenger(Passenger passenger);

    // 📊 Suma pasajeros reales para las tarjetas del panel ignorando los 'CANCELLED'
    @Query("SELECT COALESCE(SUM(r.passengerCount), 0) FROM Reservation r " +
           "WHERE r.travelDate = :fecha " +
           "AND r.notes LIKE %:horario% " +
           "AND r.status != 'CANCELLED'")
    int countPassengersByReturnDateAndNotesContaining(@Param("fecha") LocalDate fecha, @Param("horario") String horario);
}