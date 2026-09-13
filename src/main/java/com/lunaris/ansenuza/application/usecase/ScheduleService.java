package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.conversation.BotRoute;

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

    private static final List<String> RETURN_SCHEDULES = List.of("14:00", "17:30");

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
                    String baseTime = schedule.substring(0, 5);
                    String calculatedTime = pricingAndScheduleService
                            .calculateEstimatedPickupTime(
                                    pickupLocality, baseTime, false, travelDate);
                    String departureTime = calculatedTime.substring(0, 5);
                    return new ScheduleDto(
                            baseTime, departureTime, calculatedTime,
                            availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<ScheduleDto> getReturnSchedulesForWeb(LocalDate travelDate) {
        return RETURN_SCHEDULES.stream()
                .map(schedule -> {
                    int availableSeats = pricingAndScheduleService.availableReturnSeats(travelDate, schedule);
                    String label = pricingAndScheduleService.calculateEstimatedPickupTime(
                            null, schedule, true, travelDate);
                    return new ScheduleDto(
                            schedule, label.substring(0, 5), label,
                            availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate) {
        return getSchedulesForBot(pickupLocality, destination, travelDate, 1);
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate, int requestedSeats) {
        if (requestedSeats < 1) return List.of();
        if (BotRoute.fromCordoba(pickupLocality)) {
            return travelDate == null ? RETURN_SCHEDULES : RETURN_SCHEDULES.stream()
                    .filter(schedule -> pricingAndScheduleService.isWithinPlanningWindow(travelDate, schedule))
                    .filter(schedule -> pricingAndScheduleService.availableReturnSeats(travelDate, schedule) >= requestedSeats)
                    .toList();
        }
        if (!isActivePickupLocality(pickupLocality)) return List.of();
        // Se revalida disponibilidad al conocer fecha y cantidad definitiva.
        if (travelDate == null) return pricingAndScheduleService.departureSchedules();
        return pricingAndScheduleService.availableDepartureSchedules(
                pickupLocality, destination, travelDate, requestedSeats);
    }

    private boolean isActivePickupLocality(String pickupLocality) {
        if (pickupLocality == null || pickupLocality.isBlank()) {
            return true;
        }
        return localityService.findAllWithActiveFare().stream()
                .anyMatch(locality -> locality.getName().equalsIgnoreCase(pickupLocality.trim()));
    }
}
