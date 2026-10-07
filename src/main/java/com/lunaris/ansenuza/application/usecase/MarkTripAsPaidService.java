package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.AgendaTripNotFoundException;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.PaymentStatus;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarkTripAsPaidService {
    private final ReservationRepository reservations;
    private final ReservationEventRepository events;

    /** Registra el pago de todo el grupo; la emisión fiscal sigue siendo una acción posterior. */
    @Transactional
    public Reservation markTripAsPaid(UUID id, String operator) {
        String groupCode = reservations.findBillingGroupCodeById(id).orElse(null);
        List<Reservation> group = groupCode == null
                ? List.of(reservations.findByIdForUpdate(id).orElseThrow(() -> new AgendaTripNotFoundException(id)))
                : reservations.findReservationGroupForUpdate(groupCode);
        if (group.isEmpty()) {
            group = List.of(reservations.findByIdForUpdate(id).orElseThrow(() -> new AgendaTripNotFoundException(id)));
        }
        Reservation selected = group.stream().filter(r -> id.equals(r.getId())).findFirst()
                .orElseThrow(() -> new AgendaTripNotFoundException(id));
        if (group.stream().anyMatch(r -> !r.isSpecialTrip())) {
            throw new DomainValidationException("Esta operación corresponde a viajes especiales de agenda.");
        }
        if (group.stream().anyMatch(r -> Set.of("CANCELLED", "CANCELED", "EXPIRED", "REJECTED")
                .contains(r.getStatus() == null ? "" : r.getStatus().toUpperCase(java.util.Locale.ROOT)))) {
            throw new DomainValidationException("No se puede registrar el pago de un viaje cancelado o vencido.");
        }
        var paidAt = ArgentinaTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        for (Reservation leg : group) {
            if (Boolean.TRUE.equals(leg.getPaymentVerified())) continue;
            leg.setPaymentVerified(true);
            leg.setPaymentStatus(PaymentStatus.PAID);
            leg.setPaymentConfirmedAt(paidAt);
            leg.setPaymentExpiresAt(null);
            leg.setStatus("CONFIRMED");
            events.save(ReservationEvent.builder().reservationId(leg.getId())
                    .eventType("PAYMENT_VERIFIED").description("Pago de viaje especial registrado; facturación habilitada.")
                    .triggeredBy(operator).build());
        }
        reservations.saveAllAndFlush(group);
        return selected;
    }
}
