package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppWebhookParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador de entrada HTTP del webhook de WhatsApp Cloud API.
 *
 * <p>Responsabilidad única: verificar el handshake, parsear el payload crudo a un
 * {@link IncomingMessage} y delegar de forma asíncrona en la capa de aplicación
 * (orquestación conversacional o procesamiento de comprobantes). Toda la lógica de
 * negocio vive fuera de esta clase, respetando la arquitectura hexagonal.
 */
@RestController
@RequestMapping("/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final WhatsAppWebhookParser webhookParser;
    private final ConversationOrchestrator conversationOrchestrator;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        if ("lunaris123".equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        try {
            IncomingMessage message = webhookParser.parse(payload);
            if (message == null) {
                return ResponseEntity.ok().build();
            }

            CompletableFuture.runAsync(() -> {
                try {
                    if (message.isImageWithMedia()) {
                        processPaymentReceiptUseCase.execute(message.from(), message.mediaId());
                    } else if (message.body() != null) {
                        conversationOrchestrator.process(message);
                    }
                } catch (Exception ex) {
                    log.error("Error asincrónico crítico: ", ex);
                }
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error crítico general: ", e);
            return ResponseEntity.ok().build();
        }
    }
}
