package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "waiting_list_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingListEntry {

    public static final String PENDING = "PENDING";
    public static final String WAITING = "WAITING";
    public static final String CONTACTED = "CONTACTED";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";
    public static final String NOTIFIED = "NOTIFIED";
    public static final String AWAITING_PAYMENT = "AWAITING_PAYMENT";
    public static final String CONVERTED = "CONVERTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(name = "travel_date")
    private LocalDate travelDate;

    @Column(name = "pickup_locality", nullable = false, length = 100)
    private String pickupLocality;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String notes;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (passengerCount == null || passengerCount < 1) passengerCount = 1;
        if (status == null || status.isBlank()) status = WAITING;
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
