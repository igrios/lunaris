package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Representación exclusiva de persistencia de la tabla productiva existente. */
@Entity(name = "HexagonalReservationEntity")
@Table(name = "reservations")
public class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "passenger_id", nullable = false) private UUID passengerId;
    @Column(name = "driver_id") private UUID driverId;
    @Column(name = "travel_date") private LocalDate travelDate;
    @Column(name = "pickup_locality", nullable = false) private String pickupLocality;
    @Column(name = "pickup_address") private String pickupAddress;
    @Column(name = "destination", nullable = false) private String destination;
    @Column(name = "amount") private BigDecimal amount;
    @Column(name = "round_trip") private Boolean roundTrip;
    @Column(name = "trip_type", length = 50) private String tripType;
    @Column(name = "return_date") private LocalDate returnDate;
    @Column(name = "extra_amount") private BigDecimal extraAmount;
    @Column(name = "promotion_code", length = 4) private String promotionCode;
    @Column(name = "promotion_id") private UUID promotionId;
    @Column(name = "promotion_discount_percentage") private Integer promotionDiscountPercentage;
    @Column(name = "discount_amount", nullable = false) private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "payment_verified", nullable = false) private Boolean paymentVerified;
    @Column(name = "status") private String status;
    @Column(name = "source", nullable = false, length = 20) private String source;
    @Column(name = "travel_status", nullable = false, length = 20) private String travelStatus;
    @Column(name = "notes") private String notes;
    @Column(name = "payment_receipt_url") private String paymentReceiptUrl;
    @Column(name = "waiting_list_entry_id") private Long waitingListEntryId;
    @Column(name = "payment_confirmed_at") private LocalDateTime paymentConfirmedAt;
    @Column(name = "companion_names", length = 500) private String companionNames;
    @Column(name = "passenger_count") private Integer passengerCount;
    @Column(name = "returned_passenger_count", nullable = false) private Integer returnedPassengerCount;
    @Column(name = "reservation_code", length = 20) private String reservationCode;
    @Column(name = "booking_group_code", length = 40) private String bookingGroupCode;
    @Column(name = "route_direction", length = 16) private String routeDirection;
    @Column(name = "return_audit_sent_at") private LocalDateTime returnAuditSentAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "departure_schedule") private String departureSchedule;
    @Column(name = "route_sequence") private Integer routeSequence;
    @Column(name = "requires_invoice", nullable = false) private Boolean requiresInvoice;

    public ReservationEntity() { }

    @PrePersist
    void initializeDefaults() {
        if (id == null) id = UUID.randomUUID();
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (returnedPassengerCount == null || returnedPassengerCount < 0) returnedPassengerCount = 0;
        if (passengerCount == null || passengerCount < 1) passengerCount = 1;
        if (paymentVerified == null) paymentVerified = false;
        if (source == null) source = "MANUAL";
        if (travelStatus == null) travelStatus = "PENDING";
        if (requiresInvoice == null) requiresInvoice = true;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getPassengerId(){return passengerId;} public void setPassengerId(UUID v){passengerId=v;}
    public UUID getDriverId(){return driverId;} public void setDriverId(UUID v){driverId=v;}
    public LocalDate getTravelDate(){return travelDate;} public void setTravelDate(LocalDate v){travelDate=v;}
    public String getPickupLocality(){return pickupLocality;} public void setPickupLocality(String v){pickupLocality=v;}
    public String getPickupAddress(){return pickupAddress;} public void setPickupAddress(String v){pickupAddress=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public Boolean getRoundTrip(){return roundTrip;} public void setRoundTrip(Boolean v){roundTrip=v;}
    public String getTripType(){return tripType;} public void setTripType(String v){tripType=v;}
    public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;}
    public BigDecimal getExtraAmount(){return extraAmount;} public void setExtraAmount(BigDecimal v){extraAmount=v;}
    public String getPromotionCode(){return promotionCode;} public void setPromotionCode(String v){promotionCode=v;}
    public UUID getPromotionId(){return promotionId;} public void setPromotionId(UUID v){promotionId=v;}
    public Integer getPromotionDiscountPercentage(){return promotionDiscountPercentage;} public void setPromotionDiscountPercentage(Integer v){promotionDiscountPercentage=v;}
    public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;}
    public Boolean getPaymentVerified(){return paymentVerified;} public void setPaymentVerified(Boolean v){paymentVerified=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getSource(){return source;} public void setSource(String v){source=v;}
    public String getTravelStatus(){return travelStatus;} public void setTravelStatus(String v){travelStatus=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public String getPaymentReceiptUrl(){return paymentReceiptUrl;} public void setPaymentReceiptUrl(String v){paymentReceiptUrl=v;}
    public Long getWaitingListEntryId(){return waitingListEntryId;} public void setWaitingListEntryId(Long v){waitingListEntryId=v;}
    public LocalDateTime getPaymentConfirmedAt(){return paymentConfirmedAt;} public void setPaymentConfirmedAt(LocalDateTime v){paymentConfirmedAt=v;}
    public String getCompanionNames(){return companionNames;} public void setCompanionNames(String v){companionNames=v;}
    public Integer getPassengerCount(){return passengerCount;} public void setPassengerCount(Integer v){passengerCount=v;}
    public Integer getReturnedPassengerCount(){return returnedPassengerCount;} public void setReturnedPassengerCount(Integer v){returnedPassengerCount=v;}
    public String getReservationCode(){return reservationCode;} public void setReservationCode(String v){reservationCode=v;}
    public String getBookingGroupCode(){return bookingGroupCode;} public void setBookingGroupCode(String v){bookingGroupCode=v;}
    public String getRouteDirection(){return routeDirection;} public void setRouteDirection(String v){routeDirection=v;}
    public LocalDateTime getReturnAuditSentAt(){return returnAuditSentAt;} public void setReturnAuditSentAt(LocalDateTime v){returnAuditSentAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public String getDepartureSchedule(){return departureSchedule;} public void setDepartureSchedule(String v){departureSchedule=v;}
    public Integer getRouteSequence(){return routeSequence;} public void setRouteSequence(Integer v){routeSequence=v;}
    public Boolean getRequiresInvoice(){return requiresInvoice;} public void setRequiresInvoice(Boolean v){requiresInvoice=v;}
}
