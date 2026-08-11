package com.lunaris.ansenuza.reservation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Agregado de reserva puro. No conoce HTTP, Spring ni persistencia. */
public final class Reservation {
    private UUID id;
    private final UUID passengerId;
    private UUID driverId;
    private LocalDate travelDate;
    private final String pickupLocality;
    private String pickupAddress;
    private final String destination;
    private BigDecimal amount;
    private Boolean roundTrip;
    private String tripType;
    private LocalDate returnDate;
    private BigDecimal extraAmount;
    private String promotionCode;
    private UUID promotionId;
    private Integer promotionDiscountPercentage;
    private BigDecimal discountAmount;
    private boolean paymentVerified;
    private ReservationStatus status;
    private String source;
    private String travelStatus;
    private String notes;
    private String paymentReceiptUrl;
    private Long waitingListEntryId;
    private LocalDateTime paymentConfirmedAt;
    private String companionNames;
    private int passengerCount;
    private int returnedPassengerCount;
    private String reservationCode;
    private String bookingGroupCode;
    private String routeDirection;
    private LocalDateTime returnAuditSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String departureSchedule;
    private Integer routeSequence;
    private boolean requiresInvoice;

    private Reservation(Builder builder) {
        id = builder.id;
        passengerId = Objects.requireNonNull(builder.passengerId, "passengerId es obligatorio");
        driverId = builder.driverId;
        travelDate = builder.travelDate;
        pickupLocality = requireText(builder.pickupLocality, "pickupLocality");
        pickupAddress = builder.pickupAddress;
        destination = requireText(builder.destination, "destination");
        amount = nullSafe(builder.amount);
        roundTrip = builder.roundTrip;
        tripType = builder.tripType;
        returnDate = builder.returnDate;
        extraAmount = nullSafe(builder.extraAmount);
        promotionCode = builder.promotionCode;
        promotionId = builder.promotionId;
        promotionDiscountPercentage = builder.promotionDiscountPercentage;
        discountAmount = nullSafe(builder.discountAmount);
        paymentVerified = builder.paymentVerified;
        status = builder.status == null ? ReservationStatus.PENDING : builder.status;
        source = builder.source == null ? "MANUAL" : builder.source;
        travelStatus = builder.travelStatus == null ? "PENDING" : builder.travelStatus;
        notes = builder.notes;
        paymentReceiptUrl = builder.paymentReceiptUrl;
        waitingListEntryId = builder.waitingListEntryId;
        paymentConfirmedAt = builder.paymentConfirmedAt;
        companionNames = builder.companionNames;
        passengerCount = Math.max(1, builder.passengerCount);
        returnedPassengerCount = Math.max(0, builder.returnedPassengerCount);
        reservationCode = builder.reservationCode;
        bookingGroupCode = builder.bookingGroupCode;
        routeDirection = builder.routeDirection;
        returnAuditSentAt = builder.returnAuditSentAt;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        departureSchedule = builder.departureSchedule;
        routeSequence = builder.routeSequence;
        requiresInvoice = builder.requiresInvoice;
    }

    public static Builder builder(UUID passengerId, String pickupLocality, String destination) {
        return new Builder(passengerId, pickupLocality, destination);
    }

