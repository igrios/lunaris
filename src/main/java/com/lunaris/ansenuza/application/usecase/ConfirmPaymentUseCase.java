package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PromotionService promotionService;

    @Transactional
    public Reservation execute(UUID reservationId) {
        Reservation selected = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        String groupCode = groupCode(selected.getReservationCode());
        List<Reservation> group = groupCode == null
                ? List.of(selected)
                : reservationRepository.findByReservationCodeStartingWith(groupCode);
        if (group.isEmpty()) group = List.of(selected);
        String promotionCode = group.stream()
                .map(Reservation::getPromotionCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null);

        if (group.stream().allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))) {
            // Repara reservas confirmadas por flujos anteriores que no consumieron la promoción.
            promotionService.consumeIfAvailable(promotionCode);
            return selected;
        }

        promotionService.consume(promotionCode);

        LocalDateTime confirmedAt = LocalDateTime.now();
        group.forEach(reservation -> {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
            reservation.setPaymentConfirmedAt(confirmedAt);
        });
        reservationRepository.saveAll(group);
        return selected;
    }

    private String groupCode(String reservationCode) {
        if (reservationCode == null || reservationCode.isBlank()) {
            return null;
        }
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
