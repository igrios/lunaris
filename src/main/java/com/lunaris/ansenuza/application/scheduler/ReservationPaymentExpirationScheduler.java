package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.application.usecase.ExpireReservationPaymentUseCase;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentExpirationScheduler {

    private final ReservationRepository reservationRepository;
    private final ExpireReservationPaymentUseCase expirationUseCase;

    @Scheduled(fixedDelayString = "${lunaris.reservations.expiration-scan-ms:60000}")
    public void expirePendingPayments() {
        var now = ArgentinaTime.now();
        int expired = reservationRepository
                .findExpiredPaymentCandidateIds(now, PageRequest.of(0, 100))
                .stream()
                .mapToInt(id -> expirationUseCase.execute(id, now))
                .sum();
        if (expired > 0) log.info("Reservas expiradas por TTL de pago: {}", expired);
    }
}
