package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.util.List;

/** Vista completa del panel de Facturación: ingresos + pendientes + emitidas. */
public record BillingPanelView(
        BigDecimal ingresoHoy,
        long countHoy,
        BigDecimal ingresoMes,
        long countMes,
        List<PendingInvoiceRow> pendientes,
        List<IssuedInvoiceRow> emitidas
) {}
