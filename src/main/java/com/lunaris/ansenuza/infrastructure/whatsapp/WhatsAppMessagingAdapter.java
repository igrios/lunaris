package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link MessagingPort} sobre la WhatsApp Cloud API.
 * Traduce el modelo agnóstico de la aplicación (textos y {@link Button}) al formato que
 * espera {@link WhatsAppService}.
 */
@Component
@RequiredArgsConstructor
public class WhatsAppMessagingAdapter implements MessagingPort {

    private final WhatsAppService whatsAppService;

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
    public void requestLocation(String to, String message) {
        whatsAppService.sendLocationRequest(to, message);
    }

    @Override
    public void sendDocument(String to, String absoluteFilePath, String fileName, String caption) {
        whatsAppService.sendDocument(to, absoluteFilePath, fileName, caption);
    }
}
