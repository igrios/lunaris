package com.lunaris.ansenuza.application.conversation;

/**
 * Mensaje entrante ya normalizado y desacoplado del formato crudo del webhook de Meta.
 *
 * @param from    número de teléfono normalizado del remitente
 * @param type    tipo de mensaje recibido
 * @param body    texto del mensaje (para TEXT) o id del botón pulsado (para INTERACTIVE); puede ser null
 * @param mediaId id del adjunto multimedia (para IMAGE); puede ser null
 */
public record IncomingMessage(String from, MessageType type, String body, String mediaId) {

    public enum MessageType {
        TEXT, IMAGE, INTERACTIVE, OTHER
    }

    public boolean isImageWithMedia() {
        return type == MessageType.IMAGE && mediaId != null;
    }
}
