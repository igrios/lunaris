package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSpecialTripService implements UpdateSpecialTripUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip update(Long id, SpecialTripCommand command) {
        SpecialTrip current = repository.findById(id).orElseThrow(() -> new SpecialTripNotFoundException(id));
        return repository.save(current.update(command.title(), command.description(), command.origin(),
                command.destination(), command.startDate(), command.endDate(), command.price(),
                command.maxPassengers(), command.imageUrl(), command.active()));
    }
}
