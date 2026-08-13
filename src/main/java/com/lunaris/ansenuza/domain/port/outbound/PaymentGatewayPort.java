package com.lunaris.ansenuza.domain.port.outbound;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.payment.PaymentPreference;
import java.math.BigDecimal;

public interface PaymentGatewayPort {

    PaymentPreference createPaymentPreference(
            Reservation reservation, BigDecimal baseAmount);
}
