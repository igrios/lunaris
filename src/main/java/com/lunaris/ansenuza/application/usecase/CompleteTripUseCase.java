package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

/** Cierra atómicamente una hoja de ruta despachada por un chofer. */
@Service
@RequiredArgsConstructor
public class CompleteTripUseCase {
    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public int execute(UUID driverId, LocalDate effectiveDate, String direction) {
        if (driverId == null || effectiveDate == null) {
            throw new IllegalArgumentException("Chofer y fecha son obligatorios.");
        }
        String normalizedDirection = direction == null ? null : direction.trim().toUpperCase();
        List<Reservation> route = reservationRepository.findAllAssignedByDriverId(driverId).stream()
                .filter(r -> effectiveDate.equals(effectiveDate(r)))
                .filter(r -> normalizedDirection == null || normalizedDirection.equalsIgnoreCase(r.getRouteDirection()))
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getTravelStatus() != Reservation.TravelStatus.CANCELED
                        && r.getTravelStatus() != Reservation.TravelStatus.NO_SHOW
                        && r.getTravelStatus() != Reservation.TravelStatus.COMPLETED
                        && r.getTravelStatus() != Reservation.TravelStatus.REALIZED)
                .filter(this::isBoarded)
                .toList();
        for (Reservation reservation : route) {
            reservation.setTravelStatus(Reservation.TravelStatus.COMPLETED);
            reservation.setStatus("COMPLETED");
            reservationRepository.save(reservation);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId())
                    .eventType("TRIP_COMPLETED_BY_DRIVER")
                    .description("Viaje finalizado por el chofer")
                    .triggeredBy("DRIVER_WHATSAPP")
                    .build());
        }
        return route.size();
    }

    private boolean isBoarded(Reservation reservation) {
        return reservation.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARDED;
    }

    private LocalDate effectiveDate(Reservation reservation) {
        if ("VUELTA".equalsIgnoreCase(reservation.getRouteDirection())
                && reservation.getReturnDate() != null) {
            return reservation.getReturnDate();
        }
        return reservation.getTravelDate() != null ? reservation.getTravelDate() : reservation.getReturnDate();
    }
}
