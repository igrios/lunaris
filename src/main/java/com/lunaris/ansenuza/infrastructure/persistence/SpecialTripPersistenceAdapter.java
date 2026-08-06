package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import com.lunaris.ansenuza.infrastructure.persistence.mapper.SpecialTripPersistenceMapper;
import com.lunaris.ansenuza.infrastructure.persistence.repository.SpecialTripJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpecialTripPersistenceAdapter implements SpecialTripRepositoryPort {
    private final SpecialTripJpaRepository repository;
    private final SpecialTripPersistenceMapper mapper;

    @Override
    public SpecialTrip save(SpecialTrip specialTrip) {
        return mapper.toDomain(repository.save(mapper.toEntity(specialTrip)));
    }

    @Override
    public Optional<SpecialTrip> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SpecialTrip> findAll() {
        return repository.findAllByOrderByStartDateAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SpecialTrip> findActive() {
        return repository.findAllByActiveTrueOrderByStartDateAsc().stream().map(mapper::toDomain).toList();
    }
}
