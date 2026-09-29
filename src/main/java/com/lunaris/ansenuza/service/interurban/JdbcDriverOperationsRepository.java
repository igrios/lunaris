package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcDriverOperationsRepository implements DriverOperationsRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcDriverOperationsRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<Trip> lockTrip(UUID tripId) {
        return jdbc.query("SELECT id,driver_id,status,service_date,departure_at FROM interurban.trips WHERE id=:id FOR UPDATE",
                Map.of("id", tripId), (rs, row) -> new Trip(rs.getObject("id", UUID.class), rs.getObject("driver_id", UUID.class),
                rs.getString("status"), rs.getObject("service_date", LocalDate.class), instant(rs, "departure_at"))).stream().findFirst();
    }

    @Override public Optional<Ticket> lockTicket(String hash, UUID tripId) {
        return jdbc.query("""
                SELECT r.id,r.trip_id,r.status,r.pickup_at,r.fare,r.commission,q.expires_at,q.consumed_at,
                       EXISTS(SELECT 1 FROM interurban.payments p WHERE p.booking_id=r.booking_id
                              AND p.status='APPROVED') AS approved_payment
                FROM interurban.reservations r JOIN interurban.qr_tokens q ON q.reservation_id=r.id
                WHERE q.token_hash=:hash AND r.trip_id=:trip FOR UPDATE OF r,q
                """, Map.of("hash", hash, "trip", tripId), (rs, row) -> new Ticket(rs.getObject("id", UUID.class),
                rs.getObject("trip_id", UUID.class), rs.getString("status"), instant(rs, "expires_at"),
                instant(rs, "consumed_at"), instant(rs, "pickup_at"), rs.getBigDecimal("fare"),
                rs.getBigDecimal("commission"), rs.getBoolean("approved_payment"))).stream().findFirst();
    }

    @Override public void consume(UUID id, Instant now) {
        if (jdbc.update("""
                UPDATE interurban.qr_tokens SET consumed_at=:now
                WHERE reservation_id=:id AND consumed_at IS NULL AND expires_at>:now
                """, Map.of("id", id, "now", Timestamp.from(now))) != 1) throw new QrAlreadyConsumedException();
    }

    @Override public void markCheckedIn(UUID id, UUID driverId, Instant now) {
        if (jdbc.update("""
                UPDATE interurban.reservations SET status='CHECKED_IN',checked_in_at=:now,checked_in_driver_id=:driver
                WHERE id=:id AND status='PAID'
                """, Map.of("id", id, "driver", driverId, "now", Timestamp.from(now))) != 1) throw new InvalidTripException();
    }

    @Override public void earn(UUID reservationId, UUID driverId, BigDecimal amount, Instant now) {
        // Sin ON CONFLICT DO NOTHING: una inconsistencia debe revertir el check-in completo.
        jdbc.update("""
                INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key,created_at)
                VALUES (:id,:driver,:reservation,'EARNED',:amount,:key,:now)
                """, Map.of("id", UUID.randomUUID(), "driver", driverId, "reservation", reservationId,
                "amount", amount, "key", "earned:" + reservationId, "now", Timestamp.from(now)));
    }

    @Override public List<RoutePassenger> routeSheet(UUID driverId, LocalDate date) {
        return jdbc.query("""
                SELECT r.trip_id,r.id,r.passenger_name,r.phone,r.status,r.pickup_address,r.dropoff_address,r.pickup_at,r.dropoff_at
                FROM interurban.reservations r JOIN interurban.trips t ON t.id=r.trip_id
                WHERE t.driver_id=:driver AND t.service_date=:date AND t.status IN ('ASSIGNED','COMPLETED')
                  AND (r.status='CHECKED_IN' OR (r.status='PAID' AND EXISTS
                      (SELECT 1 FROM interurban.payments p WHERE p.booking_id=r.booking_id AND p.status='APPROVED')))
                ORDER BY r.pickup_at NULLS LAST,r.trip_id,r.id
                """, Map.of("driver", driverId, "date", date), (rs, row) -> new RoutePassenger(rs.getObject("trip_id", UUID.class),
                rs.getObject("id", UUID.class), rs.getString("passenger_name"), rs.getString("phone"), rs.getString("status"),
                rs.getString("pickup_address"), rs.getString("dropoff_address"), instant(rs, "pickup_at"), instant(rs, "dropoff_at")));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
