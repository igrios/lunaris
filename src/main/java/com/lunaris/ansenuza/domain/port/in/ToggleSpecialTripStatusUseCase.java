package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface ToggleSpecialTripStatusUseCase {
    SpecialTrip setActive(Long id, boolean active);
}
