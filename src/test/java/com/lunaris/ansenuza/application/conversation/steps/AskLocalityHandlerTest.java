package com.lunaris.ansenuza.application.conversation.steps;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class AskLocalityHandlerTest {

    @Test
    void selectionIsRestrictedToLocalitiesReturnedByActiveFareQuery() {
        ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
        LocalityRepository localities = mock(LocalityRepository.class);
        PricingAndScheduleService pricing = mock(PricingAndScheduleService.class);
        MessagingPort messaging = mock(MessagingPort.class);
        AskLocalityHandler handler = new AskLocalityHandler(
                sessions, localities, pricing, messaging);
        ConversationSession session = ConversationSession.builder()
                .phoneNumber("543511112222")
                .currentStep("ASK_LOCALITY")
                .build();
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Morteros").build()));

        handler.handle(session, new IncomingMessage(
                session.getPhoneNumber(), IncomingMessage.MessageType.TEXT, "2", null));

        verify(localities).findAllWithActiveFare();
        verify(messaging).sendText(eq(session.getPhoneNumber()), contains("Selección inválida"));
        verify(sessions, never()).saveAndFlush(session);
    }
}
