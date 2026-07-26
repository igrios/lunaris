package com.lunaris.ansenuza.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DriverApplicationRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Size(max = 120) String vehicleModel,
        @NotBlank @Size(max = 20) String licensePlate) {
}
