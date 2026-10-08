package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.TakeOverConversationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChatTakeoverControllerTest {
    @Test
    void contactTemplateActivatesManualManagement() {
        var takeOver = mock(TakeOverConversationUseCase.class);
        var passengers = mock(com.lunaris.ansenuza.domain.repository.PassengerRepository.class);
        var whatsapp = mock(com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService.class);
        var controller = new ChatController(null, null, null, passengers, null, whatsapp, takeOver, null);
        var attributes = new RedirectAttributesModelMap();
        when(passengers.findByPhone("123")).thenReturn(java.util.Optional.empty());
        assertEquals("redirect:/admin/chat/123", controller.reopenConversation("123", attributes));
        var order = inOrder(whatsapp, takeOver);
        order.verify(whatsapp).sendContactoPasajeroTemplate("123", "Pasajero");
        order.verify(takeOver).execute("123");
    }

    @Test
    void redirectsToPersistentInternalChatAfterTakeover() {
        var takeOver = mock(TakeOverConversationUseCase.class);
        var controller = new ChatController(null, null, null, null, null, null, takeOver, null);
        var attributes = new RedirectAttributesModelMap();
        when(takeOver.execute("5493562123456")).thenReturn("5493562123456");

        assertEquals("redirect:/admin/chat/{phoneNumber}", controller.takeOver("5493562123456", attributes));
        assertEquals("5493562123456", attributes.getAttribute("phoneNumber"));
        verify(takeOver).execute("5493562123456");
    }
}
