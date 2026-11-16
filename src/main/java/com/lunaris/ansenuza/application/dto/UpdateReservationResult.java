package com.lunaris.ansenuza.application.dto;

import com.lunaris.ansenuza.domain.model.Reservation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Snapshot de la operación; total incluye el saldo aplicado, invoiceTotal no lo incluye. */
public record UpdateReservationResult(String bookingGroupCode, int revision, String operationId,
        List<Leg> legs, BigDecimal total, BigDecimal invoiceTotal, BigDecimal priceDifference,
        BigDecimal pendingAmount, BigDecimal creditedAmount) {
    public UpdateReservationResult { legs = List.copyOf(legs); }

    public record Leg(UUID id, String reservationCode, boolean active, LocalDate travelDate,
            LocalDate returnDate, String departureSchedule, boolean flexibleReturnTime,
            String timePreference, Reservation.TravelStatus travelStatus, int seats,
            BigDecimal amount, BigDecimal extraAmount, BigDecimal usedBalance,
            BigDecimal recognizedPaidAmount, boolean paymentVerified) {}
}
