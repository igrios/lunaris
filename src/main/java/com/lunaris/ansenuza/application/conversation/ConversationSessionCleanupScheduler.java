package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🧹 Tarea programada que cierra las sesiones del bot de WhatsApp que quedaron a mitad de camino
 * y sin respuesta del cliente. Así un pasajero que abandonó la conversación arranca de cero
 * (menú principal) la próxima vez, en vez de quedar atrapado en un paso intermedio.
 *
 * <p>Se excluyen las sesiones con {@code botPaused = true}: esas están siendo atendidas por un
 * operador humano y no deben tocarse.
 *
 * <p>Parametrizable vía {@code application.yaml}:
 * <ul>
 *   <li>{@code bot.session.inactive-minutes} (default 30): minutos sin actividad para considerar abandonada.</li>
 *   <li>{@code bot.session.cleanup-interval-ms} (default 600000 = 10 min): cada cuánto corre la limpieza.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationSessionCleanupScheduler {

    private static final String SESSION_INACTIVITY_TIMEOUT_KEY = "session.inactivity.timeout.minutes";
    private static final long DEFAULT_INACTIVE_MINUTES = 30;

    private final ConversationSessionRepository conversationSessionRepository;
    private final SystemConfigurationService configurationService;

    @Scheduled(fixedDelayString = "${bot.session.cleanup-interval-ms:600000}")
    @Transactional
    public void purgeInactiveSessions() {
        long inactiveMinutes = resolveInactiveMinutes();
        LocalDateTime cutoff =
                com.lunaris.ansenuza.shared.ArgentinaTime.now().minusMinutes(inactiveMinutes);
        List<ConversationSession> abandonadas =
                conversationSessionRepository.findByBotPausedFalseAndLastInteractionBefore(cutoff);

        if (abandonadas.isEmpty()) {
            return;
        }

        conversationSessionRepository.deleteAll(abandonadas);
        log.info("[Cleanup] Se cerraron {} sesiones de bot inactivas (sin actividad > {} min).",
                abandonadas.size(), inactiveMinutes);
    }

    private long resolveInactiveMinutes() {
        String configuredValue = configurationService.getValue(
                SESSION_INACTIVITY_TIMEOUT_KEY,
                String.valueOf(DEFAULT_INACTIVE_MINUTES));
        try {
            long minutes = Long.parseLong(configuredValue.trim());
            return minutes > 0 ? minutes : DEFAULT_INACTIVE_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("[Cleanup] Timeout de inactividad inválido '{}'. Usando {} min.",
                    configuredValue, DEFAULT_INACTIVE_MINUTES);
            return DEFAULT_INACTIVE_MINUTES;
        }
    }
}
