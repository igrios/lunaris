package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConfirmationHandlerCapacityTest {

    @Test
    void fullScheduleOffersWaitingListWithoutCreatingPassenger() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        PricingAndScheduleService schedules = mock(PricingAndScheduleService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListCapacityGuard capacityGuard = mock(WaitingListCapacityGuard.class);
        ConfirmationHandler handler = new ConfirmationHandler(
                sessions, passengers, schedules, mock(PromotionService.class),
                mock(ReservationService.class), messaging, capacityGuard);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .travelDate(LocalDate.of(2026, 8, 20))
                .scheduleBlock("08:00 AM")
                .passengerCount(2)
                .currentStep("ASK_CONFIRMATION")
                .build();
        when(capacityGuard.offerWaitingListWhenFull(session)).thenReturn(true);

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "confirm_ok", null));

        verify(capacityGuard).offerWaitingListWhenFull(session);
        verify(passengers, never()).findByPhone(session.getPhoneNumber());
    }

    @Test
    void fullWalletBalanceConfirmsReservationWithoutRequestingReceipt() {
        Fixture fixture = new Fixture(new BigDecimal("60000.00"), new BigDecimal("50000.00"));
        when(fixture.reservations.saveReservationFlow(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);
                    fixture.passenger.setCurrentBalance(new BigDecimal("10000.00"));
                    reservation.setPaymentVerified(true);
                    reservation.setStatus("CONFIRMED");
                    return List.of(reservation);
                });

        fixture.handler.handle(fixture.session, fixture.confirmationMessage());

        verify(fixture.messaging, never()).sendImage(any(), any(), any());
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(fixture.messaging).sendText(eq(fixture.session.getPhoneNumber()), message.capture());
        assertTrue(message.getValue().contains("Cubrimos el total del viaje con tu saldo a favor"));
        assertTrue(message.getValue().contains("Saldo utilizado: $50000.00"));
        assertTrue(message.getValue().contains("Saldo restante en tu cuenta: $10000.00"));
    }

    @Test
    void partialWalletBalanceRequestsReceiptOnlyForRemainingAmount() {
        Fixture fixture = new Fixture(new BigDecimal("20000.00"), new BigDecimal("50000.00"));
        when(fixture.reservations.saveReservationFlow(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);
                    fixture.passenger.setCurrentBalance(BigDecimal.ZERO);
                    reservation.setAmount(new BigDecimal("30000.00"));
                    return List.of(reservation);
                });

        fixture.handler.handle(fixture.session, fixture.confirmationMessage());

        verify(fixture.messaging).sendImage(
                eq(fixture.session.getPhoneNumber()), any(), eq("Datos para la transferencia"));
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(fixture.messaging).sendText(eq(fixture.session.getPhoneNumber()), message.capture());
        assertTrue(message.getValue().contains("Aplicamos $20000.00 de tu saldo a favor"));
        assertTrue(message.getValue().contains("Importe restante a transferir: $30000.00"));
    }

    @Test
    void airportTripIsSavedPendingAndRequestsManualReviewWithoutPricing() {
        Fixture fixture = new Fixture(BigDecimal.ZERO, new BigDecimal("50000.00"));
        fixture.session.setDestination("Aeropuerto Internacional Pajas Bláncas");
        ArgumentCaptor<Reservation> reservation = ArgumentCaptor.forClass(Reservation.class);
        when(fixture.reservations.saveReservationFlow(reservation.capture()))
                .thenAnswer(invocation -> {
                    Reservation saved = invocation.getArgument(0);
                    return List.of(saved);
                });

        fixture.handler.handle(fixture.session, fixture.confirmationMessage());

        assertEquals(BigDecimal.ZERO, reservation.getValue().getAmount());
        assertEquals("PENDING", reservation.getValue().getStatus());
        verifyNoInteractions(fixture.pricing);
        verify(fixture.messaging, never()).sendImage(any(), any(), any());
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(fixture.messaging).sendText(eq(fixture.session.getPhoneNumber()), message.capture());
        assertTrue(message.getValue().contains("espera de revisión"));
        assertTrue(message.getValue().contains("horario"));
        assertTrue(message.getValue().contains("cotización"));
    }

    private static final class Fixture {
        private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        private final PassengerRepository passengers = mock(PassengerRepository.class);
        private final PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        private final PromotionService promotions = mock(PromotionService.class);
        private final ReservationService reservations = mock(ReservationService.class);
        private final MessagingPort messaging = mock(MessagingPort.class);
        private final WaitingListCapacityGuard capacity = mock(WaitingListCapacityGuard.class);
        private final Passenger passenger;
        private final ConversationSession session;
        private final ConfirmationHandler handler;

        private Fixture(BigDecimal balance, BigDecimal price) {
            UUID passengerId = UUID.randomUUID();
            passenger = Passenger.builder()
                    .id(passengerId)
                    .phone("543511112222")
                    .firstName("Ana")
                    .lastName("Pérez")
                    .currentBalance(balance)
                    .build();
            session = ConversationSession.builder()
                    .phoneNumber(passenger.getPhone())
                    .passengerName("Ana Pérez")
                    .pickupLocality("Morteros")
                    .pickupAddress("San Martín 450")
                    .destination("Córdoba")
                    .travelDate(LocalDate.of(2026, 8, 20))
                    .scheduleBlock("08:00 AM")
                    .roundTrip(false)
                    .passengerCount(1)
                    .currentStep("ASK_CONFIRMATION")
                    .build();
            when(passengers.findByPhone(passenger.getPhone())).thenReturn(Optional.of(passenger));
            when(passengers.findById(passengerId)).thenReturn(Optional.of(passenger));
            when(pricing.calculateTripPrice("Morteros", Boolean.FALSE, 1)).thenReturn(price);
            handler = new ConfirmationHandler(
                    sessions, passengers, pricing, promotions,
                    reservations, messaging, capacity);
        }

        private IncomingMessage confirmationMessage() {
            return new IncomingMessage(
                    session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                    "confirm_ok", null);
        }
    }
}
