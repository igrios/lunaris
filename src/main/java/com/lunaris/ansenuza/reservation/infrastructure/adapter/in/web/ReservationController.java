package com.lunaris.ansenuza.reservation.infrastructure.adapter.in.web;

import com.lunaris.ansenuza.reservation.application.port.in.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.reservation.application.port.in.CreateReservationUseCase;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.domain.model.ReservationStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** API versionada de migración; los endpoints REST históricos permanecen intactos. */
@RestController
@RequestMapping("/api/v2/reservations")
public class ReservationController {
    private final CreateReservationUseCase createReservation;
    private final ConfirmPaymentUseCase confirmPayment;
    private final com.lunaris.ansenuza.domain.repository.ReservationRepository reservationRepository;
    private final com.lunaris.ansenuza.domain.model.service.ReservationService reservationService;

    public ReservationController(CreateReservationUseCase createReservation, ConfirmPaymentUseCase confirmPayment,
            com.lunaris.ansenuza.domain.repository.ReservationRepository reservationRepository,
            com.lunaris.ansenuza.domain.model.service.ReservationService reservationService) {
        this.createReservation = createReservation;
        this.confirmPayment = confirmPayment;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody CreateReservationRequest request) {
        Reservation reservation = createReservation.create(request.toDomain());
        return ResponseEntity.created(URI.create("/api/v2/reservations/" + reservation.id()))
                .body(ReservationResponse.from(reservation));
    }

    @PostMapping("/{reservationId}/payment-confirmation")
    public ReservationResponse confirmPayment(@PathVariable UUID reservationId) {
        return ReservationResponse.from(confirmPayment.confirmPayment(reservationId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id, Principal principal) {
        var reservation = reservationRepository.findById(id)
                .filter(candidate -> candidate.getPassenger() != null
                        && candidate.getPassenger().getPhone().equals(principal.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reserva no encontrada."));
        reservationService.cancelReservation(reservation.getId(), "PASSENGER");
        return ResponseEntity.noContent().build();
    }

    public record CreateReservationRequest(UUID passengerId, LocalDate travelDate,
            String pickupLocality, String pickupAddress, String destination,
            BigDecimal amount, int passengerCount, String departureSchedule) {
        Reservation toDomain() {
            return Reservation.builder(passengerId, pickupLocality, destination)
                    .travelDate(travelDate).pickupAddress(pickupAddress).amount(amount)
                    .passengerCount(passengerCount).departureSchedule(departureSchedule)
                    .status(ReservationStatus.PENDING_PAYMENT).paymentVerified(false).build();
        }
    }

    public record ReservationResponse(UUID id, UUID passengerId, LocalDate travelDate,
            String pickupLocality, String destination, BigDecimal amount,
            ReservationStatus status, boolean paymentVerified) {
        static ReservationResponse from(Reservation reservation) {
            return new ReservationResponse(reservation.id(), reservation.passengerId(), reservation.travelDate(),
                    reservation.pickupLocality(), reservation.destination(), reservation.amount(),
                    reservation.status(), reservation.paymentVerified());
        }
    }
}
