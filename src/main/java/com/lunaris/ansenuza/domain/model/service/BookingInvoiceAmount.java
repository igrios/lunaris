package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.util.List;

/** Contrato compartido por la vista de pendientes y la emisión de facturas. */
public final class BookingInvoiceAmount {
    private BookingInvoiceAmount() {}

    public static String groupCode(Reservation reservation) {
        if (reservation.getBookingGroupCode() != null && !reservation.getBookingGroupCode().isBlank()) {
            return reservation.getBookingGroupCode();
        }
        return reservation.getReservationCode() == null ? "UUID:" + reservation.getId()
                : reservation.getReservationCode().replaceFirst("-(IDA|VUELTA)$", "");
    }

    public static BigDecimal total(List<Reservation> legs) {
        var groupAmounts = legs.stream().filter(Reservation::isAmountIsGroupTotal)
                .map(r -> zero(r.getAmount()).stripTrailingZeros()).distinct().toList();
        if (groupAmounts.size() > 1 || (!groupAmounts.isEmpty()
                && legs.stream().anyMatch(r -> !r.isAmountIsGroupTotal()))) {
            throw new DomainValidationException("El grupo tiene importes incompatibles. Revisar el total acordado.");
        }
        BigDecimal base = groupAmounts.isEmpty()
                ? legs.stream().map(r -> zero(r.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add)
                : groupAmounts.getFirst();
        return base.add(legs.stream().map(r -> zero(r.getExtraAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
