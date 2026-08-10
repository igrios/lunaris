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
    private final LocalityService localityService;

    public List<ScheduleDto> getSchedulesForWeb(String pickupLocality, LocalDate travelDate) {
        if (!isActivePickupLocality(pickupLocality)) {
            return List.of();
        }
        return pricingAndScheduleService.departureSchedules().stream()
                .map(schedule -> {
                    int availableSeats = pricingAndScheduleService
                            .availableSeats(travelDate, schedule);
                    String departureTime = schedule.substring(0, 5);
                    return new ScheduleDto(
                            departureTime, departureTime, schedule, availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate) {
        if (!isActivePickupLocality(pickupLocality)) {
            return List.of();
        }
        // El bot selecciona el bloque antes de solicitar la fecha de viaje.
        // Sin fecha todavía no corresponde evaluar ocupación.
        if (travelDate == null) {
            return pricingAndScheduleService.departureSchedules();
        }
        return pricingAndScheduleService.availableDepartureSchedules(
                pickupLocality, destination, travelDate);
    }

    private boolean isActivePickupLocality(String pickupLocality) {
        if (pickupLocality == null || pickupLocality.isBlank()) {
            return true;
        }
        return localityService.findAllWithActiveFare().stream()
                .anyMatch(locality -> locality.getName().equalsIgnoreCase(pickupLocality.trim()));
    }
}
