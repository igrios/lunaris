package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SelectScheduleHandlerTest {

    @Test
    void acceptsStableScheduleButtonPayload() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        PassengerRepository passengers = mock(PassengerRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        SelectScheduleHandler handler = new SelectScheduleHandler(sessions, passengers, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .currentStep("SELECT_SCHEDULE")
                .build();
        when(passengers.findByPhone(session.getPhoneNumber())).thenReturn(Optional.empty());

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "schedule_03_00", null));

        assertEquals("03:00 AM", session.getScheduleBlock());
        assertEquals("ASK_NAME", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }
}
