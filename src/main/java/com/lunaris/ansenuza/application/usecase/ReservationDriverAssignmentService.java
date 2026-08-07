package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
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

    @Transactional
    public Optional<Reservation> assign(UUID reservationId, UUID driverId) {
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        if (reservation.isEmpty()) {
            return Optional.empty();
        }
        return driverRepository.findById(driverId)
                .map(driver -> {
                    int assignedSeats = reservationRepository
                            .findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                                    driverId, reservation.get().getTravelDate()).stream()
                            .filter(assigned -> !assigned.getId().equals(reservationId))
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
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                    reservation.setTravelStatus(Reservation.TravelStatus.PENDING);
                    return reservationRepository.saveAndFlush(reservation);
                });
    }
}
