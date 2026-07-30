package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDailyOperationSummaryUseCase {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public DailyOperationSummaryResponse execute(LocalDate travelDate) {
        List<Reservation> reservations = reservationRepository.findByTravelDate(travelDate);

        List<Reservation> dailyReservations = reservations.stream()
                .filter(r -> r != null)
                .toList();

        long totalReservations = dailyReservations.size();
        
        long paidReservations = dailyReservations.stream()
                .filter(r -> Boolean.TRUE.equals(r.getPaymentVerified()))
                .count();

        long pendingPayments = totalReservations - paidReservations;

        // 👥 Sumamos todos los asientos físicos reales (Titular + Acompañantes)
        long totalPassengers = dailyReservations.stream()
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
