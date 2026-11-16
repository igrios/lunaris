package com.lunaris.ansenuza.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Un returnChange ausente conserva el regreso existente; pricing es opcional. */
public record UpdateReservationRequest(
        @NotNull @PositiveOrZero Integer expectedRevision,
        @NotBlank String operationId,
        @Valid ReturnChangeRequest returnChange,
        @Valid PricingChangeRequest pricing) {}
