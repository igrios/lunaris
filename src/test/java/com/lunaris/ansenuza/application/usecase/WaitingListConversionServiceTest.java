package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class WaitingListConversionServiceTest {

    @Test
    void convertsWaitingEntryWhenSeatsAreAvailable() {
        Fixture fixture = new Fixture(8, 12);
        when(fixture.passengers.findByPhone("543511112222"))
                .thenReturn(Optional.of(fixture.passenger));
        when(fixture.pricing.calculateReservationAmount(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(com.lunaris.ansenuza.domain.model.TripType.class),
                org.mockito.ArgumentMatchers.anyInt())).thenReturn(new BigDecimal("100000"));
        when(fixture.reservationService.saveReservationFlow(
                org.mockito.ArgumentMatchers.any(Reservation.class)))
                .thenAnswer(invocation -> List.of(
                        invocation.getArgument(0, Reservation.class)));

        Reservation reservation = fixture.service.convert(1L);

        assertEquals("CONFIRMED", fixture.entry.getStatus());
        assertEquals(3, reservation.getPassengerCount());
        verify(fixture.waitingList).saveAndFlush(fixture.entry);
    }

    @Test
    void keepsEntryWaitingWhenCapacityIsStillFull() {
        Fixture fixture = new Fixture(10, 12);

        assertThrows(SeatCapacityExceededException.class, () -> fixture.service.convert(1L));

        assertEquals("WAITING", fixture.entry.getStatus());
        verify(fixture.reservationService, never()).saveReservationFlow(
                org.mockito.ArgumentMatchers.any());
    }

    private static class Fixture {
        final WaitingListRepository waitingList = mock(WaitingListRepository.class);
        final PassengerRepository passengers = mock(PassengerRepository.class);
        final ReservationRepository reservations = mock(ReservationRepository.class);
        final ReservationService reservationService = mock(ReservationService.class);
        final PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        final SystemConfigurationService configurations = mock(SystemConfigurationService.class);
        final Passenger passenger = Passenger.builder().phone("543511112222").build();
        final WaitingListEntry entry = WaitingListEntry.builder()
                .id(1L)
                .phoneNumber("543511112222")
                .passengerName("Ana Pérez")
                .travelDate(LocalDate.of(2026, 8, 20))
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .passengerCount(3)
                .status("WAITING")
                .build();
        final WaitingListConversionService service = new WaitingListConversionService(
                waitingList, passengers, reservations, reservationService, pricing, configurations);

        Fixture(int occupied, int capacity) {
            when(waitingList.findByIdForUpdate(1L)).thenReturn(Optional.of(entry));
            when(reservations.countConfirmedPassengersByRouteAndDate(
                    "Morteros", "Córdoba", entry.getTravelDate())).thenReturn(occupied);
            when(configurations.getScheduleMaxCapacity()).thenReturn(capacity);
        }
    }
}
