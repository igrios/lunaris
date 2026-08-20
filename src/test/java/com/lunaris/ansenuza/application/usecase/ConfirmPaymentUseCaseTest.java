package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.application.port.MessagingPort;

class ConfirmPaymentUseCaseTest {

    @Test
    void consumesPromotionAndConfirmsCompleteReservationGroup() {
        UUID selectedId = UUID.randomUUID();
        Reservation outbound = reservation(selectedId, "MOR-COR-001-IDA", "1234", false);
        Reservation inbound = reservation(UUID.randomUUID(), "MOR-COR-001-VUELTA", "1234", false);
        ReservationRepository repository = mock(ReservationRepository.class);
        PromotionService promotionService = mock(PromotionService.class);
        when(repository.findById(selectedId)).thenReturn(Optional.of(outbound));
        when(repository.findReservationGroupForUpdate("MOR-COR-001"))
                .thenReturn(List.of(outbound, inbound));

        newUseCase(repository, promotionService).execute(selectedId);

        verify(promotionService).consumeIfAvailable("1234", null);
        verify(repository).saveAll(List.of(outbound, inbound));
        assertTrue(outbound.getPaymentVerified());
        assertTrue(inbound.getPaymentVerified());
        assertEquals("CONFIRMED", outbound.getStatus());
        assertEquals("CONFIRMED", inbound.getStatus());
    }

    @Test
    void repairsUnconsumedPromotionWhenReservationWasAlreadyConfirmed() {
        UUID selectedId = UUID.randomUUID();
        Reservation reservation = reservation(selectedId, "MOR-COR-002-IDA", "5678", true);
        ReservationRepository repository = mock(ReservationRepository.class);
        PromotionService promotionService = mock(PromotionService.class);
        when(repository.findById(selectedId)).thenReturn(Optional.of(reservation));
        when(repository.findReservationGroupForUpdate("MOR-COR-002"))
                .thenReturn(List.of(reservation));

        newUseCase(repository, promotionService).execute(selectedId);

        verify(promotionService).consumeIfAvailable("5678", null);
    }

    @Test
    void confirmsOneWayReservationWithCodeWithoutGroupSuffix() {
        UUID selectedId = UUID.randomUUID();
        Reservation oneWay = reservation(selectedId, "MOR-COR-003", null, false);
        ReservationRepository repository = mock(ReservationRepository.class);
        PromotionService promotionService = mock(PromotionService.class);
        when(repository.findById(selectedId)).thenReturn(Optional.of(oneWay));
        when(repository.findByIdForUpdate(selectedId)).thenReturn(Optional.of(oneWay));

        Reservation result = new ConfirmPaymentUseCase(repository, promotionService,
                mock(WaitingListRepository.class), mock(ConversationSessionRepository.class))
                .execute(selectedId);

        assertEquals(oneWay, result);
        assertTrue(oneWay.getPaymentVerified());
        assertEquals("CONFIRMED", oneWay.getStatus());
        verify(repository).saveAll(List.of(oneWay));
        verify(repository, never()).findReservationGroupForUpdate(anyString());
    }

    @Test
    void confirmedPaymentMarksLinkedWaitingEntryConverted() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = reservation(reservationId, "MOR-COR-004", null, false);
        reservation.setWaitingListEntryId(7L);
        reservation.setPassenger(Passenger.builder().phone("543511112222").build());
        WaitingListEntry entry = WaitingListEntry.builder()
                .id(7L).status(WaitingListEntry.AWAITING_PAYMENT).build();
        ReservationRepository reservations = mock(ReservationRepository.class);
        WaitingListRepository waitingList = mock(WaitingListRepository.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        when(waitingList.findByIdForUpdate(7L)).thenReturn(Optional.of(entry));

        new ConfirmPaymentUseCase(
                reservations, mock(PromotionService.class), waitingList, sessions)
                .execute(reservationId);

        assertEquals(WaitingListEntry.CONVERTED, entry.getStatus());
        verify(waitingList).saveAndFlush(entry);
    }

    @Test
    void notifiesPassengerThroughMessagingPortWhenPaymentIsApproved() {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = reservation(reservationId, "MOR-COR-005", null, false);
        reservation.setDestination("Córdoba");
        reservation.setPassenger(Passenger.builder()
                .firstName("Ana").phone("543511112222").build());
        ReservationRepository reservations = mock(ReservationRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        when(reservations.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservations.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));

        new ConfirmPaymentUseCase(reservations, mock(PromotionService.class),
                mock(WaitingListRepository.class), mock(ConversationSessionRepository.class),
                null, messaging).execute(reservationId);

        verify(messaging).sendText(
                org.mockito.ArgumentMatchers.eq("543511112222"),
                org.mockito.ArgumentMatchers.contains("Pago Verificado"));
    }

    private ConfirmPaymentUseCase newUseCase(
            ReservationRepository repository, PromotionService promotionService) {
        return new ConfirmPaymentUseCase(
                repository, promotionService,
                mock(WaitingListRepository.class),
                mock(ConversationSessionRepository.class));
    }

    private Reservation reservation(UUID id, String reservationCode, String promotionCode, boolean paid) {
        return Reservation.builder()
                .id(id)
                .reservationCode(reservationCode)
                .promotionCode(promotionCode)
                .paymentVerified(paid)
                .status(paid ? "CONFIRMED" : "PENDING_PAYMENT")
                .build();
    }
}
