package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateLocalityFareService implements UpdateLocalityFareUseCase {
    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;

    @Override
    @Transactional
    public FareLocalityView updateLocalityAndFare(UUID localityId, String name, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount) {
        String normalizedName = validateName(name);
        UpdateFareService.validateAmount(amount);
        validateNonNegative(kmsToCordoba, "Los kilómetros");
        validateNonNegative(minutesFromOrigin, "Los minutos de viaje");

        var locality = localityRepository.findById(localityId)
                .orElseThrow(() -> new DomainValidationException("La localidad indicada no existe."));
        localityRepository.findFirstByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(localityId))
                .ifPresent(existing -> { throw new DomainValidationException("Ya existe una localidad con ese nombre."); });

        String previousName = locality.getName();
        Fare fare = fareRepository.findFirstByLocalityNameIgnoreCase(previousName).orElseGet(() -> Fare.builder()
                .id(UUID.randomUUID()).localityName(normalizedName).amount(amount).build());
        fareRepository.findFirstByLocalityNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(fare.getId()))
                .ifPresent(existing -> { throw new DomainValidationException("Ya existe una tarifa para esa localidad."); });

        locality.setName(normalizedName);
        locality.setKmsToCordoba(kmsToCordoba);
        locality.setMinutesFromOrigin(minutesFromOrigin);
        fare.setLocalityName(normalizedName);
        fare.setAmount(amount);
        localityRepository.save(locality);
        fareRepository.save(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre de la localidad es obligatorio.");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new DomainValidationException("El nombre de la localidad no puede superar los 100 caracteres.");
        }
        return normalized;
    }

    private void validateNonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new DomainValidationException(field + " no pueden ser negativos.");
        }
    }
}
