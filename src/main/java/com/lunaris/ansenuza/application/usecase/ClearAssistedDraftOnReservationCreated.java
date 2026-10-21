package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.ManualReservationCreated;
import com.lunaris.ansenuza.domain.repository.AssistedReservationDraftRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ClearAssistedDraftOnReservationCreated {
    private final AssistedReservationDraftRepository drafts;
    private final ReservationRepository reservations;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void clear(ManualReservationCreated event) {
        reservations.findById(event.reservationId()).ifPresent(reservation ->
                drafts.deleteByPhone(reservation.getPassenger().getPhone()));
    }
}
