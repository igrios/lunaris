package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.SendOperatorMessageUseCase;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistedChatWebSocketTest {
    @Test
    void delegatesOnlyTextAndReturnsPersistedMessage() {
        var useCase = mock(SendOperatorMessageUseCase.class);
        var controller = new ChatWebSocketController(useCase, null, null);
        var payload = ChatMessage.builder().id(99L).messageText("Confirmamos los datos").build();
        var saved = ChatMessage.builder().id(1L).fromOperator(true).build();
        when(useCase.execute("5493562123456", "Confirmamos los datos")).thenReturn(saved);
        assertSame(saved, controller.sendMessage("5493562123456", payload));
        verify(useCase).execute("5493562123456", "Confirmamos los datos");
    }
}
