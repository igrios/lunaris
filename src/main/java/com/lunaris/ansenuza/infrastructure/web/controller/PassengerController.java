package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreatePassengerUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerDetailsService;
import com.lunaris.ansenuza.infrastructure.web.dto.UpdatePassengerRequest;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.CreatePassengerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor

public class PassengerController {

    private final PassengerDetailsService passengerDetailsService;
    private final PassengerRepository repository;
    private final CreatePassengerUseCase createPassengerUseCase;

    @GetMapping
    public List<Passenger> findAll() {
        return repository.findAll();
    }

    @GetMapping("/by-phone")
    public ResponseEntity<PassengerDetailsService.PassengerLookup> findByPhone(
            @RequestParam String phone) {
        return passengerDetailsService.findByPhone(phone)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/by-cuil")
    public ResponseEntity<PassengerDetailsService.PassengerLookup> findByCuil(
            @RequestParam String cuil) {
        return passengerDetailsService.findByCuil(cuil)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public Passenger findById(@PathVariable UUID id) {

        return passengerDetailsService.findById(id);
    }
@PutMapping("/{id}")
public Passenger update(@PathVariable UUID id,
        @RequestBody UpdatePassengerRequest request) {
    return passengerDetailsService.update(id, request);
}

@PostMapping
public Passenger create(
        @Valid
        @RequestBody CreatePassengerRequest request) {

    return createPassengerUseCase.execute(request);
}

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String health() {
        return "Lunaris OK";
    }
}


}