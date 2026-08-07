package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.BookingDemand;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolveEffectiveTripOriginService implements ResolveEffectiveTripOriginUseCase {
    private final ReservationRepository reservationRepository;
    private final LocalityRepository localityRepository;
    private final TripRouteCalculatorService calculator = new TripRouteCalculatorService();

    @Override
    @Transactional(readOnly = true)
    public RouteOriginResolution resolve(LocalDate travelDate, String scheduleBlock) {
        if (travelDate == null || scheduleBlock == null || scheduleBlock.isBlank()) {
            throw new IllegalArgumentException("La fecha y el bloque horario son obligatorios.");
        }
        String normalizedSchedule = normalizeSchedule(scheduleBlock);
        List<Reservation> reservations = reservationRepository.findConfirmedActiveByTravelDate(travelDate).stream()
                .filter(reservation -> calculator.matchesManifest(
                        reservation, RouteDirection.OUTBOUND, normalizedSchedule))
                .filter(reservation -> reservation.getTotalSeats() > 0)
                .toList();
        var calculation = calculator.calculate(reservations.stream()
                .map(reservation -> new BookingDemand(reservation.getPickupLocality(), reservation.getTotalSeats()))
                .toList());
        var offsets = calculateOffsets(calculation.effectiveOrigin());
        log.info("{} Fecha={}, turno={}", calculation.message(), travelDate, normalizedSchedule);
        return new RouteOriginResolution(travelDate, normalizedSchedule, calculation.effectiveOrigin(),
                calculation.skippedLocalities(), offsets, calculation.message());
    }

    private java.util.Map<String, Integer> calculateOffsets(String effectiveOrigin) {
        if (effectiveOrigin == null) return java.util.Map.of();
        int originMinutes = localityRepository.findFirstByNameIgnoreCase(effectiveOrigin)
                .map(locality -> locality.getMinutesFromOrigin() == null ? 0 : locality.getMinutesFromOrigin())
                .orElse(0);
        var offsets = new LinkedHashMap<String, Integer>();
        TripRouteCalculatorService.NORTH_TERMINAL_CORRIDOR.forEach(locality -> localityRepository
                .findFirstByNameIgnoreCase(locality)
                .ifPresent(found -> offsets.put(locality,
                        Math.abs((found.getMinutesFromOrigin() == null ? 0 : found.getMinutesFromOrigin())
                                - originMinutes))));
        offsets.put(effectiveOrigin, 0);
        return java.util.Map.copyOf(offsets);
    }

    static String normalizeSchedule(String schedule) {
        if (schedule == null) return "";
        String value = schedule.trim().toUpperCase(Locale.ROOT);
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("H:mm", Locale.ROOT),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ROOT))) {
            try {
                return LocalTime.parse(value, formatter).format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato soportado.
            }
        }
        return value;
    }
}
