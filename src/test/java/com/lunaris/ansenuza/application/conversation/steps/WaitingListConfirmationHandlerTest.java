package com.lunaris.ansenuza.application.conversation.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.junit.jupiter.api.Test;

class WaitingListConfirmationHandlerTest {

    @Test
    void acceptedInvitationPersistsEntryAndClosesSession() {
        WaitingListService waitingList = mock(WaitingListService.class);
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        MessagingPort messaging = mock(MessagingPort.class);
        WaitingListConfirmationHandler handler =
                new WaitingListConfirmationHandler(waitingList, sessions, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .build();

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.INTERACTIVE,
                "waiting_list_yes", null));

        verify(waitingList).join(session);
        verify(sessions).delete(session);
        verify(messaging).sendText(
                org.mockito.ArgumentMatchers.eq(session.getPhoneNumber()),
                org.mockito.ArgumentMatchers.contains("LISTA DE ESPERA"));
    }
}
