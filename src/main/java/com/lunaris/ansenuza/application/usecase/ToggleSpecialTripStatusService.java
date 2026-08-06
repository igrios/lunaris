package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.ToggleSpecialTripStatusUseCase;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleSpecialTripStatusService implements ToggleSpecialTripStatusUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip setActive(Long id, boolean active) {
        SpecialTrip current = repository.findById(id).orElseThrow(() -> new SpecialTripNotFoundException(id));
        return repository.save(current.update(current.title(), current.description(), current.origin(),
                current.destination(), current.startDate(), current.endDate(), current.price(),
                current.maxPassengers(), current.imageUrl(), active));
    }
}
