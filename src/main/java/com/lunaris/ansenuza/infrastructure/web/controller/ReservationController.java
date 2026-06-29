package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import lombok.RequiredArgsConstructor;

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
    public Reservation create(@RequestBody CreateReservationRequest request) {
        return createReservationUseCase.execute(request);
    }

    @GetMapping("/date/{travelDate}")
    public List<Reservation> findByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate) {
        return repository.findByTravelDate(travelDate);
    }
}