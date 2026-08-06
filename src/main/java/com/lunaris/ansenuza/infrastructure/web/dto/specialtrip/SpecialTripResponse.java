package com.lunaris.ansenuza.infrastructure.web.dto.specialtrip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SpecialTripResponse(
        Long id, String title, String description, String origin, String destination,
        LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
        String imageUrl, boolean active, LocalDateTime createdAt) {
}
