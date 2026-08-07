package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lunaris.ansenuza.domain.model.Reservation;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AgendaPassengerMetricsTest {

    @Test
    void sameDayRoundTripCountsPassengerSeatsOnlyOnce() {
        Reservation outbound = leg("ARR-COR-001-IDA", 1);
        Reservation returned = leg("ARR-COR-001-VUELTA", 1);

        assertEquals(1, AgendaViewController.countDistinctBookingSeats(
                List.of(outbound, returned)));
        assertEquals(1, AgendaViewController.countDistinctBookings(
                List.of(outbound, returned)));
    }

    @Test
    void companionsCountOnceAndIndependentBookingsRemainSeparate() {
        Reservation outbound = leg("ARR-COR-001-IDA", 3);
        Reservation returned = leg("ARR-COR-001-VUELTA", 3);
        Reservation anotherBooking = leg("ARR-COR-002", 2);

        assertEquals(5, AgendaViewController.countDistinctBookingSeats(
                List.of(outbound, returned, anotherBooking)));
        assertEquals(2, AgendaViewController.countDistinctBookings(
                List.of(outbound, returned, anotherBooking)));
    }

    private Reservation leg(String code, int seats) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .reservationCode(code)
                .roundTrip(code.contains("001"))
                .passengerCount(seats)
                .build();
    }
}
