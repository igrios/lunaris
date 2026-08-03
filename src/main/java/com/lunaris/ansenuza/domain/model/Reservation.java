package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    private static final LocalDate OPEN_RETURN_SENTINEL_DATE = LocalDate.of(2099, 12, 31);

    public enum TravelStatus {
        PENDING,
        REALIZED,
        OPEN_RETURN,
        CANCELED,
        NO_SHOW,
        ONBOARD,
        BOARDED,
        ONBOARDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", length = 50)
    private TripType tripType;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "extra_amount")
    private BigDecimal extraAmount;

    @Column(name = "promotion_code", length = 4)
    private String promotionCode;

    @Column(name = "promotion_id")
    private UUID promotionId;

    @ManyToOne
    @JoinColumn(name = "promotion_id", insertable = false, updatable = false)
    private Promotion promotion;

    @Column(name = "promotion_discount_percentage")
    private Integer promotionDiscountPercentage;

    @Builder.Default
    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "payment_verified", nullable = false)
    private Boolean paymentVerified;

    @Column(name = "status") // Flujo canónico: PENDING_PAYMENT, PAYMENT_RECEIVED, CONFIRMED, CANCELLED
    private String status; 

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ReservationSource source = ReservationSource.MANUAL;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "travel_status", nullable = false, length = 20)
    private TravelStatus travelStatus = TravelStatus.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "payment_receipt_url")
    private String paymentReceiptUrl;

    @Column(name = "waiting_list_entry_id")
    private Long waitingListEntryId;

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

    @Column(name = "route_sequence")
    private Integer routeSequence;

    @Builder.Default
    @Column(name = "requires_invoice", nullable = false)
    private Boolean requiresInvoice = true; // 🧾 Toda reserva confirmada debe facturarse

    @PrePersist
    @PreUpdate
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (travelStatus == null) {
            travelStatus = TravelStatus.PENDING;
        }
        if (source == null) {
            source = ReservationSource.MANUAL;
        }
        requiresInvoice = true;
        if (tripType == null) {
            tripType = Boolean.TRUE.equals(roundTrip)
                    ? (returnDate == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP)
                    : TripType.ONE_WAY;
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            routeSequence = null;
        }
    }

    public int getTotalSeats() {
        if (this.passengerCount == null || this.passengerCount < 1) {
            return 1;
        }
        return this.passengerCount;
    }

    public boolean isScheduledConfirmedTrip() {
        if (travelDate == null || OPEN_RETURN_SENTINEL_DATE.equals(travelDate)
                || departureSchedule == null || departureSchedule.isBlank()
                || !"CONFIRMED".equalsIgnoreCase(status)
                || travelStatus == TravelStatus.OPEN_RETURN) {
            return false;
        }
        boolean returnLeg = reservationCode != null && reservationCode.endsWith("-VUELTA");
        return !returnLeg || returnDate != null && !OPEN_RETURN_SENTINEL_DATE.equals(returnDate);
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }

    public void setScheduleBlock(String departureSchedule) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'setScheduleBlock'");
    }


    
}
