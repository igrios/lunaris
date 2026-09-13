package com.lunaris.ansenuza.application.port;

/**
 * Puerto de salida para reflejar los mensajes entrantes del cliente en la sala
 * de chat en vivo del operador (persistencia + broadcast por WebSocket).
 */
public interface LiveChatPort {

    default void conversationChanged() {}

    void recordIncomingMessage(String phoneNumber, String text);
}
