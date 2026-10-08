package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount;
import java.math.BigDecimal;
import java.util.List;

/** Importe persistido del tramo, compartido con el cálculo de facturación. */
public record SpecialAgendaTripView(Reservation reservation, BigDecimal totalPrice) {
    public static SpecialAgendaTripView from(Reservation reservation) {
        return new SpecialAgendaTripView(reservation, BookingInvoiceAmount.total(List.of(reservation)));
    }
}
