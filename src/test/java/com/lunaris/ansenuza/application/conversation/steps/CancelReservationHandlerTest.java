package com.lunaris.ansenuza.application.conversation.steps;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.IncomingMessage.MessageType;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CancelReservationHandlerTest {

    @Test
    void explainsThatAnOnboardTripCannotBeCancelledThroughTheBot() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        ReservationRepository reservations = mock(ReservationRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        CancelReservationHandler handler = new CancelReservationHandler(
                sessions, reservations, reservationService, messaging);
        String phone = "5493511111111";
        String code = "MOR-COR-001-IDA";
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .reservationCode(code)
                .passenger(Passenger.builder().phone(phone).build())
                .travelStatus(Reservation.TravelStatus.ONBOARD)
                .build();
        when(reservations.findByReservationCode(code)).thenReturn(Optional.of(reservation));
        doThrow(new IllegalStateException("cancelación bloqueada"))
                .when(reservationService).cancelReservation(reservationId, "BOT_WHATSAPP");

        handler.handle(
                ConversationSession.builder().phoneNumber(phone).build(),
                new IncomingMessage(phone, MessageType.TEXT, code, null));

        verify(messaging).sendText(phone,
                "⚠️ Tu viaje ya se encuentra en proceso o la ruta fue asignada al chofer, "
                        + "por lo que no es posible realizar la cancelación por este medio. "
                        + "Comunícate con un operador.");
    }
}
