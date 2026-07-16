package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository repository;
    private final ReservationRepository reservationRepository;

    @GetMapping("/drivers")
    public List<Driver> findAll() {
        return repository.findAll();
    }

    @PostMapping("/drivers")
    public Driver create(
            @RequestBody Driver driver) {

        return repository.save(driver);
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
                    reservation.setTravelStatus(TravelStatus.REALIZED);
                    Reservation saved = reservationRepository.saveAndFlush(reservation);
                    return ResponseEntity.ok(Map.of(
                            "reservationId", saved.getId(),
                            "code", saved.getReservationCode(),
                            "travelStatus", saved.getTravelStatus().name()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró una reserva con el código indicado.")));
    }

    public record ConfirmAssistanceRequest(String code) {
    }
}
