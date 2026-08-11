package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence;

import com.lunaris.ansenuza.reservation.application.port.out.ReservationRepositoryPort;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.mapper.ReservationMapper;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.repository.SpringDataReservationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReservationPersistenceAdapter implements ReservationRepositoryPort {
    private final SpringDataReservationRepository repository;
    private final ReservationMapper mapper;
    public ReservationPersistenceAdapter(SpringDataReservationRepository repository, ReservationMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    @Override public Reservation save(Reservation reservation) { return mapper.toDomain(repository.save(mapper.toEntity(reservation))); }
    @Override @Transactional(readOnly = true) public Optional<Reservation> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override @Transactional(readOnly = true) public List<Reservation> findByPickupLocality(String locality) {
        return repository.findByPickupLocalityIgnoreCase(locality).stream().map(mapper::toDomain).toList();
    }
}
