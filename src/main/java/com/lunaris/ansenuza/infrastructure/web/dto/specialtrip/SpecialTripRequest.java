package com.lunaris.ansenuza.infrastructure.web.dto.specialtrip;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SpecialTripRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @Size(max = 100) String origin,
        @Size(max = 100) String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Min(1) Integer maxPassengers,
        @Size(max = 500) String imageUrl,
        boolean active) {
}
