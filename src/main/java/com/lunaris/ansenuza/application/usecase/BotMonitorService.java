package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class BotMonitorService {
    private final ConversationSessionRepository sessions;
    private final SimpMessagingTemplate messaging;

    @Transactional(readOnly = true)
    public List<ConversationSessionRepository.MonitorRow> rows() {
        return sessions.findMonitorRows();
    }

    @Transactional
    public void setPaused(long id, boolean paused) {
        var session = sessions.findById(id)
                .orElseThrow(() -> new DomainValidationException("Sesión no encontrada."));
        session.setBotPaused(paused);
        session.setManuallyPaused(paused);
        sessions.saveAndFlush(session);
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() {
                        messaging.convertAndSend("/topic/bot-monitor", Map.of("action", "REFRESH"));
                    }
                });
    }
}
