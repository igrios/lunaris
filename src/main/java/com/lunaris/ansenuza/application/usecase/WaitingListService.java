package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListService {

    private static final Set<String> VALID_STATUSES = Set.of(
            WaitingListEntry.PENDING, "PENDIENTE", "NEW",
            WaitingListEntry.WAITING, WaitingListEntry.CONTACTED,
            WaitingListEntry.CONFIRMED, WaitingListEntry.CANCELLED,
            WaitingListEntry.NOTIFIED, WaitingListEntry.AWAITING_PAYMENT,
            WaitingListEntry.CONVERTED);

    private final WaitingListRepository repository;

    @Transactional(readOnly = true)
    public List<WaitingListEntry> find(LocalDate travelDate, String status) {
        if (status != null && !status.isBlank()) {
            String normalizedStatus = normalizeStatus(status);
            return travelDate == null
                    ? repository.findByNormalizedStatusOrderByCreatedAtDesc(normalizedStatus)
                    : repository.findByTravelDateAndNormalizedStatusOrderByCreatedAtAsc(
                            travelDate, normalizedStatus);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<WaitingListEntry> findWaiting(LocalDate travelDate) {
        return travelDate == null
                ? repository.findAllActiveWaitingOrderByCreatedAtDesc()
                : repository.findActiveWaitingForDateIncludingNull(travelDate);
    }

    @Transactional(readOnly = true)
    public List<WaitingListEntry> findAllActiveWaiting() {
        return repository.findAllActiveWaitingOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countAllActiveWaiting() {
        return repository.countAllActiveWaiting();
    }

    @Transactional
    public WaitingListEntry create(String phoneNumber, String passengerName,
            LocalDate travelDate, String pickupLocality, String destination,
            Integer passengerCount, String notes, String eventType) {
        return repository.saveAndFlush(WaitingListEntry.builder()
                .phoneNumber(requireText(phoneNumber, "teléfono del pasajero"))
                .passengerName(requireText(passengerName, "nombre del pasajero"))
                .travelDate(travelDate)
                .pickupLocality(requireText(pickupLocality, "localidad de origen"))
                .destination(requireText(destination, "destino"))
                .passengerCount(passengerCount == null ? 1 : Math.max(1, passengerCount))
                .notes(notes)
                .eventType(eventType)
                .status(WaitingListEntry.PENDING)
                .build());
    }

    @Transactional
    public WaitingListEntry join(ConversationSession session) {
        if (session.getTravelDate() == null) {
            throw new DomainValidationException(
                    "Falta la fecha de viaje para ingresar a lista de espera.");
        }
        return repository.saveAndFlush(WaitingListEntry.builder()
                .phoneNumber(session.getPhoneNumber())
                .passengerName(requireText(session.getPassengerName(), "nombre del pasajero"))
                .travelDate(session.getTravelDate())
                .pickupLocality(requireText(session.getPickupLocality(), "localidad de origen"))
                .destination(requireText(session.getDestination(), "destino"))
                .passengerCount(session.getPassengerCount() == null ? 1 : session.getPassengerCount())
                .status(WaitingListEntry.WAITING)
                .build());
    }

    @Transactional
    public WaitingListEntry updateStatus(Long id, String status) {
        WaitingListEntry entry = repository.findById(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        entry.setStatus(normalizeStatus(status));
        return repository.saveAndFlush(entry);
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new DomainValidationException("Estado de lista de espera inválido.");
        }
        return normalized;
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Falta " + field + " para ingresar a lista de espera.");
        }
        return value.trim();
    }
}
