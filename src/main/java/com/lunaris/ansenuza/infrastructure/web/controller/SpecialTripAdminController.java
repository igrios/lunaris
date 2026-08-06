package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import com.lunaris.ansenuza.infrastructure.web.mapper.SpecialTripWebMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/special-trips")
@RequiredArgsConstructor
public class SpecialTripAdminController {
    private final CreateSpecialTripUseCase createUseCase;
    private final UpdateSpecialTripUseCase updateUseCase;
    private final GetSpecialTripsQuery query;
    private final SpecialTripWebMapper mapper;

    @GetMapping
    public List<SpecialTripResponse> getAll() {
        return query.getAll().stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<SpecialTripResponse> create(@Valid @RequestBody SpecialTripRequest request) {
        SpecialTripResponse response = mapper.toResponse(createUseCase.create(mapper.toCommand(request)));
        return ResponseEntity.created(URI.create("/api/admin/special-trips/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public SpecialTripResponse update(@PathVariable Long id, @Valid @RequestBody SpecialTripRequest request) {
        return mapper.toResponse(updateUseCase.update(id, mapper.toCommand(request)));
    }
}
