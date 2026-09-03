package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;

class WhatsAppWebhookParserTest {

    @Test
    void parsesViewRouteQuickReplyPayload() {
        IncomingMessage message = parser.parse(payload(Map.of(
                "id", "wamid.123",
                "from", "5493512282251",
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button_reply",
                        "button_reply", Map.of(
                                "id", "VIEW_ROUTE",
                                "title", "🗺️ Ver Ruta")))));

        assertEquals(IncomingMessage.MessageType.INTERACTIVE, message.type());
        assertEquals("wamid.123", message.messageId());
        assertEquals("VIEW_ROUTE", message.body());
    }

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

    @Test
    void parsesInteractiveListReplyBoardingPayload() {
        IncomingMessage message = parser.parse(payload(Map.of(
                "from", "5493512282251",
                "type", "interactive",
                "interactive", Map.of(
                        "type", "list_reply",
                        "list_reply", Map.of(
                                "id", "ONBOARD_5ca1ab1e-6806-4a50-94e3-3785b4bf5b68",
                                "title", "Pasajero a bordo")))));

        assertEquals(IncomingMessage.MessageType.INTERACTIVE, message.type());
        assertEquals(
                "ONBOARD_5ca1ab1e-6806-4a50-94e3-3785b4bf5b68",
                message.body());
    }

    @Test
    void parsesInteractiveButtonReplyBoardingPayload() {
        IncomingMessage message = parser.parse(payload(Map.of(
                "from", "543512282251",
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button_reply",
                        "button_reply", Map.of(
                                "id", "BOARD_ID_5ca1ab1e-6806-4a50-94e3-3785b4bf5b68",
                                "title", "Confirmar")))));

        assertEquals(IncomingMessage.MessageType.INTERACTIVE, message.type());
        assertEquals(
                "BOARD_ID_5ca1ab1e-6806-4a50-94e3-3785b4bf5b68",
                message.body());
    }

    @Test
    void returnsNullForMissingOrEmptyWebhookStructure() {
        assertNull(parser.parse(null));
        assertNull(parser.parse(Map.of()));
        assertNull(parser.parse(Map.of("entry", List.of())));
        assertNull(parser.parse(Map.of("entry", List.of(Map.of("changes", List.of())))));
    }

    @Test
    void returnsNullForMalformedInteractiveReplyWithoutId() {
        assertNull(parser.parse(payload(Map.of(
                "from", "5493512282251",
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button_reply",
                        "button_reply", Map.of("title", "Sin identificador"))))));
    }

    @Test
    void returnsNullWhenPayloadNodesHaveUnexpectedTypes() {
        assertNull(parser.parse(Map.of("entry", "invalid")));
        assertNull(parser.parse(Map.of("entry", List.of(Map.of("changes", "invalid")))));
    }

    private Map<String, Object> payload(Map<String, Object> message) {
        return Map.of("entry", List.of(Map.of(
                "changes", List.of(Map.of(
                        "value", Map.of("messages", List.of(message)))))));
    }
}
