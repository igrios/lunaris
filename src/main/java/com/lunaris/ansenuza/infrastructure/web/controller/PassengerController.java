package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreatePassengerUseCase;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.CreatePassengerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor

public class PassengerController {

    private final PassengerRepository repository;
    private final CreatePassengerUseCase createPassengerUseCase;

    @GetMapping
    public List<Passenger> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Passenger findById(@PathVariable UUID id) {

        return repository.findById(id)
                .orElseThrow();
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