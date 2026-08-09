package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lunaris.ansenuza.infrastructure.web.json.StringOrStringListDeserializer;
import java.time.LocalDate;
import java.util.UUID;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;

public record CreateReservationRequest(
    UUID passengerId,
    @JsonAlias({"passengerName"}) String fullName,
    @JsonAlias({"passengerPhone"}) String phone,
    @JsonAlias({"documentId", "cuil"}) String cuilDni,
    @JsonAlias({"date"})
    LocalDate travelDate,
    @JsonAlias({"origin", "locality", "originLocality"}) String pickupLocality,
    String pickupAddress,
    String destination,
    @JsonAlias({"schedule", "scheduleBlock"}) String departureSchedule,
    Boolean roundTrip,
    LocalDate returnDate,
    Boolean paymentVerified,
    @JsonDeserialize(using = StringOrStringListDeserializer.class) String notes,
    @JsonAlias({"seatCount", "seats"}) Integer passengerCount,
    @JsonDeserialize(using = StringOrStringListDeserializer.class) String companionNames,
    ReservationSource source,
    TripType tripType,
    String promotionCode
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
        this(passengerId, null, null, null, travelDate, pickupLocality, pickupAddress, destination,
                notes != null && notes.contains("08:00") ? "08:00 AM" : "03:00 AM",
                roundTrip, returnDate, paymentVerified, notes, passengerCount, companionNames, source,
                null, null);
    }

    public CreateReservationRequest withSource(ReservationSource source) {
        return new CreateReservationRequest(
                passengerId,
                fullName,
                phone,
                cuilDni,
                travelDate,
                pickupLocality,
                pickupAddress,
                destination,
                departureSchedule,
                roundTrip,
                returnDate,
                paymentVerified,
                notes,
                passengerCount,
                companionNames,
                source,
                tripType,
                promotionCode);
    }
}
