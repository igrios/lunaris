package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentFeeCalculatorTest {

    @Test
    void recargaLaComisionConfiguradaSinReducirElMontoBase() {
        PaymentFeeCalculator calculator = new PaymentFeeCalculator(new BigDecimal("0.06"));

        assertEquals(
                new BigDecimal("1063.83"),
                calculator.calculateFinalAmount(new BigDecimal("1000.00")));
    }

    @Test
    void rechazaPorcentajesFueraDelRangoValido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentFeeCalculator(BigDecimal.ONE));
    }
}
