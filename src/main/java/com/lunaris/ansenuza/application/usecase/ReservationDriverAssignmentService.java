package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationDriverAssignmentService {

    private static final int VEHICLE_CAPACITY = 4;
    private static final String CAPACITY_MESSAGE =
            "No se pueden asignar más de 4 pasajeros a un solo vehículo/chofer.";

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final TripRouteCalculatorService routeCalculator = new TripRouteCalculatorService();

    @Transactional
    public Optional<Reservation> assign(UUID reservationId, UUID driverId) {
        Optional<Reservation> reservation = reservationRepository.findByIdForUpdate(reservationId);
        if (reservation.isEmpty()) {
            return Optional.empty();
        }
        return driverRepository.findById(driverId)
                .map(driver -> {
                    if (reservation.get().getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                        throw new DomainValidationException(
                                "No se puede modificar una reserva porque la ruta ya fue enviada al chofer.");
                    }
                    String direction = reservation.get().getRouteDirection();
                    if (direction == null) {
                        direction = TripRouteCalculatorService.isCordoba(reservation.get().getPickupLocality())
                                ? "VUELTA" : "IDA";
                    }
                    String schedule = reservation.get().getDepartureSchedule();
                    java.util.stream.Stream<Reservation> assignedReservations = (schedule == null || schedule.isBlank())
                            ? reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                                    driverId, reservation.get().getTravelDate()).stream()
                            : reservationRepository.findByDriverAndRouteScope(
                                    driverId, reservation.get().getTravelDate(), schedule, direction).stream();
                    int assignedSeats = assignedReservations
                            .filter(assigned -> !assigned.getId().equals(reservationId))
                            .filter(assigned -> routeCalculator.sameManifest(reservation.get(), assigned))
                            .mapToInt(Reservation::getTotalSeats)
                            .sum();
                    if (assignedSeats + reservation.get().getTotalSeats() > VEHICLE_CAPACITY) {
                        throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                                CAPACITY_MESSAGE);
                    }
                    reservation.get().setDriver(driver);
                    return reservationRepository.saveAndFlush(reservation.get());
                });
    }

    @Transactional
    public Optional<Reservation> unassign(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    if (reservation.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                        throw new DomainValidationException(
                                "No se puede modificar una reserva porque la ruta ya fue enviada al chofer.");
                    }
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                    reservation.setTravelStatus(Reservation.TravelStatus.PENDING);
                    return reservationRepository.saveAndFlush(reservation);
                });
    }
}
