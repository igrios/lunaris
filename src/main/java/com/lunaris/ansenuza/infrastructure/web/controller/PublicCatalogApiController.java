package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicCatalogApiController {

    private static final List<String> SCHEDULES = List.of("03:00", "08:00");

    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;

    @GetMapping("/api/public/schedules")
    public ResponseEntity<SchedulesResponse> schedules() {
        return ResponseEntity.ok(new SchedulesResponse(SCHEDULES));
    }

    @GetMapping({"/api/public/localities", "/api/v1/localities"})
    public ResponseEntity<List<LocalityResponse>> localities() {
        List<LocalityResponse> response = localityRepository.findLocalitiesWithFares().stream()
                .map(locality -> new LocalityResponse(
                        locality.getId(),
                        locality.getName(),
                        locality.getKmsToCordoba(),
                        locality.getMinutesFromOrigin(),
                        fareRepository.findFirstByLocalityNameIgnoreCase(locality.getName())
                                .map(fare -> fare.getAmount())
                                .orElse(null)))
                .toList();
        return ResponseEntity.ok(response);
    }

    public record SchedulesResponse(List<String> schedules) {
    }

    public record LocalityResponse(
            UUID id,
            String name,
            Integer kmsToCordoba,
            Integer minutesFromOrigin,
            BigDecimal amount) {
    }
}
