package com.lunaris.ansenuza.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final LocalityRepository localityRepository;
    private final WhatsAppService whatsAppService;

    @Transactional
    public Reservation execute(UUID reservationId) {
        Reservation onboard = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if (onboard.getDriver() == null || onboard.getTravelDate() == null) {
            throw new IllegalStateException("La reserva no pertenece a una ruta asignada.");
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
        if (next.getPassenger() == null) {
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
