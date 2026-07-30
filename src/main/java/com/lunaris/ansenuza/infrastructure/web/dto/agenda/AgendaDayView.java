package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int pendingPayments,

        int estimatedVehicles,

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
