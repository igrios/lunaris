package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.payment.BankPaymentReservationPort;
import com.lunaris.ansenuza.application.payment.ReservationPaymentCandidate;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBankPaymentReservationAdapter implements BankPaymentReservationPort {

    private final ReservationRepository repository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public JpaBankPaymentReservationAdapter(
            ReservationRepository repository,
            ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.repository = repository;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    @Override
    public Optional<ReservationPaymentCandidate> findByReservationCode(String reservationCode) {
        String groupCode = groupCode(reservationCode);
        if (groupCode != null) {
            List<Reservation> group = repository.findReservationGroupForUpdate(groupCode);
            return group.stream()
                    .filter(reservation -> reservationCode.equalsIgnoreCase(reservation.getReservationCode()))
                    .findFirst()
                    .map(selected -> new ReservationPaymentCandidate(selected.getId(), expectedTotal(group)));
        }

        return repository.findByReservationCodeForUpdate(reservationCode)
                .map(reservation -> new ReservationPaymentCandidate(
                        reservation.getId(), expectedTotal(List.of(reservation))));
    }

    @Override
    public void confirm(String reservationCode) {
        repository.findByReservationCode(reservationCode)
                .ifPresent(reservation -> confirmPaymentUseCase.execute(reservation.getId()));
    }

    private BigDecimal expectedTotal(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> nullSafe(reservation.getAmount())
                        .add(nullSafe(reservation.getExtraAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullSafe(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String groupCode(String reservationCode) {
        if (reservationCode.endsWith("-IDA")) {
            return reservationCode.substring(0, reservationCode.length() - 4);
        }
        if (reservationCode.endsWith("-VUELTA")) {
            return reservationCode.substring(0, reservationCode.length() - 7);
        }
        return null;
    }
}
