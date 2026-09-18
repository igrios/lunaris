package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TakeOverConversationUseCase {
    private final ConversationSessionRepository sessionRepository;
    private final com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry;

    @org.springframework.beans.factory.annotation.Autowired
    public TakeOverConversationUseCase(ConversationSessionRepository sessionRepository,
            com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry) {
        this.sessionRepository = sessionRepository;
        this.telemetry = telemetry;
    }

    public TakeOverConversationUseCase(ConversationSessionRepository sessionRepository) {
        this(sessionRepository, com.lunaris.ansenuza.application.port.ChatbotTelemetryPort.NOOP);
    }

    @Transactional
    public String execute(String phoneNumber) {
        // Keep every country/area digit; never interpret a session ID as a phone.
        String phone = phoneNumber == null ? "" : phoneNumber.replaceAll("[\\s()+-]", "");
        if (!phone.matches("[1-9][0-9]{7,14}")) {
            throw new DomainValidationException("El teléfono debe incluir país y área completos.");
        }
        var session = sessionRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new DomainValidationException("No existe una conversación para ese teléfono."));
        if (!session.isBotPaused()) {
            session.setBotPaused(true);
            sessionRepository.saveAndFlush(session);
        }
        telemetry.handoff(session.getPhoneNumber());
        return session.getPhoneNumber();
    }
}
