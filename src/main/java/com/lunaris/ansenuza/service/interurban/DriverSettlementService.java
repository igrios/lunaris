package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DriverSettlementService {
    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Cordoba");
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    public DriverSettlementService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }
    public static LocalDate latestCutoff(Instant now) {
        var local = now.atZone(ZONE);
        return local.toLocalTime().isBefore(LocalTime.of(22, 0)) ? local.toLocalDate().minusDays(1) : local.toLocalDate();
    }
    private record Entry(UUID id, UUID driver, BigDecimal amount, BigDecimal fare, BigDecimal fee) {}

    @Transactional
    public int settle(LocalDate date) {
        Instant cutoff = date.atTime(22, 0).atZone(ZONE).toInstant();
        if (cutoff.isAfter(clock.instant())) throw new IllegalArgumentException("El corte todavía no ocurrió");
        // Lock transaccional compartido por todas las réplicas y fechas; evita reservas dobles del saldo.
        jdbc.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(742019220)");
        var entries = jdbc.query("""
                SELECT l.id,l.driver_id,l.amount,
                       CASE WHEN l.entry_type='EARNED' THEN r.fare ELSE 0 END AS fare,
                       CASE WHEN l.entry_type='EARNED' THEN r.commission ELSE 0 END AS fee
                FROM interurban.driver_ledger l JOIN interurban.reservations r ON r.id=l.reservation_id
                WHERE l.entry_type IN ('EARNED','REVERSAL') AND l.created_at<=:cutoff
                  AND NOT EXISTS(SELECT 1 FROM interurban.payout_items i WHERE i.ledger_id=l.id)
                ORDER BY l.driver_id,l.id FOR UPDATE OF l
                """, Map.of("cutoff", Timestamp.from(cutoff)), (rs, row) -> new Entry(
                rs.getObject("id", UUID.class), rs.getObject("driver_id", UUID.class), rs.getBigDecimal("amount"),
                rs.getBigDecimal("fare"), rs.getBigDecimal("fee")));
        Map<UUID, List<Entry>> drivers = new LinkedHashMap<>();
        entries.forEach(e -> drivers.computeIfAbsent(e.driver(), key -> new ArrayList<>()).add(e));
        int created = 0;
        for (var driver : drivers.entrySet()) {
            BigDecimal net = BigDecimal.ZERO, gross = BigDecimal.ZERO, fees = BigDecimal.ZERO;
            for (var entry : driver.getValue()) {
                net = net.add(entry.amount()); gross = gross.add(entry.fare()); fees = fees.add(entry.fee());
            }
            if (net.signum() <= 0) continue;
            UUID id = UUID.randomUUID();
            String key = "interurban:payout:" + driver.getKey() + ":" + date;
            var params = new HashMap<String, Object>();
            params.put("id", id); params.put("driver", driver.getKey()); params.put("date", date);
            params.put("net", net); params.put("gross", gross); params.put("fees", fees);
            params.put("adjustments", net.subtract(gross.subtract(fees))); params.put("key", key);
            if (jdbc.update("""
                    INSERT INTO interurban.payout_orders(id,driver_id,settlement_date,amount,status,idempotency_key,
                        gross_amount,commission_amount,adjustment_amount)
                    VALUES (:id,:driver,:date,:net,'PENDING',:key,:gross,:fees,:adjustments)
                    ON CONFLICT (driver_id,settlement_date) DO NOTHING
                    """, params) == 0) continue;
            for (var entry : driver.getValue()) jdbc.update("""
                    INSERT INTO interurban.payout_items(ledger_id,payout_order_id) VALUES (:ledger,:order)
                    """, Map.of("ledger", entry.id(), "order", id));
            jdbc.update("""
                    INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                    VALUES (:event,:key,'PAYOUT_READY',:order,'{}'::jsonb)
                    """, Map.of("event", UUID.randomUUID(), "key", key, "order", id));
            created++;
        }
        return created;
    }
}
