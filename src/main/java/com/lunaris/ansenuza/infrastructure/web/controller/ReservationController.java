package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository repository;
    private final CreateReservationUseCase createReservationUseCase;
    private final ReservationService reservationService;

    @GetMapping
    public List<Reservation> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public Reservation create(@RequestBody CreateReservationRequest request) {
        return createReservationUseCase.execute(request);
    }

    @GetMapping("/date/{travelDate}")
    public List<Reservation> findByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate) {
        return repository.findByTravelDate(travelDate);
    }

    // 🤖 BAJA DESDE EL BOT / REST ASÍNCRONO
    @DeleteMapping("/api/{id}")
    public ResponseEntity<?> deleteFromBot(@PathVariable(value = "id") UUID id) {
        try {
            reservationService.cancelReservation(id, "BOT_CHAT");
            return ResponseEntity.ok().body(Map.of("status", "CANCELLED"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // 🤖 MODIFICACIÓN DESDE EL BOT / REST ASÍNCRONO
    @PutMapping("/api/{id}")
    public ResponseEntity<?> updateFromBot(@PathVariable(value = "id") UUID id, @RequestBody Reservation updatedData) {
        try {
            Reservation result = reservationService.updateReservation(id, updatedData, "BOT_CHAT");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}