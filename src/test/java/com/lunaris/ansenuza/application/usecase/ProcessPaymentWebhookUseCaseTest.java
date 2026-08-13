package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentWebhookUseCase.PaymentWebhookCommand;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
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

    @Test
    void confirmaYNotificaUnPagoAprobado() {
        Passenger passenger = Passenger.builder().phone("543511112222").build();
        Reservation reservation = Reservation.builder()
                .reservationCode("MOR-COR-001")
                .passenger(passenger)
                .status("PENDING_PAYMENT")
                .paymentVerified(false)
                .build();
        when(reservations.findByReservationCodeForUpdate("MOR-COR-001"))
                .thenReturn(Optional.of(reservation));

        new ProcessPaymentWebhookUseCase(reservations, messaging)
                .process(new PaymentWebhookCommand("approved", "MOR-COR-001"));

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
        new ProcessPaymentWebhookUseCase(reservations, messaging)
                .process(new PaymentWebhookCommand("pending", "MOR-COR-001"));

        verify(reservations, never()).findByReservationCodeForUpdate("MOR-COR-001");
        verify(messaging, never()).sendText(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }
}
