package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.mapper;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.domain.model.ReservationStatus;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationEntity toEntity(Reservation d) {
        ReservationEntity e = new ReservationEntity();
        e.setId(d.id()); e.setPassengerId(d.passengerId()); e.setDriverId(d.driverId()); e.setTravelDate(d.travelDate());
        e.setPickupLocality(d.pickupLocality()); e.setPickupAddress(d.pickupAddress()); e.setDestination(d.destination());
        e.setAmount(d.amount()); e.setRoundTrip(d.roundTrip()); e.setTripType(d.tripType()); e.setReturnDate(d.returnDate());
        e.setExtraAmount(d.extraAmount()); e.setPromotionCode(d.promotionCode()); e.setPromotionId(d.promotionId());
        e.setPromotionDiscountPercentage(d.promotionDiscountPercentage()); e.setDiscountAmount(d.discountAmount());
        e.setPaymentVerified(d.paymentVerified()); e.setStatus(d.status().name()); e.setSource(d.source());
        e.setTravelStatus(d.travelStatus()); e.setNotes(d.notes()); e.setPaymentReceiptUrl(d.paymentReceiptUrl());
        e.setWaitingListEntryId(d.waitingListEntryId()); e.setPaymentConfirmedAt(d.paymentConfirmedAt());
        e.setCompanionNames(d.companionNames()); e.setPassengerCount(d.passengerCount());
        e.setReturnedPassengerCount(d.returnedPassengerCount()); e.setReservationCode(d.reservationCode());
        e.setBookingGroupCode(d.bookingGroupCode()); e.setRouteDirection(d.routeDirection());
        e.setReturnAuditSentAt(d.returnAuditSentAt()); e.setCreatedAt(d.createdAt()); e.setUpdatedAt(d.updatedAt());
        e.setDepartureSchedule(d.departureSchedule()); e.setRouteSequence(d.routeSequence()); e.setRequiresInvoice(d.requiresInvoice());
        return e;
    }

    public Reservation toDomain(ReservationEntity e) {
        return Reservation.builder(e.getPassengerId(), e.getPickupLocality(), e.getDestination())
                .id(e.getId()).driverId(e.getDriverId()).travelDate(e.getTravelDate()).pickupAddress(e.getPickupAddress())
                .amount(e.getAmount()).roundTrip(e.getRoundTrip()).tripType(e.getTripType()).returnDate(e.getReturnDate())
                .extraAmount(e.getExtraAmount()).promotionCode(e.getPromotionCode()).promotionId(e.getPromotionId())
                .promotionDiscountPercentage(e.getPromotionDiscountPercentage()).discountAmount(e.getDiscountAmount())
                .paymentVerified(Boolean.TRUE.equals(e.getPaymentVerified())).status(ReservationStatus.fromPersistenceValue(e.getStatus()))
                .source(e.getSource()).travelStatus(e.getTravelStatus()).notes(e.getNotes()).paymentReceiptUrl(e.getPaymentReceiptUrl())
                .waitingListEntryId(e.getWaitingListEntryId()).paymentConfirmedAt(e.getPaymentConfirmedAt())
                .companionNames(e.getCompanionNames()).passengerCount(e.getPassengerCount() == null ? 1 : e.getPassengerCount())
                .returnedPassengerCount(e.getReturnedPassengerCount() == null ? 0 : e.getReturnedPassengerCount())
                .reservationCode(e.getReservationCode()).bookingGroupCode(e.getBookingGroupCode()).routeDirection(e.getRouteDirection())
                .returnAuditSentAt(e.getReturnAuditSentAt()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .departureSchedule(e.getDepartureSchedule()).routeSequence(e.getRouteSequence())
                .requiresInvoice(Boolean.TRUE.equals(e.getRequiresInvoice())).build();
    }
}
