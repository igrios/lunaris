package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@RequiredArgsConstructor
public class CreateFareLocalityService implements CreateFareLocalityUseCase {
    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FareLocalityView create(String localityName, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount) {
        String normalizedName = FareLocalityValidation.localityName(localityName);
        FareLocalityValidation.amount(amount);
        FareLocalityValidation.nonNegative(kmsToCordoba, "Los kilómetros");
        FareLocalityValidation.nonNegative(minutesFromOrigin, "Los minutos de viaje");
        int effectiveKmsToCordoba = kmsToCordoba == null ? 0 : kmsToCordoba;
        int effectiveMinutesFromOrigin = minutesFromOrigin == null ? 0 : minutesFromOrigin;
        if (localityRepository.findFirstByNameIgnoreCase(normalizedName).isPresent()) {
            throw new DomainValidationException("Ya existe una localidad con ese nombre.");
        }
        if (fareRepository.findFirstByLocalityNameIgnoreCase(normalizedName).isPresent()) {
            throw new DomainValidationException("Ya existe una tarifa para esa localidad.");
        }

        Locality locality = Locality.builder().id(UUID.randomUUID()).name(normalizedName)
                .kmsToCordoba(effectiveKmsToCordoba).minutesFromOrigin(effectiveMinutesFromOrigin).build();
        Fare fare = Fare.builder().id(UUID.randomUUID()).localityName(normalizedName).amount(amount).build();
        locality = localityRepository.saveAndFlush(locality);
        fare = fareRepository.saveAndFlush(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }
}
