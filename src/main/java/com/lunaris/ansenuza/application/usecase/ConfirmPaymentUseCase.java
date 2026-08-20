package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PromotionService promotionService;
    private final WaitingListRepository waitingListRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationEventRepository eventRepository;
    private final MessagingPort messaging;

    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions) {
        this(reservations, promotions, waitingLists, sessions, null, null);
    }

    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions,
            ReservationEventRepository events) {
        this(reservations, promotions, waitingLists, sessions, events, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions,
            ReservationEventRepository events, MessagingPort messaging) {
        this.reservationRepository = reservations;
        this.promotionService = promotions;
        this.waitingListRepository = waitingLists;
        this.conversationSessionRepository = sessions;
        this.eventRepository = events;
        this.messaging = messaging;
    }

    @Transactional
    public Reservation execute(UUID reservationId) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        String groupCode = groupCode(initial.getReservationCode());
        List<Reservation> group;
        Reservation selected;
        if (groupCode == null) {
            selected = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Reserva no encontrada: " + reservationId));
            group = List.of(selected);
        } else {
            group = reservationRepository.findReservationGroupForUpdate(groupCode);
            selected = group.stream()
                    .filter(reservation -> reservationId.equals(reservation.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "El grupo de reserva cambió durante la confirmación."));
        }
        String promotionCode = group.stream()
                .map(Reservation::getPromotionCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null);
        String phoneNumber = selected.getPassenger() != null ? selected.getPassenger().getPhone() : null;

        if (group.stream().allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))) {
            // Repara reservas confirmadas por flujos anteriores que no consumieron la promoción.
            promotionService.consumeIfAvailable(promotionCode, phoneNumber);
            completeWaitingListEntries(group, phoneNumber);
            return selected;
        }

        // La confirmación puede reintentarse desde el panel. El consumo idempotente evita que
        // una promoción ya aplicada marque la transacción como rollback-only.
        promotionService.consumeIfAvailable(promotionCode, phoneNumber);

        LocalDateTime confirmedAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        group.forEach(reservation -> {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
            reservation.setPaymentConfirmedAt(confirmedAt);
            reservation.setPaymentExpiresAt(null);
        });
        reservationRepository.saveAll(group);
        if (eventRepository != null) group.forEach(reservation -> eventRepository.save(
                ReservationEvent.builder().reservationId(reservation.getId())
                        .eventType("PAYMENT_VERIFIED")
                        .description("Pago verificado y reserva confirmada.")
                        .triggeredBy("OPERATOR").build()));
        completeWaitingListEntries(group, phoneNumber);
        notifyPaymentConfirmation(selected);
        return selected;
    }

    private void notifyPaymentConfirmation(Reservation reservation) {
        if (messaging == null || reservation.getPassenger() == null
                || reservation.getPassenger().getPhone() == null
                || reservation.getPassenger().getPhone().isBlank()) {
            return;
        }
        String passengerName = reservation.getPassenger().getFirstName();
        String destination = reservation.getDestination();
        try {
            messaging.sendText(reservation.getPassenger().getPhone(), """
                    ✅ *¡Pago Verificado con Éxito!*

                    Hola %s, te confirmamos que recibimos correctamente tu transferencia. Tu reserva para el traslado hacia *%s* ya se encuentra asentada de forma definitiva.

                    🚐 Próximamente nos comunicaremos para coordinar el horario exacto en el que el chofer pasará por tu domicilio. ¡Muchas gracias por viajar con Lunaris!
                    """.formatted(
                            passengerName == null || passengerName.isBlank() ? "Pasajero" : passengerName,
                            destination == null || destination.isBlank() ? "tu destino" : destination));
        } catch (RuntimeException exception) {
            log.warn("No se pudo emitir la notificación de confirmación para la reserva {}.",
                    reservation.getId());
        }
    }

    private void completeWaitingListEntries(List<Reservation> reservations, String phoneNumber) {
        reservations.stream()
                .map(Reservation::getWaitingListEntryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(id -> waitingListRepository.findByIdForUpdate(id).ifPresent(entry -> {
                    entry.setStatus(WaitingListEntry.CONVERTED);
                    waitingListRepository.saveAndFlush(entry);
                }));
        if (phoneNumber != null) {
            conversationSessionRepository.findByPhoneNumber(phoneNumber)
                    .filter(session -> session.getWaitingListEntryId() != null)
                    .ifPresent(conversationSessionRepository::delete);
        }
    }

    private String groupCode(String reservationCode) {
        if (reservationCode == null || reservationCode.isBlank()
                || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) {
            return null;
        }
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
