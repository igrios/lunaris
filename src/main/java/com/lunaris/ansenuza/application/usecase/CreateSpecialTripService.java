package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSpecialTripService implements CreateSpecialTripUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip create(SpecialTripCommand command) {
        SpecialTrip trip = SpecialTrip.create(command.title(), command.description(), command.origin(),
                command.destination(), command.startDate(), command.endDate(), command.price(),
                command.maxPassengers(), command.imageUrl(), command.active(), ArgentinaTime.now());
        return repository.save(trip);
    }
}
