package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.ManualReservationCreated;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ManualReservationChatHandoffService {
    private final ReservationRepository reservations;
    private final ConversationSessionRepository sessions;
    private final LiveChatPort liveChat;

    /** La pausa se confirma o revierte junto con el alta manual. */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void pause(ManualReservationCreated event) {
        var reservation = reservations.findById(event.reservationId()).orElseThrow();
        String phone = reservation.getPassenger().getPhone();
        var session = sessions.findByPhoneNumber(phone).orElseGet(() -> ConversationSession.builder()
                .phoneNumber(phone).currentStep("START").build());
        session.setBotPaused(true);
        session.setManuallyPaused(true);
        session.setLastInteraction(ArgentinaTime.now());
        sessions.saveAndFlush(session);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void refresh(ManualReservationCreated event) {
        liveChat.conversationChanged();
    }
}
