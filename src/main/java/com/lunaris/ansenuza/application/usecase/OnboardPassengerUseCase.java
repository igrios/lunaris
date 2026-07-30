package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final LocalityRepository localityRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public Reservation execute(UUID reservationId) {
        return updateTravelStatus(reservationId, Reservation.TravelStatus.ONBOARDED);
    }

    @Transactional
    public Reservation execute(UUID reservationId, String driverPhone) {
        DriverActor actor = resolveActiveDriver(driverPhone);
        return boardPassenger(reservationId, actor.driverId());
    }

    @Transactional
    public Reservation updateTravelStatus(
            UUID reservationId, Reservation.TravelStatus newStatus) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if (newStatus != Reservation.TravelStatus.ONBOARD
                && newStatus != Reservation.TravelStatus.BOARDED
                && newStatus != Reservation.TravelStatus.ONBOARDED) {
            initial.setTravelStatus(newStatus);
            return reservationRepository.saveAndFlush(initial);
        }
        return boardPassenger(reservationId, null);
    }

    private Reservation boardPassenger(UUID reservationId, UUID actorDriverId) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        LocalDate effectiveDate = effectiveLegDate(initial);
        if (initial.getDriver() == null || effectiveDate == null) {
            throw new IllegalStateException("La reserva no pertenece a una ruta asignada.");
        }
        UUID driverId = initial.getDriver().getId();
        if (actorDriverId != null && !actorDriverId.equals(driverId)) {
            throw new IllegalStateException(
                    "La reserva no pertenece al viaje del chofer autenticado.");
        }
        if (driverId == null || driverRepository.findAllByIdForUpdate(java.util.Set.of(driverId)).isEmpty()) {
            throw new IllegalStateException("No se pudo bloquear el chofer de la ruta.");
        }
        Reservation onboard = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        LocalDate lockedEffectiveDate = effectiveLegDate(onboard);
        if (onboard.getDriver() == null || !driverId.equals(onboard.getDriver().getId())
                || lockedEffectiveDate == null || !effectiveDate.equals(lockedEffectiveDate)) {
            throw new IllegalStateException(
                    "La ruta cambió durante el abordaje. Reintentá la operación.");
        }
        if (onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || onboard.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARDED) {
            log.warn(
                    "[ONBOARD] Duplicate boarding ignored. reservationId={}, travelStatus={}",
                    onboard.getId(), onboard.getTravelStatus());
            return onboard;
        }
        if (!isBoardable(onboard)) {
            log.warn(
                    "[ONBOARD] Boarding rejected by reservation state. reservationId={}, "
                            + "status={}, travelStatus={}",
                    onboard.getId(), onboard.getStatus(), onboard.getTravelStatus());
            throw new IllegalStateException(
                    "Esta reserva ya se encuentra abordada, finalizada o en un estado inválido.");
        }

        onboard.setTravelStatus(Reservation.TravelStatus.ONBOARDED);
        reservationRepository.saveAndFlush(onboard);

        Optional<Reservation> nextPassenger =
                findNextPassengerInRoute(onboard, lockedEffectiveDate);
        nextPassenger.ifPresentOrElse(
                next -> {
                    String phone = next.getPassenger() != null
                            ? next.getPassenger().getPhone()
                            : null;
                    log.info(
                            "[ONBOARD] Target N+1 passenger found. sequence={}, passengerId={}, phone={}",
                            next.getRouteSequence(), next.getId(), phone);
                    notifyNext(onboard, next);
                },
                () -> log.info(
                        "[ONBOARD] No N+1 passenger found with sequence {}",
                        expectedNextSequence(onboard)));
        return onboard;
    }

    private DriverActor resolveActiveDriver(String phone) {
        String normalized = normalizePhone(phone);
        return driverRepository.findFirstByPhone(normalized)
                .filter(com.lunaris.ansenuza.domain.model.Driver::isActive)
                .or(() -> driverRepository.findByActiveTrue().stream()
                        .filter(driver -> normalizePhone(driver.getPhone()).equals(normalized))
                        .findFirst())
                .map(driver -> new DriverActor(driver.getId()))
                .orElseThrow(() ->
                        new IllegalStateException("El callback no pertenece a un chofer activo."));
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String clean = phone.replaceAll("[^0-9]", "");
        return clean.startsWith("549") ? "54" + clean.substring(3) : clean;
    }

    private boolean isBoardable(Reservation reservation) {
        String status = reservation.getStatus();
        boolean validReservationStatus = "CONFIRMED".equalsIgnoreCase(status)
                || "PENDING".equalsIgnoreCase(status);
        return validReservationStatus
                && reservation.getTravelStatus() != Reservation.TravelStatus.CANCELED
                && reservation.getTravelStatus() != Reservation.TravelStatus.NO_SHOW;
    }

    private Optional<Reservation> findNextPassengerInRoute(
            Reservation onboard, LocalDate effectiveDate) {
        List<Reservation> route = reservationRepository.findRouteByEffectiveDate(
                onboard.getDriver().getId(), effectiveDate);
        log.info(
                "[ONBOARD] Current passenger sequence={}, passengers found in route={}",
                onboard.getRouteSequence(), route.size());
        Comparator<Reservation> fallbackOrder = Comparator
                .comparing(Reservation::getDepartureSchedule,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getId,
                        Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Reservation> routeOrder = onboard.getRouteSequence() == null
                ? fallbackOrder
                : Comparator.comparing(
                        Reservation::getRouteSequence,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(fallbackOrder);
        List<Reservation> orderedRoute = route.stream()
                .filter(candidate -> belongsToSameLegAndDate(onboard, candidate, effectiveDate))
                .sorted(routeOrder)
                .toList();
        if (onboard.getRouteSequence() != null) {
            return orderedRoute.stream()
                    .filter(candidate -> candidate.getRouteSequence() != null)
                    .filter(candidate -> candidate.getRouteSequence()
                            > onboard.getRouteSequence())
                    .filter(this::isPendingCandidate)
                    .findFirst();
        }
        int currentIndex = orderedRoute.stream()
                .map(Reservation::getId)
                .toList()
                .indexOf(onboard.getId());
        return currentIndex < 0 ? Optional.empty() : orderedRoute.stream()
                .skip(currentIndex + 1L)
                .filter(this::isPendingCandidate)
                .findFirst();
    }

    private boolean isPendingCandidate(Reservation reservation) {
        return reservation.getTravelStatus() == null
                || reservation.getTravelStatus() == Reservation.TravelStatus.PENDING;
    }

    private String expectedNextSequence(Reservation onboard) {
        return onboard.getRouteSequence() == null
                ? "UNKNOWN"
                : String.valueOf(onboard.getRouteSequence() + 1);
    }

    private boolean belongsToSameLegAndDate(
            Reservation onboard, Reservation candidate, LocalDate effectiveDate) {
        return isReturnLeg(onboard) == isReturnLeg(candidate)
                && effectiveDate.equals(effectiveLegDate(candidate));
    }

    private LocalDate effectiveLegDate(Reservation reservation) {
        if (isReturnLeg(reservation) && reservation.getReturnDate() != null) {
            return reservation.getReturnDate();
        }
        return reservation.getTravelDate() != null
                ? reservation.getTravelDate()
                : reservation.getReturnDate();
    }

    private boolean isReturnLeg(Reservation reservation) {
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().endsWith("-VUELTA");
    }

    private void notifyNext(Reservation onboard, Reservation next) {
        if (next.getPassenger() == null || next.getPassenger().getPhone() == null
                || next.getPassenger().getPhone().isBlank()) {
            return;
        }
        whatsAppService.sendProximoEnCaminoTemplate(
                next.getPassenger().getPhone(),
                next.getPassenger().getFirstName(),
                onboard.getDriver().getFullName());
        whatsAppService.sendMessage(
                next.getPassenger().getPhone(),
                "¡Hola " + next.getPassenger().getFirstName()
                        + "! La combi ya recogió al pasajero anterior y sos el próximo en subir. "
                        + "Por favor estate atento/a en la puerta.");
        String locationUrl = onboard.getDriver().getCurrentLocationUrl();
        if (locationUrl != null && !locationUrl.isBlank()) {
            whatsAppService.sendMessage(
                    next.getPassenger().getPhone(),
                    "📍 Ubicación actual del chofer: " + locationUrl);
        }
    }

    private int calculateEtaMinutes(String currentLocality, String nextLocality) {
        int currentMinutes = localityRepository.findFirstByNameIgnoreCase(currentLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        int nextMinutes = localityRepository.findFirstByNameIgnoreCase(nextLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        return Math.abs(nextMinutes - currentMinutes);
    }

    private record DriverActor(UUID driverId) {
    }
}
