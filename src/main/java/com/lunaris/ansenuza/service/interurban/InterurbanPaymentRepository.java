package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterurbanPaymentRepository {
    record Inbox(UUID id, String paymentId) {}
    record Reservation(UUID id, String status, BigDecimal fare, Instant holdExpiresAt, Instant qrExpiresAt,
            boolean completeSeats, String tripStatus) {}
    void enqueue(String eventKey, String paymentId);
    List<Inbox> pending();
    boolean lockPending(UUID inboxId);
    void lockPayment(String paymentId);
    List<Reservation> lockBooking(UUID bookingId);
    Optional<String> paymentStatus(String paymentId);
    Optional<UUID> paymentBooking(String paymentId);
    void recordPayment(PaymentQueryPort.Payment payment, UUID bookingId, String status);
    void markPaid(UUID reservationId);
    void revokeBooking(UUID bookingId, Instant now);
    void complete(UUID inboxId, String outcome);
    void retry(UUID inboxId);
    void review(String paymentId, String reason);
    boolean lockPaidReservation(UUID reservationId);
    boolean hasQr(UUID reservationId);
    void saveQr(UUID reservationId, String hash, Instant expiresAt, byte[] encryptedPng, String keyId);
}
