package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface CreateSpecialTripUseCase {
    SpecialTrip create(SpecialTripCommand command);
}
