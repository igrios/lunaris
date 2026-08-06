package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateFareUseCase;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/admin/fares")
@RequiredArgsConstructor
public class FareAdminController {
    private final GetFaresQuery query;
    private final UpdateFareUseCase updateFareUseCase;
    private final UpdateLocalityFareUseCase updateLocalityFareUseCase;
    private final CreateFareLocalityUseCase createFareLocalityUseCase;
    private final DeleteFareLocalityUseCase deleteFareLocalityUseCase;

    @GetMapping
    public List<FareLocalityView> getAll() {
        return query.getAll();
    }

    @PostMapping
    public ResponseEntity<FareLocalityView> create(@Valid @RequestBody LocalityFareRequest request) {
        FareLocalityView response = createFareLocalityUseCase.create(request.name(), request.kmsToCordoba(),
                request.minutesFromOrigin(), request.amount());
        return ResponseEntity.created(URI.create("/api/admin/fares/" + response.fareId())).body(response);
    }

    @DeleteMapping("/{fareId}")
    public ResponseEntity<Void> delete(@PathVariable UUID fareId) {
        deleteFareLocalityUseCase.delete(fareId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fareId}")
    public FareLocalityView updateFare(@PathVariable UUID fareId, @Valid @RequestBody FareAmountRequest request) {
        return updateFareUseCase.updateFare(fareId, request.amount());
    }

    @PutMapping("/localities/{localityId}")
    public FareLocalityView updateLocalityAndFare(@PathVariable UUID localityId,
            @Valid @RequestBody LocalityFareRequest request) {
        return updateLocalityFareUseCase.updateLocalityAndFare(localityId, request.name(),
                request.kmsToCordoba(), request.minutesFromOrigin(), request.amount());
    }

    public record FareAmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }

    public record LocalityFareRequest(
            @NotBlank @Size(max = 100) String name,
            @Min(0) Integer kmsToCordoba,
            @Min(0) Integer minutesFromOrigin,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }
}
