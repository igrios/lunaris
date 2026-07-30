package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int pendingPayments,

        int estimatedVehicles,

        int vehicleCapacity,

        int plannedCapacity,

        BigDecimal totalCollected,

        int paidReservations,

        UUID driverId,

        List<ReservationRow> reservations
) {
    public record ReservationRow(
            UUID id,
            String passengerName,
            String phone,
            String pickupAddress,
            int passengerCount) {
    }
}
