package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.application.usecase.WhatsAppWebhookInboxService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppWebhookParser;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppMessageDispatcher;
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
@Slf4j
public class WhatsAppWebhookController {

    private final WhatsAppWebhookParser webhookParser;
    private final ConversationOrchestrator conversationOrchestrator;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;
    private final WhatsAppMessageDispatcher messageDispatcher;
    private final WhatsAppWebhookInboxService inboxService;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final String verifyToken;
    private final String appSecret;

    public WhatsAppWebhookController(
            WhatsAppWebhookParser webhookParser,
            ConversationOrchestrator conversationOrchestrator,
            ProcessPaymentReceiptUseCase processPaymentReceiptUseCase,
            WhatsAppMessageDispatcher messageDispatcher,
            WhatsAppWebhookInboxService inboxService,
            ObjectMapper objectMapper,
            Environment environment,
            @Value("${whatsapp.verify-token:}") String verifyToken,
            @Value("${whatsapp.app-secret:}") String appSecret) {
        this.webhookParser = webhookParser;
        this.conversationOrchestrator = conversationOrchestrator;
        this.processPaymentReceiptUseCase = processPaymentReceiptUseCase;
        this.messageDispatcher = messageDispatcher;
        this.inboxService = inboxService;
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.verifyToken = verifyToken;
        this.appSecret = appSecret;
    }

    @jakarta.annotation.PostConstruct
    void validateProductionConfiguration() {
        if (environment.acceptsProfiles(Profiles.of("prod", "production"))
                && (appSecret == null || appSecret.isBlank())) {
            throw new IllegalStateException(
                    "WHATSAPP_APP_SECRET es obligatoria en producción.");
        }
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        if (!this.verifyToken.isBlank() && MessageDigest.isEqual(
                this.verifyToken.getBytes(StandardCharsets.UTF_8),
                verifyToken.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody byte[] rawPayload) {
        if (!isValidSignature(rawPayload, signature)) {
            log.warn("Webhook de WhatsApp rechazado por firma ausente o inválida.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(
                    rawPayload, new TypeReference<>() {});
            IncomingMessage message = webhookParser.parse(payload);
            if (message == null) {
                return ResponseEntity.ok().build();
            }
            if (message.messageId() == null || message.messageId().isBlank()) {
                log.warn("Webhook de WhatsApp descartado porque no contiene messageId.");
                return ResponseEntity.ok().build();
            }
            if (!inboxService.claim(message.messageId())) {
                log.debug("Webhook de WhatsApp duplicado ignorado: {}", message.messageId());
                return ResponseEntity.ok().build();
            }

            messageDispatcher.dispatch(message.from(), () -> {
                if (message.isImageWithMedia()) {
                    processPaymentReceiptUseCase.execute(message.from(), message.mediaId());
                } else if (message.body() != null) {
                    conversationOrchestrator.process(message);
                }
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error crítico general: ", e);
            return ResponseEntity.ok().build();
        }
    }

    private boolean isValidSignature(byte[] payload, String signature) {
        if (appSecret == null || appSecret.isBlank()) {
            return false;
        }
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            byte[] supplied = HexFormat.of().parseHex(signature.substring("sha256=".length()));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return MessageDigest.isEqual(mac.doFinal(payload), supplied);
        } catch (Exception exception) {
            return false;
        }
    }
}
