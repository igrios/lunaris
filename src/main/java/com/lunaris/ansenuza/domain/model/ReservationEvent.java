package com.lunaris.ansenuza.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservation_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // Ej: 'RESERVATION_CREATED', 'RECEIPT_SUBMITTED', 'PAYMENT_CONFIRMED'

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy; // 'BOT', 'ADMIN_MARTIN', 'API'
}