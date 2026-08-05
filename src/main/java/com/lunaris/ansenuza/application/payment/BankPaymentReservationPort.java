package com.lunaris.ansenuza.application.payment;

import java.util.Optional;

public interface BankPaymentReservationPort {
    Optional<ReservationPaymentCandidate> findByReservationCode(String reservationCode);

    void confirm(String reservationCode);
}
