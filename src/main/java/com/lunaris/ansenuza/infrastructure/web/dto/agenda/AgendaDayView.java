package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int confirmedPassengers,

        int waitingListPassengers,

        boolean capacityExceeded,

        int pendingPayments,

        int estimatedVehicles,

        int vehicleCapacity,

        int plannedCapacity,

        BigDecimal totalCollected,

        int paidReservations,

        UUID driverId
) {
}
