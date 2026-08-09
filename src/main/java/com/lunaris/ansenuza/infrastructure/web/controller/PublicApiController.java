package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final CreateReservationUseCase createReservationUseCase;
    private final SubmitDriverApplicationUseCase submitDriverApplicationUseCase;
    private final PricingAndScheduleService scheduleService;

    @GetMapping({"/schedules", "/v1/schedules"})
    public List<String> schedules(
            @RequestParam String pickupLocality,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) LocalDate travelDate,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "false") boolean roundTrip,
            @RequestParam(defaultValue = "false") boolean openReturn) {
        LocalDate requestedDate = travelDate != null ? travelDate : date;
        LocalDate effectiveDate = requestedDate != null
                ? requestedDate
                : com.lunaris.ansenuza.shared.ArgentinaTime.today();
        // roundTrip/openReturn no agregan horarios de regreso: sólo se consulta la ida.
        return scheduleService.availableDepartureSchedules(
                pickupLocality, destination, effectiveDate);
    }

    @PostMapping({"/reservations", "/public/reservations", "/v1/reservations"})
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody CreateReservationRequest request) {
        Reservation reservation = createReservationUseCase.execute(
                request.withSource(ReservationSource.WEB));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateReservationResponse.from(reservation));
    }

    @PostMapping(value = "/drivers/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> apply(
            @Valid @RequestBody DriverApplicationRequest request) {
        var application = submitDriverApplicationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", application.getId(),
                "status", application.getStatus().name()));
    }

}
