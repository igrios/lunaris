package com.lunaris.ansenuza.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import com.lunaris.ansenuza.application.telemetry.ChatbotEventType;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.config.ChatbotAnalyticsProperties;
import com.lunaris.ansenuza.infrastructure.persistence.repository.*;
import com.lunaris.ansenuza.infrastructure.persistence.entity.ChatbotAnalyticsSession;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.url=jdbc:h2:mem:chatbot-analytics;DB_CLOSE_DELAY=-1;NON_KEYWORDS=KEY,VALUE",
        "chatbot.analytics.enabled=true",
        "chatbot.analytics.hmac-secret=test-hmac-secret-key-32-bytes-long-for-testing!!",
        "chatbot.analytics.environment=test"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ChatbotTelemetryAdapter.class)
@EnableConfigurationProperties(ChatbotAnalyticsProperties.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatbotTelemetryTransactionTest {
    @Autowired ChatbotTelemetryAdapter telemetry;
    @Autowired ChatbotAnalyticsSessionRepository sessions;
    @Autowired ChatbotAnalyticsEventRepository events;
    @Autowired ReservationRepository reservations;
    @Autowired PassengerRepository passengers;
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManager entityManager;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("CHATBOT_ANALYTICS_TEST_PG_URL");
        if (url != null) {
            registry.add("spring.datasource.url", () -> url);
            registry.add("spring.datasource.username", () -> System.getenv("CHATBOT_ANALYTICS_TEST_PG_USER"));
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        }
    }

    @BeforeAll
    void installRealMigrationWhenTestingPostgres() {
        if (System.getenv("CHATBOT_ANALYTICS_TEST_PG_URL") != null) {
            jdbc.execute("DROP TABLE chatbot_analytics_events");
            jdbc.execute("DROP TABLE chatbot_analytics_sessions");
            new ResourceDatabasePopulator(new ClassPathResource("db/migration/V127__chatbot_analytics.sql"))
                    .execute(jdbc.getDataSource());
        }
    }

    @BeforeEach
    void clean() {
        events.deleteAllInBatch();
        sessions.deleteAllInBatch();
        reservations.deleteAllInBatch();
        passengers.deleteAllInBatch();
    }

    @Test
    void conversionAppearsOnlyAfterBusinessCommitAndRoundTripCountsOnce() {
        var interaction = telemetry.begin("3515551234", "roundtrip", false);
        new TransactionTemplate(manager).executeWithoutResult(tx -> {
            Reservation outbound = saveReservation("GROUP-001");
            saveReservation("GROUP-001");
            interaction.bookingCreated("ASK_CONFIRMATION", "GROUP-001", outbound.getId());
            assertThat(conversions()).isZero();
        });
        assertThat(reservations.count()).isEqualTo(2);
        assertThat(conversions()).isEqualTo(1);
        assertThat(sessions.findById(interaction.sessionId()).orElseThrow().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void rollbackDoesNotPublishConversion() {
        var interaction = telemetry.begin("3515551234", "rollback", false);
        new TransactionTemplate(manager).executeWithoutResult(tx -> {
            Reservation reservation = saveReservation("GROUP-ROLLBACK");
            interaction.bookingCreated("ASK_CONFIRMATION", "GROUP-ROLLBACK", reservation.getId());
            tx.setRollbackOnly();
        });
        assertThat(reservations.count()).isZero();
        assertThat(conversions()).isZero();
        assertThat(sessions.findById(interaction.sessionId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void analyticsConstraintFailureCannotRollbackARealReservation() {
        var interaction = telemetry.begin("3515551234", "analytics-failure", false);
        long failuresBefore = telemetry.getFailureCount();
        UUID id = new TransactionTemplate(manager).execute(tx -> {
            Reservation saved = saveReservation("GROUP-SAFE");
            // Force a real database constraint failure in the analytics transaction.
            interaction.bookingCreated("X".repeat(65), "GROUP-SAFE", saved.getId());
            return saved.getId();
        });
        assertThat(reservations.findById(id)).isPresent();
        assertThat(conversions()).isZero();
        assertThat(telemetry.getFailureCount()).isEqualTo(failuresBefore + 1);
        assertThat(sessions.findById(interaction.sessionId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void committedEventsWithoutBusinessTransactionPersistImmediatelyAndDeduplicate() {
        var interaction = telemetry.begin("3515551234", "price", false);
        interaction.emit(ChatbotEventType.PRICE_REQUESTED, "MAIN_MENU");
        interaction.emit(ChatbotEventType.PRICE_REQUESTED, "MAIN_MENU");
        assertThat(events.findAll()).filteredOn(e -> "PRICE_REQUESTED".equals(e.getEventType())).hasSize(1);
        assertThat(telemetry.begin("3515551234", "price", false).sessionId()).isEqualTo(interaction.sessionId());
        assertThat(sessions.count()).isEqualTo(1);
    }

    @Test
    void concurrentDuplicateSignalsUseTheSameSessionLock() throws Exception {
        long failuresBefore = telemetry.getFailureCount();
        var interaction = telemetry.begin("3515551234", "concurrent", false);
        assertThat(interaction.active()).isTrue();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks = java.util.stream.IntStream.range(0, 6).mapToObj(i -> executor.submit(() ->
                    interaction.emit(ChatbotEventType.PRICE_SENT, "ASK_MARKETING_CONFIRMATION"))).toList();
            for (var task : tasks) task.get(30, TimeUnit.SECONDS);
        }
        assertThat(events.findAll()).filteredOn(e -> "PRICE_SENT".equals(e.getEventType())).hasSize(1);
        assertThat(telemetry.getFailureCount()).isEqualTo(failuresBefore);
    }

    @Test
    void expiredBookingClosesBeforeNextEpisodeAndPreservesItsPendingStep() {
        var first = telemetry.begin("3515551234", "first", false);
        assertThat(first.active()).isTrue();
        new TransactionTemplate(manager).executeWithoutResult(tx -> {
            var session = sessions.findForUpdate(first.sessionId()).orElseThrow();
            Instant past = Instant.now().minusSeconds(3600);
            session.setBookingStartedAt(past.minusSeconds(60));
            session.setCurrentStep("ASK_DATE");
            session.setStartedAt(past.minusSeconds(60));
            session.setLastInteractionAt(past);
            session.setExpiresAt(past.plusSeconds(1800));
        });
        var second = telemetry.begin("3515551234", "second", false);
        assertThat(second.sessionId()).isNotEqualTo(first.sessionId());
        var abandoned = sessions.findById(first.sessionId()).orElseThrow();
        assertThat(abandoned.getStatus()).isEqualTo("ABANDONED");
        assertThat(abandoned.getCurrentStep()).isEqualTo("ASK_DATE");
        assertThat(events.findAll()).filteredOn(e -> "SESSION_EXPIRED".equals(e.getEventType())).hasSize(1);
    }

    @Test
    void assignedUuidPersistsAndLoadedSessionCanBeUpdatedWithoutAnotherInsert() {
        UUID id = UUID.randomUUID();
        new TransactionTemplate(manager).executeWithoutResult(tx -> {
            var session = new ChatbotAnalyticsSession();
            session.setId(id);
            session.setSubjectKey("test-subject");
            session.setSubjectKeyVersion((short) 1);
            session.setEnvironment("test");
            session.setSource("WHATSAPP");
            session.setStartedAt(Instant.now());
            session.setLastInteractionAt(Instant.now());
            session.setExpiresAt(Instant.now().plusSeconds(1800));
            assertThat(session.isNew()).isTrue();
            assertThat(sessions.saveAndFlush(session).getId()).isEqualTo(id);
            assertThat(session.isNew()).isFalse();
            entityManager.clear();
            var loaded = sessions.findForUpdate(id).orElseThrow();
            assertThat(loaded.isNew()).isFalse();
            loaded.setCurrentStep("ASK_DATE");
            sessions.saveAndFlush(loaded);
            entityManager.clear();
            assertThat(sessions.findById(id).orElseThrow().getCurrentStep()).isEqualTo("ASK_DATE");
        });
        assertThat(sessions.count()).isEqualTo(1);
    }

    @Test
    void subsequentMessageUpdatesExistingSessionAndKeepsItsEvents() {
        long failuresBefore = telemetry.getFailureCount();
        var first = telemetry.begin("3515551234", "activity-first", false);
        assertThat(first.active()).isTrue();
        first.emit(ChatbotEventType.STEP_CHANGED, "ASK_DATE");
        var second = telemetry.begin("3515551234", "activity-second", false);
        assertThat(second.sessionId()).isEqualTo(first.sessionId());
        assertThat(sessions.findById(first.sessionId()).orElseThrow().getCurrentStep()).isEqualTo("ASK_DATE");
        assertThat(events.findAll()).filteredOn(e -> "MESSAGE_RECEIVED".equals(e.getEventType())).hasSize(2);
        assertThat(telemetry.getFailureCount()).isEqualTo(failuresBefore);
    }

    private long conversions() {
        return events.findAll().stream().filter(e -> "BOOKING_CREATED".equals(e.getEventType())).count();
    }

    private Reservation saveReservation(String group) {
        var passenger = passengers.saveAndFlush(Passenger.builder().firstName("Test").lastName("Analytics")
                .phone("543515551234").build());
        return reservations.saveAndFlush(Reservation.builder().passenger(passenger)
                .pickupLocality("Morteros").destination("Córdoba").bookingGroupCode(group)
                .status("PENDING_PAYMENT")
                .paymentVerified(false)
                .amountIsGroupTotal(false)
                .usedBalance(java.math.BigDecimal.ZERO)
                .discountAmount(java.math.BigDecimal.ZERO)
                .source(com.lunaris.ansenuza.domain.model.ReservationSource.MANUAL)
                .travelStatus(Reservation.TravelStatus.PENDING)
                .returnedPassengerCount(0)
                .passengerCount(1)
                .requiresInvoice(true)
                .build());
    }
}
