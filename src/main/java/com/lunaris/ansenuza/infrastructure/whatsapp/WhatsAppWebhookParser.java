 package com.lunaris.ansenuza.infrastructure.whatsapp;

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
    public IncomingMessage parse(Map<String, Object> payload) {
        Map<?, ?> entry = firstMap(payload == null ? null : payload.get("entry"));
        Map<?, ?> change = firstMap(entry == null ? null : entry.get("changes"));
        Map<?, ?> value = mapValue(change == null ? null : change.get("value"));
        Map<?, ?> message = firstMap(value == null ? null : value.get("messages"));
        if (message == null) {
            return null;
        }

        String from = normalizeWhatsAppNumber(stringValue(message.get("from")));
        String type = stringValue(message.get("type"));

        if ("image".equals(type)) {
            Map<?, ?> imageData = mapValue(message.get("image"));
            String mediaId = imageData != null ? stringValue(imageData.get("id")) : null;
            return new IncomingMessage(from, MessageType.IMAGE, null, mediaId);
        }

        if ("text".equals(type)) {
            Map<?, ?> text = mapValue(message.get("text"));
            String body = text != null ? stringValue(text.get("body")) : null;
            return new IncomingMessage(from, MessageType.TEXT, body, null);
        }

        if ("location".equals(type)) {
            Map<?, ?> location = mapValue(message.get("location"));
            Double latitude = numberValue(location, "latitude");
            Double longitude = numberValue(location, "longitude");
            if (latitude == null || longitude == null) {
                return new IncomingMessage(from, MessageType.OTHER, null, null);
            }
            String mapsUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
            return new IncomingMessage(
                    from, MessageType.LOCATION, mapsUrl, null, latitude, longitude);
        }

        if ("button".equals(type)) {
            Map<?, ?> buttonData = mapValue(message.get("button"));
            String body = buttonData != null ? stringValue(buttonData.get("payload")) : null;
            if (body == null && buttonData != null) {
                body = stringValue(buttonData.get("text"));
            }
            if (body == null || body.isBlank()) return null;
            return new IncomingMessage(from, MessageType.INTERACTIVE, body, null);
        }

        if ("interactive".equals(type)) {
            String body = null;
            Map<?, ?> interactive = mapValue(message.get("interactive"));
            if (interactive != null) {
                if ("button_reply".equals(interactive.get("type"))) {
                    body = interactiveReplyId(interactive, "button_reply");
                } else if ("list_reply".equals(interactive.get("type"))) {
                    body = interactiveReplyId(interactive, "list_reply");
                }
            }
            log.info(
                    "[WhatsApp Webhook] Interactive response parsed. from={}, type={}, payload={}",
                    from, interactive != null ? interactive.get("type") : null, body);
            if (body == null || body.isBlank()) {
                log.warn("[WhatsApp Webhook] Respuesta interactiva descartada: falta un ID válido.");
                return null;
            }
            return new IncomingMessage(from, MessageType.INTERACTIVE, body, null);
        }

        return new IncomingMessage(from, MessageType.OTHER, null, null);
    }

    private String normalizeWhatsAppNumber(String phone) {
        return (phone != null && phone.startsWith("549")) ? "54" + phone.substring(3) : phone;
    }

    private Double numberValue(Map<?, ?> values, String key) {
        if (values == null || !(values.get(key) instanceof Number number)) {
            return null;
        }
        return number.doubleValue();
    }

    private String interactiveReplyId(Map<?, ?> interactive, String replyKey) {
        Object reply = interactive.get(replyKey);
        if (!(reply instanceof Map<?, ?> replyData)) {
            return null;
        }
        Object id = replyData.get("id");
        return id instanceof String value ? value : null;
    }

    private Map<?, ?> firstMap(Object value) {
        if (!(value instanceof java.util.List<?> values) || values.isEmpty()) return null;
        return mapValue(values.getFirst());
    }

    private Map<?, ?> mapValue(Object value) {
        return value instanceof Map<?, ?> map ? map : null;
    }

    private String stringValue(Object value) {
        return value instanceof String string ? string : null;
    }
}
