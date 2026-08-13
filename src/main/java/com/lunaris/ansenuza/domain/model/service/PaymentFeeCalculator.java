package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentFeeCalculator {

    private final BigDecimal feePercentage;

    public PaymentFeeCalculator(
            @Value("${mercadopago.fee-percentage:0.06}") BigDecimal feePercentage) {
        if (feePercentage == null
                || feePercentage.signum() < 0
                || feePercentage.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("La comisión debe estar entre 0 y 1.");
        }
        this.feePercentage = feePercentage;
    }

    public BigDecimal calculateFinalAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.signum() < 0) {
            throw new IllegalArgumentException("El monto base no puede ser negativo.");
        }
        return baseAmount.divide(
                BigDecimal.ONE.subtract(feePercentage), 2, RoundingMode.HALF_UP);
    }
}
