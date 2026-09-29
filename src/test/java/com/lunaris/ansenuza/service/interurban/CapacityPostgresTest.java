package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Requiere base DESECHABLE con V129 aplicada; usa viajes únicos por prueba. */
@EnabledIfEnvironmentVariable(named = "INTERURBAN_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class CapacityPostgresTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(InterurbanConfiguration.class, Transactions.class)
            .withPropertyValues("lunaris.interurban.enabled=true");

    @TestConfiguration(proxyBeanMethods = false)
    @EnableTransactionManagement
    static class Transactions {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource(System.getenv("INTERURBAN_TEST_JDBC_URL"),
                    System.getenv().getOrDefault("INTERURBAN_TEST_DB_USER", "ignacio"),
                    System.getenv().getOrDefault("INTERURBAN_TEST_DB_PASSWORD", ""));
        }
        @Bean NamedParameterJdbcTemplate jdbc(DataSource ds) { return new NamedParameterJdbcTemplate(ds); }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) {
            return new DataSourceTransactionManager(ds);
        }
    }

    private record Fixture(UUID trip, UUID fare) {}

    private Fixture fixture(JdbcTemplate jdbc) {
        UUID trip = UUID.randomUUID();
        // Tarifa compartida sólo dentro de esta base de pruebas, con clave natural estable.
        jdbc.update("""
                INSERT INTO interurban.interurban_fares(id,origin_stop,destination_stop,valid_from,fare,commission)
                VALUES (?,0,3,DATE '2026-01-01',100,10)
                ON CONFLICT (origin_stop,destination_stop,valid_from) DO NOTHING
                """, UUID.randomUUID());
        UUID fare = jdbc.queryForObject("SELECT id FROM interurban.interurban_fares WHERE origin_stop=0 "
                + "AND destination_stop=3 AND valid_from=DATE '2026-01-01'", UUID.class);
        jdbc.update("""
                INSERT INTO interurban.trips(id,service_date,departure_at,closes_at,direction,status)
                VALUES (?,CURRENT_DATE,CURRENT_TIMESTAMP + INTERVAL '2 days',
                    CURRENT_TIMESTAMP + INTERVAL '1 day',1,'OPEN')
                """, trip);
        for (int ordinal = 1; ordinal <= 3; ordinal++) {
            jdbc.update("INSERT INTO interurban.trip_legs(id,trip_id,corridor_leg_id) "
                    + "SELECT ?,?,id FROM interurban.corridor_legs WHERE ordinal=?",
                    UUID.randomUUID(), trip, ordinal);
        }
        return new Fixture(trip, fare);
    }

    private PassengerHold passenger(UUID booking, UUID fare) {
        return new PassengerHold(booking, fare, "Prueba", "000", "Origen", "Destino",
                new BigDecimal("100"), new BigDecimal("10"));
    }

    @Test void fiveConcurrentBuyersNeverExceedFourOnAnyLeg() {
        runner.run(context -> {
            assertNull(context.getStartupFailure());
            var service = context.getBean(CapacityService.class);
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var f = fixture(jdbc);
            var start = new CountDownLatch(1);
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = new ArrayList<java.util.concurrent.Future<Boolean>>();
                for (int i = 0; i < 5; i++) {
                    futures.add(executor.submit(() -> {
                        assertTrue(start.await(10, TimeUnit.SECONDS));
                        try {
                            service.hold(f.trip(), new Route(0, 3), List.of(passenger(UUID.randomUUID(), f.fare())),
                                    Instant.now().plusSeconds(600));
                            return true;
                        } catch (CapacityExceededException expected) { return false; }
                    }));
                }
                start.countDown();
                int accepted = 0;
                for (var future : futures) if (future.get(20, TimeUnit.SECONDS)) accepted++;
                assertEquals(4, accepted);
            }
            assertEquals(List.of(4, 4, 4), jdbc.queryForList("SELECT count(*) FROM interurban.leg_seats "
                    + "WHERE trip_id=? GROUP BY trip_leg_id", Integer.class, f.trip()));
            assertEquals(0, service.available(f.trip(), new Route(0, 3)));
        });
    }

    @Test void failingSecondReservationRollsBackFirstInsert() {
        runner.run(context -> {
            var service = context.getBean(CapacityService.class);
            var jdbc = new JdbcTemplate(context.getBean(DataSource.class));
            var f = fixture(jdbc);
            UUID booking = UUID.randomUUID();
            assertThrows(DataIntegrityViolationException.class, () -> service.hold(f.trip(), new Route(0, 3),
                    List.of(passenger(booking, f.fare()), passenger(booking, UUID.randomUUID())),
                    Instant.now().plusSeconds(600)));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.reservations WHERE trip_id=?",
                    Integer.class, f.trip()));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM interurban.leg_seats WHERE trip_id=?",
                    Integer.class, f.trip()));
        });
    }
}
