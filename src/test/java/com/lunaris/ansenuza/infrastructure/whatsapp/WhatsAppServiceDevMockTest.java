package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class WhatsAppServiceDevMockTest {

    private final WhatsAppServiceDevMock service = new WhatsAppServiceDevMock();

    @Test
    void capturesTextAndInteractiveMessagesInOrder() {
        service.sendMessage("351 555-1234", "Hola");
        service.sendInteractiveButtons("5493515551234", "Elegí", List.of(
                Map.of("id", "YES", "title", "Sí")));

        var messages = service.messagesFor("3515551234");

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).body()).isEqualTo("Hola");
        assertThat(messages.get(1).buttons()).containsExactly(
                new WhatsAppServiceDevMock.SimulatorButton("YES", "Sí"));
    }

    @Test
    void resetOnlyClearsRequestedPhone() {
        service.sendMessage("3515551234", "Uno");
        service.sendMessage("3515555678", "Dos");

        service.reset("3515551234");

        assertThat(service.messagesFor("3515551234")).isEmpty();
        assertThat(service.messagesFor("3515555678")).hasSize(1);
    }

    @Test
    void devProfileRegistersOnlyTheMockStrategy() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(WhatsAppService.class, WhatsAppServiceDevMock.class);
            context.refresh();

            assertThat(context.getBeansOfType(WhatsAppService.class))
                    .hasSize(1)
                    .allSatisfy((name, bean) -> assertThat(bean)
                            .isInstanceOf(WhatsAppServiceDevMock.class));
        }
    }

    @Test
    void recordsUserMediaWithItsTypeAndResourceUrl() {
        service.recordUserMessage("3515551234", IncomingMessage.MessageType.IMAGE,
                "Comprobante", null, "/images/receipt.jpg");
        service.recordUserMessage("3515551234", IncomingMessage.MessageType.DOCUMENT,
                null, null, "/documents/receipt.pdf");

        var messages = service.messagesFor("3515551234");
        assertThat(messages).extracting(WhatsAppServiceDevMock.SimulatorMessage::type)
                .containsExactly(WhatsAppServiceDevMock.MessageType.IMAGE,
                        WhatsAppServiceDevMock.MessageType.DOCUMENT);
        assertThat(messages).extracting(WhatsAppServiceDevMock.SimulatorMessage::resourceUrl)
                .containsExactly("/images/receipt.jpg", "/documents/receipt.pdf");
    }
}
