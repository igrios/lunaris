package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReturnWindowSelectionHandlerTest {

    @Test
    void vespertineSelectionUpdatesReturnScheduleAndStatus() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        ReturnWindowSelectionHandler handler = new ReturnWindowSelectionHandler(
                reservations, sessions, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("5493511111111")
                .reservationCode("ARR-COR-001-VUELTA")
                .currentStep("RETURN_WINDOW_SELECTION")
                .build();
        Reservation reservation = Reservation.builder()
                .reservationCode(session.getReservationCode())
                .travelStatus(Reservation.TravelStatus.OPEN_RETURN)
                .build();
        when(reservations.findByReservationCodeForUpdate(session.getReservationCode()))
                .thenReturn(Optional.of(reservation));

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE, "2", null));

        assertEquals("17:30", reservation.getDepartureSchedule());
        assertEquals(Reservation.TravelStatus.CONFIRMED, reservation.getTravelStatus());
        assertEquals("START", session.getCurrentStep());
        verify(reservations).saveAndFlush(reservation);
    }
}
