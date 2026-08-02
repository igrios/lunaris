package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.TripType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingVerificationData(
        LocalDate travelDate,
        String scheduleBlock,
        String pickupLocality,
        String destination,
        Integer passengerCount,
        TripType tripType,
        BigDecimal totalAmount) {
}
