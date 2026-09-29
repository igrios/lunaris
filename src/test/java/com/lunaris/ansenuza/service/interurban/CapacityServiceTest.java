package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CapacityServiceTest {
    private final CapacityRepository repository = mock(CapacityRepository.class);
    private final Instant now = Instant.parse("2026-09-20T21:00:00Z");
    private final UUID tripId = UUID.randomUUID();
    private final Route route = new Route(0, 3);
    private final List<CapacityRepository.Leg> legs = List.of(
            new CapacityRepository.Leg(UUID.randomUUID(), 1),
            new CapacityRepository.Leg(UUID.randomUUID(), 2),
            new CapacityRepository.Leg(UUID.randomUUID(), 3));
    private CapacityService service;

    @BeforeEach void setup() {
        service = new CapacityService(repository, Clock.fixed(now, ZoneOffset.UTC));
    }

    private CapacityRepository.Trip trip(int direction, String status, Instant closesAt) {
        return new CapacityRepository.Trip(tripId, direction, status, closesAt);
    }

    private void readable(Route requested, int direction) {
        when(repository.findTrip(tripId)).thenReturn(Optional.of(trip(direction, "OPEN", now.plusSeconds(3600))));
        when(repository.findLegs(tripId, requested.ordinals())).thenReturn(legs.stream()
                .filter(l -> requested.ordinals().contains(l.ordinal())).toList());
    }

    private void writable() {
        when(repository.lockTrip(tripId)).thenReturn(Optional.of(trip(1, "OPEN", now.plusSeconds(3600))));
        when(repository.lockLegs(tripId, route.ordinals())).thenReturn(legs);
    }

    private PassengerHold passenger(UUID booking) {
        return new PassengerHold(booking, UUID.randomUUID(), "Pasajero", "+5493562000000",
                "Origen 123", "Destino 456", new BigDecimal("1000.00"), new BigDecimal("100.00"));
    }

    @Test void minimumAcrossThreeLegsIsNotSum() {
        readable(route, 1);
        when(repository.occupiedSeats(legs.get(0).id())).thenReturn(List.of(1, 2));
        when(repository.occupiedSeats(legs.get(1).id())).thenReturn(List.of(1, 2, 3, 4));
        when(repository.occupiedSeats(legs.get(2).id())).thenReturn(List.of(1));
        assertEquals(0, service.available(tripId, route));
    }

    @Test void partialRouteIgnoresFullUnrequestedLeg() {
        var partial = new Route(2, 3);
        readable(partial, 1);
        when(repository.occupiedSeats(legs.get(2).id())).thenReturn(List.of(2));
        assertEquals(3, service.available(tripId, partial));
        verify(repository, never()).occupiedSeats(legs.get(0).id());
    }

    @Test void reverseRouteUsesSamePhysicalOrdinals() {
        var reverse = new Route(3, 0);
        readable(reverse, -1);
        assertEquals(List.of(1, 2, 3), reverse.ordinals());
        assertEquals(4, service.available(tripId, reverse));
    }

    @ParameterizedTest @CsvSource({"0,0", "-1,2", "1,4", "4,0"})
    void rejectsInvalidStops(int origin, int destination) {
        assertThrows(InvalidRouteException.class, () -> new Route(origin, destination));
    }

    @Test void rejectsOppositeDirection() {
        when(repository.findTrip(tripId)).thenReturn(Optional.of(trip(-1, "OPEN", now.plusSeconds(3600))));
        assertThrows(InvalidRouteException.class, () -> service.available(tripId, route));
        verify(repository, never()).findLegs(any(), any());
    }

    @Test void rejectsMissingLegInsteadOfReportingCapacity() {
        readable(route, 1);
        when(repository.findLegs(tripId, route.ordinals())).thenReturn(legs.subList(0, 2));
        assertThrows(InvalidRouteException.class, () -> service.available(tripId, route));
    }

    @Test void unknownTripHasDomainException() {
        assertThrows(InvalidRouteException.class, () -> service.available(tripId, route));
    }

    @Test void holdLocksBeforeReadingAndOccupiesEveryLegForEveryPassenger() {
        writable();
        UUID booking = UUID.randomUUID();
        var passengers = List.of(passenger(booking), passenger(booking));
        when(repository.occupiedSeats(legs.get(0).id())).thenReturn(List.of(1, 3));
        var ids = service.hold(tripId, route, passengers, now.plusSeconds(600));
        assertEquals(2, ids.stream().distinct().count());
        var order = inOrder(repository);
        order.verify(repository).lockTrip(tripId);
        order.verify(repository).lockLegs(tripId, route.ordinals());
        for (var leg : legs) order.verify(repository).occupiedSeats(leg.id());
        for (int i = 0; i < 2; i++) {
            order.verify(repository).insertReservation(ids.get(i), tripId, route, passengers.get(i), now.plusSeconds(600));
        }
        verify(repository).insertSeat(tripId, legs.get(0).id(), 2, ids.get(0));
        verify(repository).insertSeat(tripId, legs.get(0).id(), 4, ids.get(1));
        verify(repository, times(6)).insertSeat(eq(tripId), any(), anyInt(), any());
    }

    @Test void fullLastLegPreventsAllWrites() {
        writable();
        when(repository.occupiedSeats(legs.get(2).id())).thenReturn(List.of(1, 2, 3, 4));
        assertThrows(CapacityExceededException.class, () -> service.hold(tripId, route,
                List.of(passenger(UUID.randomUUID())), now.plusSeconds(600)));
        verify(repository, never()).insertReservation(any(), any(), any(), any(), any());
        verify(repository, never()).insertSeat(any(), any(), anyInt(), any());
    }

    @Test void exactCutoffRejectsHoldEvenIfStatusStillOpen() {
        writable();
        when(repository.lockTrip(tripId)).thenReturn(Optional.of(trip(1, "OPEN", now)));
        assertThrows(BookingClosedException.class, () -> service.hold(tripId, route,
                List.of(passenger(UUID.randomUUID())), now.plusSeconds(600)));
        verify(repository, never()).insertReservation(any(), any(), any(), any(), any());
    }

    @Test void closedTripReportsNoSellableSeats() {
        readable(route, 1);
        when(repository.findTrip(tripId)).thenReturn(Optional.of(trip(1, "CLOSED", now.plusSeconds(3600))));
        assertEquals(0, service.available(tripId, route));
    }

    @Test void expiryCannotOutliveCutoff() {
        writable();
        assertThrows(InvalidHoldException.class, () -> service.hold(tripId, route,
                List.of(passenger(UUID.randomUUID())), now.plusSeconds(3601)));
        verify(repository, never()).insertReservation(any(), any(), any(), any(), any());
    }

    @Test void rejectsOversizedPartyBeforeLocking() {
        var p = passenger(UUID.randomUUID());
        assertThrows(InvalidHoldException.class, () -> service.hold(tripId, route,
                List.of(p, p, p, p, p), now.plusSeconds(600)));
        verifyNoInteractions(repository);
    }

    @Test void rejectsDifferentBookingGroups() {
        assertThrows(InvalidHoldException.class, () -> service.hold(tripId, route,
                List.of(passenger(UUID.randomUUID()), passenger(UUID.randomUUID())), now.plusSeconds(600)));
        verifyNoInteractions(repository);
    }
}
