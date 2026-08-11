package com.lunaris.ansenuza.reservation.application.service;

import com.lunaris.ansenuza.reservation.application.port.in.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.reservation.application.port.in.CreateReservationUseCase;
import com.lunaris.ansenuza.reservation.application.port.out.PaymentGatewayPort;
import com.lunaris.ansenuza.reservation.application.port.out.ReservationRepositoryPort;
import com.lunaris.ansenuza.reservation.domain.exception.ReservationNotFoundException;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationApplicationService implements CreateReservationUseCase, ConfirmPaymentUseCase {
    private final ReservationRepositoryPort repository;
    private final PaymentGatewayPort paymentGateway;
    private final Clock clock;

    public ReservationApplicationService(ReservationRepositoryPort repository, PaymentGatewayPort paymentGateway) {
        this(repository, paymentGateway, Clock.systemDefaultZone());
    }

    ReservationApplicationService(ReservationRepositoryPort repository, PaymentGatewayPort paymentGateway, Clock clock) {
        this.repository = repository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    @Override @Transactional
    public Reservation create(Reservation reservation) { return repository.save(reservation); }

    @Override @Transactional
    public Reservation confirmPayment(UUID reservationId) {
        Reservation reservation = repository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        paymentGateway.confirm(reservation);
        reservation.confirmPayment(LocalDateTime.now(clock));
        return repository.save(reservation);
    }
}
