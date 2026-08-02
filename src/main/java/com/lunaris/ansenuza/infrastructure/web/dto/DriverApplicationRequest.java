package com.lunaris.ansenuza.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DriverApplicationRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 30) String phone,
        @Size(max = 120) String locality,
        @Size(max = 120) String vehicleModel,
        @Positive Integer vehicleYear,
        @Size(max = 20) String licensePlate,
        @Size(max = 500) String greenCardFileUrl,
        @Size(max = 500) String insuranceFileUrl) {

    public DriverApplicationRequest(
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String licensePlate) {
        this(fullName, phone, locality, vehicleModel, vehicleYear, licensePlate, null, null);
    }
}
