 package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.IncomingMessage.MessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce el payload crudo del webhook de WhatsApp Cloud API a un {@link IncomingMessage}
 * agnóstico. Aísla el formato propietario de Meta dentro de la capa de infraestructura.
 */
@Component
@Slf4j
public class WhatsAppWebhookParser {

    /**
     * @return el mensaje entrante, o {@code null} si el payload no contiene un mensaje procesable
     *         (eventos de status, payloads vacíos, etc.).
     */
    @SuppressWarnings("unchecked")
    public IncomingMessage parse(Map<String, Object> payload) {
        List<Map<String, Object>> entry = (List<Map<String, Object>>) payload.get("entry");
        if (entry == null || entry.isEmpty()) {
            return null;
        }

        Map<String, Object> change =
                (Map<String, Object>) ((List<?>) entry.get(0).get("changes")).get(0);
        Map<String, Object> value = (Map<String, Object>) change.get("value");
        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");

        if (messages == null || messages.isEmpty()) {
            return null;
        }

        Map<String, Object> message = messages.get(0);
        String from = normalizeWhatsAppNumber((String) message.get("from"));
        String type = (String) message.get("type");

        if ("image".equals(type)) {
            Map<String, Object> imageData = (Map<String, Object>) message.get("image");
            String mediaId = imageData != null ? (String) imageData.get("id") : null;
            return new IncomingMessage(from, MessageType.IMAGE, null, mediaId);
        }

        if ("text".equals(type)) {
            Map<String, Object> text = (Map<String, Object>) message.get("text");
            String body = text != null ? (String) text.get("body") : null;
            return new IncomingMessage(from, MessageType.TEXT, body, null);
        }

        if ("button".equals(type)) {
            Map<String, Object> buttonData = (Map<String, Object>) message.get("button");
            String body = buttonData != null ? (String) buttonData.get("payload") : null;
            if (body == null && buttonData != null) {
                body = (String) buttonData.get("text");
            }
            return new IncomingMessage(from, MessageType.INTERACTIVE, body, null);
        }

        if ("interactive".equals(type)) {
            String body = null;
            Map<String, Object> interactive = (Map<String, Object>) message.get("interactive");
            if (interactive != null) {
                if ("button_reply".equals(interactive.get("type"))) {
                    body = (String) ((Map<String, Object>) interactive.get("button_reply")).get("id");
                } else if ("list_reply".equals(interactive.get("type"))) {
                    body = (String) ((Map<String, Object>) interactive.get("list_reply")).get("id");
                }
            }
            return new IncomingMessage(from, MessageType.INTERACTIVE, body, null);
        }

        return new IncomingMessage(from, MessageType.OTHER, null, null);
    }

    private String normalizeWhatsAppNumber(String phone) {
        return (phone != null && phone.startsWith("549")) ? "54" + phone.substring(3) : phone;
    }
}
