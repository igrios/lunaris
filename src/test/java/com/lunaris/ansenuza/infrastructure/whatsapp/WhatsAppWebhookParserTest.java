package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;

class WhatsAppWebhookParserTest {

    private final WhatsAppWebhookParser parser = new WhatsAppWebhookParser();

    @Test
    void parsesSharedLocationAndBuildsDriverMapsLink() {
        IncomingMessage message = parser.parse(payload(Map.of(
                "from", "5493512282251",
                "type", "location",
                "location", Map.of(
                        "latitude", -31.4201,
                        "longitude", -64.1888))));

        assertEquals(IncomingMessage.MessageType.LOCATION, message.type());
        assertEquals("543512282251", message.from());
        assertEquals(-31.4201, message.latitude());
        assertEquals(-64.1888, message.longitude());
        assertEquals("https://maps.google.com/?q=-31.4201,-64.1888", message.pickupAddress());
    }

    @Test
    void preservesTypedStreetAndNumberAsPickupAddress() {
        IncomingMessage message = parser.parse(payload(Map.of(
                "from", "543512282251",
                "type", "text",
                "text", Map.of("body", "  Av. San Martín 450  "))));

        assertEquals(IncomingMessage.MessageType.TEXT, message.type());
        assertEquals("Av. San Martín 450", message.pickupAddress());
    }

    private Map<String, Object> payload(Map<String, Object> message) {
        return Map.of("entry", List.of(Map.of(
                "changes", List.of(Map.of(
                        "value", Map.of("messages", List.of(message)))))));
    }
}
