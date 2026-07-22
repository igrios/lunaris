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
        List<Reservation> group = reservationRepository.findByReservationCodeStartingWith(groupCode(selected.getReservationCode()));
        if (group.isEmpty()) {
            group = List.of(selected);
        }
        if (group.stream().allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))) {
            return selected;
        }

        String promotionCode = selected.getPromotionCode();
        if (promotionCode != null && !promotionCode.isBlank()) {
            promotionService.consume(promotionCode);
        }

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
        if (reservationCode == null) {
            return "";
        }
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
