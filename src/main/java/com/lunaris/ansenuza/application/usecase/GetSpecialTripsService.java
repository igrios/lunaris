package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSpecialTripsService implements GetSpecialTripsQuery {
    private final SpecialTripRepositoryPort repository;

    @Override
    public List<SpecialTrip> getAll() {
        return repository.findAll();
    }

    @Override
    public List<SpecialTrip> getActive() {
        return repository.findActive();
    }
}
