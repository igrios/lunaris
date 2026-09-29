package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Consume únicamente eventos de revisión interna; nunca cambia la orden a PAID. */
public class DriverPayoutReviewService {
    private final NamedParameterJdbcTemplate jdbc;
    private final DriverPayoutPort port;
    public DriverPayoutReviewService(NamedParameterJdbcTemplate jdbc, DriverPayoutPort port) {
        this.jdbc = jdbc; this.port = port;
    }
    @Transactional(readOnly = true)
    public List<DriverPayoutPort.Order> pending() {
        return jdbc.getJdbcTemplate().query("""
                SELECT p.id,p.driver_id,p.amount,p.idempotency_key FROM interurban.payout_orders p
                JOIN interurban.outbox o ON o.aggregate_id=p.id AND o.event_type='PAYOUT_READY'
                WHERE o.delivered_at IS NULL AND p.status='PENDING' ORDER BY p.created_at,p.id
                """, (rs, row) -> new DriverPayoutPort.Order(rs.getObject("id", UUID.class),
                rs.getObject("driver_id", UUID.class), rs.getBigDecimal("amount"), rs.getString("idempotency_key")));
    }
    public void dispatch(List<DriverPayoutPort.Order> orders) {
        // La liquidación ya confirmó su transacción; el adaptador se invoca sin locks contables.
        for (var result : port.submitBatch(orders)) {
            if (result.status() == DriverPayoutPort.Status.READY_FOR_APPROVAL) {
                jdbc.getJdbcTemplate().update("""
                        UPDATE interurban.outbox SET delivered_at=CURRENT_TIMESTAMP,attempts=attempts+1
                        WHERE event_key=? AND event_type='PAYOUT_READY' AND delivered_at IS NULL
                        """, result.idempotencyKey());
            }
        }
    }
}
