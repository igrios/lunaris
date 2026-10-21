package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Grupo visible en facturación; la emisión requiere validar el pago de todos los tramos. */
public record PendingInvoiceRow(
        UUID reservationId,
        String reservationCode,
        String passengerName,
        String phone,
        String rawDocument,
        String suggestedCuil,
        BigDecimal amount,
        LocalDate travelDate,
        String route,
        boolean paymentVerified
) {}
