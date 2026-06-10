package com.lunaris.ansenuza.infrastructure.web.dto.dashboard;

import java.time.LocalDate;

public record DailyOperationSummaryResponse(

        LocalDate travelDate,

        long totalReservations,

        long paidReservations,

        long pendingPayments,

        long estimatedVehicles
) {
}