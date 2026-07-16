package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReturnScheduler {

    private static final List<Map<String, String>> RETURN_DECISION_BUTTONS = List.of(
            Map.of("id", "return_yes_ID", "title", "SÍ, VOLVER ✅"),
            Map.of("id", "return_later_ID", "title", "OTRO DÍA 📅"),
            Map.of("id", "return_no_ID", "title", "NO, CANCELAR ❌"));

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(cron = "0 0 15 * * *")
    public void askPassengersAboutTodayReturn() {
        LocalDate today = LocalDate.now();

        List<Reservation> returnReservations = new ArrayList<>(
                reservationRepository.findScheduledReturnsWithRealizedOutbound(today, TravelStatus.REALIZED));

        returnReservations.addAll(
                reservationRepository.findRealizedOutboundReservationsWithReturnDate(today, TravelStatus.REALIZED));

        Set<String> notifiedPhones = new LinkedHashSet<>();
        for (Reservation reservation : returnReservations) {
            if (reservation.getPassenger() == null || reservation.getPassenger().getPhone() == null
                    || reservation.getPassenger().getPhone().isBlank()) {
                log.warn("[DailyReturnScheduler] Reserva {} sin teléfono de pasajero; se omite.",
                        reservation.getId());
                continue;
            }

            String phone = reservation.getPassenger().getPhone().trim();
            if (!notifiedPhones.add(phone)) {
                continue;
            }

            String body = """
                    Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?
                    Elegí una opción para que podamos organizar las butacas.
                    """;

            whatsAppService.sendInteractiveButtons(phone, "Confirmación de vuelta", body, RETURN_DECISION_BUTTONS);
            log.info("[DailyReturnScheduler] Botones de vuelta enviados a {} para la fecha {}.", phone, today);
        }
    }
}
