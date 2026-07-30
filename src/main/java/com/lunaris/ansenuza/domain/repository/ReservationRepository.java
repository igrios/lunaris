package com.lunaris.ansenuza.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    // 🌟 1. LA FIRMA CRUCIAL: Soluciona los errores de compilación de Maven en el Service
    boolean existsByReservationCode(String reservationCode);

    // 🤖 2. BUSCADOR POR CÓDIGO: Necesario para que el Bot valide y procese la BAJA (Opción 5)
    Optional<Reservation> findByReservationCode(String reservationCode);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.reservationCode = CONCAT(:groupCode, '-IDA')
              OR r.reservationCode = CONCAT(:groupCode, '-VUELTA')
           """)
    List<Reservation> findReservationGroup(@Param("groupCode") String groupCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT r FROM Reservation r
           WHERE r.reservationCode = CONCAT(:groupCode, '-IDA')
              OR r.reservationCode = CONCAT(:groupCode, '-VUELTA')
           ORDER BY r.reservationCode
           """)
    List<Reservation> findReservationGroupForUpdate(@Param("groupCode") String groupCode);

    // 📱 3. BUSCADOR POR TELÉFONO: Permite a la consulta del Bot (Opción 4) traer el historial por nro de celular
    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           ORDER BY r.travelDate ASC, r.departureSchedule ASC, r.createdAt DESC
           """)
    List<Reservation> findByPassengerPhone(@Param("phone") String phone);

    // 🤖 BUSCADOR PARA BOT: Trae las reservas activas confirmadas de un pasajero para poder listar en botones
    @Query("SELECT r FROM Reservation r WHERE r.passenger.phone = :phone AND r.status = :status")
    List<Reservation> findByPassengerPhoneAndStatus(@Param("phone") String phone, @Param("status") String status);

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

    List<Reservation> findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(
            Passenger passenger);

    List<Reservation> findByPassenger(Passenger passenger);

    @Query("""
           SELECT COALESCE(SUM(CASE WHEN r.passengerCount IS NULL OR r.passengerCount < 1
                                   THEN 1 ELSE r.passengerCount END), 0)
           FROM Reservation r
           WHERE r.travelDate = :date
           AND COALESCE(r.departureSchedule, '03:00 AM') = :schedule
           AND r.status <> 'CANCELLED'
           """)
    long countReservedSeats(
            @Param("date") LocalDate date,
            @Param("schedule") String schedule);

    // 📊 Suma pasajeros reales para las tarjetas del panel ignorando los 'CANCELLED'
    @Query("SELECT COALESCE(SUM(r.passengerCount), 0) FROM Reservation r " +
           "WHERE r.travelDate = :fecha " +
           "AND r.notes LIKE %:horario% " +
           "AND r.status != 'CANCELLED'")
    int countPassengersByReturnDateAndNotesContaining(@Param("fecha") LocalDate fecha, @Param("horario") String horario);

    // 🧾 Listado para el panel de Facturación (reservas con pago confirmado)
    List<Reservation> findByStatus(String status);

    // 💰 INGRESO DE DINERO: suma de todos los tramos confirmados; en ida y vuelta
    // cada tramo lleva la mitad del importe neto, por lo que la suma es el total cobrado.
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Reservation r " +
           "WHERE r.paymentConfirmedAt >= :start AND r.paymentConfirmedAt < :end " +
           "AND r.status <> 'CANCELLED'")
    BigDecimal sumConfirmedIncomeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.paymentConfirmedAt >= :start AND r.paymentConfirmedAt < :end " +
           "AND r.status <> 'CANCELLED' " +
           "AND (r.reservationCode IS NULL OR r.reservationCode NOT LIKE '%-VUELTA')")
    long countConfirmedIncomeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 💡 NUEVO MÉTODO FILTRADO: Para limpiar la grilla de vueltas abiertas en el controlador web
    @Query("""
           SELECT r FROM Reservation r
           WHERE r.travelDate = :fechaCentinela
           AND r.driver IS NULL
           AND r.status <> 'CANCELLED'
           """)
    List<Reservation> findVueltasAbiertasActive(@Param("fechaCentinela") LocalDate fechaCentinela);

    @Query("""
           SELECT ret FROM Reservation ret
           WHERE ret.travelDate = :date
           AND ret.status <> 'CANCELLED'
           AND ret.reservationCode LIKE '%-VUELTA'
           AND EXISTS (
               SELECT outbound.id FROM Reservation outbound
               WHERE outbound.reservationCode = CONCAT(SUBSTRING(ret.reservationCode, 1, LENGTH(ret.reservationCode) - 7), '-IDA')
               AND outbound.travelStatus = :travelStatus
           )
           """)
    List<Reservation> findScheduledReturnsWithRealizedOutbound(
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.returnDate = :date
           AND r.travelStatus = :travelStatus
           AND r.status <> 'CANCELLED'
           """)
    List<Reservation> findRealizedOutboundReservationsWithReturnDate(
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           AND r.travelDate = :date
           AND r.status <> 'CANCELLED'
           AND r.reservationCode LIKE '%-VUELTA'
           ORDER BY r.createdAt DESC
           """)
    List<Reservation> findActiveReturnReservationsByPassengerPhoneAndDate(
            @Param("phone") String phone,
            @Param("date") LocalDate date);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           AND r.returnDate = :date
           AND r.travelStatus = :travelStatus
           AND r.status <> 'CANCELLED'
           ORDER BY r.createdAt DESC
           """)
    List<Reservation> findRealizedOutboundReservationsByPassengerPhoneAndReturnDate(
            @Param("phone") String phone,
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.travelDate BETWEEN :startDate AND :endDate
           AND r.status <> 'CANCELLED'
           ORDER BY r.travelDate ASC, r.departureSchedule ASC
           """)
    List<Reservation> findByDriverIdAndTravelDateBetween(
            @Param("driverId") UUID driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND (r.status IS NULL OR r.status <> 'CANCELLED')
           ORDER BY r.travelDate ASC, r.routeSequence ASC NULLS LAST,
                    r.departureSchedule ASC
           """)
    List<Reservation> findAllAssignedByDriverId(@Param("driverId") UUID driverId);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.travelDate = :travelDate
           AND (r.status IS NULL OR r.status <> 'CANCELLED')
           ORDER BY r.routeSequence ASC NULLS LAST
           """)
    List<Reservation> findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
            @Param("driverId") UUID driverId, @Param("travelDate") LocalDate travelDate);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.status <> 'CANCELLED'
           AND (
               r.travelDate = :effectiveDate
               OR (r.reservationCode LIKE '%-VUELTA' AND r.returnDate = :effectiveDate)
           )
           ORDER BY r.routeSequence ASC NULLS LAST
           """)
    List<Reservation> findRouteByEffectiveDate(
            @Param("driverId") UUID driverId,
            @Param("effectiveDate") LocalDate effectiveDate);
}
