package com.lunaris.ansenuza.reservation.application.port.in;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.util.UUID;

public interface ConfirmPaymentUseCase {
    Reservation confirmPayment(UUID reservationId);
}
