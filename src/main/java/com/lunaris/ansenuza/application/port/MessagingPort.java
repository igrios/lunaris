package com.lunaris.ansenuza.application.port;

import java.util.List;

/**
 * Puerto de salida para enviar mensajes salientes al pasajero.
 * La capa de aplicación depende de esta abstracción, nunca del proveedor concreto
 * (WhatsApp Cloud API), respetando la inversión de dependencias hexagonal.
 */
public interface MessagingPort {

    void sendText(String to, String message);

    void sendButtons(String to, String header, String body, List<Button> buttons);

    void requestLocation(String to, String message);

    void sendImage(String to, String imageUrl, String caption);

    void sendTemplate(String to, String templateName, List<String> parameters);

    /**
     * Envía un documento (PDF) ubicado en {@code absoluteFilePath} al destinatario.
     * Usado para mandar la factura por WhatsApp.
     */
    void sendDocument(String to, String absoluteFilePath, String fileName, String caption);
}
