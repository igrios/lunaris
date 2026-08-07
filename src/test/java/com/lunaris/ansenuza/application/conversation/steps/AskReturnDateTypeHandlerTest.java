package com.lunaris.ansenuza.application.conversation.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AskReturnDateTypeHandlerTest {

    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final MessagingPort messaging = mock(MessagingPort.class);
    private final AskReturnDateTypeHandler handler = new AskReturnDateTypeHandler(sessions, messaging);

    @Test
    void sameDayReturnCopiesOutboundDateAndContinues() {
        LocalDate outboundDate = LocalDate.of(2026, 8, 10);
        ConversationSession session = session(outboundDate);

        handler.handle(session, interactive("return_same_day"));

        assertEquals(outboundDate, session.getReturnDate());
        assertEquals("ASK_DNI_REQUIRED", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void openReturnLeavesDateNullAndContinues() {
        ConversationSession session = session(LocalDate.of(2026, 8, 10));
        session.setReturnDate(LocalDate.of(2026, 8, 11));

        handler.handle(session, interactive("return_open"));

        assertNull(session.getReturnDate());
        assertEquals("ASK_DNI_REQUIRED", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }

    @Test
    void chooseDateRequestsCustomDate() {
        ConversationSession session = session(LocalDate.of(2026, 8, 10));

        handler.handle(session, interactive("return_choose_date"));

        assertEquals("ASK_RETURN_DATE", session.getCurrentStep());
        verify(sessions).saveAndFlush(session);
    }

    private ConversationSession session(LocalDate outboundDate) {
        return ConversationSession.builder()
                .phoneNumber("543511111111")
                .travelDate(outboundDate)
                .roundTrip(true)
                .build();
    }

    private IncomingMessage interactive(String body) {
        return new IncomingMessage("543511111111",
                IncomingMessage.MessageType.INTERACTIVE, body, null);
    }
}
