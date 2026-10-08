package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssistedChatService {
    private final ConversationSessionRepository sessions;
    private final ReservationRepository reservations;
    private final WhatsAppConversationWindowService window;

    @Transactional(readOnly = true)
    public boolean canSend(String phone) {
        return window.isActive(phone) || sessions.findByPhoneNumber(phone)
                .map(s -> s.isBotPaused() && s.isManuallyPaused()).orElse(false);
    }

    @Transactional(readOnly = true)
    public ConversationSession prefill(String phone) {
        var data = ConversationSession.builder().phoneNumber(phone).passengerCount(1).build();
        var session = sessions.findByPhoneNumber(phone).orElse(null);
        var trip = reservations.findByPassengerPhone(phone).stream()
                .filter(r -> r.getSource() == ReservationSource.MANUAL || r.isSpecialTrip())
                .filter(r -> r.getTravelDate() != null && !r.getTravelDate().isBefore(ArgentinaTime.now().toLocalDate()))
                .filter(r -> !java.util.Set.of("CANCELLED", "CANCELED", "EXPIRED", "REJECTED").contains(r.getStatus() == null ? "" : r.getStatus().toUpperCase(java.util.Locale.ROOT)))
                .filter(r -> r.getTravelStatus() != Reservation.TravelStatus.COMPLETED && r.getTravelStatus() != Reservation.TravelStatus.REALIZED && r.getTravelStatus() != Reservation.TravelStatus.CANCELED)
                .findFirst().orElse(null);
        if (trip != null) {
            data.setPickupLocality(trip.getPickupLocality());
            data.setDestination(trip.getDestination());
            data.setPickupAddress(trip.getPickupAddress());
            data.setPassengerCount(trip.getTotalSeats());
            data.setTravelDate(trip.getTravelDate());
            data.setReturnDate(trip.getReturnDate());
            data.setRoundTrip(trip.getRoundTrip());
            data.setRequiresInvoice(trip.getRequiresInvoice());
            data.setCompanionNames(trip.getCompanionNames());
            data.setScheduleBlock(trip.getDepartureSchedule());
        }
        if (session != null) {
            if (session.getPickupLocality() != null) data.setPickupLocality(session.getPickupLocality());
            if (session.getDestination() != null) data.setDestination(session.getDestination());
            if (session.getPickupAddress() != null) data.setPickupAddress(session.getPickupAddress());
            if (session.getPassengerCount() != null) data.setPassengerCount(session.getPassengerCount());
            if (session.getTravelDate() != null) data.setTravelDate(session.getTravelDate());
            if (session.getReturnDate() != null) data.setReturnDate(session.getReturnDate());
            if (session.getRoundTrip() != null) data.setRoundTrip(session.getRoundTrip());
            if (session.getRequiresInvoice() != null) data.setRequiresInvoice(session.getRequiresInvoice());
            if (session.getCompanionNames() != null) data.setCompanionNames(session.getCompanionNames());
            if (session.getScheduleBlock() != null) data.setScheduleBlock(session.getScheduleBlock());
            data.setPassengerName(session.getPassengerName());
            data.setCuil(session.getCuil());
        }
        return data;
    }
}
