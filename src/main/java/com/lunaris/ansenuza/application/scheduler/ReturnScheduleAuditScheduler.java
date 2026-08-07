package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnScheduleAuditScheduler {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(cron = "0 0 9 * * *", zone = "America/Argentina/Cordoba")
    @Transactional
    public void auditReturnSchedules() {
        LocalDate today = ArgentinaTime.today();
        Map<String, Reservation> candidateByPhone = new LinkedHashMap<>();
        reservationRepository.findReturnScheduleAuditCandidates(today, today.plusDays(1))
                .stream()
                .filter(reservation -> reservation.getPassenger() != null)
                .filter(reservation -> reservation.getPassenger().getPhone() != null
                        && !reservation.getPassenger().getPhone().isBlank())
                .forEach(reservation -> candidateByPhone.merge(
                        reservation.getPassenger().getPhone().trim(), reservation,
                        ReturnScheduleAuditScheduler::preferReturnLeg));

        candidateByPhone.forEach((phone, reservation) -> {
            ConversationSession session = conversationSessionRepository.findByPhoneNumber(phone)
                    .orElseGet(() -> ConversationSession.builder().phoneNumber(phone).build());
            session.setCurrentStep("RETURN_WINDOW_SELECTION");
            session.setReservationCode(reservation.getReservationCode());
            conversationSessionRepository.saveAndFlush(session);
            whatsAppService.sendInteractiveButtons(
                    phone,
                    "Horario de regreso",
                    "Elegí la ventana de salida desde Córdoba:",
                    List.of(
                            Map.of("id", "1", "title", "Turno Tarde"),
                            Map.of("id", "2", "title", "Turno Vespertino")));
            log.info("[ReturnScheduleAudit] Preferencia solicitada para reserva {}.",
                    reservation.getReservationCode());
        });
    }

    private static Reservation preferReturnLeg(Reservation first, Reservation second) {
        return returnPriority(second) > returnPriority(first) ? second : first;
    }

    private static int returnPriority(Reservation reservation) {
        if (reservation.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN) return 3;
        String code = reservation.getReservationCode();
        if (code != null && (code.endsWith("-VUELTA") || code.startsWith("VTA-"))) return 2;
        return com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService
                .isCordoba(reservation.getPickupLocality()) ? 1 : 0;
    }
}
