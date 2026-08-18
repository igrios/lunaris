package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock.SimulatorMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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

    public WhatsAppSimulatorDevController(WhatsAppServiceDevMock whatsApp,
            ConversationOrchestrator orchestrator, ConversationSessionRepository sessions) {
        this.whatsApp = whatsApp;
        this.orchestrator = orchestrator;
        this.sessions = sessions;
    }

    @GetMapping("/messages")
    public List<SimulatorMessage> messages(@RequestParam @NotBlank String phone) {
        return whatsApp.messagesFor(phone);
    }

    @PostMapping("/send-user-reply")
    public ResponseEntity<List<SimulatorMessage>> reply(@Valid @RequestBody UserReply request) {
        String phone = whatsApp.normalize(request.phone());
        String value = request.isButton() ? request.payload() : request.text();
        whatsApp.recordUserMessage(phone, value, request.isButton() ? request.payload() : null);
        orchestrator.process(new IncomingMessage(phone,
                request.isButton() ? IncomingMessage.MessageType.INTERACTIVE
                        : IncomingMessage.MessageType.TEXT,
                value, null));
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

    public record UserReply(@NotBlank String phone, String text, String payload) {
        public boolean isButton() { return payload != null && !payload.isBlank(); }

        @AssertTrue(message = "Debe indicar text o payload, pero no ambos.")
        public boolean isValidContent() {
            boolean hasText = text != null && !text.isBlank();
            boolean hasPayload = payload != null && !payload.isBlank();
            return hasText ^ hasPayload;
        }
    }
}
