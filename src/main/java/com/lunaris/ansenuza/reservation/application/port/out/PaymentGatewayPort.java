package com.lunaris.ansenuza.reservation.application.port.out;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;

public interface PaymentGatewayPort {
    void confirm(Reservation reservation);
}
