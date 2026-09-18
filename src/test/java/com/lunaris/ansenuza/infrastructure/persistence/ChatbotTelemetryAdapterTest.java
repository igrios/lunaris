package com.lunaris.ansenuza.infrastructure.persistence;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.application.telemetry.*;
import com.lunaris.ansenuza.infrastructure.config.ChatbotAnalyticsProperties;
import com.lunaris.ansenuza.infrastructure.persistence.entity.*;
import com.lunaris.ansenuza.infrastructure.persistence.repository.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

class ChatbotTelemetryAdapterTest {
    private final ChatbotAnalyticsSessionRepository sessions = mock(ChatbotAnalyticsSessionRepository.class);
    private final ChatbotAnalyticsEventRepository events = mock(ChatbotAnalyticsEventRepository.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final ChatbotAnalyticsProperties properties = new ChatbotAnalyticsProperties();
    private final Instant now = Instant.parse("2026-09-17T12:00:00Z");
    private ChatbotTelemetryAdapter adapter;

    @BeforeEach
    void setup() {
        properties.setHmacSecret("0123456789abcdef0123456789abcdef");
        properties.setEnvironment("prod");
        when(manager.getTransaction(any())).thenAnswer(inv -> new SimpleTransactionStatus());
        when(sessions.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        adapter = new ChatbotTelemetryAdapter(sessions, events, properties, publisher, manager,
                Clock.fixed(now, ZoneOffset.UTC));
        doAnswer(inv -> {
            adapter.afterCommit(inv.getArgument(0));
            return null;
        }).when(publisher).publishEvent(any(Object.class));
    }

    @Test
    void hmacMatchesKnownVectorAndNormalizesEquivalentPhones() {
        assertThat(adapter.subjectKey("3515551234"))
                .isEqualTo("7386104b1165198dded79d427171371e3aa543b4443b8831f271c47960c12b6d")
                .isEqualTo(adapter.subjectKey("+54 9 351 555-1234"));
        assertThat(adapter.subjectKey("3515559999")).isNotEqualTo(adapter.subjectKey("3515551234"));
    }

    @Test
    void missingSecretDoesNotPersistOrFailTheBot() {
        properties.setHmacSecret("");
        assertThat(adapter.begin("3515551234", "message-1", false).active()).isFalse();
        verifyNoInteractions(sessions, events);
    }

    @Test
    void simulatorAndInternalPhonesAreMarkedAsTestsInProduction() {
        adapter.begin("3515551234", "simulator", true);
        properties.setInternalPhones(List.of("+54 9 351 555-9999"));
        adapter.begin("3515559999", "internal", false);
        ArgumentCaptor<ChatbotAnalyticsSession> capture = ArgumentCaptor.forClass(ChatbotAnalyticsSession.class);
        verify(sessions, times(2)).saveAndFlush(capture.capture());
        assertThat(capture.getAllValues()).allMatch(ChatbotAnalyticsSession::isTest);
        assertThat(capture.getAllValues()).allMatch(s -> s.getId() != null && s.getSubjectKey().length() == 64);
    }

    @Test
    void startCreatesOneEpisodeAndKeepsRawPhoneAndMessageIdOutOfEvents() {
        var context = adapter.begin("3515551234", "wamid.private", false);
        assertThat(context.active()).isTrue();
        ArgumentCaptor<ChatbotAnalyticsEvent> capture = ArgumentCaptor.forClass(ChatbotAnalyticsEvent.class);
        verify(events, times(2)).saveAndFlush(capture.capture());
        assertThat(capture.getAllValues()).extracting(ChatbotAnalyticsEvent::getEventType)
                .containsExactly("SESSION_STARTED", "MESSAGE_RECEIVED");
        assertThat(capture.getAllValues()).allMatch(e -> !e.getDedupeKey().contains("wamid.private")
                && !e.getDedupeKey().contains("3515551234"));
    }

    @Test
    void repeatedInboundReturnsOriginalEpisodeEvenAfterClosure() {
        var prior = new ChatbotAnalyticsEvent();
        prior.setSessionId(UUID.randomUUID());
        when(events.findFirstByDedupeKeyOrderByOccurredAtAsc(anyString())).thenReturn(Optional.of(prior));
        assertThat(adapter.begin("3515551234", "same", false).sessionId()).isEqualTo(prior.getSessionId());
        verify(sessions, never()).saveAndFlush(any());
        verify(events, never()).saveAndFlush(any());
    }

    @Test
    void sameSignalIsIdempotentButDistinctMessagesCanReachTheSameMilestone() {
        var session = session();
        when(sessions.findForUpdate(session.getId())).thenReturn(Optional.of(session));
        Set<String> keys = new HashSet<>();
        when(events.existsBySessionIdAndDedupeKey(eq(session.getId()), anyString()))
                .thenAnswer(inv -> keys.contains(inv.getArgument(1)));
        when(events.saveAndFlush(any())).thenAnswer(inv -> {
            ChatbotAnalyticsEvent e = inv.getArgument(0);
            keys.add(e.getDedupeKey());
            return e;
        });
        var first = new ChatbotInteraction(adapter, session.getId(), "message-1");
        first.emit(PRICE_REQUESTED, "MAIN_MENU");
        first.emit(PRICE_REQUESTED, "MAIN_MENU");
        new ChatbotInteraction(adapter, session.getId(), "message-2").emit(PRICE_REQUESTED, "MAIN_MENU");
        verify(events, times(2)).saveAndFlush(any());
    }

    @Test
    void persistenceFailureIsCaughtOutsideIndependentTransaction() {
        var session = session();
        when(sessions.findForUpdate(session.getId())).thenReturn(Optional.of(session));
        when(events.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("private SQL detail"));
        var context = new ChatbotInteraction(adapter, session.getId(), "message");
        assertThatCode(() -> context.bookingCreated("ASK_CONFIRMATION", "ARR-COR-001", UUID.randomUUID()))
                .doesNotThrowAnyException();
        assertThat(adapter.getFailureCount()).isEqualTo(1);
        verify(manager).rollback(any());
        ArgumentCaptor<TransactionDefinition> definitions = ArgumentCaptor.forClass(TransactionDefinition.class);
        verify(manager).getTransaction(definitions.capture());
        assertThat(definitions.getValue().getPropagationBehavior()).isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        assertThat(session.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void twoLegBookingRecordsOneConversionByGroup() {
        var session = session();
        when(sessions.findForUpdate(session.getId())).thenReturn(Optional.of(session));
        when(events.existsBySessionIdAndDedupeKey(eq(session.getId()), anyString())).thenReturn(false, true);
        var context = new ChatbotInteraction(adapter, session.getId(), "message");
        UUID outbound = UUID.randomUUID();
        context.bookingCreated("ASK_CONFIRMATION", "ARR-COR-001", outbound);
        context.bookingCreated("ASK_CONFIRMATION", "ARR-COR-001", UUID.randomUUID());
        verify(events, times(1)).saveAndFlush(any());
        assertThat(session.getBookingGroupCode()).isEqualTo("ARR-COR-001");
        assertThat(session.getReservationId()).isEqualTo(outbound);
        assertThat(session.getStatus()).isEqualTo("COMPLETED");
        assertThat(session.getCompletedAt()).isEqualTo(now);
    }

    @Test
    void inactivitySeparatesInformationalVisitsFromAbandonedBookings() {
        var info = session();
        var booking = session();
        booking.setBookingStartedAt(now.minusSeconds(3600));
        when(sessions.findExpiredIds(any(), any())).thenReturn(List.of(info.getId(), booking.getId()));
        when(sessions.findForUpdate(info.getId())).thenReturn(Optional.of(info));
        when(sessions.findForUpdate(booking.getId())).thenReturn(Optional.of(booking));
        adapter.expireSessions();
        assertThat(info.getStatus()).isEqualTo("EXPIRED");
        assertThat(booking.getStatus()).isEqualTo("ABANDONED");
        assertThat(booking.getEndedAt()).isEqualTo(booking.getExpiresAt());
    }

    @Test
    void schedulerRechecksExpiryAfterAcquiringLock() {
        var session = session();
        session.setExpiresAt(now.plusSeconds(1800));
        when(sessions.findExpiredIds(any(), any())).thenReturn(List.of(session.getId()));
        when(sessions.findForUpdate(session.getId())).thenReturn(Optional.of(session));
        adapter.expireSessions();
        assertThat(session.getStatus()).isEqualTo("ACTIVE");
        verify(events, never()).saveAndFlush(any());
    }

    @Test
    void delayedSendDoesNotReopenOrChangePendingStepAfterHandoff() {
        var session = session();
        session.setCurrentStep("ASK_DATE");
        when(sessions.findForUpdate(session.getId())).thenReturn(Optional.of(session));
        var context = new ChatbotInteraction(adapter, session.getId(), "message");
        context.emit(PRICE_SENT, "ASK_MARKETING_CONFIRMATION");
        assertThat(session.getCurrentStep()).isEqualTo("ASK_DATE");
        context.emit(HUMAN_HANDOFF, "ASK_DATE", ChatbotReason.OPERATOR);
        context.emit(SUMMARY_SENT, "ASK_CONFIRMATION");
        assertThat(session.getStatus()).isEqualTo("HANDED_OFF");
        assertThat(session.getCurrentStep()).isEqualTo("ASK_DATE");
    }

    private ChatbotAnalyticsSession session() {
        var session = new ChatbotAnalyticsSession();
        session.setId(UUID.randomUUID());
        session.setStartedAt(now.minusSeconds(3600));
        session.setLastInteractionAt(now.minusSeconds(1800));
        session.setExpiresAt(now.minusSeconds(1));
        return session;
    }
}
