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

@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnScheduleAuditScheduler {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(cron = "0 0 9 * * *", zone = "America/Argentina/Cordoba")
    public void auditReturnSchedules() {
        LocalDate today = ArgentinaTime.today();
        Map<String, Reservation> candidateByPhone = new LinkedHashMap<>();
        reservationRepository.findReturnScheduleAuditCandidates(today.atStartOfDay())
                .stream()
                .filter(reservation -> reservation.getPassenger() != null)
                .filter(reservation -> reservation.getPassenger().getPhone() != null
                        && !reservation.getPassenger().getPhone().isBlank())
                .forEach(reservation -> candidateByPhone.merge(
                        reservation.getPassenger().getPhone().trim(), reservation,
                        ReturnScheduleAuditScheduler::preferReturnLeg));

        candidateByPhone.forEach((phone, candidate) -> {
            try {
                Reservation reservation = candidate;
                if (reservation == null || alreadyAuditedToday(reservation, today)) {
                    return;
                }
                ConversationSession session = conversationSessionRepository.findByPhoneNumber(phone)
                        .orElseGet(() -> ConversationSession.builder().phoneNumber(phone).build());
                if (session.isBotPaused()) {
                    log.info("[ReturnScheduleAudit] Se omite {}: conversación bajo atención humana.",
                            phone);
                    return;
                }
                if (session.getCurrentStep() != null
                        && !"RETURN_WINDOW_SELECTION".equals(session.getCurrentStep())) {
                    log.info("[ReturnScheduleAudit] Se omite {}: conversación activa en {}.",
                            phone, session.getCurrentStep());
                    return;
                }
                if (reservation.getId() != null
                        && reservationRepository.claimReturnAudit(reservation.getId(),
                                ArgentinaTime.now(), today.atStartOfDay()) != 1) {
                    return;
                }
                session.setCurrentStep("START");
                session.setReservationCode(reservation.getReservationCode());
                conversationSessionRepository.saveAndFlush(session);
                // La marca se reclama atómicamente antes de la llamada externa para que
                // dos instancias nunca dupliquen el prompt.
                whatsAppService.sendInteractiveButtons(
                        phone,
                        "Confirmación de regreso",
                        "¿Volvés hoy?",
                        List.of(
                                Map.of("id", "return_yes_ID", "title", "Sí, vuelvo hoy"),
                                Map.of("id", "return_postpone", "title", "No vuelvo hoy")));
                log.info("[ReturnScheduleAudit] Regreso consultado para reserva {}.",
                        reservation.getReservationCode());
            } catch (Exception exception) {
                log.error("[ReturnScheduleAudit] Error procesando aviso para {}", phone, exception);
            }
        });
    }

    private boolean alreadyAuditedToday(Reservation reservation, LocalDate today) {
        return reservation.getReturnAuditSentAt() != null
                && reservation.getReturnAuditSentAt().toLocalDate().equals(today);
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
