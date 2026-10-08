package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyPassengerManifestServiceTest {
    @Test
    void includesSpecialTripsWithCustomRouteAndBothPaymentStates() {
        var paid = Reservation.builder().reservationCode("ESP-001")
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .departureSchedule("09:00").originCustom("Hotel Central")
                .destinationCustom("Aeropuerto").passengerCount(4).paymentVerified(true).build();
        var pending = Reservation.builder().reservationCode("ESP-002")
                .pickupLocality("Miramar").destination("Córdoba")
                .passengerCount(2).paymentVerified(false).build();
        String pdf = new String(new DailyPassengerManifestService().generatePdf(
                LocalDate.of(2026, 9, 12), List.of(), List.of(paid, pending)),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("Viajes Especiales"));
        assertTrue(pdf.contains("09:00 | ESP-001 | Ana Pérez | Hotel Central -> Aeropuerto | 4 | VERIFICADO"));
        assertTrue(pdf.contains("ESP-002 | Sin pasajero | Miramar -> Córdoba | 2 | PENDIENTE"));
    }

    @Test
    void paginatesSoSpecialTripsRemainVisibleAfterLargeRegularManifest() {
        var regular = java.util.stream.IntStream.range(0, 80)
                .mapToObj(i -> Reservation.builder().pickupLocality("Morteros").build()).toList();
        var special = Reservation.builder().reservationCode("LAST-SPECIAL").passengerCount(3).build();
        String pdf = new String(new DailyPassengerManifestService().generatePdf(
                LocalDate.of(2026, 9, 12), regular, List.of(special)),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("/Count 3"));
        assertTrue(pdf.contains("LAST-SPECIAL"));
        assertTrue(pdf.endsWith("%%EOF"));
    }

    @Test
    void separatesUniquePeopleFromSeatsAcrossOutboundAndReturnLegs() {
        Passenger passenger = Passenger.builder().id(UUID.randomUUID())
                .firstName("Ana").lastName("Pérez").phone("5493515550101").build();
        Reservation outbound = Reservation.builder().id(UUID.randomUUID()).passenger(passenger)
                .passengerCount(1).routeDirection("IDA").build();
        Reservation returned = Reservation.builder().id(UUID.randomUUID()).passenger(passenger)
                .passengerCount(1).routeDirection("VUELTA").build();
        var reservations = List.of(outbound, returned);
        var service = new DailyPassengerManifestService();

        assertEquals(1, service.uniquePassengers(reservations));
        assertEquals(2, service.reservedSeats(reservations));
        String pdf = new String(service.generatePdf(LocalDate.of(2026, 9, 12), reservations),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("Pasajeros únicos: 1 | Butacas reservadas: 2"));
    }
    @Test
    void countsDistinctIdsAndSumsGroupSeatsAcrossLegs() {
        UUID passengerId = UUID.randomUUID();
        Passenger outboundPassenger = Passenger.builder().id(passengerId).phone("123").build();
        Passenger returnPassenger = Passenger.builder().id(passengerId).phone("456").build();
        Passenger otherPassenger = Passenger.builder().id(UUID.randomUUID()).phone("123").build();
        var reservations = List.of(
                Reservation.builder().passenger(outboundPassenger).passengerCount(3).build(),
                Reservation.builder().passenger(returnPassenger).passengerCount(2).build(),
                Reservation.builder().passenger(otherPassenger).passengerCount(1).build());
        var service = new DailyPassengerManifestService();

        assertEquals(2, service.uniquePassengers(reservations));
        assertEquals(6, service.reservedSeats(reservations));
        String pdf = new String(service.generatePdf(LocalDate.of(2026, 9, 12), reservations),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("Pasajeros únicos: 2 | Butacas reservadas: 6"));
    }

    @Test
    void ignoresNullIdsAndCountsOnlyStoredPassengerCounts() {
        var reservations = List.of(
                Reservation.builder().passenger(Passenger.builder().phone("123").build())
                        .passengerCount(0).build(),
                Reservation.builder().passengerCount(null).build(),
                Reservation.builder().passengerCount(2).build());
        var service = new DailyPassengerManifestService();

        assertEquals(0, service.uniquePassengers(reservations));
        assertEquals(2, service.reservedSeats(reservations));
    }

    @Test
    void emptyManifestHasZeroPassengersAndSeats() {
        var service = new DailyPassengerManifestService();

        assertEquals(0, service.uniquePassengers(List.of()));
        assertEquals(0, service.reservedSeats(List.of()));
        String pdf = new String(service.generatePdf(LocalDate.of(2026, 9, 12), List.of()),
                java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(pdf.contains("Pasajeros únicos: 0 | Butacas reservadas: 0"));
    }
}
