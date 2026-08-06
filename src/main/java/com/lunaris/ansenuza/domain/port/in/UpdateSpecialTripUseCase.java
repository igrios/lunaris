package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface UpdateSpecialTripUseCase {
    SpecialTrip update(Long id, SpecialTripCommand command);
}
