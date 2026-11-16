package com.lunaris.ansenuza.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** El caso de uso debe exigir agreedAmount cuando la modalidad sea AGREED_TOTAL. */
public record PricingChangeRequest(
        @NotNull PricingMode mode,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal agreedAmount) {}
