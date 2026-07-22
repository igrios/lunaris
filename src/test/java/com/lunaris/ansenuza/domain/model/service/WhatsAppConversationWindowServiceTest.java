package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;

class WhatsAppConversationWindowServiceTest {

    @Test
    void incomingReplyKeepsFreeChatActiveForTwentyFourHours() {
        ChatMessageRepository repository = mock(ChatMessageRepository.class);
        when(repository.findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc("543512282251"))
                .thenReturn(Optional.of(incomingAt(LocalDateTime.now().minusHours(23))));

        assertTrue(new WhatsAppConversationWindowService(repository).isActive("543512282251"));
    }

    @Test
    void freeChatIsDisabledAfterTwentyFourHours() {
        ChatMessageRepository repository = mock(ChatMessageRepository.class);
        when(repository.findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc("543512282251"))
                .thenReturn(Optional.of(incomingAt(LocalDateTime.now().minusHours(25))));

        assertFalse(new WhatsAppConversationWindowService(repository).isActive("543512282251"));
    }

    private ChatMessage incomingAt(LocalDateTime timestamp) {
        return ChatMessage.builder()
                .phoneNumber("543512282251")
                .messageText("Ok Gracias")
                .fromOperator(false)
                .timestamp(timestamp)
                .build();
    }
}
