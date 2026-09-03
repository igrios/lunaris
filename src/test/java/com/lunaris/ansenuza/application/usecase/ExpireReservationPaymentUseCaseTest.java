package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpireReservationPaymentUseCaseTest {

    @Test
    void expiresUnverifiedReservationAndWritesAuditEvent() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationEventRepository events = mock(ReservationEventRepository.class);
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 8, 17, 12, 0);
        Reservation reservation = Reservation.builder()
                .id(id).status("PENDING_PAYMENT").paymentVerified(false)
                .paymentExpiresAt(now.minusSeconds(1)).build();
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        int result = new ExpireReservationPaymentUseCase(reservations, events)
                .execute(id, now);

        assertEquals(1, result);
        assertEquals("EXPIRED", reservation.getStatus());
        assertEquals(Reservation.TravelStatus.CANCELED, reservation.getTravelStatus());
        verify(events).save(org.mockito.ArgumentMatchers.argThat(event ->
                "PAYMENT_EXPIRED".equals(event.getEventType())
                        && "SYSTEM_TTL".equals(event.getTriggeredBy())));
    }

    @Test
    void neverExpiresReservationWhoseReceiptIsUnderReview() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationEventRepository events = mock(ReservationEventRepository.class);
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 8, 17, 12, 0);
        Reservation reservation = Reservation.builder()
                .id(id).status("PAYMENT_RECEIVED").paymentVerified(false)
                .paymentExpiresAt(now.minusMinutes(10)).build();
        when(reservations.findByIdForUpdate(id)).thenReturn(Optional.of(reservation));

        int result = new ExpireReservationPaymentUseCase(reservations, events)
                .execute(id, now);

        assertEquals(0, result);
        assertEquals("PAYMENT_RECEIVED", reservation.getStatus());
        verify(reservations, never()).save(reservation);
        verify(events, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
