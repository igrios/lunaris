package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PricingAndScheduleServiceCapacityTest {
    private final LocalDate date = LocalDate.of(2026, 9, 8);
    private final ReservationRepository repository = mock(ReservationRepository.class);
    private PricingAndScheduleService serviceAt(int hour, int minute, int second) {
        var clock = java.time.Clock.fixed(date.atTime(hour, minute, second)
                .atZone(ArgentinaTime.ZONE_ID).toInstant(), ArgentinaTime.ZONE_ID);
        return new PricingAndScheduleService(mock(FareRepository.class), mock(LocalityRepository.class),
                mock(BusinessParameterRepository.class), repository, clock);
    }

    private Reservation leg(int seats, String schedule, boolean open) {
        return Reservation.builder().passengerCount(seats).departureSchedule(schedule)
                .travelStatus(open ? Reservation.TravelStatus.OPEN_RETURN : Reservation.TravelStatus.CONFIRMED)
                .build();
    }

    @Test
    void testCapacity_IncludesRoundTripAndOpenReturnsBeforeCutoff() {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(4, "14:00", false), leg(3, null, true)));
        {
            var service = serviceAt(10, 59, 59);
            assertEquals(1, service.availableReturnSeats(date, "14:00"));
            assertTrue(service.hasReturnCapacity(date, "14:00", 1));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {11, 12, 23})
    void testCapacity_ReleasesOpenReturnsAfterCutoff(int hour) {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(4, "14:00", false), leg(3, null, true)));
        {
            var service = serviceAt(hour, 0, 0);
            assertEquals(4, service.availableReturnSeats(date, "14:00"));
        }
    }

    @Test
    void testCapacity_ZeroOverbooking() {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(4, "14:00", false), leg(3, null, true)));
        {
            var service = serviceAt(10, 0, 0);
            assertFalse(service.hasReturnCapacity(date, "14:00", 2));
            assertFalse(service.hasReturnCapacity(date, "14:00", 0));
        }
    }

    @Test
    void countsSoldOneWaySeatsAndSeparatesBlocks() {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(
                leg(4, "14:00", false), leg(2, "02:00 PM", false), leg(8, "17:30", false)));
        assertEquals(2, ReturnCapacityPolicy.availableSeats(repository, date, "14:00", date.atTime(12, 0)));
        assertEquals(0, ReturnCapacityPolicy.availableSeats(repository, date, "17:30", date.atTime(12, 0)));
    }

    @Test
    void reservationWriteLocksReturnInventoryBeforeRejectingOverbooking() {
        var locks = mock(CapacityLockRepository.class);
        String key = date + "|DAY|RETURN";
        when(locks.findForUpdate(key)).thenReturn(new com.lunaris.ansenuza.domain.model.CapacityLock(key));
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(7, "14:00", false)));
        var writer = new ReservationService(repository, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class),
                mock(com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase.class), locks);
        var requested = Reservation.builder().pickupLocality("Córdoba").destination("Morteros")
                .travelDate(date).departureSchedule("14:00").passengerCount(2).build();
        assertThrows(com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException.class,
                () -> writer.saveReservationFlow(requested));
        var order = inOrder(locks, repository);
        order.verify(locks).ensureExists(key);
        order.verify(locks).findForUpdate(key);
        order.verify(repository).findReturnCapacityCandidates(date);
        verify(repository, never()).save(any());
    }

    @Test
    void neverReportsNegativeAvailability() {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(10, "14:00", false)));
        assertEquals(0, ReturnCapacityPolicy.availableSeats(repository, date, "14:00", date.atTime(12, 0)));
    }

    @Test
    void futureDateRetainsSeatsEvenAfterTodaysCutoff() {
        when(repository.findReturnCapacityCandidates(date)).thenReturn(List.of(leg(3, null, true)));
        assertEquals(5, ReturnCapacityPolicy.availableSeats(repository, date, "14:00", date.minusDays(1).atTime(15, 0)));
    }
}