    public void confirmPayment(LocalDateTime confirmedAt) {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("No se puede confirmar el pago de una reserva cancelada");
        }
        paymentVerified = true;
        status = ReservationStatus.CONFIRMED;
        paymentConfirmedAt = Objects.requireNonNull(confirmedAt, "confirmedAt es obligatorio");
        requiresInvoice = true;
    }

    public void cancel() {
        if (status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("No se puede cancelar una reserva completada");
        }
        status = ReservationStatus.CANCELLED;
        routeSequence = null;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " es obligatorio");
        return value.trim();
    }
    private static BigDecimal nullSafe(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public UUID id() { return id; }
    public UUID passengerId() { return passengerId; }
    public UUID driverId() { return driverId; }
    public LocalDate travelDate() { return travelDate; }
    public String pickupLocality() { return pickupLocality; }
    public String pickupAddress() { return pickupAddress; }
    public String destination() { return destination; }
    public BigDecimal amount() { return amount; }
    public Boolean roundTrip() { return roundTrip; }
    public String tripType() { return tripType; }
    public LocalDate returnDate() { return returnDate; }
    public BigDecimal extraAmount() { return extraAmount; }
    public String promotionCode() { return promotionCode; }
    public UUID promotionId() { return promotionId; }
    public Integer promotionDiscountPercentage() { return promotionDiscountPercentage; }
    public BigDecimal discountAmount() { return discountAmount; }
    public boolean paymentVerified() { return paymentVerified; }
    public ReservationStatus status() { return status; }
    public String source() { return source; }
    public String travelStatus() { return travelStatus; }
    public String notes() { return notes; }
    public String paymentReceiptUrl() { return paymentReceiptUrl; }
    public Long waitingListEntryId() { return waitingListEntryId; }
    public LocalDateTime paymentConfirmedAt() { return paymentConfirmedAt; }
    public String companionNames() { return companionNames; }
    public int passengerCount() { return passengerCount; }
    public int returnedPassengerCount() { return returnedPassengerCount; }
    public String reservationCode() { return reservationCode; }
    public String bookingGroupCode() { return bookingGroupCode; }
    public String routeDirection() { return routeDirection; }
    public LocalDateTime returnAuditSentAt() { return returnAuditSentAt; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
    public String departureSchedule() { return departureSchedule; }
    public Integer routeSequence() { return routeSequence; }
    public boolean requiresInvoice() { return requiresInvoice; }

    public static final class Builder {
        private UUID id; private final UUID passengerId; private UUID driverId; private LocalDate travelDate;
        private final String pickupLocality; private String pickupAddress; private final String destination;
        private BigDecimal amount; private Boolean roundTrip; private String tripType; private LocalDate returnDate;
        private BigDecimal extraAmount; private String promotionCode; private UUID promotionId;
        private Integer promotionDiscountPercentage; private BigDecimal discountAmount; private boolean paymentVerified;
        private ReservationStatus status; private String source; private String travelStatus; private String notes;
        private String paymentReceiptUrl; private Long waitingListEntryId; private LocalDateTime paymentConfirmedAt;
        private String companionNames; private int passengerCount = 1; private int returnedPassengerCount;
        private String reservationCode; private String bookingGroupCode; private String routeDirection;
        private LocalDateTime returnAuditSentAt; private LocalDateTime createdAt; private LocalDateTime updatedAt;
        private String departureSchedule; private Integer routeSequence; private boolean requiresInvoice = true;
        private Builder(UUID passengerId, String pickupLocality, String destination) { this.passengerId=passengerId; this.pickupLocality=pickupLocality; this.destination=destination; }
        public Builder id(UUID v){id=v;return this;} public Builder driverId(UUID v){driverId=v;return this;}
        public Builder travelDate(LocalDate v){travelDate=v;return this;} public Builder pickupAddress(String v){pickupAddress=v;return this;}
        public Builder amount(BigDecimal v){amount=v;return this;} public Builder roundTrip(Boolean v){roundTrip=v;return this;}
        public Builder tripType(String v){tripType=v;return this;} public Builder returnDate(LocalDate v){returnDate=v;return this;}
        public Builder extraAmount(BigDecimal v){extraAmount=v;return this;} public Builder promotionCode(String v){promotionCode=v;return this;}
        public Builder promotionId(UUID v){promotionId=v;return this;} public Builder promotionDiscountPercentage(Integer v){promotionDiscountPercentage=v;return this;}
        public Builder discountAmount(BigDecimal v){discountAmount=v;return this;} public Builder paymentVerified(boolean v){paymentVerified=v;return this;}
        public Builder status(ReservationStatus v){status=v;return this;} public Builder source(String v){source=v;return this;}
        public Builder travelStatus(String v){travelStatus=v;return this;} public Builder notes(String v){notes=v;return this;}
        public Builder paymentReceiptUrl(String v){paymentReceiptUrl=v;return this;} public Builder waitingListEntryId(Long v){waitingListEntryId=v;return this;}
        public Builder paymentConfirmedAt(LocalDateTime v){paymentConfirmedAt=v;return this;} public Builder companionNames(String v){companionNames=v;return this;}
        public Builder passengerCount(int v){passengerCount=v;return this;} public Builder returnedPassengerCount(int v){returnedPassengerCount=v;return this;}
        public Builder reservationCode(String v){reservationCode=v;return this;} public Builder bookingGroupCode(String v){bookingGroupCode=v;return this;}
        public Builder routeDirection(String v){routeDirection=v;return this;} public Builder returnAuditSentAt(LocalDateTime v){returnAuditSentAt=v;return this;}
        public Builder createdAt(LocalDateTime v){createdAt=v;return this;} public Builder updatedAt(LocalDateTime v){updatedAt=v;return this;}
        public Builder departureSchedule(String v){departureSchedule=v;return this;} public Builder routeSequence(Integer v){routeSequence=v;return this;}
        public Builder requiresInvoice(boolean v){requiresInvoice=v;return this;} public Reservation build(){return new Reservation(this);}
    }
}
