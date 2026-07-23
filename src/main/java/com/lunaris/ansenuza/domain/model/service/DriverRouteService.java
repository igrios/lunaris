package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverRouteService {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public List<Reservation> replaceRoute(
            Driver driver, LocalDate travelDate, List<UUID> orderedReservationIds) {
        if (driver == null || driver.getId() == null || travelDate == null
                || orderedReservationIds == null) {
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

        Set<UUID> affectedDriverIds = new HashSet<>();
        affectedDriverIds.add(driver.getId());
        selected.stream()
                .filter(reservation -> reservation.getDriver() != null)
                .map(reservation -> reservation.getDriver().getId())
                .forEach(affectedDriverIds::add);
        List<Driver> lockedDrivers = driverRepository.findAllByIdForUpdate(affectedDriverIds);
        if (lockedDrivers.size() != affectedDriverIds.size()) {
            throw new IllegalArgumentException("No se pudo bloquear la totalidad de los choferes afectados.");
        }
        Driver targetDriver = lockedDrivers.stream()
                .filter(locked -> locked.getId().equals(driver.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado: " + driver.getId()));

        selected = reservationRepository.findAllById(orderedReservationIds);
        if (selected.size() != orderedReservationIds.size()
                || selected.stream().anyMatch(reservation -> !travelDate.equals(reservation.getTravelDate()))
                || selected.stream()
                        .filter(reservation -> reservation.getDriver() != null)
                        .anyMatch(reservation ->
                                !affectedDriverIds.contains(reservation.getDriver().getId()))) {
            throw new IllegalArgumentException(
                    "Las reservas cambiaron mientras se actualizaba la ruta. Reintentá la operación.");
        }
        Set<Reservation> changed = new LinkedHashSet<>();
        List<Reservation> targetRoute =
                reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                        targetDriver.getId(), travelDate);
        targetRoute.stream()
                .filter(reservation -> !uniqueIds.contains(reservation.getId()))
                .forEach(reservation -> {
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                    changed.add(reservation);
                });

        var selectedById = selected.stream()
                .collect(java.util.stream.Collectors.toMap(Reservation::getId, reservation -> reservation));
        List<Reservation> ordered = java.util.stream.IntStream.range(0, orderedReservationIds.size())
                .mapToObj(index -> {
                    Reservation reservation = selectedById.get(orderedReservationIds.get(index));
                    reservation.setDriver(targetDriver);
                    reservation.setRouteSequence(index + 1);
                    changed.add(reservation);
                    return reservation;
                })
                .toList();

        lockedDrivers.stream()
                .filter(locked -> !locked.getId().equals(targetDriver.getId()))
                .forEach(previousDriver -> {
                    List<Reservation> remaining =
                            reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                                    previousDriver.getId(), travelDate).stream()
                                    .filter(reservation -> !uniqueIds.contains(reservation.getId()))
                                    .toList();
                    java.util.stream.IntStream.range(0, remaining.size()).forEach(index -> {
                        remaining.get(index).setRouteSequence(index + 1);
                        changed.add(remaining.get(index));
                    });
                });

        reservationRepository.saveAll(changed);
        return reservationRepository.saveAllAndFlush(ordered);
    }
}
