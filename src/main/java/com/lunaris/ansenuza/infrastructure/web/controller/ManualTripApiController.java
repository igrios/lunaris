package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase;
import com.lunaris.ansenuza.application.usecase.MarkTripAsPaidService;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.web.mapper.ManualTripWebMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/trips")
@RequiredArgsConstructor
public class ManualTripApiController {
    private final CreateManualReservationUseCase create;
    private final MarkTripAsPaidService payments;

    @PostMapping
    public ResponseEntity<List<AdminReservationApiController.AdminReservationResponse>> create(
            @Valid @RequestBody CreateReservationForm request) {
        var saved = create.execute(ManualTripWebMapper.toReservation(request), request.getReturnDepartureSchedule());
        return ResponseEntity.created(URI.create("/api/admin/reservations?travelDate=" + request.getTravelDate()))
                .body(saved.stream().map(AdminReservationApiController.AdminReservationResponse::from).toList());
    }

    @PostMapping("/{id}/paid")
    public AdminReservationApiController.AdminReservationResponse markTripAsPaid(
            @PathVariable UUID id, Principal operator) {
        return AdminReservationApiController.AdminReservationResponse.from(
                payments.markTripAsPaid(id, operator.getName()));
    }
}
