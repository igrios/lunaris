package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverOperationsRepository {
    record Trip(UUID id, UUID driverId, String status, LocalDate date, Instant departureAt) {}
    record Ticket(UUID reservationId, UUID tripId, String status, Instant expiresAt, Instant consumedAt,
            Instant pickupAt, BigDecimal fare, BigDecimal commission, boolean approvedPayment) {}
    record RoutePassenger(UUID tripId, UUID reservationId, String name, String phone, String status,
            String pickupAddress, String dropoffAddress, Instant pickupAt, Instant dropoffAt) {}

    Optional<Trip> lockTrip(UUID tripId);
    Optional<Ticket> lockTicket(String tokenHash, UUID tripId);
    void consume(UUID reservationId, Instant now);
    void markCheckedIn(UUID reservationId, UUID driverId, Instant now);
    void earn(UUID reservationId, UUID driverId, BigDecimal amount, Instant now);
    List<RoutePassenger> routeSheet(UUID driverId, LocalDate date);
}
