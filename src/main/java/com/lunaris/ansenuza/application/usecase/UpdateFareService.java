package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.UpdateFareUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFareService implements UpdateFareUseCase {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;

    @Override
    @Transactional
    public FareLocalityView updateFare(UUID fareId, BigDecimal amount) {
        validateAmount(amount);
        var fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new DomainValidationException("La tarifa indicada no existe."));
        var locality = localityRepository.findFirstByNameIgnoreCase(fare.getLocalityName())
                .orElseThrow(() -> new DomainValidationException("La tarifa no tiene una localidad válida asociada."));
        fare.setAmount(amount);
        fareRepository.save(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }

    static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainValidationException("La tarifa debe ser mayor a cero.");
        }
    }
}
