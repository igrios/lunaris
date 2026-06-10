package com.lunaris.ansenuza.infrastructure.web.dto;
import jakarta.validation.constraints.NotBlank;

public record CreatePassengerRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String cuil,

        String phone,

        String address,

        String locality
) {
}