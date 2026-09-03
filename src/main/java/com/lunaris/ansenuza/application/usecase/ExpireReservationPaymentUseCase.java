package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpireReservationPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository eventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int execute(UUID candidateId, LocalDateTime now) {
        Reservation candidate = reservationRepository.findByIdForUpdate(candidateId).orElse(null);
        if (!isExpired(candidate, now)) return 0;

        String groupCode = candidate.getBookingGroupCode();
        List<Reservation> reservations = groupCode == null || groupCode.isBlank()
                ? List.of(candidate)
                : reservationRepository.findByBookingGroupCodeForUpdate(groupCode);
        int expired = 0;
        for (Reservation reservation : reservations) {
            if (!isExpired(reservation, now)) continue;
            reservation.setStatus("EXPIRED");
            reservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
            reservationRepository.save(reservation);
            eventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId())
                    .eventType("PAYMENT_EXPIRED")
                    .description("Reserva expirada automáticamente luego de 20 minutos sin pago verificado.")
                    .triggeredBy("SYSTEM_TTL")
                    .build());
            expired++;
        }
        return expired;
    }

    private boolean isExpired(Reservation reservation, LocalDateTime now) {
        return reservation != null
                && !Boolean.TRUE.equals(reservation.getPaymentVerified())
                && reservation.getPaymentExpiresAt() != null
                && !reservation.getPaymentExpiresAt().isAfter(now)
                && ("PENDING_PAYMENT".equalsIgnoreCase(reservation.getStatus())
                    || "PENDING_VERIFICATION".equalsIgnoreCase(reservation.getStatus()));
    }
}
