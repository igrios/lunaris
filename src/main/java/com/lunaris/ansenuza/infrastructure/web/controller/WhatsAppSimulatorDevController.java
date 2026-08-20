package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock.SimulatorMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/whatsapp-simulator")
public class WhatsAppSimulatorDevController {

    private final WhatsAppServiceDevMock whatsApp;
    private final ConversationOrchestrator orchestrator;
    private final ConversationSessionRepository sessions;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;

    public WhatsAppSimulatorDevController(WhatsAppServiceDevMock whatsApp,
            ConversationOrchestrator orchestrator, ConversationSessionRepository sessions,
            ProcessPaymentReceiptUseCase processPaymentReceiptUseCase) {
        this.whatsApp = whatsApp;
        this.orchestrator = orchestrator;
        this.sessions = sessions;
        this.processPaymentReceiptUseCase = processPaymentReceiptUseCase;
    }

    @GetMapping("/messages")
    public List<SimulatorMessage> messages(@RequestParam @NotBlank String phone) {
        return whatsApp.messagesFor(phone);
    }

    @PostMapping("/send-user-reply")
    public ResponseEntity<List<SimulatorMessage>> reply(@Valid @RequestBody UserReply request) {
        String phone = whatsApp.normalize(request.phone());
        IncomingMessage.MessageType type = request.resolvedType();
        String value = type == IncomingMessage.MessageType.INTERACTIVE
                ? request.payload() : request.text();
        String resourceUrl = request.mediaUrl();
        whatsApp.recordUserMessage(phone, type, value, request.payload(), resourceUrl);

        IncomingMessage incoming = new IncomingMessage(
                phone, type, value, request.isMedia() ? resourceUrl : null);
        orchestrator.process(incoming);
        if (incoming.isMediaWithResource()) {
            processPaymentReceiptUseCase.executeStoredReceipt(phone, resourceUrl);
        }
        return ResponseEntity.ok(whatsApp.messagesFor(phone));
    }

    @DeleteMapping("/reset-session")
    @Transactional
    public ResponseEntity<Void> reset(@RequestParam @NotBlank String phone) {
        String normalized = whatsApp.normalize(phone);
        sessions.findByPhoneNumber(normalized).ifPresent(sessions::delete);
        whatsApp.reset(normalized);
        return ResponseEntity.noContent().build();
    }

    public record UserReply(@NotBlank String phone, String text, String payload,
            String mediaUrl, String messageType) {

        public UserReply(String phone, String text, String payload) {
            this(phone, text, payload, null, null);
        }

        public boolean isButton() { return payload != null && !payload.isBlank(); }

        public boolean isMedia() {
            IncomingMessage.MessageType type = resolvedType();
            return type == IncomingMessage.MessageType.IMAGE
                    || type == IncomingMessage.MessageType.DOCUMENT;
        }

        public IncomingMessage.MessageType resolvedType() {
            if (messageType == null || messageType.isBlank()) {
                if (isButton()) return IncomingMessage.MessageType.INTERACTIVE;
                if (mediaUrl != null && !mediaUrl.isBlank()) return IncomingMessage.MessageType.IMAGE;
                return IncomingMessage.MessageType.TEXT;
            }
            try {
                return IncomingMessage.MessageType.valueOf(
                        messageType.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return IncomingMessage.MessageType.OTHER;
            }
        }

        @AssertTrue(message = "El contenido no coincide con el tipo de mensaje.")
        public boolean isValidContent() {
            boolean hasText = text != null && !text.isBlank();
            boolean hasPayload = payload != null && !payload.isBlank();
            boolean hasMedia = mediaUrl != null && !mediaUrl.isBlank();
            return switch (resolvedType()) {
                case TEXT -> hasText && !hasPayload && !hasMedia;
                case INTERACTIVE -> hasPayload && !hasText && !hasMedia;
                case IMAGE, DOCUMENT -> hasMedia && !hasPayload;
                default -> false;
            };
        }
    }
}
