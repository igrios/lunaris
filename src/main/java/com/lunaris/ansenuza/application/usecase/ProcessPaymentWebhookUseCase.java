package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public void process(PaymentWebhookCommand command) {
        if (command == null
                || !"approved".equalsIgnoreCase(command.status())
                || command.externalReference() == null
                || command.externalReference().isBlank()) {
            return;
        }
        Reservation reservation = reservationRepository
                .findByReservationCodeForUpdate(command.externalReference())
                .orElse(null);
        if (reservation == null || "CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            return;
        }
        List<Reservation> reservations = paymentGroup(reservation);
        boolean alreadyConfirmed = reservations.stream().allMatch(item ->
                Boolean.TRUE.equals(item.getPaymentVerified())
                        && "CONFIRMED".equalsIgnoreCase(item.getStatus()));
        LocalDateTime confirmedAt = ArgentinaTime.now();
        reservations.forEach(item -> {
            item.setPaymentVerified(true);
            item.setStatus("CONFIRMED");
            if (item.getPaymentConfirmedAt() == null) {
                item.setPaymentConfirmedAt(confirmedAt);
            }
        });
        reservationRepository.saveAllAndFlush(reservations);
        if (!alreadyConfirmed && reservation.getPassenger() != null) {
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

    private List<Reservation> paymentGroup(Reservation reservation) {
        String code = reservation.getReservationCode();
        if (code == null || !(code.endsWith("-IDA") || code.endsWith("-VUELTA"))) {
            return List.of(reservation);
        }
        String groupCode = code.replaceFirst("-(IDA|VUELTA)$", "");
        List<Reservation> group = reservationRepository.findReservationGroupForUpdate(groupCode);
        return group.isEmpty() ? List.of(reservation) : group;
    }

    public record PaymentWebhookCommand(String status, String externalReference) {
    }
}
