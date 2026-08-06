package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpecialTripCommand(
        String title, String description, String origin, String destination,
        LocalDate startDate, LocalDate endDate, BigDecimal price,
        Integer maxPassengers, String imageUrl, boolean active) {
}
