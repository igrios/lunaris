package com.lunaris.ansenuza.application.usecase;

import java.util.Comparator;
import java.util.List;
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

@Service
@RequiredArgsConstructor
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final LocalityRepository localityRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public Reservation execute(UUID reservationId) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if (initial.getDriver() == null || initial.getTravelDate() == null) {
            throw new IllegalStateException("La reserva no pertenece a una ruta asignada.");
        }
        UUID driverId = initial.getDriver().getId();
        if (driverId == null || driverRepository.findAllByIdForUpdate(java.util.Set.of(driverId)).isEmpty()) {
            throw new IllegalStateException("No se pudo bloquear el chofer de la ruta.");
        }
        Reservation onboard = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if (onboard.getDriver() == null || !driverId.equals(onboard.getDriver().getId())
                || onboard.getTravelDate() == null) {
            throw new IllegalStateException(
                    "La ruta cambió durante el abordaje. Reintentá la operación.");
        }
        if (onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || onboard.getTravelStatus() == Reservation.TravelStatus.BOARDED) {
            return onboard;
        }

        onboard.setTravelStatus(Reservation.TravelStatus.ONBOARD);
        reservationRepository.saveAndFlush(onboard);

        List<Reservation> route =
                reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                        onboard.getDriver().getId(), onboard.getTravelDate()).stream()
                        .sorted(Comparator.comparing(
                                Reservation::getRouteSequence,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
        int currentIndex = route.stream().map(Reservation::getId).toList().indexOf(onboard.getId());
        if (currentIndex >= 0 && currentIndex + 1 < route.size()) {
            notifyNext(onboard, route.get(currentIndex + 1));
        }
        return onboard;
    }

    private void notifyNext(Reservation onboard, Reservation next) {
        if (next.getPassenger() == null || next.getPassenger().getPhone() == null
                || next.getPassenger().getPhone().isBlank()) {
            return;
        }
        int etaMinutes = calculateEtaMinutes(onboard.getPickupLocality(), next.getPickupLocality());
        whatsAppService.sendProximoEnCaminoTemplate(
                next.getPassenger().getPhone(),
                next.getPassenger().getFirstName(),
                onboard.getDriver().getFullName(),
                etaMinutes);
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
}
