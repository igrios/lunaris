package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PromotionService promotionService;
    private final WaitingListRepository waitingListRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final CreateInvoiceUseCase createInvoiceUseCase;

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
            createInvoiceUseCase.execute(invoiceHeader(group, selected), group);
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
        });
        reservationRepository.saveAll(group);
        completeWaitingListEntries(group, phoneNumber);
        createInvoiceUseCase.execute(invoiceHeader(group, selected), group);
        return selected;
    }

    private Reservation invoiceHeader(List<Reservation> group, Reservation selected) {
        return group.stream()
                .filter(reservation -> reservation.getReservationCode() != null
                        && reservation.getReservationCode().endsWith("-IDA"))
                .findFirst()
                .orElse(selected);
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
