package com.lunaris.ansenuza.service.interurban;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcInterurbanPaymentRepository implements InterurbanPaymentRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInterurbanPaymentRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void enqueue(String eventKey, String paymentId) {
        jdbc.update("""
                INSERT INTO interurban.payment_inbox(id,event_id,payment_id) VALUES (:id,:event,:payment)
                ON CONFLICT (event_id) DO NOTHING
                """, Map.of("id", UUID.randomUUID(), "event", eventKey, "payment", paymentId));
    }

    @Override public List<Inbox> pending() {
        return jdbc.query("""
                SELECT id,payment_id FROM interurban.payment_inbox WHERE processed_at IS NULL
                ORDER BY attempts,received_at,id LIMIT 50
                """, Map.of(), (rs, row) -> new Inbox(rs.getObject("id", UUID.class), rs.getString("payment_id")));
    }

    @Override public boolean lockPending(UUID inboxId) {
        return !jdbc.queryForList("""
                SELECT id FROM interurban.payment_inbox WHERE id=:id AND processed_at IS NULL FOR UPDATE
                """, Map.of("id", inboxId), UUID.class).isEmpty();
    }

    @Override public void lockPayment(String paymentId) {
        // Serializa distintos eventos del mismo pago también entre réplicas.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))",
                Map.of("key", "interurban-payment:" + paymentId), (rs, row) -> 1);
    }

    @Override public List<Reservation> lockBooking(UUID bookingId) {
        jdbc.queryForList("""
                SELECT t.id FROM interurban.trips t WHERE t.id IN
                    (SELECT trip_id FROM interurban.reservations WHERE booking_id=:booking)
                ORDER BY t.id FOR UPDATE OF t
                """, Map.of("booking", bookingId), UUID.class);
        return jdbc.query("""
                SELECT r.id,r.status,r.fare,r.hold_expires_at,t.status AS trip_status,
                       t.departure_at + INTERVAL '12 hours' AS qr_expires_at,
                       ((SELECT count(*) FROM interurban.leg_seats s WHERE s.reservation_id=r.id)
                         = abs(r.destination_stop-r.origin_stop)
                        AND (SELECT count(*) FROM interurban.leg_seats s
                             JOIN interurban.trip_legs tl ON tl.id=s.trip_leg_id
                             JOIN interurban.corridor_legs cl ON cl.id=tl.corridor_leg_id
                             WHERE s.reservation_id=r.id AND cl.ordinal > least(r.origin_stop,r.destination_stop)
                               AND cl.ordinal <= greatest(r.origin_stop,r.destination_stop))
                         = abs(r.destination_stop-r.origin_stop)) AS complete_seats
                FROM interurban.reservations r JOIN interurban.trips t ON t.id=r.trip_id
                WHERE r.booking_id=:booking ORDER BY r.id FOR UPDATE OF r
                """, Map.of("booking", bookingId), (rs, row) -> new Reservation(rs.getObject("id", UUID.class),
                rs.getString("status"), rs.getBigDecimal("fare"), rs.getTimestamp("hold_expires_at").toInstant(),
                rs.getTimestamp("qr_expires_at").toInstant(), rs.getBoolean("complete_seats"), rs.getString("trip_status")));
    }

    @Override public Optional<String> paymentStatus(String paymentId) {
        return jdbc.queryForList("SELECT status FROM interurban.payments WHERE payment_id=:id",
                Map.of("id", paymentId), String.class).stream().findFirst();
    }

    @Override public Optional<UUID> paymentBooking(String paymentId) {
        return jdbc.queryForList("SELECT booking_id FROM interurban.payments WHERE payment_id=:id",
                Map.of("id", paymentId), UUID.class).stream().findFirst();
    }

    @Override public void recordPayment(PaymentQueryPort.Payment p, UUID bookingId, String status) {
        jdbc.update("""
                INSERT INTO interurban.payments(id,payment_id,booking_id,amount,currency,status,verified_at)
                VALUES (:id,:payment,:booking,:amount,:currency,:status,CURRENT_TIMESTAMP)
                ON CONFLICT (payment_id) DO UPDATE SET status=EXCLUDED.status,verified_at=EXCLUDED.verified_at
                """, Map.of("id", UUID.randomUUID(), "payment", p.id(), "booking", bookingId,
                "amount", p.amount(), "currency", p.currency(), "status", status));
    }

    @Override public void markPaid(UUID reservationId) {
        if (jdbc.update("UPDATE interurban.reservations SET status='PAID' WHERE id=:id AND status='HELD'",
                Map.of("id", reservationId)) != 1) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }

    @Override public void revokeBooking(UUID bookingId, Instant now) {
        jdbc.update("""
                UPDATE interurban.qr_tokens SET expires_at=least(expires_at,:now)
                WHERE reservation_id IN (SELECT id FROM interurban.reservations WHERE booking_id=:booking)
                """, Map.of("now", Timestamp.from(now), "booking", bookingId));
        jdbc.update("""
                UPDATE interurban.reservations SET status='REFUNDED'
                WHERE booking_id=:booking AND status='PAID'
                """, Map.of("booking", bookingId));
    }

    @Override public void complete(UUID inboxId, String outcome) {
        jdbc.update("""
                UPDATE interurban.payment_inbox SET processed_at=CURRENT_TIMESTAMP,attempts=attempts+1,last_error=:outcome
                WHERE id=:id
                """, Map.of("id", inboxId, "outcome", outcome));
    }

    @Override public void retry(UUID inboxId) {
        jdbc.update("""
                UPDATE interurban.payment_inbox SET attempts=attempts+1,last_error='RETRY_REQUIRED'
                WHERE id=:id AND processed_at IS NULL
                """, Map.of("id", inboxId));
    }

    @Override public void review(String paymentId, String reason) {
        jdbc.update("""
                INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                VALUES (:id,:key,'PAYMENT_REVIEW_REQUIRED',:id,
                    jsonb_build_object('paymentId',CAST(:payment AS text),'reason',CAST(:reason AS text)))
                ON CONFLICT (event_key) DO NOTHING
                """, Map.of("id", UUID.randomUUID(), "key", "payment-review:" + paymentId + ":" + reason,
                "payment", paymentId, "reason", reason));
    }

    @Override public boolean lockPaidReservation(UUID reservationId) {
        return !jdbc.queryForList("SELECT id FROM interurban.reservations WHERE id=:id AND status='PAID' FOR UPDATE",
                Map.of("id", reservationId), UUID.class).isEmpty();
    }

    @Override public boolean hasQr(UUID reservationId) {
        return !jdbc.queryForList("SELECT reservation_id FROM interurban.qr_tokens WHERE reservation_id=:id",
                Map.of("id", reservationId), UUID.class).isEmpty();
    }

    @Override public void saveQr(UUID id, String hash, Instant expiresAt, byte[] png, String keyId) {
        jdbc.update("INSERT INTO interurban.qr_tokens(reservation_id,token_hash,expires_at) VALUES (:id,:hash,:expires)",
                Map.of("id", id, "hash", hash, "expires", Timestamp.from(expiresAt)));
        jdbc.update("INSERT INTO interurban.qr_artifacts(reservation_id,encrypted_png,key_id) VALUES (:id,:png,:key)",
                Map.of("id", id, "png", png, "key", keyId));
        jdbc.update("""
                INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                VALUES (:id,:key,'QR_READY',:reservation,jsonb_build_object('reservationId',CAST(:reservation AS text)))
                """, Map.of("id", UUID.randomUUID(), "key", "qr-ready:" + id, "reservation", id));
    }
}
