package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.PassengerAddressResolver;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.junit.jupiter.api.Test;

class AskIndividualCompanionHandlerTest {

    @Test
    void repeatedWebhookDoesNotDuplicateCompanionOrSeat() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskIndividualCompanionHandler handler = new AskIndividualCompanionHandler(
                sessions, mock(PassengerAddressResolver.class), messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111").passengerCount(1).totalCompanions(2)
                .currentCompanionIndex(1).companionNames("").build();
        IncomingMessage message = new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.TEXT, "Ada Lovelace", null);

        handler.handle(session, message);
        handler.handle(session, message);

        assertEquals("Ada Lovelace", session.getCompanionNames());
        assertEquals(2, session.getPassengerCount());
        assertEquals(2, session.getCurrentCompanionIndex());
        verify(sessions, org.mockito.Mockito.times(2)).saveAndFlush(session);
    }

    @Test
    void corruptedSessionDataIsCappedAtFourSeats() {
        PassengerAddressResolver addressResolver = mock(PassengerAddressResolver.class);
        AskIndividualCompanionHandler handler = new AskIndividualCompanionHandler(
                mock(ConversationSessionRepository.class), addressResolver,
                mock(MessagingPort.class));
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511111111").passengerCount(99).totalCompanions(99)
                .companionNames("Uno, Dos, Tres").build();

        handler.handle(session, new IncomingMessage(session.getPhoneNumber(),
                IncomingMessage.MessageType.TEXT, "Cuatro", null));

        assertEquals(4, session.getPassengerCount());
        assertEquals("Uno, Dos, Tres", session.getCompanionNames());
        verify(addressResolver).resolve(session.getPhoneNumber(), session);
    }
}
