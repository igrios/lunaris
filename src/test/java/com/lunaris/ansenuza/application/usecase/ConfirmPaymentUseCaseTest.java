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

        new ConfirmPaymentUseCase(repository, promotionService).execute(selectedId);

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

        new ConfirmPaymentUseCase(repository, promotionService).execute(selectedId);

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

        Reservation result = new ConfirmPaymentUseCase(repository, promotionService)
                .execute(selectedId);

        assertEquals(oneWay, result);
        assertTrue(oneWay.getPaymentVerified());
        assertEquals("CONFIRMED", oneWay.getStatus());
        verify(repository).saveAll(List.of(oneWay));
        verify(repository, never()).findReservationGroupForUpdate(anyString());
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
