package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.hojaruta.HojaRutaViewModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetHojaDeRutaUseCase {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository sessionRepository;

    public HojaRutaViewModel execute(LocalDate travelDate) {
        List<Reservation> reservations = reservationRepository.findByTravelDate(travelDate);
        List<ConversationSession> sesiones = sessionRepository.findAll();

        // 🏙️ Si el origen es "Córdoba", asumimos que es "VUELTA" hacia los pueblos.
        // Si NO es Córdoba (Morteros, La Puerta, etc.), es "IDA" hacia Córdoba.
        long totalYendo = reservations.stream()
                .filter(r -> r.getPickupLocality() != null && !"Córdoba".equalsIgnoreCase(r.getPickupLocality())) 
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        long totalVolviendo = reservations.stream()
                .filter(r -> r.getPickupLocality() != null && "Córdoba".equalsIgnoreCase(r.getPickupLocality()))
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        // 🚨 Filtramos el turno de las 08:00 AM buscando en las notas que genera tu bot
        long pasajeros0800Count = reservations.stream()
                .filter(r -> r.getNotes() != null && r.getNotes().contains("08:00")) 
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        // El hub de La Puerta se activa si pasan los 15 pasajeros en ese turno
        boolean hubActivado = pasajeros0800Count > 15; 

        return new HojaRutaViewModel(
                travelDate,
                totalYendo,
                totalVolviendo,
                hubActivado,
                pasajeros0800Count,
                reservations,
                sesiones
        );
    }
}