package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.AssistedChatService;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistedChatWebSocketTest {
    @Test void manualPermissionDispatchesMessage() {
        var repository = mock(ChatMessageRepository.class);
        var whatsapp = mock(WhatsAppService.class);
        var assisted = mock(AssistedChatService.class);
        var controller = new ChatWebSocketController(repository, whatsapp, null, null, null, assisted);
        var message = ChatMessage.builder().messageText("Confirmamos los datos").build();
        when(assisted.canSend("123")).thenReturn(true);
        when(repository.save(message)).thenReturn(message);
        assertSame(message, controller.sendMessage("123", message));
        verify(whatsapp).sendMessage("123", "Confirmamos los datos");
        assertTrue(message.isFromOperator());
    }

    @Test void expiredUnmanagedConversationIsRejectedBeforeSending() {
        var repository = mock(ChatMessageRepository.class);
        var whatsapp = mock(WhatsAppService.class);
        var assisted = mock(AssistedChatService.class);
        var controller = new ChatWebSocketController(repository, whatsapp, null, null, null, assisted);
        assertThrows(IllegalStateException.class, () -> controller.sendMessage("123", new ChatMessage()));
        verifyNoInteractions(repository, whatsapp);
    }
}
