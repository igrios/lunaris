package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository repository;

    @GetMapping
    public List<Driver> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public Driver create(
            @RequestBody Driver driver) {

        return repository.save(driver);
    }
}