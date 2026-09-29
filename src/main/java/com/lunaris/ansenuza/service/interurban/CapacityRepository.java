package com.lunaris.ansenuza.service.interurban;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Todos los escritores bloquean primero el viaje, luego los tramos por UUID ascendente. */
public interface CapacityRepository {
    record Trip(UUID id, int direction, String status, Instant closesAt) {}
    record Leg(UUID id, int ordinal) {}

    Optional<Trip> findTrip(UUID tripId);
    Optional<Trip> lockTrip(UUID tripId);
    List<Leg> findLegs(UUID tripId, List<Integer> ordinals);
    List<Leg> lockLegs(UUID tripId, List<Integer> ordinals);
    List<Integer> occupiedSeats(UUID legId);
    void insertReservation(UUID id, UUID tripId, Route route, PassengerHold passenger, Instant expiresAt);
    void insertSeat(UUID tripId, UUID legId, int seat, UUID reservationId);
}
