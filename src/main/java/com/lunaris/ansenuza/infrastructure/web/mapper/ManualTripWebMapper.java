package com.lunaris.ansenuza.infrastructure.web.mapper;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.ManualReservationOptions;

public final class ManualTripWebMapper {
    private ManualTripWebMapper() {}

    public static void applyOptions(Reservation reservation, ManualReservationOptions options) {
        reservation.setTripCategory(options.getTripCategory());
        reservation.setOriginCustom(options.getOriginCustom());
        reservation.setDestinationCustom(options.getDestinationCustom());
        reservation.setCustomPrice(options.getCustomPrice());
        reservation.setPaymentStatus(options.getPaymentStatus());
        reservation.setAmount(options.getAmount());
        reservation.setExtraAmount(options.getExtraAmount());
        reservation.setDiscountAmount(options.getDiscountAmount());
        reservation.setCompanionNames(options.getCompanionNames());
        reservation.setRouteDirection(options.getRouteDirection());
    }

    public static Reservation toReservation(CreateReservationForm form) {
        boolean roundTrip = Boolean.TRUE.equals(form.getRoundTrip());
        Reservation reservation = Reservation.builder()
                .passenger(Passenger.builder().firstName(form.getFirstName()).lastName(form.getLastName())
                        .phone(form.getPhone()).cuil(form.getCuil()).build())
                .travelDate(form.getTravelDate()).pickupLocality(form.getPickupLocality())
                .pickupAddress(form.getPickupAddress()).destination(form.getDestination())
                .passengerCount(form.getPassengerCount()).roundTrip(roundTrip).returnDate(form.getReturnDate())
                .tripType(roundTrip ? (form.getReturnDate() == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP)
                        : TripType.ONE_WAY)
                .departureSchedule(form.getDepartureSchedule()).requiresInvoice(form.getRequiresInvoice())
                .notes(form.getNotes()).paymentVerified(Boolean.TRUE.equals(form.getPaymentVerified())).build();
        applyOptions(reservation, form);
        return reservation;
    }
}
