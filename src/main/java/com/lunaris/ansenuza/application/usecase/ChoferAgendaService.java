package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChoferAgendaService {
    private final ReservationRepository reservationRepository;
    private final DriverAuthorizationService driverAuthorizationService;

    @Transactional(readOnly = true)
    public List<ServicioAgenda> execute(LocalDate fecha, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Se requiere autenticación.");
        }
        boolean agendaCompleta = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_OPERADOR".equals(a.getAuthority()));
        boolean chofer = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CHOFER".equals(a.getAuthority()));
        if (!agendaCompleta && !chofer) {
            throw new AccessDeniedException("No tiene acceso a la agenda de choferes.");
        }
        UUID driverId = agendaCompleta ? null
                : driverAuthorizationService.resolveActiveDriverId(authentication.getName());
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return reservationRepository.findDriverAgenda(dia, dia.plusDays(1), driverId).stream()
                .map(ServicioAgenda::from).toList();
    }

    public record ServicioAgenda(UUID id, String codigo, LocalDate fecha, String horario,
            String origen, String destino, UUID choferId, int pasajeros,
            String estado, Reservation.TravelStatus estadoViaje) {
        private static ServicioAgenda from(Reservation reservation) {
            return new ServicioAgenda(reservation.getId(), reservation.getReservationCode(),
                    reservation.getTravelDate(), reservation.getDepartureSchedule(),
                    reservation.getPickupLocality(), reservation.getDestination(),
                    reservation.getDriver() == null ? null : reservation.getDriver().getId(),
                    reservation.getTotalSeats(), reservation.getStatus(), reservation.getTravelStatus());
        }
    }
}
