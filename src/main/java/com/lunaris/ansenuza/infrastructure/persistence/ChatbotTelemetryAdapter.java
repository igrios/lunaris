package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.application.telemetry.*;
import com.lunaris.ansenuza.infrastructure.config.ChatbotAnalyticsProperties;
import com.lunaris.ansenuza.infrastructure.persistence.entity.*;
import com.lunaris.ansenuza.infrastructure.persistence.repository.*;
import com.lunaris.ansenuza.shared.PhoneUtils;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/** Best effort analytics: exceptions are caught OUTSIDE the independent transaction. */
@Component
@Slf4j
public class ChatbotTelemetryAdapter implements ChatbotTelemetryPort {
    private static final String SOURCE = "WHATSAPP";
    private final ChatbotAnalyticsSessionRepository sessions;
    private final ChatbotAnalyticsEventRepository events;
    private final ChatbotAnalyticsProperties properties;
    private final ApplicationEventPublisher publisher;
    private final TransactionTemplate write;
    private final TransactionTemplate read;
    private final Clock clock;
    private final AtomicLong failures = new AtomicLong();

    @Autowired
    public ChatbotTelemetryAdapter(ChatbotAnalyticsSessionRepository sessions,
            ChatbotAnalyticsEventRepository events, ChatbotAnalyticsProperties properties,
            ApplicationEventPublisher publisher, PlatformTransactionManager transactionManager) {
        this(sessions, events, properties, publisher, transactionManager, Clock.systemUTC());
    }

    ChatbotTelemetryAdapter(ChatbotAnalyticsSessionRepository sessions,
            ChatbotAnalyticsEventRepository events, ChatbotAnalyticsProperties properties,
            ApplicationEventPublisher publisher, PlatformTransactionManager transactionManager, Clock clock) {
        this.sessions = sessions;
        this.events = events;
        this.properties = properties;
        this.publisher = publisher;
        this.clock = clock;
        this.write = template(transactionManager, false);
        this.read = template(transactionManager, true);
    }

    private TransactionTemplate template(PlatformTransactionManager manager, boolean readOnly) {
        TransactionTemplate result = new TransactionTemplate(manager);
        result.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        result.setReadOnly(readOnly);
        // H2 en los tests puede tardar varios segundos en adquirir el lock
        // durante el arranque; no convertir ese retraso transitorio en NONE.
        result.setTimeout(30);
        return result;
    }

    @PostConstruct
    void checkConfiguration() {
        if (properties.isEnabled() && !enabled()) {
            log.warn("Chatbot analytics deshabilitado: configurar secreto HMAC de al menos 32 bytes, ambiente y duraciones válidas.");
        }
    }

    private boolean enabled() {
        return properties.isEnabled() && properties.getHmacSecret() != null
                && properties.getHmacSecret().getBytes(StandardCharsets.UTF_8).length >= 32
                && properties.getEnvironment() != null && !properties.getEnvironment().isBlank()
                && properties.getEnvironment().length() <= 20 && properties.getSubjectKeyVersion() > 0
                && properties.getInactivityTimeout() != null && properties.getInactivityTimeout().isPositive()
                && properties.getRetention() != null && properties.getRetention().isPositive();
    }

