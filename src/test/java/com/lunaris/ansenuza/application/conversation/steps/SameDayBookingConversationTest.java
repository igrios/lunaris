package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SameDayBookingConversationTest {

    @Test
    void rejectsTypedTodayWithRequiredLogisticsMessage() {
        MessagingPort messaging = mock(MessagingPort.class);
        SameDayBookingPolicy policy = mock(SameDayBookingPolicy.class);
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        doThrow(new SameDayBookingClosedException()).when(policy).validate(today, null);
        AskDateHandler handler = new AskDateHandler(
                mock(ConversationSessionRepository.class), messaging,
                mock(OperationControlService.class), policy);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111").build();

        handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.TEXT,
                today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), null));

        verify(messaging).sendText(session.getPhoneNumber(),
                SameDayBookingClosedException.MESSAGE);
    }

    @Test
    void excludesTodayButtonAfterCutoff() {
        MessagingPort messaging = mock(MessagingPort.class);
        SameDayBookingPolicy policy = mock(SameDayBookingPolicy.class);
        when(policy.isTodayClosed(null)).thenReturn(true);
        OperationControlService operationControl = mock(OperationControlService.class);
        AskTripTypeHandler handler = new AskTripTypeHandler(
                mock(ConversationSessionRepository.class), messaging, policy, operationControl);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111").build();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Button>> buttons = ArgumentCaptor.forClass(List.class);

        handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.INTERACTIVE, "trip_ida", null));

        verify(messaging).sendButtons(eq(session.getPhoneNumber()), eq("Fecha del viaje"),
                argThat(body -> body.contains("¿Qué día")
                        && body.contains("Otra fecha")), buttons.capture());
        assertFalse(buttons.getValue().stream().anyMatch(button -> button.title().startsWith("Hoy (")));
        assertTrue(buttons.getValue().stream().anyMatch(button ->
                button.title().equals("Mañana")));
        assertTrue(buttons.getValue().stream().anyMatch(button ->
                "return_other_date".equals(button.id())));
    }

    @Test
    void usesDayAfterTomorrowAsPrimaryOptionWhenTomorrowLogisticsAreClosed() {
        MessagingPort messaging = mock(MessagingPort.class);
        SameDayBookingPolicy policy = mock(SameDayBookingPolicy.class);
        OperationControlService operationControl = mock(OperationControlService.class);
        when(policy.isTodayClosed(null)).thenReturn(true);
        when(operationControl.isPastCutoffTime()).thenReturn(true);
        AskTripTypeHandler handler = new AskTripTypeHandler(
                mock(ConversationSessionRepository.class), messaging, policy, operationControl);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111").build();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Button>> buttons = ArgumentCaptor.forClass(List.class);

        handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.INTERACTIVE, "trip_ida", null));

        verify(messaging).sendButtons(eq(session.getPhoneNumber()), eq("Fecha del viaje"),
                contains("¿Qué día"), buttons.capture());
        String expected = com.lunaris.ansenuza.shared.ArgentinaTime.today().plusDays(2)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertTrue(buttons.getValue().getFirst().id().equals(expected));
        assertFalse(buttons.getValue().stream()
                .anyMatch(button -> button.title().startsWith("Mañana (")));
    }
}
