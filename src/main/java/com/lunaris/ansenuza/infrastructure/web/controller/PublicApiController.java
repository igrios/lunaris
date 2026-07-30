package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicApiController {

    private static final List<String> SCHEDULES = List.of("03:00 AM", "08:00 AM");

    private final CreateReservationUseCase createReservationUseCase;
    private final SubmitDriverApplicationUseCase submitDriverApplicationUseCase;
    private final PricingAndScheduleService scheduleService;

    @Value("${lunaris.trips.capacity:4}")
    private int tripCapacity = 8;

    @GetMapping("/schedules")
    public List<ScheduleResponse> schedules(@RequestParam(required = false) LocalDate date) {
        LocalDate effectiveDate = date != null
                ? date
                : com.lunaris.ansenuza.shared.ArgentinaTime.today();
        return SCHEDULES.stream()
                .map(schedule -> {
                    long reserved = scheduleService.countReservedSeats(effectiveDate, schedule);
                    return new ScheduleResponse(
                            effectiveDate, schedule, Math.max(0, tripCapacity - reserved), reserved < tripCapacity);
                })
                .toList();
    }

    @PostMapping({"/reservations", "/public/reservations"})
    public ResponseEntity<Map<String, Object>> createReservation(
            @RequestBody CreateReservationRequest request) {
        Reservation reservation = createReservationUseCase.execute(
                request.withSource(ReservationSource.WEB));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", reservation.getId(),
                "reservationCode", reservation.getReservationCode(),
                "status", reservation.getStatus(),
                "source", reservation.getSource()));
    }

    @PostMapping("/drivers/apply")
    public ResponseEntity<Map<String, Object>> apply(
            @Valid @RequestBody DriverApplicationRequest request) {
        var application = submitDriverApplicationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", application.getId(),
                "status", application.getStatus().name()));
    }

    public record ScheduleResponse(
            LocalDate date,
            String departureTime,
            long availableSeats,
            boolean available) {
    }
}
