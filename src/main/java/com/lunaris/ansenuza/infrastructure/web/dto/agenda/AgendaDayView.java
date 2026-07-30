package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;
import java.util.UUID;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int pendingPayments,

        int estimatedVehicles,

        UUID driverId
) {
}
