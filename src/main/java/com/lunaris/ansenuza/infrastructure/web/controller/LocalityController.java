package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.domain.model.Locality;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocalityController {

    private final LocalityService localityService;

    @GetMapping("/localities")
    public List<Locality> findAll() {
        return localityService.findAllWithActiveFare();
    }
}
