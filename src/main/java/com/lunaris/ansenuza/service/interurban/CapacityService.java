package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.transaction.annotation.Transactional;

/** Se registra exclusivamente mediante InterurbanConfiguration. */
public class CapacityService {
    private final CapacityRepository repository;
    private final Clock clock;

    public CapacityService(CapacityRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Lectura orientativa; hold siempre vuelve a comprobar con locks. */
    @Transactional(readOnly = true)
    public int available(UUID tripId, Route route) {
        validateRequest(tripId, route);
        var trip = repository.findTrip(tripId)
                .orElseThrow(() -> new InvalidRouteException("Viaje inexistente."));
        validateDirection(trip, route);
        var legs = repository.findLegs(tripId, route.ordinals());
        validateLegs(legs, route);
        if (!isOpen(trip)) return 0;
        return legs.stream().mapToInt(leg -> freeSeats(leg).size()).min().orElse(0);
    }

    /** Inserta reservas y plazas de todo el grupo en una única transacción. */
    @Transactional
    public List<UUID> hold(UUID tripId, Route route, List<PassengerHold> passengers, Instant expiresAt) {
        validateRequest(tripId, route);
        if (passengers == null || passengers.isEmpty() || passengers.size() > 4
                || passengers.stream().anyMatch(p -> p == null) || expiresAt == null) {
            throw new InvalidHoldException("Se requieren entre uno y cuatro pasajeros y vencimiento.");
        }
        var party = List.copyOf(passengers);
        if (party.stream().map(PassengerHold::bookingId).distinct().count() != 1) {
            throw new InvalidHoldException("Los pasajeros deben pertenecer al mismo grupo.");
        }
        var trip = repository.lockTrip(tripId)
                .orElseThrow(() -> new InvalidRouteException("Viaje inexistente."));
        validateDirection(trip, route);
        // El orden se aplica en SQL ANTES de adquirir locks, no en memoria después.
        var legs = repository.lockLegs(tripId, route.ordinals());
        validateLegs(legs, route);
        if (!isOpen(trip)) throw new BookingClosedException();
        if (!expiresAt.isAfter(clock.instant()) || expiresAt.isAfter(trip.closesAt())) {
            throw new InvalidHoldException("El hold debe vencer en el futuro y no superar el cierre.");
        }
        var freeByLeg = legs.stream().map(this::freeSeats).toList();
        if (freeByLeg.stream().anyMatch(seats -> seats.size() < party.size())) {
            throw new CapacityExceededException();
        }
        var ids = new ArrayList<UUID>();
        for (var passenger : party) {
            UUID id = UUID.randomUUID(); // JDBC: no entidades JPA ni generación implícita.
            repository.insertReservation(id, tripId, route, passenger, expiresAt);
            ids.add(id);
        }
        for (int leg = 0; leg < legs.size(); leg++) {
            for (int passenger = 0; passenger < ids.size(); passenger++) {
                repository.insertSeat(tripId, legs.get(leg).id(), freeByLeg.get(leg).get(passenger), ids.get(passenger));
            }
        }
        return List.copyOf(ids);
    }

    private List<Integer> freeSeats(CapacityRepository.Leg leg) {
        var occupied = repository.occupiedSeats(leg.id());
        return IntStream.rangeClosed(1, 4).filter(seat -> !occupied.contains(seat)).boxed().toList();
    }

    private boolean isOpen(CapacityRepository.Trip trip) {
        return "OPEN".equals(trip.status()) && clock.instant().isBefore(trip.closesAt());
    }

    private void validateRequest(UUID tripId, Route route) {
        if (tripId == null || route == null) throw new InvalidRouteException("Viaje y recorrido obligatorios.");
    }

    private void validateDirection(CapacityRepository.Trip trip, Route route) {
        if (trip.direction() != route.direction()) throw new InvalidRouteException("Sentido incompatible con el viaje.");
    }

    private void validateLegs(List<CapacityRepository.Leg> legs, Route route) {
        if (legs.size() != route.ordinals().size()
                || !new HashSet<>(legs.stream().map(CapacityRepository.Leg::ordinal).toList())
                        .equals(new HashSet<>(route.ordinals()))) {
            throw new InvalidRouteException("El viaje no contiene todos los tramos requeridos.");
        }
    }
}
