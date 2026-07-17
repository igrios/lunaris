package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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

    private static final String RETURN_SCHEDULER_TIME_KEY = "return.scheduler.time";
    private static final String RETURN_MESSAGE_HEADER_KEY = "return.message.header";
    private static final String RETURN_MESSAGE_BODY_KEY = "return.message.body";
    private static final String RETURN_BUTTON_YES_TITLE_KEY = "return.button.yes.title";
    private static final String RETURN_BUTTON_LATER_TITLE_KEY = "return.button.later.title";
    private static final String RETURN_BUTTON_NO_TITLE_KEY = "return.button.no.title";

    private static final String DEFAULT_RETURN_SCHEDULER_TIME = "15:00";
    private static final String DEFAULT_RETURN_MESSAGE_HEADER = "Confirmación de vuelta";
    private static final String DEFAULT_RETURN_MESSAGE_BODY = """
            Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?
            Elegí una opción para que podamos organizar las butacas.
            """;

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final SystemConfigurationService configurationService;

    private LocalDate lastExecutionDate;

    @Scheduled(fixedDelayString = "60000")
    public void askPassengersAboutTodayReturn() {
        LocalDate today = LocalDate.now();
        LocalTime configuredTime = resolveConfiguredReturnTime();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        if (!now.equals(configuredTime)) {
            return;
        }

        if (today.equals(lastExecutionDate)) {
            return;
        }

        lastExecutionDate = today;

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

            String header = configurationService.getValue(RETURN_MESSAGE_HEADER_KEY, DEFAULT_RETURN_MESSAGE_HEADER);
            String body = configurationService.getValue(RETURN_MESSAGE_BODY_KEY, DEFAULT_RETURN_MESSAGE_BODY);

            whatsAppService.sendInteractiveButtons(phone, header, body, buildReturnDecisionButtons());
            log.info("[DailyReturnScheduler] Botones de vuelta enviados a {} para la fecha {}.", phone, today);
        }
    }

    private LocalTime resolveConfiguredReturnTime() {
        String configuredTime = configurationService.getValue(
                RETURN_SCHEDULER_TIME_KEY,
                DEFAULT_RETURN_SCHEDULER_TIME);
        try {
            return LocalTime.parse(configuredTime.trim()).withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            log.warn("[DailyReturnScheduler] Hora configurada inválida '{}'. Usando {}.",
                    configuredTime, DEFAULT_RETURN_SCHEDULER_TIME);
            return LocalTime.parse(DEFAULT_RETURN_SCHEDULER_TIME);
        }
    }

    private List<Map<String, String>> buildReturnDecisionButtons() {
        return List.of(
                Map.of(
                        "id", "return_yes_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_YES_TITLE_KEY, "SÍ, VOLVER ✅")),
                Map.of(
                        "id", "return_later_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_LATER_TITLE_KEY, "OTRO DÍA 📅")),
                Map.of(
                        "id", "return_no_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_NO_TITLE_KEY, "NO, CANCELAR ❌")));
    }
}
