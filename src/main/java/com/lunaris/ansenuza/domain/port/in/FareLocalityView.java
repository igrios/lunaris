package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record FareLocalityView(
        UUID fareId, UUID localityId, String localityName, BigDecimal amount,
        Integer kmsToCordoba, Integer minutesFromOrigin) {
}
