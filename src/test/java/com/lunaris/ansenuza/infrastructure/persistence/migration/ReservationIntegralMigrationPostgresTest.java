package com.lunaris.ansenuza.infrastructure.persistence.migration;

import com.lunaris.ansenuza.domain.model.ReservationUpdateOperation;
import com.lunaris.ansenuza.domain.model.ReservationUpdateState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;

import static org.assertj.core.api.Assertions.*;

/** PostgreSQL desechable; cada ejecución usa y elimina su propio esquema. */
@EnabledIfEnvironmentVariable(named = "RESERVATION_UPDATE_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
class ReservationIntegralMigrationPostgresTest {
    @TempDir Path migrations;

    @Test
    void flywayMigratesExistingTriggerAndJpaValidatesAndMigrationCanBeRepeated() throws Exception {
        for (String resource : new String[] {"db/v139/V118__reservation_fixture.sql",
                "db/migration/V119__financial_hardening_and_immutable_events.sql",
                "db/migration/V139__reservation_integral_update.sql"}) {
            try (var input = new ClassPathResource(resource).getInputStream()) {
                Files.copy(input, migrations.resolve(Path.of(resource).getFileName()));
            }
        }
        String url = System.getenv("RESERVATION_UPDATE_TEST_JDBC_URL");
        String user = System.getenv().getOrDefault("RESERVATION_UPDATE_TEST_DB_USER", "ignacio");
        String password = System.getenv().getOrDefault("RESERVATION_UPDATE_TEST_DB_PASSWORD", "");
        String schema = "migration139_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                            FlywayAutoConfiguration.class, HibernateJpaAutoConfiguration.class))
                    .withBean(PersistenceManagedTypes.class, () -> PersistenceManagedTypes.of(
                            ReservationSchema.class.getName(), ReservationUpdateState.class.getName(),
                            ReservationUpdateOperation.class.getName()))
                    .withPropertyValues("spring.datasource.url=" + url,
                            "spring.datasource.username=" + user, "spring.datasource.password=" + password,
                            "spring.flyway.locations=filesystem:" + migrations,
                            "spring.flyway.schemas=" + schema, "spring.flyway.default-schema=" + schema,
                            "spring.jpa.hibernate.ddl-auto=validate",
                            "spring.jpa.properties.hibernate.default_schema=" + schema)
                    .run(context -> {
                        assertThat(context).hasNotFailed().hasSingleBean(EntityManagerFactory.class);
                        assertThat(context.getBean(Flyway.class).info().current().getVersion().getVersion())
                                .isEqualTo("139");
                        connection.setSchema(schema);
                        // Se vuelve a ejecutar el SQL, además de verificar el arranque normal de Flyway.
                        connection.setAutoCommit(false);
                        ScriptUtils.executeSqlScript(connection, new ClassPathResource(
                                "db/migration/V139__reservation_integral_update.sql"));
                        connection.commit();
                        connection.setAutoCommit(true);
                        try (var statement = connection.createStatement()) {
                            try (var result = statement.executeQuery("SELECT booking_group_code FROM reservations")) {
                                assertThat(result.next()).isTrue();
                                assertThat(result.getString(1)).isEqualTo("HISTORICO");
                            }
                            // El trigger mantiene la regla: otro grupo no puede reutilizar el comprobante.
                            assertThatThrownBy(() -> statement.executeUpdate("""
                                    INSERT INTO reservations (id, booking_group_code, payment_receipt_url, status)
                                    VALUES ('00000000-0000-0000-0000-000000000002', 'OTRO',
                                            'comprobante-historico', 'CONFIRMED')
                                    """))
                                    .isInstanceOf(SQLException.class)
                                    .hasMessageContaining("El comprobante ya est");
                            statement.executeUpdate("""
                                    INSERT INTO reservations (id, booking_group_code, payment_receipt_url, status)
                                    VALUES ('00000000-0000-0000-0000-000000000003', 'HISTORICO',
                                            'comprobante-historico', 'CONFIRMED')
                                    """);
                            statement.executeUpdate("INSERT INTO reservations (id, booking_group_code) VALUES ('"
                                    + UUID.randomUUID() + "', '" + "G".repeat(64) + "')");
                            try (var result = statement.executeQuery("""
                                    SELECT COUNT(*) FROM pg_indexes WHERE schemaname = current_schema()
                                    AND tablename = 'reservations' AND indexdef LIKE '%(booking_group_code)'
                                    """)) {
                                result.next();
                                assertThat(result.getInt(1)).isEqualTo(1);
                            }
                        }
                    });
        } finally {
            try (Connection cleanup = DriverManager.getConnection(url, user, password);
                 var statement = cleanup.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
            }
        }
    }

    @Entity
    @Table(name = "reservations")
    public static class ReservationSchema {
        @Id UUID id;
        @Column(name = "booking_group_code", length = 64) String bookingGroupCode;
        @Column(name = "recognized_paid_amount", precision = 10, scale = 2) BigDecimal recognizedPaidAmount;
    }
}
