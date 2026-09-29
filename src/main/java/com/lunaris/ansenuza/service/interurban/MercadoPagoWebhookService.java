package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class MercadoPagoWebhookService {
    public record Notification(String type, String action, Data data) {
        public record Data(String id) {}
    }

    private final MercadoPagoSignatureValidator signatures;
    private final PaymentQueryPort payments;
    private final InterurbanPaymentRepository repository;
    private final QrGeneratorService qr;
    private final TransactionTemplate transaction;
    private final String collectorId;
    private final boolean liveMode;
    private final Clock clock;

    public MercadoPagoWebhookService(MercadoPagoSignatureValidator signatures, PaymentQueryPort payments,
            InterurbanPaymentRepository repository, QrGeneratorService qr, PlatformTransactionManager txManager,
            String collectorId, boolean liveMode, Clock clock) {
        this.signatures = signatures;
        this.payments = payments;
        this.repository = repository;
        this.qr = qr;
        this.collectorId = collectorId;
        this.liveMode = liveMode;
        this.clock = clock;
        this.transaction = new TransactionTemplate(txManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Confirma recepción sólo después del commit del inbox. El cuerpo no autoriza pagos. */
    public void receive(String queryPaymentId, String requestId, String signature, Notification notification) {
        signatures.validate(queryPaymentId, requestId, signature);
        if (notification == null || !"payment".equals(notification.type()) || notification.data() == null
                || !queryPaymentId.equals(notification.data().id())
                || !("payment.updated".equals(notification.action()) || "payment.created".equals(notification.action()))) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_NOTIFICATION);
        }
        // El event-id del body no está firmado. Deduplicar mediante datos autenticados.
        String eventKey = fingerprint(queryPaymentId + ":" + requestId + ":" + signature);
        transaction.executeWithoutResult(status -> repository.enqueue(eventKey, queryPaymentId));
    }

    @Scheduled(fixedDelayString = "${lunaris.interurban.payments.poll-ms:10000}")
    public void processPending() {
        for (var inbox : repository.pending()) {
            try {
                process(inbox);
            } catch (RuntimeException e) {
                // Se conserva pendiente. No loguear respuestas API, firmas ni tokens.
                transaction.executeWithoutResult(status -> repository.retry(inbox.id()));
            }
        }
    }

    public void process(InterurbanPaymentRepository.Inbox inbox) {
        // HTTP antes de adquirir locks. Cada intento consulta nuevamente al proveedor.
        var payment = payments.getPayment(inbox.paymentId());
        transaction.executeWithoutResult(status -> reconcile(inbox, payment));
    }

    private void reconcile(InterurbanPaymentRepository.Inbox inbox, PaymentQueryPort.Payment payment) {
        if (!repository.lockPending(inbox.id())) return;
        if (payment == null || !inbox.paymentId().equals(payment.id()) || !collectorId.equals(payment.collectorId())
                || payment.liveMode() != liveMode || !"ARS".equals(payment.currency())
                || payment.amount() == null || payment.amount().signum() < 0 || payment.refundedAmount() == null) {
            review(inbox, "PAYMENT_IDENTITY_MISMATCH");
            return;
        }
        UUID bookingId = bookingId(payment.externalReference());
        if (bookingId == null) {
            review(inbox, "INVALID_REFERENCE");
            return;
        }
        repository.lockPayment(payment.id());
        if (repository.paymentBooking(payment.id()).filter(id -> !id.equals(bookingId)).isPresent()) {
            review(inbox, "PAYMENT_BOOKING_MISMATCH");
            return;
        }
        var reservations = repository.lockBooking(bookingId);
        if (reservations.isEmpty()) {
            review(inbox, "BOOKING_NOT_FOUND");
            return;
        }
        var previous = repository.paymentStatus(payment.id()).orElse("");
        if ("REVOKED".equals(previous)) {
            repository.complete(inbox.id(), "ALREADY_REVOKED");
            return;
        }
        if ("refunded".equals(payment.status()) || "charged_back".equals(payment.status())
                || payment.refundedAmount().signum() > 0) {
            if ("APPROVED".equals(previous)) {
                repository.revokeBooking(bookingId, clock.instant());
            }
            repository.recordPayment(payment, bookingId, "REVOKED");
            review(inbox, "REFUND_REQUIRES_REVIEW");
            return;
        }
        if (!"approved".equals(payment.status())) {
            repository.complete(inbox.id(), "NOT_APPROVED");
            return;
        }
        BigDecimal expected = reservations.stream().map(InterurbanPaymentRepository.Reservation::fare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (expected.compareTo(payment.amount()) != 0) {
            review(inbox, "AMOUNT_MISMATCH");
            return;
        }
        if ("APPROVED".equals(previous)) {
            repository.complete(inbox.id(), "ALREADY_APPROVED");
            return;
        }
        if (reservations.stream().anyMatch(r -> !"HELD".equals(r.status())
                || !r.holdExpiresAt().isAfter(clock.instant()) || !r.qrExpiresAt().isAfter(clock.instant())
                || !r.completeSeats() || "CANCELLED".equals(r.tripStatus()) || "COMPLETED".equals(r.tripStatus()))) {
            review(inbox, "BOOKING_NOT_PAYABLE");
            return;
        }
        repository.recordPayment(payment, bookingId, "APPROVED");
        for (var reservation : reservations) {
            repository.markPaid(reservation.id());
            qr.generate(reservation.id(), reservation.qrExpiresAt());
        }
        repository.complete(inbox.id(), "APPROVED");
    }

    private void review(InterurbanPaymentRepository.Inbox inbox, String reason) {
        repository.review(inbox.paymentId(), reason);
        repository.complete(inbox.id(), reason);
    }

    private UUID bookingId(String reference) {
        if (reference == null || !reference.startsWith("INTERURBAN:")) return null;
        String id = reference.substring("INTERURBAN:".length());
        try {
            UUID uuid = UUID.fromString(id);
            return uuid.toString().equalsIgnoreCase(id) ? uuid : null;
        } catch (IllegalArgumentException e) { return null; }
    }

    private String fingerprint(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_SIGNATURE); }
    }
}
