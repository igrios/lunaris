package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.payment.BankTransferNotification;
import com.lunaris.ansenuza.application.payment.ProcessedTransactionLedgerPort;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentWebhookUseCase {

    private final ReservationRepository reservationRepository;
    private final MessagingPort messaging;
    private final ProcessedTransactionLedgerPort transactionLedger;

    @Transactional
    public void process(PaymentWebhookCommand command) {
        if (command == null
                || !"approved".equalsIgnoreCase(command.status())
                || command.paymentId() == null || command.paymentId().isBlank()
                || command.amount() == null || command.amount().signum() <= 0) {
            return;
        }
        BankTransferNotification notification = new BankTransferNotification(
                "MERCADOPAGO", command.paymentId(), command.paymentId(),
                command.externalReference() == null ? "UNMATCHED" : command.externalReference(),
                command.amount(), payer(command.payerIdentifier()), Instant.now());
        if (!transactionLedger.claim(notification)) {
            return;
        }

        Optional<List<Reservation>> match = findMatch(command);
        if (match.isEmpty()) {
            transactionLedger.recordOutcome("MERCADOPAGO", command.paymentId(),
                    "UNMATCHED", null, null,
                    "No existe un grupo pendiente único por monto o referencia");
            log.warn("Pago Mercado Pago {} por {} sin match único.",
                    command.paymentId(), command.amount());
            return;
        }
        List<Reservation> reservations = match.get();
        LocalDateTime confirmedAt = ArgentinaTime.now();
        reservations.forEach(item -> {
            item.setPaymentVerified(true);
            item.setStatus("CONFIRMED");
            if (item.getPaymentConfirmedAt() == null) {
                item.setPaymentConfirmedAt(confirmedAt);
            }
        });
        reservationRepository.saveAllAndFlush(reservations);
        Reservation reservation = reservations.getFirst();
        transactionLedger.recordOutcome("MERCADOPAGO", command.paymentId(),
                "AUTO_CONFIRMED", reservation.getId(), expectedAmount(reservations),
                "Transferencia acreditada automáticamente");
        if (reservation.getPassenger() != null) {
            try {
                messaging.sendText(reservation.getPassenger().getPhone(), """
                        ✅ *¡Pago acreditado! Tu reserva está confirmada.*

                        🚗 Un operador coordinará los detalles de tu retiro.
                        """);
            } catch (RuntimeException exception) {
                log.error("Pago confirmado, pero falló la notificación WhatsApp de la reserva {}.",
                        reservation.getReservationCode(), exception);
            }
        }
    }

    private Optional<List<Reservation>> findMatch(PaymentWebhookCommand command) {
        List<Reservation> pending = reservationRepository.findPendingPaymentCandidatesForUpdate();
        Map<String, List<Reservation>> groups = new LinkedHashMap<>();
        for (Reservation reservation : pending) {
            groups.computeIfAbsent(groupKey(reservation), ignored -> new java.util.ArrayList<>())
                    .add(reservation);
        }
        if (command.externalReference() != null && !command.externalReference().isBlank()) {
            Optional<List<Reservation>> referenced = groups.values().stream()
                    .filter(group -> group.stream().anyMatch(item ->
                            command.externalReference().equalsIgnoreCase(item.getReservationCode())))
                    .filter(group -> sameAmount(expectedAmount(group), command.amount()))
                    .findFirst();
            if (referenced.isPresent()) {
                return referenced;
            }
        }
        List<List<Reservation>> amountMatches = groups.values().stream()
                .filter(group -> sameAmount(expectedAmount(group), command.amount()))
                .toList();
        return amountMatches.size() == 1 ? Optional.of(amountMatches.getFirst()) : Optional.empty();
    }

    private String groupKey(Reservation reservation) {
        if (reservation.getBookingGroupCode() != null && !reservation.getBookingGroupCode().isBlank()) {
            return "GROUP:" + reservation.getBookingGroupCode();
        }
        String code = reservation.getReservationCode();
        if (code != null && (code.endsWith("-IDA") || code.endsWith("-VUELTA"))) {
            return "CODE:" + code.replaceFirst("-(IDA|VUELTA)$", "");
        }
        return "ID:" + reservation.getId();
    }

    private BigDecimal expectedAmount(List<Reservation> group) {
        return group.stream().map(item -> value(item.getAmount()).add(value(item.getExtraAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private boolean sameAmount(BigDecimal expected, BigDecimal received) {
        return expected.setScale(2, java.math.RoundingMode.HALF_UP)
                .compareTo(received.setScale(2, java.math.RoundingMode.HALF_UP)) == 0;
    }

    private String payer(String payerIdentifier) {
        return payerIdentifier == null || payerIdentifier.isBlank()
                ? "NO_INFORMADO" : payerIdentifier;
    }

    public record PaymentWebhookCommand(
            String paymentId,
            String status,
            BigDecimal amount,
            String externalReference,
            String payerIdentifier) {
    }
}
