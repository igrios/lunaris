package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IssuedInvoiceRow(
        UUID id,
        String invoiceNumber,
        String passengerName,
        String passengerCuil,
        BigDecimal amount,
        String pdfUrl,
        Boolean sentViaWhatsapp,
        LocalDateTime createdAt,
        String reservationStatus,
        boolean refundedToWallet
) {}
