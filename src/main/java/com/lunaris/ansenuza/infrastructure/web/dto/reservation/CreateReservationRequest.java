package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReservationRequest(

        UUID passengerId,

        LocalDate travelDate,

        String pickupLocality,

        String pickupAddress,

        String destination,

        Boolean roundTrip,

        LocalDate returnDate,

        Boolean paymentVerified,

        String notes
) {
}