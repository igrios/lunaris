package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;

@EnabledIfEnvironmentVariable(named = "INTERURBAN_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class CheckInPostgresTest {
    final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            CapacityPostgresTest.Transactions.class, Operations.class);
    final Authentication auth = mock(Authentication.class);

    @TestConfiguration(proxyBeanMethods = false)
    static class Operations {
        @Bean DriverOperationsRepository repository(NamedParameterJdbcTemplate jdbc) {
            return spy(new JdbcDriverOperationsRepository(jdbc));
        }
        @Bean DriverIdentityPort identity() { return mock(DriverIdentityPort.class); }
        @Bean CheckInService checkIn(DriverOperationsRepository repository, DriverIdentityPort identity) {
            return new CheckInService(repository, identity, Clock.systemUTC(), Duration.ofHours(1), Duration.ofHours(2));
        }
        @Bean DriverRouteSheetQuery routeSheet(DriverOperationsRepository repository, DriverIdentityPort identity) {
            return new DriverRouteSheetQuery(repository, identity);
        }
    }

    record Fixture(UUID trip, UUID driver, UUID reservation, String token) {}

    Fixture fixture(JdbcTemplate jdbc) {
        UUID trip = UUID.randomUUID(), driver = UUID.randomUUID(), reservation = UUID.randomUUID(), booking = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO interurban.interurban_fares(id,origin_stop,destination_stop,valid_from,fare,commission)
                VALUES (?,0,3,DATE '2026-03-01',100,10)
                ON CONFLICT (origin_stop,destination_stop,valid_from) DO NOTHING
                """, UUID.randomUUID());
        UUID fare = jdbc.queryForObject("SELECT id FROM interurban.interurban_fares WHERE origin_stop=0 AND destination_stop=3 "
                + "AND valid_from=DATE '2026-03-01'", UUID.class);
        jdbc.update("""
                INSERT INTO interurban.trips(id,service_date,departure_at,closes_at,direction,status,driver_id,vehicle_id,assignment_source)
                VALUES (?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP-INTERVAL '1 hour',1,'ASSIGNED',?,?,'LOCAL')
                """, trip, LocalDate.now(ZoneId.of("America/Argentina/Cordoba")), driver, UUID.randomUUID());
        jdbc.update("""
                INSERT INTO interurban.reservations(id,booking_id,trip_id,origin_stop,destination_stop,passenger_name,phone,
                    pickup_address,dropoff_address,pickup_at,dropoff_at,fare_id,fare,commission,status,hold_expires_at)
                VALUES (?,?,?,0,3,'Fixture','+5493562123456','Origen','Destino',CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP+INTERVAL '1 hour',?,100,10,'PAID',CURRENT_TIMESTAMP)
                """, reservation, booking, trip, fare);
        jdbc.update("""
                INSERT INTO interurban.payments(id,payment_id,booking_id,amount,currency,status,verified_at)
                VALUES (?,?,?,100,'ARS','APPROVED',CURRENT_TIMESTAMP)
                """, UUID.randomUUID(), UUID.randomUUID().toString(), booking);
        byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update("INSERT INTO interurban.qr_tokens(reservation_id,token_hash,expires_at) VALUES (?,?,CURRENT_TIMESTAMP+INTERVAL '2 hours')",
                reservation, CheckInService.tokenHash(token));
        return new Fixture(trip, driver, reservation, token);
    }

    @Test void concurrentScansConsumeOnceAndInsertExactlyOneImmutableCredit() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var fixture = fixture(jdbc);
            when(context.getBean(DriverIdentityPort.class).requireDriver(auth)).thenReturn(fixture.driver());
            var service = context.getBean(CheckInService.class);
            var start = new CountDownLatch(1);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                java.util.concurrent.Callable<Boolean> scan = () -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    try { service.verify(fixture.token(), fixture.trip(), auth); return true; }
                    catch (QrAlreadyConsumedException expected) { return false; }
                };
                var first = executor.submit(scan);
                var second = executor.submit(scan);
                start.countDown();
                assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            }
            assertEquals("CHECKED_IN", jdbc.queryForObject("SELECT status FROM interurban.reservations WHERE id=?", String.class, fixture.reservation()));
            assertEquals(fixture.driver(), jdbc.queryForObject("SELECT checked_in_driver_id FROM interurban.reservations WHERE id=?", UUID.class, fixture.reservation()));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM interurban.driver_ledger WHERE reservation_id=? AND entry_type='EARNED'", Integer.class, fixture.reservation()));
            assertEquals(0, new BigDecimal("90").compareTo(jdbc.queryForObject("SELECT amount FROM interurban.driver_ledger WHERE reservation_id=?", BigDecimal.class, fixture.reservation())));
            assertEquals("earned:" + fixture.reservation(), jdbc.queryForObject("SELECT event_key FROM interurban.driver_ledger WHERE reservation_id=?", String.class, fixture.reservation()));
            assertNotNull(jdbc.queryForObject("SELECT consumed_at FROM interurban.qr_tokens WHERE reservation_id=?", java.sql.Timestamp.class, fixture.reservation()));
            assertThrows(org.springframework.dao.DataAccessException.class, () -> jdbc.update(
                    "UPDATE interurban.driver_ledger SET amount=0 WHERE reservation_id=?", fixture.reservation()));
        });
    }

    @Test void creditFailureRollsBackTokenConsumptionAndReservationState() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var fixture = fixture(jdbc);
            when(context.getBean(DriverIdentityPort.class).requireDriver(auth)).thenReturn(fixture.driver());
            var repository = context.getBean(DriverOperationsRepository.class);
            doThrow(new DataAccessResourceFailureException("fixture")).when(repository).earn(any(), any(), any(), any());
            assertThrows(DataAccessResourceFailureException.class, () -> context.getBean(CheckInService.class).verify(fixture.token(), fixture.trip(), auth));
            assertEquals("PAID", jdbc.queryForObject("SELECT status FROM interurban.reservations WHERE id=?", String.class, fixture.reservation()));
            assertNull(jdbc.queryForObject("SELECT consumed_at FROM interurban.qr_tokens WHERE reservation_id=?", java.sql.Timestamp.class, fixture.reservation()));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.driver_ledger WHERE reservation_id=?", Integer.class, fixture.reservation()));
            doCallRealMethod().when(repository).earn(any(), any(), any(), any());
            assertEquals("CHECKED_IN", context.getBean(CheckInService.class).verify(fixture.token(), fixture.trip(), auth).status());
        });
    }

    @Test void routeSheetFiltersOtherDriversInSql() {
        runner.run(context -> {
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var mine = fixture(jdbc);
            fixture(jdbc); // Otro chofer, misma fecha.
            when(context.getBean(DriverIdentityPort.class).requireDriver(auth)).thenReturn(mine.driver());
            var sheet = context.getBean(DriverRouteSheetQuery.class).find(LocalDate.now(ZoneId.of("America/Argentina/Cordoba")), auth);
            assertEquals(2, sheet.stops().size());
            assertTrue(sheet.stops().stream().allMatch(stop -> stop.tripId().equals(mine.trip())));
            assertEquals(DriverRouteSheetQuery.StopType.PICKUP, sheet.stops().getFirst().type());
        });
    }
}
