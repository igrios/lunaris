package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TakeOverConversationUseCase {
    private final ConversationSessionRepository sessionRepository;

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
        return session.getPhoneNumber();
    }
}
