package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDailyOperationSummaryUseCase {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public DailyOperationSummaryResponse execute(LocalDate travelDate) {
        long totalReservations = reservationRepository.findByTravelDate(travelDate).stream()
                .filter(r -> r != null).count();
        long totalPassengers = reservationRepository.countDistinctPassengersByTravelDate(travelDate);
        long paidReservations = reservationRepository.countDistinctPaidPassengersByTravelDate(travelDate);
        long pendingPayments = reservationRepository.countDistinctPendingPassengersByTravelDate(travelDate);

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
