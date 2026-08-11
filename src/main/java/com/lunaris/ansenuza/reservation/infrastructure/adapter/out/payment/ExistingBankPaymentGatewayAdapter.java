package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.payment;

import com.lunaris.ansenuza.application.payment.BankPaymentReservationPort;
import com.lunaris.ansenuza.reservation.application.port.out.PaymentGatewayPort;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import org.springframework.stereotype.Component;

/** Puente temporal hacia la integración bancaria que continúa atendiendo WhatsApp. */
@Component
public class ExistingBankPaymentGatewayAdapter implements PaymentGatewayPort {
    private final BankPaymentReservationPort delegate;
    public ExistingBankPaymentGatewayAdapter(BankPaymentReservationPort delegate) { this.delegate = delegate; }
    @Override public void confirm(Reservation reservation) {
        if (reservation.reservationCode() != null) delegate.confirm(reservation.reservationCode());
    }
}
