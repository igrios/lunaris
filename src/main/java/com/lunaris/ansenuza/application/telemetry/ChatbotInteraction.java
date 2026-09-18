package com.lunaris.ansenuza.application.telemetry;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import java.util.List;
import java.util.UUID;

/** Immutable context passed explicitly across business transactions and async sends. */
public record ChatbotInteraction(ChatbotTelemetryPort port, UUID sessionId, String interactionKey) {
    public static final ChatbotInteraction NONE = new ChatbotInteraction(null, null, "");
    public boolean active() { return port != null && sessionId != null; }

    public void emit(ChatbotEventType type, String step) { emit(type, step, null); }
    public void emit(ChatbotEventType type, String step, ChatbotReason reason) {
        if (active()) port.record(new ChatbotSignal(sessionId, interactionKey, type, step, reason, null, null));
    }
    public void bookingCreated(String step, String group, UUID reservationId) {
        if (active()) port.record(new ChatbotSignal(sessionId, interactionKey,
                ChatbotEventType.BOOKING_CREATED, step, null, group, reservationId));
    }
    public void sendButtons(MessagingPort messaging, String to, String header, String body,
            List<Button> buttons, ChatbotEventType success, String step) {
        if (!active()) {
            messaging.sendButtons(to, header, body, buttons);
            return;
        }
        messaging.sendButtons(to, header, body, buttons, sent -> emit(
                sent ? success : ChatbotEventType.SEND_FAILED, step,
                sent ? null : ChatbotReason.SEND_FAILED));
    }
}

