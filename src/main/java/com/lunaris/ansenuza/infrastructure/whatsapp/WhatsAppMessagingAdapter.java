package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.List;
import java.util.Map;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link MessagingPort} sobre la WhatsApp Cloud API.
 * Traduce el modelo agnóstico de la aplicación (textos y {@link Button}) al formato que
 * espera {@link WhatsAppService}.
 */
@RequiredArgsConstructor
public class WhatsAppMessagingAdapter implements MessagingPort {

    private final WhatsAppService whatsAppService;

    @Override
    public void sendText(String to, String text, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendText(to, text, outcome);
    }

    @Override
    public void sendTemplate(String to, String template, List<String> parameters, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendTemplate(to, template, parameters, outcome);
    }

    @Override
    public void sendDocumentUrl(String to, String url, String name, String caption, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendDocumentUrl(to, url, name, caption, outcome);
    }

    @Override
    public void sendText(String to, String message) {
        whatsAppService.sendMessage(to, message);
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons) {
        List<Map<String, String>> mappedButtons = buttons.stream()
                .map(b -> Map.of("id", b.id(), "title", b.title()))
                .toList();
        whatsAppService.sendInteractiveButtons(to, header, body, mappedButtons);
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons,
            java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendButtons(to, header, body, buttons, outcome);
    }

    @Override
    public void requestLocation(String to, String message) {
        whatsAppService.sendLocationRequest(to, message);
    }

    @Override
    public void sendImage(String to, String imageUrl, String caption) {
        whatsAppService.sendImageMessage(to, imageUrl, caption);
    }

    @Override
    public void sendTemplate(String to, String templateName, List<String> parameters) {
        whatsAppService.sendTemplate(to, templateName, parameters);
    }

    @Override
    public void sendOtp(String to, String passengerName, String code) {
        whatsAppService.sendOtpMessage(to, passengerName, code);
    }

    @Override
    public void sendDocument(String to, String absoluteFilePath, String fileName, String caption) {
        whatsAppService.sendDocument(to, absoluteFilePath, fileName, caption);
    }

    @Override
    public void sendDocumentUrl(String to, String documentUrl, String fileName, String caption) {
        whatsAppService.sendDocumentUrl(to, documentUrl, fileName, caption);
    }
}
