package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
@RequiredArgsConstructor
public class OpenReturnCutoffScheduler {
    private final ReservationRepository reservations;
    private final ReservationEventRepository events;
    private final CapacityLockRepository locks;
    private final SimpMessagingTemplate messaging;

    @Scheduled(cron = "0 0 11 * * *", zone = "America/Argentina/Cordoba")
    @Transactional
    public void alertPendingReturns() {
        var today = ArgentinaTime.today();
        String key = today + "|DAY|RETURN";
        locks.ensureExists(key);
        if (locks.findForUpdate(key) == null) throw new IllegalStateException("No se pudo bloquear el regreso.");
        var passengers = new ArrayList<String>();
        for (Reservation reservation : reservations.findReturnCapacityCandidates(today)) {
            if (reservation.getTravelStatus() != Reservation.TravelStatus.OPEN_RETURN
                    || events.existsByReservationIdAndEventTypeAndCreatedAtGreaterThanEqual(
                            reservation.getId(), "OPEN_RETURN_CUTOFF", today.atStartOfDay())) continue;
            String label = reservation.getReservationCode() + " - "
                    + (reservation.getPassenger() == null ? "Sin pasajero" : reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName());
            events.save(ReservationEvent.builder().id(UUID.randomUUID())
                    .reservationId(reservation.getId()).eventType("OPEN_RETURN_CUTOFF")
                    .description("11:00: vuelta sin confirmar; cupo preventivo liberado.")
                    .triggeredBy("SYSTEM").build());
            passengers.add(label);
        }
        if (!passengers.isEmpty()) {
            messaging.convertAndSend("/topic/system-alerts", Map.of(
                    "action", "OPEN_RETURN_CUTOFF", "message",
                    "Vueltas sin confirmar: cupos liberados a las 11:00. " + String.join(", ", passengers)));
        }
    }
}
