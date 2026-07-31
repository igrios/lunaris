package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;
import java.util.UUID;
import com.lunaris.ansenuza.domain.model.ReservationSource;

public record CreateReservationRequest(
    UUID passengerId,
    @JsonAlias({"passengerName"}) String fullName,
    @JsonAlias({"passengerPhone"}) String phone,
    @JsonAlias({"date"})
    LocalDate travelDate,
    @JsonAlias({"origin", "locality", "originLocality"}) String pickupLocality,
    String pickupAddress,
    String destination,
    Boolean roundTrip,
    LocalDate returnDate,
    Boolean paymentVerified,
    String notes,
    @JsonAlias({"seatCount", "seats"}) Integer passengerCount,
    String companionNames,
    ReservationSource source
) {
    public CreateReservationRequest(
            UUID passengerId,
            LocalDate travelDate,
            String pickupLocality,
            String pickupAddress,
            String destination,
            Boolean roundTrip,
            LocalDate returnDate,
            Boolean paymentVerified,
            String notes,
            Integer passengerCount,
            String companionNames,
            ReservationSource source) {
        this(passengerId, null, null, travelDate, pickupLocality, pickupAddress, destination,
                roundTrip, returnDate, paymentVerified, notes, passengerCount, companionNames, source);
    }

    public CreateReservationRequest withSource(ReservationSource source) {
        return new CreateReservationRequest(
                passengerId,
                fullName,
                phone,
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
