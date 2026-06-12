package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "pickup_locality", nullable = false)
    private String pickupLocality;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "round_trip")
    private Boolean roundTrip;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "extra_amount")
    private BigDecimal extraAmount;

    @Column(name = "payment_verified", nullable = false)
    private Boolean paymentVerified;

    @Column(name = "notes")
    private String notes;
}
