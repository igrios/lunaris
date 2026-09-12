package com.lunaris.ansenuza.infrastructure.chat;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebSocketLiveChatAdapterTest {
    private final ConversationSessionRepository sessions = mock(ConversationSessionRepository.class);
    private final LocalityRepository localities = mock(LocalityRepository.class);
    private final WebSocketLiveChatAdapter adapter = new WebSocketLiveChatAdapter(
            mock(ChatMessageRepository.class), mock(SimpMessagingTemplate.class), sessions, localities);

    @Test
    void mapsInteractiveButtonPayloadsToOperatorFriendlyText() {
        assertEquals("Mantener reserva (No cancelar)", adapter.readableText("549351", "no_cancel"));
        assertEquals("Confirmar cancelación", adapter.readableText("549351", "confirm_cancel"));
        verifyNoInteractions(sessions, localities);
    }

    @Test
    void mapsOriginSelectionIncludingCordobaOption() {
        when(sessions.findByPhoneNumber("549351")).thenReturn(Optional.of(
                ConversationSession.builder().currentStep("ASK_LOCALITY").build()));
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Morteros").build(), Locality.builder().name("Arroyito").build()));

        assertEquals("1 - Morteros", adapter.readableText("549351", "1"));
        assertEquals("3 - Córdoba", adapter.readableText("549351", "3"));
    }

    @Test
    void mapsDestinationSelectionAndLeavesUnrelatedNumbersUntouched() {
        when(sessions.findByPhoneNumber("549351")).thenReturn(Optional.of(
                ConversationSession.builder().currentStep("ASK_TOWN_DESTINATION").build()));
        when(localities.findAllWithActiveFare()).thenReturn(List.of(
                Locality.builder().name("Morteros").build(), Locality.builder().name("Arroyito").build()));

        assertEquals("1 - Arroyito", adapter.readableText("549351", "1"));
        assertEquals("99", adapter.readableText("549351", "99"));
        assertEquals("texto libre", adapter.readableText("549351", "texto libre"));
    }
}
