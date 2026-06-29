package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDailyOperationSummaryUseCase {

    private final ReservationRepository reservationRepository;

    public DailyOperationSummaryResponse execute(LocalDate travelDate) {
        List<Reservation> reservations = reservationRepository.findByTravelDate(travelDate);

        List<Reservation> activeReservations = reservations.stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                .toList();

        long totalReservations = activeReservations.size();
        
        long paidReservations = activeReservations.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPaymentVerified()))
                .count();

        long pendingPayments = totalReservations - paidReservations;

        // 👥 Sumamos todos los asientos físicos reales (Titular + Acompañantes)
        long totalPassengers = activeReservations.stream()
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        // 🚗 División limpia por 4.0 para agrupar en autos de a cuatro
        long estimatedVehicles = totalPassengers == 0 ? 0 : (long) Math.ceil(totalPassengers / 4.0);

        return new DailyOperationSummaryResponse(
                travelDate,
                totalReservations,
                totalPassengers, // 👈 Pasamos el nuevo conteo de asientos físicos
                paidReservations,
                pendingPayments,
                estimatedVehicles
        );
    }
}