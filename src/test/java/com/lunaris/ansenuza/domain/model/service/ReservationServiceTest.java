package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

class ReservationServiceTest {

    @Test
    void genericUpdateDelegatesOnboardTransitionToCanonicalUseCase() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationEventRepository events = mock(ReservationEventRepository.class);
        OnboardPassengerUseCase onboardPassenger = mock(OnboardPassengerUseCase.class);
        ReservationService service = new ReservationService(
                reservations,
                events,
                mock(PassengerRepository.class),
                onboardPassenger);
        UUID reservationId = UUID.randomUUID();
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        Reservation existing = Reservation.builder()
                .id(reservationId)
                .passenger(Passenger.builder().phone("5493511111111").build())
                .driver(driver)
                .travelDate(LocalDate.of(2026, 8, 1))
                .travelStatus(Reservation.TravelStatus.PENDING)
                .build();
        Reservation update = new Reservation();
        update.setTravelStatus(Reservation.TravelStatus.ONBOARD);
        when(reservations.findById(reservationId)).thenReturn(Optional.of(existing));
        when(reservations.saveAndFlush(existing)).thenReturn(existing);
        when(onboardPassenger.updateTravelStatus(
                reservationId, Reservation.TravelStatus.ONBOARD))
                .thenReturn(existing);

        Reservation result = service.updateReservation(
                reservationId, update, "ADMIN_PANEL");

        assertSame(existing, result);
        verify(onboardPassenger).updateTravelStatus(
                reservationId, Reservation.TravelStatus.ONBOARD);
    }
}
