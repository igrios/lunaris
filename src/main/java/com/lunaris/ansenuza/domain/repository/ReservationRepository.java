package com.lunaris.ansenuza.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // 🌟 1. LA FIRMA CRUCIAL: Soluciona los errores de compilación de Maven en el Service
    boolean existsByReservationCode(String reservationCode);

    // 🤖 2. BUSCADOR POR CÓDIGO: Necesario para que el Bot valide y procese la BAJA (Opción 5)
    Optional<Reservation> findByReservationCode(String reservationCode);

    // 📱 3. BUSCADOR POR TELÉFONO: Permite a la consulta del Bot (Opción 4) traer el historial por nro de celular
    @Query("SELECT r FROM Reservation r WHERE r.passenger.phone = :phone")
    List<Reservation> findByPassengerPhone(@Param("phone") String phone);

    // 🌟 4. LA SECUENCIA: Cuenta cuántas reservas hay en esa ruta exacta y fecha para armar el código base
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.pickupLocality = :origin AND r.destination = :dest AND r.travelDate = :date")
    long countSequenceByRouteAndDate(
        @Param("origin") String origin, 
        @Param("dest") String dest, 
        @Param("date") LocalDate date
    );

    // 🔄 Métodos preexistentes del repositorio
    List<Reservation> findByTravelDate(LocalDate travelDate);

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