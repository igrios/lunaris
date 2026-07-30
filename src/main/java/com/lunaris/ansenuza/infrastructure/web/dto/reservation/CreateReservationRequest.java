package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;
import java.util.UUID;
import com.lunaris.ansenuza.domain.model.ReservationSource;

public record CreateReservationRequest(
    UUID passengerId,
    LocalDate travelDate,
    String pickupLocality,
    String pickupAddress,
    String destination,
    Boolean roundTrip,
    LocalDate returnDate,
    Boolean paymentVerified,
    String notes,
    Integer passengerCount, // 🌟 ¡Asegurate de que este campo esté acá escrito tal cual!
    String companionNames,
    ReservationSource source
) {
    public CreateReservationRequest withSource(ReservationSource source) {
        return new CreateReservationRequest(
                passengerId,
                travelDate,
                pickupLocality,
                pickupAddress,
                destination,
                roundTrip,
                returnDate,
                paymentVerified,
                notes,
                passengerCount,
                companionNames,
                source);
    }
}
