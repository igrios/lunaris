package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int pendingPayments,

        int estimatedVehicles
) {
}