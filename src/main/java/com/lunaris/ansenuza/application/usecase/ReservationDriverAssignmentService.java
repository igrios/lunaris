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
