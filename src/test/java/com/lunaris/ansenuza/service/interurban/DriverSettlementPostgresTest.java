package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Base desechable con V129–V131 aplicadas. */
@EnabledIfEnvironmentVariable(named = "INTERURBAN_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class DriverSettlementPostgresTest {
    static final LocalDate DATE = LocalDate.of(2090, 1, 1);
    static final Clock CLOCK = Clock.fixed(DATE.atTime(22, 0).atZone(DriverSettlementService.ZONE).toInstant(), ZoneOffset.UTC);
    final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            CapacityPostgresTest.Transactions.class, CheckInPostgresTest.Operations.class, Settlement.class);
    @TestConfiguration(proxyBeanMethods = false)
    static class Settlement {
        @Bean DriverSettlementService settlement(NamedParameterJdbcTemplate jdbc) { return new DriverSettlementService(jdbc, CLOCK); }
        @Bean DriverPayoutPort port() { return new SimulatedDriverPayoutAdapter(); }
        @Bean DriverPayoutReviewService review(NamedParameterJdbcTemplate jdbc, DriverPayoutPort port) {
            return new DriverPayoutReviewService(jdbc, port);
        }
    }
    CheckInPostgresTest.Fixture scan(org.springframework.context.ApplicationContext context, JdbcTemplate jdbc) {
        var helper = new CheckInPostgresTest();
        var f = helper.fixture(jdbc);
        jdbc.update("UPDATE interurban.reservations SET fare=100.37,commission=10.24 WHERE id=?", f.reservation());
        when(context.getBean(DriverIdentityPort.class).requireDriver(helper.auth)).thenReturn(f.driver());
        context.getBean(CheckInService.class).verify(f.token(), f.trip(), helper.auth);
        return f;
    }
    @Test void checkedInTicketProducesExactBreakdownAndRepeatedJobDoesNotDuplicate() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var f = scan(context, jdbc);
            var job = new DailyDriverPayoutJob(context.getBean(DriverSettlementService.class), context.getBean(DriverPayoutReviewService.class), CLOCK);
            job.run(); job.run();
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_orders WHERE driver_id=?", Integer.class, f.driver()));
            var order = jdbc.queryForMap("SELECT * FROM interurban.payout_orders WHERE driver_id=?", f.driver());
            assertEquals(new BigDecimal("90.13"), order.get("amount"));
            assertEquals(new BigDecimal("100.37"), order.get("gross_amount"));
            assertEquals(new BigDecimal("10.24"), order.get("commission_amount"));
            assertEquals(new BigDecimal("0.00"), order.get("adjustment_amount"));
            assertEquals("PENDING", order.get("status"));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_items WHERE payout_order_id=?", Integer.class, order.get("id")));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.driver_ledger WHERE driver_id=? AND entry_type='PAYOUT'", Integer.class, f.driver()));
        });
    }
    @Test void concurrentCutoffsReserveCreditOnceAndExcludeEntriesAfterCutoff() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var f = scan(context, jdbc);
            var service = context.getBean(DriverSettlementService.class);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var start = new CountDownLatch(1);
                Callable<Integer> task = () -> { start.await(); return service.settle(DATE); };
                var a = executor.submit(task); var b = executor.submit(task); start.countDown();
                a.get(15, TimeUnit.SECONDS); b.get(15, TimeUnit.SECONDS);
            }
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_orders WHERE driver_id=?", Integer.class, f.driver()));
            var late = scan(context, jdbc);
            // Asiento explícitamente posterior al corte; el ledger es inmutable.
            jdbc.update("INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key,created_at) VALUES (?,?,?,'REVERSAL',-1.11,?,?)",
                    UUID.randomUUID(), late.driver(), late.reservation(), UUID.randomUUID().toString(), java.sql.Timestamp.from(CLOCK.instant().plusSeconds(1)));
            service.settle(DATE);
            assertEquals(new BigDecimal("90.13"), jdbc.queryForObject("SELECT amount FROM interurban.payout_orders WHERE driver_id=?", BigDecimal.class, late.driver()));
        });
    }
    @Test void lateCreditWaitsForNextCutoffAndExistingOrderIsNeverExpanded() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var first = scan(context, jdbc);
            var service = context.getBean(DriverSettlementService.class);
            service.settle(DATE.minusDays(1));
            var late = new CheckInPostgresTest().fixture(jdbc);
            var earnedAt = DATE.minusDays(1).atTime(22, 0).atZone(DriverSettlementService.ZONE).toInstant().plusSeconds(1);
            jdbc.update("UPDATE interurban.reservations SET status='CHECKED_IN',checked_in_driver_id=?,checked_in_at=? WHERE id=?",
                    first.driver(), java.sql.Timestamp.from(earnedAt), late.reservation());
            jdbc.update("INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key,created_at) VALUES (?,?,?,'EARNED',90,?,?)",
                    UUID.randomUUID(), first.driver(), late.reservation(), "earned:" + late.reservation(), java.sql.Timestamp.from(earnedAt));
            service.settle(DATE.minusDays(1));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_items i JOIN interurban.driver_ledger l ON l.id=i.ledger_id WHERE l.driver_id=?", Integer.class, first.driver()));
            service.settle(DATE);
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_orders WHERE driver_id=?", Integer.class, first.driver()));
            assertEquals(new BigDecimal("90.00"), jdbc.queryForObject("SELECT amount FROM interurban.payout_orders WHERE driver_id=? AND settlement_date=?", BigDecimal.class, first.driver(), DATE));
        });
    }
    @Test void reversalsReduceNetAndNonPositiveBalancesRemainUnreserved() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var positive = scan(context, jdbc);
            var negative = scan(context, jdbc);
            for (var f : java.util.List.of(positive, negative)) {
                var adjustment = f == positive ? new BigDecimal("-20.12") : new BigDecimal("-100.00");
                jdbc.update("INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key) VALUES (?,?,?,'REVERSAL',?,?)",
                        UUID.randomUUID(), f.driver(), f.reservation(), adjustment, UUID.randomUUID().toString());
            }
            context.getBean(DriverSettlementService.class).settle(DATE);
            var order = jdbc.queryForMap("SELECT * FROM interurban.payout_orders WHERE driver_id=?", positive.driver());
            assertEquals(new BigDecimal("70.01"), order.get("amount"));
            assertEquals(new BigDecimal("-20.12"), order.get("adjustment_amount"));
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_items WHERE payout_order_id=?", Integer.class, order.get("id")));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_orders WHERE driver_id=?", Integer.class, negative.driver()));
        });
    }
    @Test void failureInOutboxRollsBackOrderAndItems() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var f = scan(context, jdbc);
            String key = "interurban:payout:" + f.driver() + ":" + DATE;
            jdbc.update("INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload) VALUES (?,?,'PAYOUT_READY',?,'{}')", UUID.randomUUID(), key, UUID.randomUUID());
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> context.getBean(DriverSettlementService.class).settle(DATE));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_orders WHERE driver_id=?", Integer.class, f.driver()));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.payout_items i JOIN interurban.driver_ledger l ON l.id=i.ledger_id WHERE l.driver_id=?", Integer.class, f.driver()));
            jdbc.update("DELETE FROM interurban.outbox WHERE event_key=?", key);
        });
    }
}
