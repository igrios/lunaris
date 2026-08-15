package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCancellationService {

    public static final String RETURN_YES_ID = "return_yes_ID";
    public static final String RETURN_LATER_ID = "return_later_ID";
    public static final String RETURN_POSTPONE_ID = "return_postpone";
    public static final String RETURN_NO_ID = "return_no_ID";
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @Transactional
    public void processReturnDecision(String passengerPhone, String decisionId) {
        if (RETURN_YES_ID.equals(decisionId)) {
            return;
        }

        Reservation returnReservation = findTodayReturnReservation(passengerPhone);
        if ("COMPLETED".equalsIgnoreCase(returnReservation.getStatus())
                || returnReservation.getTravelStatus() == TravelStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }

        if (isPostponeDecision(decisionId)) {
            returnReservation.setTravelStatus(TravelStatus.OPEN_RETURN);
            returnReservation.setReturnAuditSentAt(
                    com.lunaris.ansenuza.shared.ArgentinaTime.now());
            returnReservation.setNotes(appendNote(returnReservation.getNotes(),
                    "Vuelta marcada como abierta por decisión del pasajero."));
            reservationRepository.saveAndFlush(returnReservation);
            return;
        }

        if (RETURN_NO_ID.equals(decisionId)) {
            reservationService.cancelReservation(
                    returnReservation.getId(), "PASSENGER_RETURN_DECISION");
            return;
        }

        throw new IllegalArgumentException("Decisión de vuelta no soportada: " + decisionId);
    }

    public boolean isReturnDecision(String decisionId) {
        return RETURN_YES_ID.equals(decisionId)
                || RETURN_LATER_ID.equals(decisionId)
                || RETURN_POSTPONE_ID.equals(decisionId)
                || RETURN_NO_ID.equals(decisionId);
    }

    private Reservation findTodayReturnReservation(String passengerPhone) {
        String normalizedPhone = passengerPhone != null ? passengerPhone.trim() : "";
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();

        List<Reservation> scheduledReturns =
                reservationRepository.findActiveReturnReservationsByPassengerPhoneAndDate(normalizedPhone, today);
        if (!scheduledReturns.isEmpty()) {
            return scheduledReturns.get(0);
        }

        List<Reservation> outboundReservations =
                reservationRepository.findRealizedOutboundReservationsByPassengerPhoneAndReturnDate(
                        normalizedPhone, today, TravelStatus.REALIZED);
        if (!outboundReservations.isEmpty()) {
            return outboundReservations.get(0);
        }

        List<Reservation> openReturns =
                reservationRepository.findOpenReturnReservationsByPassengerPhone(normalizedPhone);
        if (!openReturns.isEmpty()) {
            return openReturns.get(0);
        }

        throw new IllegalArgumentException("No hay una vuelta activa para confirmar hoy.");
    }

    private boolean isPostponeDecision(String decisionId) {
        return RETURN_POSTPONE_ID.equals(decisionId) || RETURN_LATER_ID.equals(decisionId);
    }

    private String appendNote(String currentNotes, String newNote) {
        if (currentNotes == null || currentNotes.isBlank()) {
            return newNote;
        }
        return currentNotes + " | " + newNote;
    }
}
