package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.DriverManagementService;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final ReservationRepository reservationRepository;
    private final OnboardPassengerUseCase onboardPassengerUseCase;
    private final DriverManagementService driverManagementService;

    @GetMapping({"/drivers", "/api/drivers"})
    public List<Driver> findAll() {
        return driverManagementService.findAll();
    }

    @PostMapping({"/drivers", "/api/drivers"})
    public Driver create(
            @RequestBody CreateDriverRequest request) {
        return driverManagementService.create(
                request.fullName(), request.phone(), request.ranking(), request.active());
    }

    @PostMapping("/api/driver/confirm-assistance")
    public ResponseEntity<?> confirmAssistance(@RequestBody ConfirmAssistanceRequest request) {
        String code = request != null && request.code() != null
                ? request.code().trim().toUpperCase()
                : "";

        if (code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El código de reserva es obligatorio."));
        }

        return reservationRepository.findByReservationCode(code)
                .map(reservation -> {
                    Reservation saved = onboardPassengerUseCase.updateTravelStatus(
                            reservation.getId(), TravelStatus.REALIZED);
                    return ResponseEntity.ok(Map.of(
                            "reservationId", saved.getId(),
                            "code", saved.getReservationCode(),
                            "travelStatus", saved.getTravelStatus().name()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró una reserva con el código indicado.")));
    }

    @RequestMapping(
            value = "/api/reservations/{id}/travel-status",
            method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateTravelStatus(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateTravelStatusRequest request) {
        String rawTravelStatus = request != null ? request.travelStatus() : null;
        log.info(
                "[TravelStatus API] Incoming travelStatus={} for reservationId={}",
                rawTravelStatus, id);
        if (rawTravelStatus == null || rawTravelStatus.isBlank()) {
            log.warn(
                    "[TravelStatus API] Null or blank travelStatus received. "
                            + "reservationId={}, payload={}",
                    id, request);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El estado de viaje es obligatorio."));
        }
        TravelStatus travelStatus;
        try {
            travelStatus = TravelStatus.valueOf(rawTravelStatus);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[TravelStatus API] Invalid travelStatus received. "
                            + "reservationId={}, payload={}",
                    id, request);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado de viaje inválido: " + rawTravelStatus));
        }
        Reservation saved =
                onboardPassengerUseCase.updateTravelStatus(id, travelStatus);
        return ResponseEntity.ok(Map.of(
                "reservationId", saved.getId(),
                "travelStatus", saved.getTravelStatus().name()));
    }

    public record ConfirmAssistanceRequest(String code) {
    }

    public record UpdateTravelStatusRequest(String travelStatus) {
    }

    public record CreateDriverRequest(
            @JsonAlias("name") String fullName,
            String phone,
            Integer ranking,
            Boolean active) {
    }
}
