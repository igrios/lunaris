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
}
