package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/** Base desechable PostgreSQL con V129 y V130, nunca producción. */
@EnabledIfEnvironmentVariable(named = "INTERURBAN_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class InterurbanPaymentPostgresTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            InterurbanConfiguration.class, CapacityPostgresTest.Transactions.class, Payments.class)
            .withPropertyValues("lunaris.interurban.enabled=true");

    @TestConfiguration(proxyBeanMethods = false)
    static class Payments {
        @Bean InterurbanPaymentRepository repository(NamedParameterJdbcTemplate jdbc) {
            return spy(new JdbcInterurbanPaymentRepository(jdbc));
        }
        @Bean PaymentQueryPort query() { return mock(PaymentQueryPort.class); }
        @Bean QrGeneratorService qr(InterurbanPaymentRepository repository) {
            return new QrGeneratorService(repository, new QrArtifactCipher(
                    Base64.getEncoder().encodeToString(MercadoPagoWebhookServiceTest.AES_KEY), "test-key"), Clock.systemUTC());
        }
        @Bean MercadoPagoWebhookService webhook(InterurbanPaymentRepository repository, PaymentQueryPort query,
                QrGeneratorService qr, PlatformTransactionManager transactions) {
            return new MercadoPagoWebhookService(new MercadoPagoSignatureValidator(MercadoPagoWebhookServiceTest.SECRET,
                    Clock.systemUTC(), Duration.ofHours(24)), query, repository, qr, transactions, "999", false, Clock.systemUTC());
        }
    }

    private record Fixture(UUID booking, List<UUID> reservations, String paymentId) {}

    private Fixture fixture(JdbcTemplate jdbc, CapacityService capacity) {
        UUID trip = UUID.randomUUID();
        UUID booking = UUID.randomUUID();
        UUID fare = UUID.randomUUID();
        // Tarifa fija e inmutable de esta base desechable, compartida entre fixtures.
        jdbc.update("""
                INSERT INTO interurban.interurban_fares(id,origin_stop,destination_stop,valid_from,fare,commission)
                VALUES (?,0,3,DATE '2026-02-01',100,10)
                ON CONFLICT (origin_stop,destination_stop,valid_from) DO NOTHING
                """, fare);
        fare = jdbc.queryForObject("SELECT id FROM interurban.interurban_fares WHERE origin_stop=0 AND destination_stop=3 "
                + "AND valid_from=DATE '2026-02-01'", UUID.class);
        jdbc.update("""
                INSERT INTO interurban.trips(id,service_date,departure_at,closes_at,direction,status)
                VALUES (?,CURRENT_DATE,CURRENT_TIMESTAMP+INTERVAL '2 days',CURRENT_TIMESTAMP+INTERVAL '1 day',1,'OPEN')
                """, trip);
        for (int i = 1; i <= 3; i++) {
            jdbc.update("INSERT INTO interurban.trip_legs(id,trip_id,corridor_leg_id) SELECT ?,?,id "
                    + "FROM interurban.corridor_legs WHERE ordinal=?", UUID.randomUUID(), trip, i);
        }
        var passenger = new PassengerHold(booking, fare, "Prueba", "000", "Origen", "Destino",
                new BigDecimal("100"), new BigDecimal("10"));
        var reservations = capacity.hold(trip, new Route(0, 3), List.of(passenger, passenger), Instant.now().plusSeconds(600));
        return new Fixture(booking, reservations, Long.toUnsignedString(UUID.randomUUID().getMostSignificantBits()));
    }

    private InterurbanPaymentRepository.Inbox receive(MercadoPagoWebhookService service, JdbcTemplate jdbc,
            Fixture fixture) throws Exception {
        String request = UUID.randomUUID().toString();
        service.receive(fixture.paymentId(), request, MercadoPagoWebhookServiceTest.signature(fixture.paymentId(), request,
                Long.toString(Instant.now().toEpochMilli())), new MercadoPagoWebhookService.Notification(
                "payment", "payment.updated", new MercadoPagoWebhookService.Notification.Data(fixture.paymentId())));
        UUID id = jdbc.queryForObject("SELECT id FROM interurban.payment_inbox WHERE payment_id=? ORDER BY received_at DESC LIMIT 1",
                UUID.class, fixture.paymentId());
        return new InterurbanPaymentRepository.Inbox(id, fixture.paymentId());
    }

    @Test void concurrentEventsGenerateOnlyOneQrPerPassengerAndOnePayment() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var fixture = fixture(jdbc, context.getBean(CapacityService.class));
            var service = context.getBean(MercadoPagoWebhookService.class);
            var query = context.getBean(PaymentQueryPort.class);
            when(query.getPayment(fixture.paymentId())).thenReturn(new PaymentQueryPort.Payment(fixture.paymentId(),
                    "approved", "INTERURBAN:" + fixture.booking(), new BigDecimal("200"), "ARS", "999", false, BigDecimal.ZERO));
            var first = receive(service, jdbc, fixture);
            var second = receive(service, jdbc, fixture);
            var start = new CountDownLatch(1);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var one = executor.submit(() -> { start.await(); service.process(first); return true; });
                var two = executor.submit(() -> { start.await(); service.process(second); return true; });
                start.countDown();
                assertTrue(one.get(15, TimeUnit.SECONDS));
                assertTrue(two.get(15, TimeUnit.SECONDS));
            }
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payments WHERE payment_id=?", Integer.class, fixture.paymentId()));
            String priorToken = null;
            for (UUID id : fixture.reservations()) {
                assertEquals("PAID", jdbc.queryForObject("SELECT status FROM interurban.reservations WHERE id=?", String.class, id));
                byte[] encrypted = jdbc.queryForObject("SELECT encrypted_png FROM interurban.qr_artifacts WHERE reservation_id=?", byte[].class, id);
                String token = MercadoPagoWebhookServiceTest.decodeToken(id, encrypted);
                assertEquals(32, Base64.getUrlDecoder().decode(token).length);
                assertNotEquals(priorToken, token);
                priorToken = token;
                assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.outbox WHERE event_key=?", Integer.class, "qr-ready:" + id));
            }
        });
    }

    @Test void qrFailureRollsBackPaymentReservationsAndArtifactsButKeepsInbox() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var fixture = fixture(jdbc, context.getBean(CapacityService.class));
            var service = context.getBean(MercadoPagoWebhookService.class);
            when(context.getBean(PaymentQueryPort.class).getPayment(fixture.paymentId())).thenReturn(new PaymentQueryPort.Payment(
                    fixture.paymentId(), "approved", "INTERURBAN:" + fixture.booking(), new BigDecimal("200"), "ARS", "999", false, BigDecimal.ZERO));
            var calls = new AtomicInteger();
            doAnswer(invocation -> {
                if (calls.incrementAndGet() == 2) throw new RuntimeException("fixture");
                return invocation.callRealMethod();
            }).when(context.getBean(InterurbanPaymentRepository.class)).saveQr(any(), any(), any(), any(), any());
            var inbox = receive(service, jdbc, fixture);
            assertThrows(InterurbanPaymentException.class, () -> service.process(inbox));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.payments WHERE payment_id=?", Integer.class, fixture.paymentId()));
            for (UUID id : fixture.reservations()) {
                assertEquals("HELD", jdbc.queryForObject("SELECT status FROM interurban.reservations WHERE id=?", String.class, id));
                assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.qr_tokens WHERE reservation_id=?", Integer.class, id));
                assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.qr_artifacts WHERE reservation_id=?", Integer.class, id));
            }
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payment_inbox WHERE id=? AND processed_at IS NULL", Integer.class, inbox.id()));
        });
    }
}
