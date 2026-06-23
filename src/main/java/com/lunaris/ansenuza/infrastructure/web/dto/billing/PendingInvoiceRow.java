package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Una reserva con pago confirmado que todavía no tiene factura emitida. */
public record PendingInvoiceRow(
        UUID reservationId,
        String reservationCode,
        String passengerName,
        String phone,
        String rawDocument,
        String suggestedCuil,
        BigDecimal amount,
        LocalDate travelDate,
        String route
) {}
