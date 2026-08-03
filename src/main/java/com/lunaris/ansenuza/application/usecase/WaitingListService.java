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
            WaitingListEntry.WAITING, WaitingListEntry.CONTACTED,
            WaitingListEntry.CONFIRMED, WaitingListEntry.CANCELLED);

    private final WaitingListRepository repository;

    @Transactional(readOnly = true)
    public List<WaitingListEntry> find(LocalDate travelDate, String status) {
        if (travelDate != null && status != null && !status.isBlank()) {
            return repository.findByTravelDateAndStatusOrderByCreatedAtAsc(
                    travelDate, normalizeStatus(status));
        }
        return repository.findAllByOrderByCreatedAtDesc();
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
