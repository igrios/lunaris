package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Las claves identifican órdenes inmutables y deben reutilizarse en cada reintento. */
public interface DriverPayoutPort {
    record Order(UUID id, UUID driverId, BigDecimal amount, String idempotencyKey) {}
    enum Status { READY_FOR_APPROVAL, UNKNOWN, PAID, FAILED }
    record Result(String idempotencyKey, Status status, String providerReference) {}
    List<Result> submitBatch(List<Order> orders);
    Result reconcile(String idempotencyKey);
}
