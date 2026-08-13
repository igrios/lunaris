package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.payment.ProcessedTransactionLedgerPort;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentWebhookUseCase.PaymentWebhookCommand;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentWebhookUseCaseTest {

    @Mock
    private ReservationRepository reservations;
    @Mock
    private MessagingPort messaging;
    @Mock
    private ProcessedTransactionLedgerPort ledger;

    @Test
    void confirmaYNotificaUnPagoAprobado() {
        Passenger passenger = Passenger.builder().phone("543511112222").build();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .reservationCode("MOR-COR-001")
                .passenger(passenger)
                .status("PENDING_PAYMENT")
                .paymentVerified(false)
                .amount(new BigDecimal("25000.00"))
                .build();
        when(ledger.claim(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(reservations.findPendingPaymentCandidatesForUpdate()).thenReturn(List.of(reservation));

        new ProcessPaymentWebhookUseCase(reservations, messaging, ledger)
                .process(new PaymentWebhookCommand("123", "approved",
                        new BigDecimal("25000.00"), null, "payer@test.com"));

        assertTrue(reservation.getPaymentVerified());
        assertEquals("CONFIRMED", reservation.getStatus());
        verify(reservations).saveAllAndFlush(java.util.List.of(reservation));
        verify(messaging).sendText("543511112222", """
                ✅ *¡Pago acreditado! Tu reserva está confirmada.*

                🚗 Un operador coordinará los detalles de tu retiro.
                """);
    }

    @Test
    void ignoraNotificacionesQueNoEstanAprobadas() {
        new ProcessPaymentWebhookUseCase(reservations, messaging, ledger)
                .process(new PaymentWebhookCommand("123", "pending",
                        new BigDecimal("25000.00"), null, null));

        verify(reservations, never()).findPendingPaymentCandidatesForUpdate();
        verify(messaging, never()).sendText(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void noConfirmaCuandoElMontoCoincideConMasDeUnaReserva() {
        Reservation first = pending("A-001", "18000.00");
        Reservation second = pending("B-001", "18000.00");
        when(ledger.claim(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(reservations.findPendingPaymentCandidatesForUpdate())
                .thenReturn(List.of(first, second));

        new ProcessPaymentWebhookUseCase(reservations, messaging, ledger)
                .process(new PaymentWebhookCommand("124", "approved",
                        new BigDecimal("18000.00"), null, null));

        verify(reservations, never()).saveAllAndFlush(org.mockito.ArgumentMatchers.any());
        verify(messaging, never()).sendText(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    private Reservation pending(String code, String amount) {
        return Reservation.builder().id(UUID.randomUUID()).reservationCode(code)
                .passenger(Passenger.builder().phone("543511112222").build())
                .status("PENDING_PAYMENT").paymentVerified(false)
                .amount(new BigDecimal(amount)).build();
    }
}
