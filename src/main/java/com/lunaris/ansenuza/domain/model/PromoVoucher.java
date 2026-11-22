package com.lunaris.ansenuza.domain.model;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promo_vouchers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromoVoucher {
    public enum Status { DISPONIBLE, CANJEADA, ANULADA }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String number;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;
    @Column(nullable = false, length = 100)
    private String firstName;
    @Column(nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, length = 15)
    private String phone;
    @Column(nullable = false, length = 11)
    private String cuil;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;
    @Column(length = 500)
    private String receiptUrl;
    @Column(length = 500)
    private String notes;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime redeemedAt;
    @Column(unique = true)
    private UUID reservationId;

    @Builder
    private PromoVoucher(String number, String firstName, String lastName, String phone, String cuil,
            BigDecimal paidAmount, String receiptUrl, String notes, LocalDateTime createdAt) {
        this.number = number;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.cuil = cuil;
        this.paidAmount = paidAmount;
        this.receiptUrl = receiptUrl;
        this.notes = notes;
        this.createdAt = createdAt;
        this.status = Status.DISPONIBLE;
    }

    @PrePersist
    void ensureId() {
        if (id == null) id = UUID.randomUUID();
    }

    public void requireAvailable() {
        if (status != Status.DISPONIBLE) {
            throw new DomainValidationException("La promo " + number + " ya no está disponible.");
        }
    }

    public void redeem(UUID reservationId, LocalDateTime redeemedAt) {
        requireAvailable();
        this.reservationId = reservationId;
        this.redeemedAt = redeemedAt;
        status = Status.CANJEADA;
    }

    public void cancel() {
        requireAvailable();
        status = Status.ANULADA;
    }

    public String getPassengerName() {
        return firstName + " " + lastName;
    }

    public String getPassengerMessage() {
        return "Tu promo Lunaris fue cargada con el Nro " + number
                + ". Cuando decidas viajar te van a pedir este nro.";
    }
}
