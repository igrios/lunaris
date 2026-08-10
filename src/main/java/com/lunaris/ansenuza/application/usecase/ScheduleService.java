package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final PricingAndScheduleService pricingAndScheduleService;

    public List<ScheduleDto> getSchedulesForWeb(LocalDate travelDate) {
        return pricingAndScheduleService.departureSchedules().stream()
                .map(schedule -> {
                    int availableSeats = pricingAndScheduleService
                            .availableSeats(travelDate, schedule);
                    String departureTime = schedule.substring(0, 5);
                    return new ScheduleDto(
                            departureTime, departureTime, availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate) {
        // El bot selecciona el bloque antes de solicitar la fecha de viaje.
        // Sin fecha todavía no corresponde evaluar ocupación.
        if (travelDate == null) {
            return pricingAndScheduleService.departureSchedules();
        }
        return pricingAndScheduleService.availableDepartureSchedules(
                pickupLocality, destination, travelDate);
    }
}
