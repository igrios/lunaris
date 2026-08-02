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
import com.lunaris.ansenuza.domain.exception.DomainValidationException;

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

    @Column(name = "vehicle_year")
    private Integer vehicleYear;

    @Column(name = "license_plate")
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

    public void approve() {
        requirePending();
        status = Status.APPROVED;
    }

    public void reject() {
        requirePending();
        status = Status.REJECTED;
    }

    public void updateSubmission(
            String fullName,
            String phone,
            String vehicleModel,
            Integer vehicleYear,
            String licensePlate,
            boolean wantsDirectContact) {
        this.fullName = fullName;
        this.phone = phone;
        this.vehicleModel = vehicleModel;
        this.vehicleYear = vehicleYear;
        this.licensePlate = licensePlate;
        this.wantsDirectContact = wantsDirectContact;
        this.status = Status.PENDING;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void updateDocuments(
            String insuranceFileUrl,
            String greenCardFileUrl,
            String criminalRecordFileUrl) {
        if (insuranceFileUrl != null) {
            this.insuranceFileUrl = insuranceFileUrl;
        }
        if (greenCardFileUrl != null) {
            this.greenCardFileUrl = greenCardFileUrl;
        }
        if (criminalRecordFileUrl != null) {
            this.criminalRecordFileUrl = criminalRecordFileUrl;
        }
    }

    private void requirePending() {
        if (status != Status.PENDING) {
            throw new DomainValidationException(
                    "La solicitud ya fue procesada con estado " + status + ".");
        }
    }
}
