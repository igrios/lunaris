package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;

class AskAddressTextHandlerTest {

    @Test
    void storesMapsLinkWhenPassengerSharesWhatsappLocation() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskAddressTextHandler handler = new AskAddressTextHandler(sessions, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543512282251")
                .currentStep("ASK_ADDRESS_TEXT")
                .build();
        IncomingMessage location = new IncomingMessage(
                session.getPhoneNumber(),
                IncomingMessage.MessageType.LOCATION,
                "https://maps.google.com/?q=-31.42,-64.18",
                null,
                -31.42,
                -64.18);

        handler.handle(session, location);

        assertEquals("https://maps.google.com/?q=-31.42,-64.18", session.getPickupAddress());
        assertEquals("ASK_DESTINATION", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }
}
