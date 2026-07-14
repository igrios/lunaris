package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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

    // 🛠️ CORRECCIÓN CRÍTICA: Se cambia a nullable = true para sincronizar con la migración V31 de Flyway
    // Evita que Hibernate lance una excepción en Render al procesar flujos o vueltas diferidas
    @Column(name = "travel_date", nullable = true)
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

    @Column(name = "status") // Flujo canónico: PENDING_PAYMENT, PAYMENT_RECEIVED, CONFIRMED, CANCELLED
    private String status; 

    @Column(name = "notes")
    private String notes;

    @Column(name = "payment_receipt_url")
    private String paymentReceiptUrl;

    // 💰 Momento exacto en que se confirmó el pago (para el registro de ingresos diario/mensual)
    @Column(name = "payment_confirmed_at")
    private LocalDateTime paymentConfirmedAt;

    @Column(name = "companion_names", length = 500)
    private String companionNames;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "reservation_code", unique = true, length = 20)
    private String reservationCode;

    // 🕒 TIMESTAMPS DE AUDITORÍA EMPRESARIAL (Nativos de Hibernate)
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 🕒 Campo de horario de salida (unificado con el bot y la agenda)
    @Column(name = "departure_schedule")
    private String departureSchedule;

    @Column(name = "requires_invoice")
    private Boolean requiresInvoice; // 🧾 Flag unificado para facturación


    public int getTotalSeats() {
        if (this.passengerCount == null || this.passengerCount < 1) {
            return 1;
        }
        return this.passengerCount;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public void setScheduleBlock(String departureSchedule) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'setScheduleBlock'");
    }


    
}