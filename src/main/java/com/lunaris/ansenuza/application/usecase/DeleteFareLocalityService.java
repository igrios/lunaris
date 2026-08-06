package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.FareLocalityInUseException;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFareLocalityService implements DeleteFareLocalityUseCase {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void delete(UUID fareId) {
        var fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new DomainValidationException("La tarifa indicada no existe."));
        String localityName = fare.getLocalityName();
        var locality = localityRepository.findFirstByNameIgnoreCase(localityName)
                .orElseThrow(() -> new DomainValidationException("La tarifa no tiene una localidad válida asociada."));
        if (reservationRepository.existsActiveByLocality(localityName)) {
            throw new FareLocalityInUseException(localityName);
        }

        fareRepository.delete(fare);
        localityRepository.delete(locality);
    }
}
