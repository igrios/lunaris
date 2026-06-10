package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.time.LocalDate;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor

public class ReservationController {

  private final ReservationRepository repository;
private final CreateReservationUseCase createReservationUseCase;

   

    @GetMapping
    public List<Reservation> findAll() {
        return repository.findAll();
    }

    @PostMapping
public Reservation create(
        @RequestBody CreateReservationRequest request) {

    return createReservationUseCase.execute(request);
}

@GetMapping("/date/{travelDate}")
public List<Reservation> findByDate(
        @PathVariable LocalDate travelDate) {

    return repository.findByTravelDate(travelDate);
}

  }