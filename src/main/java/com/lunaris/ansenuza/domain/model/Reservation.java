package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

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

    @Column(name = "payment_verified", nullable = false)
    private Boolean paymentVerified;

    @Column(name = "notes")
    private String notes;
}