package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDailyOperationSummaryUseCase {

    private final ReservationRepository reservationRepository;

    public DailyOperationSummaryResponse execute(
            LocalDate travelDate) {

        List<Reservation> reservations =
                reservationRepository.findByTravelDate(travelDate);

        long totalReservations = reservations.size();

        long paidReservations =
                reservations.stream()
                        .filter(r -> Boolean.TRUE.equals(
                                r.getPaymentVerified()))
                        .count();

        long pendingPayments =
                totalReservations - paidReservations;

        long estimatedVehicles =
                (long) Math.ceil(totalReservations / 4.0);

        return new DailyOperationSummaryResponse(
                travelDate,
                totalReservations,
                paidReservations,
                pendingPayments,
                estimatedVehicles
        );
    }
}