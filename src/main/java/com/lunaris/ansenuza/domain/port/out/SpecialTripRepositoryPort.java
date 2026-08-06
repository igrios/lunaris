package com.lunaris.ansenuza.domain.port.out;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import java.util.List;
import java.util.Optional;

public interface SpecialTripRepositoryPort {
    SpecialTrip save(SpecialTrip specialTrip);
    Optional<SpecialTrip> findById(Long id);
    List<SpecialTrip> findAll();
    List<SpecialTrip> findActive();
}