    String subjectKey(String phone) {
        return hmac(PhoneUtils.normalizeArgentinePhone(phone));
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular la identidad analítica");
        }
    }

    @Override
    public ChatbotInteraction begin(String phone, String messageId, boolean test) {
        if (!enabled()) return ChatbotInteraction.NONE;
        try {
            String subject = subjectKey(phone);
            String key = hmac(properties.getEnvironment() + ":" + properties.getSubjectKeyVersion()
                    + ":" + subject + ":" + (messageId == null || messageId.isBlank() ? UUID.randomUUID() : messageId));
            boolean internal = test || !"prod".equals(properties.getEnvironment())
                    || properties.getInternalPhones().stream().filter(p -> p != null && !p.isBlank())
                            .anyMatch(p -> subjectKey(p).equals(subject));
            // A concurrent first message can win the partial unique index; retry in a NEW transaction.
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    UUID id = write.execute(tx -> beginLocked(subject, key, internal));
                    return id == null ? ChatbotInteraction.NONE : new ChatbotInteraction(this, id, key);
                } catch (DataIntegrityViolationException collision) {
                    if (attempt == 1) throw collision;
                }
            }
        } catch (RuntimeException exception) {
            failed(exception);
        }
        return ChatbotInteraction.NONE;
    }

    private UUID beginLocked(String subject, String key, boolean test) {
        var previous = events.findFirstByDedupeKeyOrderByOccurredAtAsc(key);
        if (previous.isPresent()) return previous.get().getSessionId();
        Instant now = clock.instant();
        var session = active(subject).orElse(null);
        if (session != null && !session.getExpiresAt().isAfter(now)) {
            expireLocked(session);
            sessions.flush(); // Release the partial-index slot before inserting the next episode.
            session = null;
        }
        if (session != null && events.existsBySessionIdAndDedupeKey(session.getId(), key)) return session.getId();
        boolean created = session == null;
        if (created) {
            session = new ChatbotAnalyticsSession();
            session.setId(UUID.randomUUID());
            session.setSubjectKey(subject);
            session.setSubjectKeyVersion(properties.getSubjectKeyVersion());
            session.setEnvironment(properties.getEnvironment());
            session.setSource(SOURCE);
            session.setStartedAt(now);
            session.setLastMilestone(ChatbotEventType.SESSION_STARTED.name());
        }
        session.setTest(session.isTest() || test);
        session.setLastInteractionAt(now.isBefore(session.getLastInteractionAt() == null ? now : session.getLastInteractionAt())
                ? session.getLastInteractionAt() : now);
        session.setExpiresAt(session.getLastInteractionAt().plus(properties.getInactivityTimeout()));
        session = sessions.saveAndFlush(session);
        if (created) insert(session.getId(), ChatbotEventType.SESSION_STARTED, now, null, null, "start:" + session.getId());
        // The lookup is repeated after the row lock for concurrent duplicate messages.
        if (!events.existsBySessionIdAndDedupeKey(session.getId(), key)) {
            insert(session.getId(), ChatbotEventType.MESSAGE_RECEIVED, now, null, null, key);
        }
        return session.getId();
    }

    private java.util.Optional<ChatbotAnalyticsSession> active(String subject) {
        return sessions.findFirstByEnvironmentAndSourceAndSubjectKeyVersionAndSubjectKeyAndStatus(
                properties.getEnvironment(), SOURCE, properties.getSubjectKeyVersion(), subject, "ACTIVE");
    }

    @Override
    public void record(ChatbotSignal signal) {
        if (!enabled() || signal == null || signal.sessionId() == null) return;
        try {
            publisher.publishEvent(new PendingSignal(signal, clock.instant()));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public record PendingSignal(ChatbotSignal signal, Instant occurredAt) {}

    /** Works immediately without a transaction and after commit when business owns one. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void afterCommit(PendingSignal pending) {
        try {
            write.executeWithoutResult(tx -> recordLocked(pending));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    private void recordLocked(PendingSignal pending) {
        ChatbotSignal signal = pending.signal();
        var session = sessions.findForUpdate(signal.sessionId()).orElse(null);
        if (session == null) return; // Retention may have removed an old, delayed callback.
        String key = hmac(signal.type() == ChatbotEventType.BOOKING_CREATED
                ? "booking:" + signal.bookingGroupCode()
                : signal.interactionKey() + ":" + signal.type() + ":" + signal.step() + ":" + signal.reason());
        if (events.existsBySessionIdAndDedupeKey(session.getId(), key)) return;
        if (signal.type() == ChatbotEventType.BOOKING_CREATED
                && (signal.reservationId() == null || signal.bookingGroupCode() == null || signal.bookingGroupCode().isBlank())) {
            throw new IllegalArgumentException("Conversión sin referencia comercial");
        }
        insert(session.getId(), signal.type(), pending.occurredAt(), signal.step(), signal.reason(), key);
        if (signal.type() == ChatbotEventType.DRIVER_IDENTIFIED) session.setAudience("DRIVER");
        if (signal.type() == ChatbotEventType.PASSENGER_IDENTIFIED) session.setAudience("PASSENGER");
        if (!"ACTIVE".equals(session.getStatus())
                && (signal.type() != ChatbotEventType.BOOKING_CREATED || "COMPLETED".equals(session.getStatus()))) return;
        if (signal.type() == ChatbotEventType.STEP_CHANGED && signal.step() != null) session.setCurrentStep(signal.step());
        switch (signal.type()) {
            case BOOKING_STARTED -> {
                if (session.getBookingStartedAt() == null) session.setBookingStartedAt(pending.occurredAt());
            }
            case BOOKING_CREATED -> {
                session.setBookingGroupCode(signal.bookingGroupCode());
                session.setReservationId(signal.reservationId());
                session.setCompletedAt(pending.occurredAt());
                close(session, "COMPLETED", pending.occurredAt(), null);
            }
            case BOOKING_DECLINED -> close(session, "DECLINED", pending.occurredAt(), signal.reason());
            case HUMAN_HANDOFF -> close(session, "HANDED_OFF", pending.occurredAt(), signal.reason());
            case WAITLISTED -> close(session, "WAITLISTED", pending.occurredAt(), signal.reason());
            default -> { }
        }
        switch (signal.type()) {
            case BOOKING_STARTED, PRICE_REQUESTED, PRICE_SENT, ROUTE_SELECTED, DATE_SELECTED,
                    PASSENGER_DATA_COMPLETED, SUMMARY_SENT, BOOKING_CREATED -> session.setLastMilestone(signal.type().name());
            default -> { }
        }
    }

    private void insert(UUID sessionId, ChatbotEventType type, Instant at, String step, ChatbotReason reason, String key) {
        var event = new ChatbotAnalyticsEvent();
        event.setId(UUID.randomUUID());
        event.setSessionId(sessionId);
        event.setEventType(type.name());
        event.setOccurredAt(at);
        event.setStep(step);
        event.setReasonCode(reason == null ? null : reason.name());
        event.setDedupeKey(key);
        events.saveAndFlush(event);
    }

    private void close(ChatbotAnalyticsSession session, String status, Instant at, ChatbotReason reason) {
        session.setStatus(status);
        session.setEndedAt(at);
        session.setEndReason(reason == null ? null : reason.name());
    }

    private void expireLocked(ChatbotAnalyticsSession session) {
        insert(session.getId(), ChatbotEventType.SESSION_EXPIRED, session.getExpiresAt(),
                session.getCurrentStep(), ChatbotReason.INACTIVITY, "expire:" + session.getId());
        close(session, session.getBookingStartedAt() == null ? "EXPIRED" : "ABANDONED",
                session.getExpiresAt(), ChatbotReason.INACTIVITY);
    }

    @Override
    public void handoff(String phone) {
        if (!enabled()) return;
        try {
            String subject = subjectKey(phone);
            UUID id = write.execute(tx -> active(subject).map(ChatbotAnalyticsSession::getId).orElse(null));
            if (id != null) record(new ChatbotSignal(id, "operator:" + id,
                    ChatbotEventType.HUMAN_HANDOFF, null, ChatbotReason.OPERATOR, null, null));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    @Scheduled(fixedDelayString = "${chatbot.analytics.cleanup-interval-ms:60000}")
    public void expireSessions() {
        if (!enabled()) return;
        try {
            List<UUID> ids = read.execute(tx -> sessions.findExpiredIds(clock.instant(), PageRequest.of(0, 200)));
            if (ids != null) for (UUID id : ids) {
                write.executeWithoutResult(tx -> sessions.findForUpdate(id).ifPresent(session -> {
                    if ("ACTIVE".equals(session.getStatus()) && !session.getExpiresAt().isAfter(clock.instant())) expireLocked(session);
                }));
            }
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    @Scheduled(fixedDelayString = "${chatbot.analytics.retention-interval-ms:86400000}")
    public void purgeExpiredRetention() {
        if (!enabled()) return;
        try {
            List<UUID> ids = read.execute(tx -> sessions.findRetainedIds(
                    clock.instant().minus(properties.getRetention()), PageRequest.of(0, 500)));
            if (ids != null) for (UUID id : ids) write.executeWithoutResult(tx -> {
                sessions.findForUpdate(id).ifPresent(session -> {
                    events.deleteBySessionId(id);
                    sessions.delete(session);
                });
            });
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public long getFailureCount() { return failures.get(); }

    private void failed(RuntimeException exception) {
        // Do not log messages/stack traces: JDBC errors may contain bound values.
        log.warn("Fallo de telemetría de chatbot; operación comercial preservada. tipo={}, total={}",
                exception.getClass().getSimpleName(), failures.incrementAndGet());
    }
}
