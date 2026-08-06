package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateFareUseCase {
    FareLocalityView updateFare(UUID fareId, BigDecimal amount);
}
