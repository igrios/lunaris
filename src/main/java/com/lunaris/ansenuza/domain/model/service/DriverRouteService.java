package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverRouteService {

    private final ReservationRepository reservationRepository;

    @Transactional
    public List<Reservation> replaceRoute(
            Driver driver, LocalDate travelDate, List<UUID> orderedReservationIds) {
        if (driver == null || travelDate == null || orderedReservationIds == null) {
            throw new IllegalArgumentException("Chofer, fecha y orden de reservas son obligatorios.");
        }
        Set<UUID> uniqueIds = new HashSet<>(orderedReservationIds);
        if (uniqueIds.size() != orderedReservationIds.size()) {
            throw new IllegalArgumentException("La ruta no puede contener reservas duplicadas.");
        }

        List<Reservation> selected = reservationRepository.findAllById(orderedReservationIds);
        if (selected.size() != orderedReservationIds.size()
                || selected.stream().anyMatch(reservation -> !travelDate.equals(reservation.getTravelDate()))) {
            throw new IllegalArgumentException("Todas las reservas deben existir y pertenecer a la misma fecha.");
        }

        List<Reservation> currentRoute =
                reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                        driver.getId(), travelDate);
        currentRoute.stream()
                .filter(reservation -> !uniqueIds.contains(reservation.getId()))
                .forEach(reservation -> {
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                });

        var selectedById = selected.stream()
                .collect(java.util.stream.Collectors.toMap(Reservation::getId, reservation -> reservation));
        List<Reservation> ordered = java.util.stream.IntStream.range(0, orderedReservationIds.size())
                .mapToObj(index -> {
                    Reservation reservation = selectedById.get(orderedReservationIds.get(index));
                    reservation.setDriver(driver);
                    reservation.setRouteSequence(index + 1);
                    return reservation;
                })
                .toList();

        reservationRepository.saveAll(currentRoute);
        return reservationRepository.saveAllAndFlush(ordered);
    }
}
