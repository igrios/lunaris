package com.lunaris.ansenuza.application.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record PanelReservationEdit(
        Integer expectedRevision, String operationId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
        @Pattern(regexp = "(?:[01][0-9]|2[0-3]):[0-5][0-9]") String departureSchedule,
        @Size(max = 255) String pickupAddress,
        @Size(max = 500) String notes,
        @Size(max = 100) String origin,
        @Size(max = 100) String destination,
        Integer seats,
        @Size(max = 500) String companionNames,
        ReturnAction returnAction, String returnLegId, ReturnMode returnMode,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
        String returnTime, String returnPreference,
        PricingMode pricingMode, BigDecimal agreedAmount,
        @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal paidAmount) {

    public PanelReservationEdit {
        if (departureSchedule != null && departureSchedule.isBlank()) departureSchedule = null;
    }

    public UpdateReservationRequest request() {
        var change = returnAction == null || returnAction == ReturnAction.KEEP ? null
                : new ReturnChangeRequest(returnAction, returnLegId,
                        returnAction == ReturnAction.REMOVE ? null : returnMode,
                        returnAction == ReturnAction.REMOVE ? null : returnDate,
                        returnAction == ReturnAction.REMOVE || returnMode != ReturnMode.SCHEDULED ? null : returnTime,
                        returnAction == ReturnAction.REMOVE || returnMode != ReturnMode.FLEXIBLE ? null : returnPreference);
        return new UpdateReservationRequest(expectedRevision, operationId, change,
                pricingMode == null ? null : new PricingChangeRequest(pricingMode, agreedAmount));
    }

    public boolean changesCommercialFields() {
        return destination != null || seats != null || companionNames != null
                || paidAmount != null || request().returnChange() != null || pricingMode != null || agreedAmount != null;
    }
}
