package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateLocalityFareUseCase {
    FareLocalityView updateLocalityAndFare(UUID localityId, String name, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount);
}
