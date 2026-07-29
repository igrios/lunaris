package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ReservationDriverAssignmentService;
import com.lunaris.ansenuza.domain.model.Reservation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationDriverAssignmentService assignmentService;

    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<ReservationDriverResponse> assignDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDriverRequest request) {
        return assignmentService.assign(id, request.driverId())
                .map(ReservationDriverResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/unassign-driver")
    public ResponseEntity<ReservationDriverResponse> unassignDriver(@PathVariable UUID id) {
        return assignmentService.unassign(id)
                .map(ReservationDriverResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record AssignDriverRequest(@NotNull UUID driverId) {
    }

    public record ReservationDriverResponse(
            UUID id,
            UUID driverId,
            String travelStatus) {

        static ReservationDriverResponse from(Reservation reservation) {
            return new ReservationDriverResponse(
                    reservation.getId(),
                    reservation.getDriver() != null ? reservation.getDriver().getId() : null,
                    reservation.getTravelStatus() != null
                            ? reservation.getTravelStatus().name() : null);
        }
    }
}
