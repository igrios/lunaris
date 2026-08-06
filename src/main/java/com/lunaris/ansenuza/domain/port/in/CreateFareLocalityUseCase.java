package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;

public interface CreateFareLocalityUseCase {
    FareLocalityView create(String localityName, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount);
}
