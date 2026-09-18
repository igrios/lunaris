package com.lunaris.ansenuza.application.telemetry;

import java.util.UUID;

/** Only pseudonymous correlation and controlled codes; never message content. */
public record ChatbotSignal(UUID sessionId, String interactionKey, ChatbotEventType type,
        String step, ChatbotReason reason, String bookingGroupCode, UUID reservationId) {}

