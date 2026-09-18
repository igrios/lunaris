package com.lunaris.ansenuza.application.conversation;

/**
 * Mensaje entrante ya normalizado y desacoplado del formato crudo del webhook de Meta.
 *
 * @param from    número de teléfono normalizado del remitente
 * @param type    tipo de mensaje recibido
 * @param body    texto del mensaje (para TEXT) o id del botón pulsado (para INTERACTIVE); puede ser null
 * @param mediaId id o URL del adjunto multimedia (para IMAGE o DOCUMENT); puede ser null
 * @param latitude latitud compartida (para LOCATION); puede ser null
 * @param longitude longitud compartida (para LOCATION); puede ser null
 */
public record IncomingMessage(
        String messageId, String from, MessageType type, String body, String mediaId,
        Double latitude, Double longitude,
        com.lunaris.ansenuza.application.telemetry.ChatbotInteraction telemetry, boolean analyticsTest) {

    public IncomingMessage {
        if (telemetry == null) telemetry = com.lunaris.ansenuza.application.telemetry.ChatbotInteraction.NONE;
    }

    public IncomingMessage(String messageId, String from, MessageType type, String body, String mediaId,
            Double latitude, Double longitude) {
        this(messageId, from, type, body, mediaId, latitude, longitude,
                com.lunaris.ansenuza.application.telemetry.ChatbotInteraction.NONE, false);
    }

    public IncomingMessage withTelemetry(com.lunaris.ansenuza.application.telemetry.ChatbotInteraction context) {
        return context == null || !context.active() ? this
                : new IncomingMessage(messageId, from, type, body, mediaId, latitude, longitude, context, analyticsTest);
    }

    public IncomingMessage asAnalyticsTest() {
        return new IncomingMessage(messageId, from, type, body, mediaId, latitude, longitude, telemetry, true);
    }

    public enum MessageType {
        TEXT, IMAGE, DOCUMENT, INTERACTIVE, LOCATION, OTHER
    }

    public IncomingMessage(String from, MessageType type, String body, String mediaId) {
        this(null, from, type, body, mediaId, null, null);
    }

    public IncomingMessage(String from, MessageType type, String body, String mediaId,
            Double latitude, Double longitude) {
        this(null, from, type, body, mediaId, latitude, longitude);
    }

    public boolean isImageWithMedia() {
        return type == MessageType.IMAGE && mediaId != null;
    }

    public boolean isMediaWithResource() {
        return (type == MessageType.IMAGE || type == MessageType.DOCUMENT)
                && mediaId != null && !mediaId.isBlank();
    }

    public String pickupAddress() {
        if (type == MessageType.LOCATION && latitude != null && longitude != null) {
            return "https://maps.google.com/?q=" + latitude + "," + longitude;
        }
        return body == null ? null : body.trim();
    }
}
