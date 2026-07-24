package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
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
    public static final String RETURN_NO_ID = "return_no_ID";
    private static final LocalDate OPEN_RETURN_SENTINEL_DATE = LocalDate.of(2099, 12, 31);

    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;

    @Transactional
    public void processReturnDecision(String passengerPhone, String decisionId) {
        if (RETURN_YES_ID.equals(decisionId)) {
            return;
        }

        Reservation returnReservation = findTodayReturnReservation(passengerPhone);

        if (RETURN_LATER_ID.equals(decisionId)) {
            returnReservation.setTravelStatus(TravelStatus.OPEN_RETURN);
            returnReservation.setTravelDate(OPEN_RETURN_SENTINEL_DATE);
            returnReservation.setNotes(appendNote(returnReservation.getNotes(),
                    "Vuelta marcada como abierta por decisión del pasajero."));
            reservationRepository.saveAndFlush(returnReservation);
            return;
        }

        if (RETURN_NO_ID.equals(decisionId)) {
            returnReservation.setTravelStatus(TravelStatus.CANCELED);
            returnReservation.setStatus("CANCELLED");
            returnReservation.setNotes(appendNote(returnReservation.getNotes(),
                    "Vuelta cancelada por decisión del pasajero; reintegro acreditado en cuenta corriente."));

            Passenger passenger = returnReservation.getPassenger();
            BigDecimal currentBalance = passenger.getCurrentBalance() != null
                    ? passenger.getCurrentBalance()
                    : BigDecimal.ZERO;
            BigDecimal returnFare = returnReservation.getAmount() != null
                    ? returnReservation.getAmount()
                    : BigDecimal.ZERO;

            passenger.setCurrentBalance(currentBalance.add(returnFare));
            reservationRepository.saveAndFlush(returnReservation);
            passengerRepository.saveAndFlush(passenger);
            return;
        }

        throw new IllegalArgumentException("Decisión de vuelta no soportada: " + decisionId);
    }

    public boolean isReturnDecision(String decisionId) {
        return RETURN_YES_ID.equals(decisionId)
                || RETURN_LATER_ID.equals(decisionId)
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

        throw new IllegalArgumentException("No hay una vuelta activa para confirmar hoy.");
    }

    private String appendNote(String currentNotes, String newNote) {
        if (currentNotes == null || currentNotes.isBlank()) {
            return newNote;
        }
        return currentNotes + " | " + newNote;
    }
}
