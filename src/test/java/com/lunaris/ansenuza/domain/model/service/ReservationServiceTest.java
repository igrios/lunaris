package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

class ReservationServiceTest {

    @Test
    void generatesRouteBasedCodeWithThreeDigitSequence() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate("Balnearia", "Córdoba", LocalDate.of(2026, 8, 1)))
                .thenReturn(0L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation reservation = reservation("Balnearia", "Córdoba");

        service.saveReservationFlow(reservation);

        assertEquals("BAL-COR-001", reservation.getReservationCode());
    }

    @Test
    void incrementsSequenceAndFallsBackToLunForMissingLocality() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate("", "Morteros", LocalDate.of(2026, 8, 1)))
                .thenReturn(1L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation reservation = reservation(null, "Morteros");

        service.saveReservationFlow(reservation);

        assertEquals("LUN-MOR-002", reservation.getReservationCode());
    }

    @Test
    void fallsBackToLunForBlankDestination() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate("Ansenuza", "", LocalDate.of(2026, 8, 1)))
                .thenReturn(0L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation reservation = reservation("Ansenuza", "   ");

        service.saveReservationFlow(reservation);

        assertEquals("ANS-LUN-001", reservation.getReservationCode());
    }

    private Reservation reservation(String origin, String destination) {
        return Reservation.builder()
                .passenger(Passenger.builder().currentBalance(BigDecimal.ZERO).build())
                .pickupLocality(origin)
                .destination(destination)
                .travelDate(LocalDate.of(2026, 8, 1))
                .amount(new BigDecimal("1000.00"))
                .discountAmount(BigDecimal.ZERO)
                .roundTrip(false)
                .paymentVerified(false)
                .build();
    }

    @Test
    void splitsFullNameWhenPassengerHasNoLastName() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations,
                mock(ReservationEventRepository.class),
                passengers,
                mock(OnboardPassengerUseCase.class));
        Passenger passenger = Passenger.builder()
                .firstName("Juna Fenogloi")
                .lastName("Sin apellido")
                .currentBalance(BigDecimal.ZERO)
                .build();
        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .pickupLocality("Miramar")
                .destination("Cordoba")
                .travelDate(LocalDate.of(2026, 8, 1))
                .amount(new BigDecimal("1000.00"))
                .discountAmount(BigDecimal.ZERO)
                .roundTrip(false)
                .paymentVerified(false)
                .build();
        when(reservations.save(reservation)).thenReturn(reservation);

        service.saveReservationFlow(reservation);

        assertEquals("Juna", passenger.getFirstName());
        assertEquals("Fenogloi", passenger.getLastName());
        verify(passengers).save(passenger);
    }

    @Test
    void cancellationCreditsPaidReservationAmountAndUpdatesBothStatuses() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations,
                mock(ReservationEventRepository.class),
                passengers,
                mock(OnboardPassengerUseCase.class));
        UUID reservationId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .currentBalance(new BigDecimal("100.00"))
                .build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .passenger(passenger)
                .amount(new BigDecimal("2500.00"))
                .paymentVerified(true)
                .status("CONFIRMED")
                .reservationCode("COR-MIR-001-VUELTA")
                .build();
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));

        service.cancelReservation(reservationId, "PASSENGER");

        assertEquals(new BigDecimal("2600.00"), passenger.getCurrentBalance());
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, reservation.getTravelStatus());
        verify(passengers).saveAndFlush(passenger);
    }

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

    @Test
    void openReturnCreatesUndatedReturnAndCopiesReceipt() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate(any(), any(), any())).thenReturn(0L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation outbound = reservation("Morteros", "Córdoba");
        outbound.setRoundTrip(true);
        outbound.setTripType(TripType.OPEN_RETURN);
        outbound.setAmount(new BigDecimal("2000.00"));
        outbound.setPaymentReceiptUrl("https://receipts.example/payment.jpg");

        var saved = service.saveReservationFlow(outbound);

        assertEquals(2, saved.size());
        Reservation openReturn = saved.get(1);
        assertNull(openReturn.getTravelDate());
        assertEquals(Reservation.TravelStatus.OPEN_RETURN, openReturn.getTravelStatus());
        assertEquals(new BigDecimal("1000.00"), openReturn.getAmount());
        assertEquals(outbound.getPaymentReceiptUrl(), openReturn.getPaymentReceiptUrl());
    }

    @Test
    void verifyPaymentLocksAndUpdatesManagedReservation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        UUID id = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(id).paymentVerified(false).status("PAYMENT_RECEIVED").build();
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        when(reservations.saveAndFlush(reservation)).thenReturn(reservation);
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));

        Reservation result = service.verifyPayment(id);

        assertSame(reservation, result);
        assertEquals(true, reservation.getPaymentVerified());
        assertEquals("CONFIRMED", reservation.getStatus());
        assertNotNull(reservation.getPaymentConfirmedAt());
        verify(reservations).findByIdForUpdate(id);
        verify(reservations).saveAndFlush(reservation);
    }
}
