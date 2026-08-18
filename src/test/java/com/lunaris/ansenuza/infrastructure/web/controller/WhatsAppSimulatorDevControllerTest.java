package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WhatsAppSimulatorDevControllerTest {

    private final WhatsAppServiceDevMock whatsApp = new WhatsAppServiceDevMock();
    private final ConversationOrchestrator orchestrator = mock(ConversationOrchestrator.class);
    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final WhatsAppSimulatorDevController controller =
            new WhatsAppSimulatorDevController(whatsApp, orchestrator, sessions);

    @Test
    void sendsButtonPayloadToTheRealConversationEntryPoint() {
        controller.reply(new WhatsAppSimulatorDevController.UserReply(
                "3515551234", null, "CONFIRM_TRIP"));

        ArgumentCaptor<IncomingMessage> captor = ArgumentCaptor.forClass(IncomingMessage.class);
        verify(orchestrator).process(captor.capture());
        assertThat(captor.getValue().from()).isEqualTo("5493515551234");
        assertThat(captor.getValue().type()).isEqualTo(IncomingMessage.MessageType.INTERACTIVE);
        assertThat(captor.getValue().body()).isEqualTo("CONFIRM_TRIP");
        assertThat(whatsApp.messagesFor("3515551234").getFirst().direction())
                .isEqualTo(WhatsAppServiceDevMock.Direction.USER);
    }

    @Test
    void resetsPersistentAndInMemorySession() {
        ConversationSession session = new ConversationSession();
        whatsApp.sendMessage("3515551234", "Mensaje");
        when(sessions.findByPhoneNumber("5493515551234")).thenReturn(Optional.of(session));

        controller.reset("3515551234");

        verify(sessions).delete(session);
        assertThat(whatsApp.messagesFor("3515551234")).isEmpty();
    }
}
