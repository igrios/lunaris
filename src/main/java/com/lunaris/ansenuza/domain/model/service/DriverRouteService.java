package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverRouteService {

    private static final int VEHICLE_CAPACITY = 4;
    private static final String CAPACITY_MESSAGE =
            "No se pueden asignar más de 4 pasajeros a un solo vehículo/chofer.";

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final TripRouteCalculatorService routeCalculator = new TripRouteCalculatorService();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
                || selected.stream().anyMatch(reservation ->
                        !travelDate.equals(reservation.getTravelDate())
                                || !reservation.isScheduledConfirmedTrip())
                || !sameManifest(selected)) {
            throw new IllegalArgumentException(
                    "Solo se pueden asignar reservas confirmadas del mismo sentido, fecha y turno.");
        }
        assertVehicleCapacity(selected);

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
                || selected.stream().anyMatch(reservation -> !reservation.isScheduledConfirmedTrip())
                || !sameManifest(selected)
                || selected.stream()
                        .filter(reservation -> reservation.getDriver() != null)
                        .anyMatch(reservation ->
                                !affectedDriverIds.contains(reservation.getDriver().getId()))) {
            throw new IllegalArgumentException(
                    "Las reservas cambiaron mientras se actualizaba la ruta. Reintentá la operación.");
        }
        assertVehicleCapacity(selected);
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
        List<Reservation> geographicallyOrdered = orderedReservationIds.stream()
                .map(selectedById::get)
                .sorted(routeComparator(selected.getFirst()))
                .toList();
        List<Reservation> ordered = java.util.stream.IntStream.range(0, geographicallyOrdered.size())
                .mapToObj(index -> {
                    Reservation reservation = geographicallyOrdered.get(index);
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

    private boolean sameManifest(List<Reservation> reservations) {
        if (reservations.isEmpty()) return true;
        Reservation first = reservations.getFirst();
        return reservations.stream().allMatch(candidate -> routeCalculator.sameManifest(first, candidate));
    }

    private void assertVehicleCapacity(List<Reservation> reservations) {
        int occupiedSeats = reservations.stream().mapToInt(Reservation::getTotalSeats).sum();
        if (occupiedSeats > VEHICLE_CAPACITY) {
            throw new IllegalArgumentException(CAPACITY_MESSAGE);
        }
    }

    private java.util.Comparator<Reservation> routeComparator(Reservation reference) {
        boolean returnDirection = TripRouteCalculatorService.isCordoba(reference.getPickupLocality());
        java.util.Comparator<Reservation> localityOrder = java.util.Comparator.comparingInt(reservation -> {
            String locality = returnDirection
                    ? reservation.getDestination() : reservation.getPickupLocality();
            int index = routeCalculator.corridorIndex(locality);
            if (index < 0) return Integer.MAX_VALUE;
            return returnDirection ? -index : index;
        });
        return localityOrder
                .thenComparing(reservation -> reservation.getPickupAddress() == null
                        ? "" : reservation.getPickupAddress(), String.CASE_INSENSITIVE_ORDER);
    }
}
