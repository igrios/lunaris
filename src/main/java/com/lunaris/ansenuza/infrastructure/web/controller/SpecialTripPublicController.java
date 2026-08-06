package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import com.lunaris.ansenuza.infrastructure.web.mapper.SpecialTripWebMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/special-trips")
@RequiredArgsConstructor
public class SpecialTripPublicController {
    private final GetSpecialTripsQuery query;
    private final SpecialTripWebMapper mapper;

    @GetMapping
    public List<SpecialTripResponse> getActive() {
        return query.getActive().stream().map(mapper::toResponse).toList();
    }
}
