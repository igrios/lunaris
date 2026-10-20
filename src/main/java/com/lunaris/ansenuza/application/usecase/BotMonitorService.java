package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.TripCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class BotMonitorService {
    private final ConversationSessionRepository sessions;
    private final SimpMessagingTemplate messaging;

    @Transactional(readOnly = true)
    public List<ConversationSessionRepository.MonitorRow> rows() {
        return sessions.findMonitorRows();
    }

    public record SpecialTripDetails(String originCustom, String destinationCustom,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate travelDate, Integer passengerCount, BigDecimal customPrice) {}

    @Transactional
    public void updateSpecialTrip(long id, SpecialTripDetails details) {
        validateLocation(details.originCustom());
        validateLocation(details.destinationCustom());
        if (details.passengerCount() != null
                && (details.passengerCount() < 1 || details.passengerCount() > 9)) {
            throw new DomainValidationException("El viaje especial admite de 1 a 9 pasajeros.");
        }
        var price = details.customPrice();
        if (price != null && (price.signum() <= 0
                || price.compareTo(new BigDecimal("99999999.99")) > 0
                || price.stripTrailingZeros().scale() > 2)) {
            throw new DomainValidationException("El precio total debe ser positivo y tener hasta dos decimales.");
        }
        var session = findSession(id);
        session.setTripCategory(TripCategory.SPECIAL);
        session.setOriginCustom(trim(details.originCustom()));
        session.setDestinationCustom(trim(details.destinationCustom()));
        session.setTravelDate(details.travelDate());
        session.setPassengerCount(details.passengerCount());
        session.setCustomPrice(price);
        sessions.saveAndFlush(session);
        refreshAfterCommit();
    }

    @Transactional(readOnly = true)
    public ConversationSession specialTrip(long id) {
        var session = findSession(id);
        if (session.getTripCategory() != TripCategory.SPECIAL) {
            throw new DomainValidationException("La sesión no corresponde a un viaje especial.");
        }
        return session;
    }

    private ConversationSession findSession(long id) {
        return sessions.findById(id)
                .orElseThrow(() -> new DomainValidationException("Sesión no encontrada."));
    }

    private static void validateLocation(String value) {
        if (value != null && value.trim().length() > 100) {
            throw new DomainValidationException("El origen y destino admiten hasta 100 caracteres.");
        }
    }

    private static String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void setPaused(long id, boolean paused) {
        var session = sessions.findById(id)
                .orElseThrow(() -> new DomainValidationException("Sesión no encontrada."));
        session.setBotPaused(paused);
        session.setManuallyPaused(paused);
        sessions.saveAndFlush(session);
        refreshAfterCommit();
    }

    private void refreshAfterCommit() {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() {
                        messaging.convertAndSend("/topic/bot-monitor", Map.of("action", "REFRESH"));
                    }
                });
    }
}
