package com.lunaris.ansenuza.application.port;

import com.lunaris.ansenuza.application.telemetry.ChatbotInteraction;
import com.lunaris.ansenuza.application.telemetry.ChatbotSignal;

public interface ChatbotTelemetryPort {
    ChatbotInteraction begin(String phone, String messageId, boolean test);
    void record(ChatbotSignal signal);
    default void handoff(String phone) {}

    ChatbotTelemetryPort NOOP = new ChatbotTelemetryPort() {
        public ChatbotInteraction begin(String phone, String messageId, boolean test) {
            return ChatbotInteraction.NONE;
        }
        public void record(ChatbotSignal signal) {}
    };
}

