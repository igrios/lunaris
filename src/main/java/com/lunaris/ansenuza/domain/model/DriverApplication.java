package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_applications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverApplication {

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String locality;

    @Column(name = "vehicle_model", nullable = false)
    private String vehicleModel;

    @Column(name = "license_plate", nullable = false)
    private String licensePlate;

    @Column(name = "wants_direct_contact", nullable = false)
    private boolean wantsDirectContact;

    @Column(name = "insurance_file_url", length = 500)
    private String insuranceFileUrl;

    @Column(name = "green_card_file_url", length = 500)
    private String greenCardFileUrl;

    @Column(name = "criminal_record_file_url", length = 500)
    private String criminalRecordFileUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = Status.PENDING;
        }
        if (createdAt == null) {
            createdAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        }
    }
}
