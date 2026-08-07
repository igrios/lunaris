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
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final LocalityRepository localityRepository;
    private final MessagingPort messaging;

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
        if ("COMPLETED".equalsIgnoreCase(initial.getStatus())
                || initial.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException();
        }
        if (newStatus != Reservation.TravelStatus.ONBOARD
                && newStatus != Reservation.TravelStatus.BOARDED
                && newStatus != Reservation.TravelStatus.ONBOARDED) {
            initial.setTravelStatus(newStatus);
            return reservationRepository.saveAndFlush(initial);
        }
        return boardPassenger(reservationId, null);
    }

    @Transactional
    public Reservation recordReturnedPassengers(UUID reservationId, int returnedPassengerCount) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada: " + reservationId));
        if ("COMPLETED".equalsIgnoreCase(reservation.getStatus())
                || reservation.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException();
        }
        if (!isReturnLeg(reservation)) {
            throw new IllegalArgumentException("El conteo de regreso solo aplica al tramo de vuelta.");
        }
        int total = reservation.getTotalSeats();
        if (returnedPassengerCount < 0 || returnedPassengerCount > total) {
            throw new IllegalArgumentException("La cantidad de pasajeros regresados no es válida.");
        }
        reservation.setReturnedPassengerCount(returnedPassengerCount);
        if (returnedPassengerCount == total) {
            reservation.setTravelStatus(Reservation.TravelStatus.COMPLETED);
            reservation.setStatus("COMPLETED");
        } else if (returnedPassengerCount > 0) {
            reservation.setTravelStatus(Reservation.TravelStatus.PARTIALLY_COMPLETED);
            reservation.setStatus("PARTIALLY_COMPLETED");
        }
        return reservationRepository.saveAndFlush(reservation);
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

        if (isReturnLeg(onboard)) {
            int passengerCount = onboard.getTotalSeats();
            onboard.setReturnedPassengerCount(passengerCount);
            onboard.setTravelStatus(Reservation.TravelStatus.COMPLETED);
            onboard.setStatus("COMPLETED");
        } else {
            onboard.setTravelStatus(Reservation.TravelStatus.ONBOARDED);
        }
        reservationRepository.saveAndFlush(onboard);

        Optional<Reservation> nextPassenger =
                findNextPassengerInRoute(onboard, lockedEffectiveDate);
        notifyDriver(onboard, nextPassenger);
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

    private void notifyDriver(Reservation onboard, Optional<Reservation> nextPassenger) {
        if (onboard.getDriver() == null || onboard.getDriver().getPhone() == null
                || onboard.getDriver().getPhone().isBlank()) {
            return;
        }
        String passengerName = onboard.getPassenger() == null
                ? "El pasajero"
                : (onboard.getPassenger().getFirstName() + " "
                        + onboard.getPassenger().getLastName()).trim();
        String nextNotice = nextPassenger
                .filter(next -> next.getPassenger() != null)
                .map(next -> " Ya avisamos a " + next.getPassenger().getFirstName()
                        + " que es el próximo pasajero.")
                .orElse(" No quedan pasajeros pendientes en esta ruta.");
        messaging.sendButtons(
                onboard.getDriver().getPhone(),
                "Abordaje confirmado",
                "✅ " + passengerName + " fue confirmado a bordo." + nextNotice,
                List.of(new Button("VIEW_ROUTE", "🗺️ Ver Ruta")));
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
        return com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phone);
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
        if (onboard.getRouteSequence() != null && onboard.getRouteDirection() != null) {
            Optional<Reservation> persistedNext = reservationRepository.findNextRoutePassenger(
                            onboard.getDriver().getId(), effectiveDate,
                            onboard.getRouteDirection(), onboard.getRouteSequence() + 1)
                    .stream()
                    .filter(candidate -> belongsToSameLegAndDate(onboard, candidate, effectiveDate))
                    .filter(this::isPendingCandidate)
                    .findFirst();
            if (persistedNext.isPresent()) {
                log.info("[ONBOARD] N+1 encontrado por secuencia persistida: {}",
                        persistedNext.get().getRouteSequence());
                return persistedNext;
            }
        }
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
        messaging.sendTemplate(
                next.getPassenger().getPhone(),
                "proximo_en_camino",
                List.of(
                        textOrDefault(next.getPassenger().getFirstName(), "Pasajero"),
                        textOrDefault(onboard.getDriver().getFullName(), "Chofer")));
        messaging.sendText(
                next.getPassenger().getPhone(),
                "¡Hola " + next.getPassenger().getFirstName()
                        + "! El auto de Lunaris ya recogió al pasajero anterior y sos el próximo en la lista. "
                        + "Por favor estate atento/a en la puerta.");
        String locationUrl = onboard.getDriver().getCurrentLocationUrl();
        if (locationUrl != null && !locationUrl.isBlank()) {
            messaging.sendText(
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

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private record DriverActor(UUID driverId) {
    }
}
