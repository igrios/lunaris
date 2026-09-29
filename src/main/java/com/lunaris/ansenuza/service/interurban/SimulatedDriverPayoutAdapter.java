package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Revisión interna exclusivamente: no realiza transferencias ni confirma pagos. */
public class SimulatedDriverPayoutAdapter implements DriverPayoutPort {
    private static final Logger log = LoggerFactory.getLogger(SimulatedDriverPayoutAdapter.class);
    @Override public List<Result> submitBatch(List<Order> orders) {
        return orders.stream().map(order -> {
            log.info("Liquidación {} lista para aprobación interna; clave={}", order.id(), order.idempotencyKey());
            return new Result(order.idempotencyKey(), Status.READY_FOR_APPROVAL, null);
        }).toList();
    }
    @Override public Result reconcile(String idempotencyKey) {
        return new Result(idempotencyKey, Status.READY_FOR_APPROVAL, null);
    }
}
