package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ReservationDriverAssignmentService;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationDriverAssignmentService assignmentService;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAll(
            @RequestParam(required = false) LocalDate travelDate) {
        List<Reservation> reservations = travelDate != null
                ? reservationRepository.findByTravelDate(travelDate)
                : reservationRepository.findAll();
        return reservations.stream()
                .map(AdminReservationResponse::from)
                .toList();
    }

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

    public record AdminReservationResponse(
            UUID id,
            String reservationCode,
            LocalDate travelDate,
            String pickupLocality,
            String pickupAddress,
            String destination,
            BigDecimal amount,
            BigDecimal extraAmount,
            Integer passengerCount,
            String status,
            String source,
            String travelStatus,
            Boolean paymentVerified,
            UUID passengerId,
            String passengerName,
            UUID driverId,
            String driverName) {

        static AdminReservationResponse from(Reservation reservation) {
            var passenger = reservation.getPassenger();
            var driver = reservation.getDriver();
            String passengerName = passenger == null
                    ? null
                    : String.join(" ",
                            passenger.getFirstName() != null ? passenger.getFirstName() : "",
                            passenger.getLastName() != null ? passenger.getLastName() : "").trim();
            return new AdminReservationResponse(
                    reservation.getId(),
                    reservation.getReservationCode(),
                    reservation.getTravelDate(),
                    reservation.getPickupLocality(),
                    reservation.getPickupAddress(),
                    reservation.getDestination(),
                    reservation.getAmount(),
                    reservation.getExtraAmount(),
                    reservation.getPassengerCount(),
                    reservation.getStatus(),
                    reservation.getSource() != null ? reservation.getSource().name() : null,
                    reservation.getTravelStatus() != null
                            ? reservation.getTravelStatus().name() : null,
                    reservation.getPaymentVerified(),
                    passenger != null ? passenger.getId() : null,
                    passengerName,
                    driver != null ? driver.getId() : null,
                    driver != null ? driver.getFullName() : null);
        }
    }
}
