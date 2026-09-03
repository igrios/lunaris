package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;

class ReservationServiceTest {

    @Test
    void roundTripLegsInheritInvoiceAndVerifiedPaymentFlags() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate(
                "Morteros", "Córdoba", LocalDate.of(2026, 8, 10))).thenReturn(0L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation reservation = Reservation.builder()
                .passenger(Passenger.builder().currentBalance(BigDecimal.ZERO).build())
                .pickupLocality("Morteros")
                .destination("Córdoba")
                .travelDate(LocalDate.of(2026, 8, 10))
                .returnDate(LocalDate.of(2026, 8, 12))
                .roundTrip(true)
                .tripType(TripType.ROUND_TRIP)
                .amount(new BigDecimal("105000.00"))
                .discountAmount(BigDecimal.ZERO)
                .paymentVerified(true)
                .requiresInvoice(true)
                .status("CONFIRMED")
                .build();

        List<Reservation> saved = service.saveReservationFlow(reservation, "17:30");

        assertEquals(2, saved.size());
        assertEquals(new BigDecimal("52500.00"), saved.get(0).getAmount());
        assertEquals(new BigDecimal("52500.00"), saved.get(1).getAmount());
        assertEquals(new BigDecimal("105000.00"),
                saved.get(0).getAmount().add(saved.get(1).getAmount()));
        assertEquals("17:30", saved.get(1).getDepartureSchedule());
        assertEquals("Morteros", saved.get(0).getPickupLocality());
        assertEquals("Córdoba", saved.get(0).getDestination());
        assertEquals("IDA", saved.get(0).getRouteDirection());
        assertTrue(saved.get(0).getReservationCode().endsWith("-IDA"));
        assertEquals("Córdoba", saved.get(1).getPickupLocality());
        assertEquals("Morteros", saved.get(1).getDestination());
        assertEquals("VUELTA", saved.get(1).getRouteDirection());
        assertTrue(saved.get(1).getReservationCode().endsWith("-VUELTA"));
        saved.forEach(leg -> {
            assertSame(reservation.getPassenger(), leg.getPassenger());
            assertEquals(true, leg.getRequiresInvoice());
            assertEquals(true, leg.getPaymentVerified());
        });
    }

    @Test
    void roundTripStartingInCordobaAlignsCodesAndDirectionsWithAgenda() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        when(reservations.countSequenceByRouteAndDate(
                "Córdoba", "Miramar", LocalDate.of(2026, 8, 10))).thenReturn(0L);
        when(reservations.existsByReservationCode(any())).thenReturn(false);
        when(reservations.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));
        Reservation reservation = Reservation.builder()
                .passenger(Passenger.builder().currentBalance(BigDecimal.ZERO).build())
                .pickupLocality("Córdoba").destination("Miramar")
                .travelDate(LocalDate.of(2026, 8, 10))
                .returnDate(LocalDate.of(2026, 8, 12))
                .departureSchedule("14:00").roundTrip(true)
                .tripType(TripType.ROUND_TRIP).amount(new BigDecimal("1000.00"))
                .discountAmount(BigDecimal.ZERO).paymentVerified(false).build();

        List<Reservation> saved = service.saveReservationFlow(reservation, "03:00 AM");

        assertEquals("VUELTA", saved.get(0).getRouteDirection());
        assertTrue(saved.get(0).getReservationCode().endsWith("-VUELTA"));
        assertEquals("Córdoba", saved.get(0).getPickupLocality());
        assertEquals("Miramar", saved.get(0).getDestination());
        assertEquals("IDA", saved.get(1).getRouteDirection());
        assertTrue(saved.get(1).getReservationCode().endsWith("-IDA"));
        assertEquals("Miramar", saved.get(1).getPickupLocality());
        assertEquals("Córdoba", saved.get(1).getDestination());
    }

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
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        ReservationService.CancellationResult result =
                service.cancelReservation(reservationId, "PASSENGER");

        assertEquals(new BigDecimal("2600.00"), passenger.getCurrentBalance());
        assertEquals(new BigDecimal("2500.00"), result.creditedAmount());
        assertTrue(result.paymentVerified());
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, reservation.getTravelStatus());
        verify(passengers).saveAndFlush(passenger);
    }

    @Test
    void cancellationRestoresBalanceUsedEvenWhenTransferWasNotVerified() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class), passengers,
                mock(OnboardPassengerUseCase.class));
        UUID reservationId = UUID.randomUUID();
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .passenger(passenger)
                .amount(new BigDecimal("1500.00"))
                .usedBalance(new BigDecimal("1000.00"))
                .paymentVerified(false)
                .status("PENDING_PAYMENT")
                .reservationCode("COR-MIR-099")
                .build();
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        ReservationService.CancellationResult result =
                service.cancelReservation(reservationId, "PASSENGER");

        assertEquals(new BigDecimal("1000.00"), passenger.getCurrentBalance());
        assertEquals(new BigDecimal("1000.00"), result.creditedAmount());
        verify(passengers).saveAndFlush(passenger);
    }

    @Test
    void cancellationRestoresFullyUsedBalanceWhenCashAmountIsZero() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class), passengers,
                mock(OnboardPassengerUseCase.class));
        UUID reservationId = UUID.randomUUID();
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId).passenger(passenger).amount(BigDecimal.ZERO)
                .usedBalance(new BigDecimal("2500.00")).paymentVerified(true)
                .status("CONFIRMED").reservationCode("COR-MIR-100").build();
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        ReservationService.CancellationResult result =
                service.cancelReservation(reservationId, "PASSENGER");

        assertEquals(new BigDecimal("2500.00"), result.creditedAmount());
        assertEquals(new BigDecimal("2500.00"), passenger.getCurrentBalance());
    }

    @Test
    void cancellationWithoutVerifiedPaymentDoesNotCreditBalanceAndAuditsReason() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationEventRepository events = mock(ReservationEventRepository.class);
        ReservationService service = new ReservationService(
                reservations, events, passengers, mock(OnboardPassengerUseCase.class));
        UUID reservationId = UUID.randomUUID();
        Passenger passenger = Passenger.builder()
                .currentBalance(new BigDecimal("100.00"))
                .build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .passenger(passenger)
                .amount(new BigDecimal("2500.00"))
                .paymentVerified(false)
                .paymentReceiptUrl("https://example.test/unverified-receipt.jpg")
                .status("PAYMENT_RECEIVED")
                .reservationCode("COR-MIR-002-VUELTA")
                .build();
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        ReservationService.CancellationResult result =
                service.cancelReservation(reservationId, "PASSENGER");

        assertEquals(new BigDecimal("100.00"), passenger.getCurrentBalance());
        assertEquals(BigDecimal.ZERO, result.creditedAmount());
        assertTrue(!result.paymentVerified());
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, reservation.getTravelStatus());
        verify(passengers, never()).saveAndFlush(passenger);
        ArgumentCaptor<ReservationEvent> eventCaptor =
                ArgumentCaptor.forClass(ReservationEvent.class);
        verify(events).save(eventCaptor.capture());
        assertEquals("CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT",
                eventCaptor.getValue().getEventType());
    }

    @Test
    void rejectsCancellationWhenRouteWasSent() {
        assertCancellationLockedFor(Reservation.TravelStatus.ROUTE_SENT);
    }

    @Test
    void cancellingParentCancelsSplitReturnsAndCreditsOnlyTheirLegAmounts() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations,
                mock(ReservationEventRepository.class),
                passengers,
                mock(OnboardPassengerUseCase.class));
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        String groupCode = "ARR-COR-001";
        Reservation outbound = paidLeg(passenger, "ARR-COR-001-IDA", groupCode);
        Reservation returnLeg = paidLeg(passenger, "ARR-COR-001-VUELTA", groupCode);
        Reservation splitReturn = paidLeg(passenger, "VTA-BLK-f4ad-030", groupCode);
        when(reservations.findByIdForUpdate(outbound.getId())).thenReturn(Optional.of(outbound));
        when(reservations.findByBookingGroupCodeForUpdate(groupCode))
                .thenReturn(List.of(outbound, returnLeg, splitReturn));

        service.cancelReservation(outbound.getId(), "BOT_WHATSAPP");

        assertEquals("CANCELLED", outbound.getStatus());
        assertEquals("CANCELLED", returnLeg.getStatus());
        assertEquals("CANCELLED", splitReturn.getStatus());
        assertEquals(new BigDecimal("417000.00"), passenger.getCurrentBalance());
    }

    @Test
    void cancellingReturnLegAlsoCancelsOutboundLegFromBookingGroup() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class), passengers,
                mock(OnboardPassengerUseCase.class));
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        String groupCode = "MIR-COR-021";
        Reservation outbound = paidLeg(passenger, groupCode + "-IDA", groupCode);
        Reservation returnLeg = paidLeg(passenger, groupCode + "-VUELTA", groupCode);
        when(reservations.findByIdForUpdate(returnLeg.getId())).thenReturn(Optional.of(returnLeg));
        when(reservations.findByBookingGroupCodeForUpdate(groupCode))
                .thenReturn(List.of(outbound, returnLeg));

        service.cancelReservation(returnLeg.getId(), "ADMIN_PANEL");

        assertEquals("CANCELLED", returnLeg.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, returnLeg.getTravelStatus());
        assertEquals("CANCELLED", outbound.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, outbound.getTravelStatus());
        assertEquals(new BigDecimal("278000.00"), passenger.getCurrentBalance());
        verify(reservations).saveAndFlush(returnLeg);
        verify(reservations).saveAndFlush(outbound);
    }

    private Reservation paidLeg(Passenger passenger, String code, String groupCode) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(passenger)
                .reservationCode(code)
                .bookingGroupCode(groupCode)
                .amount(new BigDecimal("139000.00"))
                .paymentVerified(true)
                .status("CONFIRMED")
                .travelStatus(Reservation.TravelStatus.PENDING)
                .build();
    }

    @Test
    void rejectsCancellationWhenPassengerIsOnboard() {
        assertCancellationLockedFor(Reservation.TravelStatus.ONBOARD);
    }

    private void assertCancellationLockedFor(Reservation.TravelStatus travelStatus) {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(
                reservations,
                mock(ReservationEventRepository.class),
                passengers,
                mock(OnboardPassengerUseCase.class));
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .passenger(Passenger.builder().currentBalance(BigDecimal.ZERO).build())
                .status("CONFIRMED")
                .travelStatus(travelStatus)
                .amount(new BigDecimal("2500.00"))
                .build();
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.cancelReservation(reservationId, "PASSENGER"));

        assertEquals(
                "No se puede cancelar la reserva porque la ruta ya fue enviada al chofer o el viaje está en curso.",
                exception.getMessage());
        verify(reservations, never()).saveAndFlush(reservation);
        verify(passengers, never()).saveAndFlush(any());
    }

    @Test
    void pastOutboundCancellationKeepsOutboundAndCreditsOnlyUnusedReturn() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(reservations,
                mock(ReservationEventRepository.class), passengers,
                mock(OnboardPassengerUseCase.class));
        UUID outboundId = UUID.randomUUID();
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        Reservation outbound = Reservation.builder()
                .id(outboundId).passenger(passenger).amount(new BigDecimal("5000.00"))
                .paymentVerified(true).status("CONFIRMED")
                .travelDate(LocalDate.now().minusDays(1))
                .reservationCode("SAN-COR-014-IDA").build();
        Reservation returnLeg = Reservation.builder()
                .id(UUID.randomUUID()).passenger(passenger).amount(new BigDecimal("5000.00"))
                .passengerCount(2).returnedPassengerCount(0)
                .paymentVerified(true).status("CONFIRMED")
                .reservationCode("SAN-COR-014-VUELTA").build();
        when(reservations.findByIdForUpdate(outboundId)).thenReturn(Optional.of(outbound));
        when(reservations.findByReservationCode("SAN-COR-014-VUELTA"))
                .thenReturn(Optional.of(returnLeg));

        service.cancelReservation(outboundId, "PASSENGER");

        assertEquals("CONFIRMED", outbound.getStatus());
        assertEquals("CANCELLED", returnLeg.getStatus());
        assertEquals(new BigDecimal("5000.00"), passenger.getCurrentBalance());
    }

    @Test
    void partialReturnCancellationCreditsOnlyRemainingUnusedSeats() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        ReservationService service = new ReservationService(reservations,
                mock(ReservationEventRepository.class), passengers,
                mock(OnboardPassengerUseCase.class));
        UUID id = UUID.randomUUID();
        Passenger passenger = Passenger.builder().currentBalance(BigDecimal.ZERO).build();
        Reservation returnLeg = Reservation.builder()
                .id(id).passenger(passenger).amount(new BigDecimal("3000.00"))
                .passengerCount(3).returnedPassengerCount(1)
                .paymentVerified(true).status("PARTIALLY_COMPLETED")
                .travelStatus(Reservation.TravelStatus.PARTIALLY_COMPLETED)
                .reservationCode("SAN-COR-015-VUELTA").build();
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(returnLeg));

        service.cancelReservation(id, "PASSENGER");

        assertEquals(new BigDecimal("2000.00"), passenger.getCurrentBalance());
        assertEquals("CANCELLED", returnLeg.getStatus());
    }

    @Test
    void completedReservationCannotBeCancelled() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationService service = new ReservationService(reservations,
                mock(ReservationEventRepository.class), mock(PassengerRepository.class),
                mock(OnboardPassengerUseCase.class));
        UUID id = UUID.randomUUID();
        Reservation completed = Reservation.builder().id(id).status("COMPLETED")
                .travelStatus(Reservation.TravelStatus.COMPLETED)
                .reservationCode("SAN-COR-016-VUELTA").build();
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(completed));

        assertThrows(ReservationAlreadyCompletedException.class,
                () -> service.cancelReservation(id, "ADMIN_PANEL"));
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
    void assigningDateToOpenReturnMovesItToPending() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationEventRepository events = mock(ReservationEventRepository.class);
        ReservationService service = new ReservationService(
                reservations, events, mock(PassengerRepository.class),
                mock(OnboardPassengerUseCase.class));
        UUID id = UUID.randomUUID();
        Reservation openReturn = Reservation.builder()
                .id(id).travelStatus(Reservation.TravelStatus.OPEN_RETURN)
                .reservationCode("MOR-COR-030-VUELTA").build();
        LocalDate scheduledDate = LocalDate.of(2026, 9, 15);
        Reservation update = Reservation.builder().travelDate(scheduledDate).build();
        when(reservations.findById(id)).thenReturn(Optional.of(openReturn));
        when(reservations.saveAndFlush(openReturn)).thenReturn(openReturn);

        Reservation saved = service.updateReservation(id, update, "ADMIN_PANEL");

        assertEquals(scheduledDate, saved.getTravelDate());
        assertEquals(Reservation.TravelStatus.PENDING, saved.getTravelStatus());
        verify(reservations).saveAndFlush(openReturn);
    }

    @Test
    void verifyPaymentLocksAndUpdatesManagedReservation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        UUID id = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(id).paymentVerified(false).status("PAYMENT_RECEIVED").build();
        when(reservations.findById(id)).thenReturn(Optional.of(reservation));
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));

        Reservation result = service.verifyPayment(id);

        assertSame(reservation, result);
        assertEquals(true, reservation.getPaymentVerified());
        assertEquals("CONFIRMED", reservation.getStatus());
        assertNotNull(reservation.getPaymentConfirmedAt());
        verify(reservations).findByIdForUpdate(id);
        verify(reservations).saveAllAndFlush(List.of(reservation));
    }

    @Test
    void verifyPaymentSynchronizesBothRoundTripLegs() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        UUID outboundId = UUID.randomUUID();
        Reservation outbound = Reservation.builder().id(outboundId)
                .reservationCode("ARR-COR-001-IDA").paymentVerified(false)
                .status("PAYMENT_RECEIVED").build();
        Reservation returned = Reservation.builder().id(UUID.randomUUID())
                .reservationCode("ARR-COR-001-VUELTA").paymentVerified(false)
                .status("PENDING_PAYMENT").build();
        when(reservations.findById(outboundId)).thenReturn(Optional.of(outbound));
        when(reservations.findReservationGroupForUpdate("ARR-COR-001"))
                .thenReturn(List.of(outbound, returned));
        ReservationService service = new ReservationService(
                reservations, mock(ReservationEventRepository.class),
                mock(PassengerRepository.class), mock(OnboardPassengerUseCase.class));

        service.verifyPayment(outboundId);

        assertTrue(outbound.getPaymentVerified());
        assertTrue(returned.getPaymentVerified());
        assertEquals("CONFIRMED", outbound.getStatus());
        assertEquals("CONFIRMED", returned.getStatus());
        assertNotNull(outbound.getPaymentConfirmedAt());
        assertEquals(outbound.getPaymentConfirmedAt(), returned.getPaymentConfirmedAt());
        verify(reservations).saveAllAndFlush(List.of(outbound, returned));
    }
}
