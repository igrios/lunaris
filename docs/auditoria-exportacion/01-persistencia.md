# 1. CAPA DE PERSISTENCIA

160 archivos. Rutas relativas a la raíz del repositorio. Código original, sin reformatear.

La agrupación es funcional: no modifica paquetes. Cada archivo aparece una sola vez; los archivos con responsabilidades mixtas se asignan a su función principal.

## Índice

- `src/main/java/com/lunaris/ansenuza/domain/model/Account.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/BusinessParameter.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/CapacityLock.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ChatMessage.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ConversationSession.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Driver.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/DriverApplication.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Fare.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Inquiry.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Invoice.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Locality.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/NewsBanner.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/OperatorNotificationPhone.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Passenger.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Promotion.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/PromotionUsage.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Reservation.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ReservationEvent.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/SystemConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Vehicle.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/WaitingListEntry.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/converter/ReservationStatusConverter.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/converter/RoleConverter.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/converter/TravelStatusConverter.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/AccountRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/BusinessParameterRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/CapacityLockRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/ChatMessageRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/ConversationSessionRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/DriverApplicationRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/DriverRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/FareRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/InquiryRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/InvoiceRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/LocalityRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/NewsBannerRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/OperatorNotificationPhoneRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/PassengerRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/PromotionRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/PromotionUsageRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/ReservationEventRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/ReservationRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/SystemConfigurationRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/VehicleRepository.java`
- `src/main/java/com/lunaris/ansenuza/domain/repository/WaitingListRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/ChatbotTelemetryAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/JpaBankPaymentReservationAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/JpaPaymentAuditOutboxAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/ProcessedTransactionLedgerAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/SpecialTripPersistenceAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/AssignedOrGeneratedUuid.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ChatbotAnalyticsEvent.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ChatbotAnalyticsSession.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/PaymentAuditOutboxEntity.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ProcessedPaymentTransactionEntity.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/SpecialTripEntity.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/WhatsAppWebhookInboxEntity.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/mapper/SpecialTripPersistenceMapper.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ChatbotAnalyticsEventRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ChatbotAnalyticsSessionRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/PaymentAuditOutboxJpaRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ProcessedPaymentTransactionJpaRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/SpecialTripJpaRepository.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/WhatsAppWebhookInboxRepository.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/ReservationPersistenceAdapter.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/entity/ReservationEntity.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/mapper/ReservationMapper.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/repository/SpringDataReservationRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverOperationsRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcCapacityRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcDriverOperationsRepository.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcInterurbanPaymentRepository.java`
- `src/main/resources/db/migration/V100__add_missing_foreign_keys_and_indexes.sql`
- `src/main/resources/db/migration/V101__extend_driver_applications.sql`
- `src/main/resources/db/migration/V102__add_source_to_reservations_if_missing.sql`
- `src/main/resources/db/migration/V103__add_vehicle_year_to_driver_applications.sql`
- `src/main/resources/db/migration/V104__add_trip_type_to_reservations.sql`
- `src/main/resources/db/migration/V105__allow_company_vehicle_driver_applications.sql`
- `src/main/resources/db/migration/V106__create_news_banners.sql`
- `src/main/resources/db/migration/V107__create_waiting_list_entries.sql`
- `src/main/resources/db/migration/V108__link_waiting_list_reengagement.sql`
- `src/main/resources/db/migration/V109__add_return_completion_tracking.sql`
- `src/main/resources/db/migration/V10__insert_airport_locality.sql`
- `src/main/resources/db/migration/V110__create_payment_email_audit.sql`
- `src/main/resources/db/migration/V111__create_special_trips_table.sql`
- `src/main/resources/db/migration/V112__add_arrufo_locality_and_constraints.sql`
- `src/main/resources/db/migration/V113__link_reservation_booking_groups.sql`
- `src/main/resources/db/migration/V114__capacity_locks_and_route_scope.sql`
- `src/main/resources/db/migration/V115__extend_invoice_pdf_url.sql`
- `src/main/resources/db/migration/V116__waiting_list_special_events.sql`
- `src/main/resources/db/migration/V117__extend_news_banners_for_waiting_lists.sql`
- `src/main/resources/db/migration/V118__optimize_fares_and_localities_indexes.sql`
- `src/main/resources/db/migration/V119__financial_hardening_and_immutable_events.sql`
- `src/main/resources/db/migration/V11__add_reservation_fields_to_conversation.sql`
- `src/main/resources/db/migration/V120__enforce_single_invoice_per_reservation.sql`
- `src/main/resources/db/migration/V121__track_balance_used_by_reservations.sql`
- `src/main/resources/db/migration/V122__create_whatsapp_webhook_inbox.sql`
- `src/main/resources/db/migration/V123__create_inquiries.sql`
- `src/main/resources/db/migration/V124__operator_notification_phones.sql`
- `src/main/resources/db/migration/V125__manual_bot_pause.sql`
- `src/main/resources/db/migration/V126__reservation_amount_scope.sql`
- `src/main/resources/db/migration/V127__chatbot_analytics.sql`
- `src/main/resources/db/migration/V128__manual_reservation_notifications.sql`
- `src/main/resources/db/migration/V129__add_interurban_corridor.sql`
- `src/main/resources/db/migration/V12__add_passenger_name_to_conversation_sessions.sql`
- `src/main/resources/db/migration/V130__interurban_encrypted_qr_artifacts.sql`
- `src/main/resources/db/migration/V131__interurban_settlement_breakdown.sql`
- `src/main/resources/db/migration/V132__link_accounts_to_drivers_and_add_indexes.sql`
- `src/main/resources/db/migration/V14__add_round_trip_to_conversation_sessions.sql`
- `src/main/resources/db/migration/V15__add_invoice_fields_to_conversation_sessions.sql`
- `src/main/resources/db/migration/V16__make_passenger_cuil_nullable.sql`
- `src/main/resources/db/migration/V17__add_status_to_reservations.sql`
- `src/main/resources/db/migration/V18__business_parameters.sql`
- `src/main/resources/db/migration/V19__INSERT_INTO_fares.sql`
- `src/main/resources/db/migration/V1__initial_schema.sql`
- `src/main/resources/db/migration/V20__conversation_sessions.sql`
- `src/main/resources/db/migration/V21__passenger_count.sql`
- `src/main/resources/db/migration/V22__add_companions_and_return_date.sql`
- `src/main/resources/db/migration/V23__ALTER_TABLE_RESERVATION.sql`
- `src/main/resources/db/migration/V24__add_logistics_to_localities.sql`
- `src/main/resources/db/migration/V25__companion_names.sql`
- `src/main/resources/db/migration/V26__Reservations.sql`
- `src/main/resources/db/migration/V27__add_bot_paused_to_conversation_sessions.sql`
- `src/main/resources/db/migration/V28__create_chat_messages_table.sql`
- `src/main/resources/db/migration/V29__add_reservation_code_to_reservations.sql`
- `src/main/resources/db/migration/V2__drivers.sql`
- `src/main/resources/db/migration/V30__enterprise_trazability_and_timestamps.sql`
- `src/main/resources/db/migration/V31__Reservatoin_not_null_ALTER.sql`
- `src/main/resources/db/migration/V32__create_invoices_and_payment_timestamp.sql`
- `src/main/resources/db/migration/V33__remove_unique_reservation_code.sql`
- `src/main/resources/db/migration/V34__add_passenger_current_balance.sql`
- `src/main/resources/db/migration/V35__add_assigned_operator_to_conversation_sessions.sql`
- `src/main/resources/db/migration/V36__add_departure_schedule_to_reservations.sql`
- `src/main/resources/db/migration/V37__add_requires_invoice_to_reservations.sql`
- `src/main/resources/db/migration/V38__add_travel_status_to_reservations.sql`
- `src/main/resources/db/migration/V39__create_system_configurations.sql`
- `src/main/resources/db/migration/V3__vehicles.sql`
- `src/main/resources/db/migration/V40__add_driver_to_reservations.sql`
- `src/main/resources/db/migration/V41__create_accounts.sql`
- `src/main/resources/db/migration/V42__create_promotions.sql`
- `src/main/resources/db/migration/V43__add_promotion_fields.sql`
- `src/main/resources/db/migration/V44__add_promotion_expiration.sql`
- `src/main/resources/db/migration/V45__normalize_massive_promotion_usages.sql`
- `src/main/resources/db/migration/V46__audit_promotions_on_reservations.sql`
- `src/main/resources/db/migration/V47__add_dynamic_route_sequence.sql`
- `src/main/resources/db/migration/V48__enforce_unique_route_sequence.sql`
- `src/main/resources/db/migration/V49__create_driver_applications.sql`
- `src/main/resources/db/migration/V4__reservations.sql`
- `src/main/resources/db/migration/V50__add_driver_current_location.sql`
- `src/main/resources/db/migration/V53__reset_admin_password.sql`
- `src/main/resources/db/migration/V54__add_reservation_source.sql`
- `src/main/resources/db/migration/V55__add_trip_type_to_reservations.sql`
- `src/main/resources/db/migration/V5__create_localities_and_fares.sql`
- `src/main/resources/db/migration/V6__add_amount_to_reservations.sql`
- `src/main/resources/db/migration/V7__add_round_trip_fields.sql`
- `src/main/resources/db/migration/V8__insert_localities.sql`
- `src/main/resources/db/migration/V99__reset_admin_password_force.sql`
- `src/main/resources/db/migration/V9__create_conversation_sessions.sql`

## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Account.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "account_roles", joinColumns = @JoinColumn(name = "account_id"))
    @Convert(converter = com.lunaris.ansenuza.domain.model.converter.RoleConverter.class)
    @Column(name = "role", nullable = false, length = 30)
    private Set<Role> roles = new HashSet<>();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/BusinessParameter.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "business_parameters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessParameter {

    // 🔑 Usamos la clave natural de texto como la Clave Primaria para JPA
    @Id
    @Column(name = "parameter_key", nullable = false, unique = true)
    private String parameterKey;

    @Column(name = "parameter_value", nullable = false)
    private String parameterValue;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/CapacityLock.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila estable utilizada como mutex transaccional por fecha/turno/corredor. */
@Entity
@Table(name = "reservation_capacity_locks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CapacityLock {
    @Id
    @Column(name = "lock_key", length = 255)
    private String lockKey;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ChatMessage.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(name = "is_from_operator", nullable = false)
    private boolean fromOperator;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ConversationSession.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "conversation_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "current_step")
    private String currentStep;

    @Column(name = "last_interaction")
    private LocalDateTime lastInteraction;

    private String pickupLocality;
    private String passengerName;
    private String pickupAddress;
    private String destination;
    private Boolean roundTrip;
    private LocalDate travelDate;
    private Boolean requiresInvoice;
    private String cuil;

    @Column(name = "promotion_code", length = 4)
    private String promotionCode;

    @Column(name = "promotion_discount_percentage")
    private Integer promotionDiscountPercentage;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "companion_names", length = 500)
    private String companionNames;

    @Column(name = "current_companion_index")
    private Integer currentCompanionIndex;

    @Column(name = "total_companions")
    private Integer totalCompanions;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "bot_paused")
    private boolean botPaused = false;

    @Column(name = "schedule_block")
    private String scheduleBlock;

    @Column(name = "manually_paused", nullable = false)
    private boolean manuallyPaused;

    @Column(name = "reservation_code")
    private String reservationCode;

    // ⚖️ 🆕 NUEVO CAMPO: Persistencia del operador asignado por el Load Balancer
    @Column(name = "assigned_operator")
    private String assignedOperator;

    @Column(name = "waiting_list_entry_id")
    private Long waitingListEntryId;

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Driver.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", unique = true)
    private UUID accountId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "current_location_url", length = 500)
    private String currentLocationUrl;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @PrePersist
    void initializeId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/DriverApplication.java`

```java
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Fare.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "locality_name", nullable = false)
    private String localityName;

    @Column(nullable = false)
    private BigDecimal amount;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Inquiry.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
    @Column(nullable = false)
    private String phone;
    @Column(name = "passenger_name")
    private String passengerName;
    @Column(nullable = false, columnDefinition = "text")
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getWhatsAppUrl() {
        return "https://wa.me/" + phone.replaceAll("[^0-9]", "");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Invoice.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🧾 Factura emitida para una reserva con pago confirmado.
 * La factura fiscal la arma la operadora por fuera del sistema; acá guardamos el
 * registro, el PDF subido a mano y la marca de envío por WhatsApp.
 */
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false, insertable = false, updatable = false)
    private Reservation reservation;

    @Column(name = "authorization_code", length = 40)
    private String authorizationCode;

    @Column(name = "invoice_number", length = 40)
    private String invoiceNumber;

    @Column(name = "passenger_name", length = 200)
    private String passengerName;

    @Column(name = "passenger_cuil", length = 20)
    private String passengerCuil;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;

    @Column(name = "sent_via_whatsapp", nullable = false)
    private Boolean sentViaWhatsapp;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Locality.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "localities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locality {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    // 🛣️ Kilómetros reales desde esta localidad hasta Córdoba Capital
    @Column(name = "kms_to_cordoba")
    private Integer kmsToCordoba;

    // ⏱️ Minutos de viaje acumulados desde el inicio del recorrido en Morteros
    @Column(name = "minutes_from_origin")
    private Integer minutesFromOrigin;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/NewsBanner.java`

```java
package com.lunaris.ansenuza.domain.model;

import com.lunaris.ansenuza.shared.ArgentinaTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "news_banners")
@Getter
@Setter
@NoArgsConstructor
public class NewsBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "has_waiting_list", nullable = false)
    private boolean hasWaitingList;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = ArgentinaTime.now();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/OperatorNotificationPhone.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operator_notification_phones")
@Getter @Setter @NoArgsConstructor
public class OperatorNotificationPhone {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true, length = 15)
    private String phone;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Passenger.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "passengers")
@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.PrePersist
    void ensureId() {
        if (id == null) id = UUID.randomUUID();
    }

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String cuil;

    private String phone;

    private String address;

    private String locality;

    // 💰 BILLETERA VIRTUAL: Cuenta corriente para saldos a favor por cancelaciones o promos
    @Builder.Default
    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Promotion.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Column(name = "discount_percentage", nullable = false)
    private Integer discountPercentage;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "is_massive", nullable = false)
    private boolean massive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/PromotionUsage.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion_usages")
@Getter
@NoArgsConstructor
public class PromotionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PromotionUsage(Promotion promotion, String phoneNumber) {
        this.promotion = promotion;
        this.phoneNumber = phoneNumber;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) {
            createdAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Reservation.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
        SCHEDULED,
        PENDING,
        REALIZED,
        OPEN_RETURN,
        PARTIALLY_COMPLETED,
        COMPLETED,
        CANCELED,
        NO_SHOW,
        CONFIRMED,
        ROUTE_SENT,
        IN_PROGRESS,
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

    /** true únicamente cuando amount contiene el total del grupo repetido en cada tramo. */
    @Column(name = "amount_is_group_total", nullable = false)
    private boolean amountIsGroupTotal;

    /** Saldo histórico debitado al crear esta reserva; forma parte del valor pagado. */
    @Builder.Default
    @Column(name = "used_balance", nullable = false)
    private BigDecimal usedBalance = BigDecimal.ZERO;

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

    @Convert(converter = com.lunaris.ansenuza.domain.model.converter.ReservationStatusConverter.class)
    @Column(name = "status") // Flujo canónico: PENDING_PAYMENT, PAYMENT_RECEIVED, CONFIRMED, CANCELLED
    private String status; 

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private ReservationSource source = ReservationSource.MANUAL;

    @Builder.Default
    @Convert(converter = com.lunaris.ansenuza.domain.model.converter.TravelStatusConverter.class)
    @Column(name = "travel_status", nullable = false, length = 20)
    private TravelStatus travelStatus = TravelStatus.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "manual_notification_attempt_at")
    private LocalDateTime manualNotificationAttemptAt;

    @Column(name = "invoice_url", length = 2048)
    private String invoiceUrl;

    @Column(name = "manual_notification_pending", nullable = false)
    private boolean manualNotificationPending;

    @Column(name = "manual_notification_waiting_reply", nullable = false)
    private boolean manualNotificationWaitingReply;

    @Column(name = "payment_receipt_url")
    private String paymentReceiptUrl;

    @Column(name = "payment_expires_at")
    private LocalDateTime paymentExpiresAt;

    @Column(name = "waiting_list_entry_id")
    private Long waitingListEntryId;

    // 💰 Momento exacto en que se confirmó el pago (para el registro de ingresos diario/mensual)
    @Column(name = "payment_confirmed_at")
    private LocalDateTime paymentConfirmedAt;

    @Column(name = "companion_names", length = 500)
    private String companionNames;

    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Builder.Default
    @Column(name = "returned_passenger_count", nullable = false)
    private Integer returnedPassengerCount = 0;

    @Column(name = "reservation_code", unique = true, length = 20)
    private String reservationCode;

    @Column(name = "booking_group_code", length = 40)
    private String bookingGroupCode;

    @Column(name = "route_direction", length = 16)
    private String routeDirection;

    @Column(name = "return_audit_sent_at")
    private LocalDateTime returnAuditSentAt;

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
    private Boolean requiresInvoice = true; // Valor legado; se respeta la selección explícita del operador.

    @PrePersist
    @PreUpdate
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        }
        if (paymentExpiresAt == null && !Boolean.TRUE.equals(paymentVerified)
                && ("PENDING_PAYMENT".equalsIgnoreCase(status)
                    || "PENDING_VERIFICATION".equalsIgnoreCase(status))) {
            paymentExpiresAt = createdAt.plusMinutes(20);
        }
        if (travelStatus == null) {
            travelStatus = TravelStatus.PENDING;
        }
        if (source == null) {
            source = ReservationSource.MANUAL;
        }
        if (requiresInvoice == null) requiresInvoice = true;
        if (returnedPassengerCount == null || returnedPassengerCount < 0) {
            returnedPassengerCount = 0;
        }
        if (tripType == null) {
            tripType = Boolean.TRUE.equals(roundTrip)
                    ? (returnDate == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP)
                    : TripType.ONE_WAY;
        }
        if ((routeDirection == null || routeDirection.isBlank())
                && pickupLocality != null && destination != null) {
            boolean fromCordoba = pickupLocality.toLowerCase(java.util.Locale.ROOT)
                    .replace("ó", "o").contains("cordoba");
            boolean toCordoba = destination.toLowerCase(java.util.Locale.ROOT)
                    .replace("ó", "o").contains("cordoba");
            routeDirection = fromCordoba && !toCordoba ? "VUELTA"
                    : !fromCordoba && toCordoba ? "IDA" : routeDirection;
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ReservationEvent.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false, insertable = false, updatable = false)
    private Reservation reservation;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // Ej: 'RESERVATION_CREATED', 'RECEIPT_SUBMITTED', 'PAYMENT_CONFIRMED'

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy; // 'BOT', 'ADMIN_MARTIN', 'API'

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/SystemConfiguration.java`

```java
package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {

    @Id
    @Column(name = "\"key\"", nullable = false)
    private String key;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Vehicle.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String plate;

  @Builder.Default
  private Integer capacity = 4;

  private Boolean active;

  @PrePersist
  void applyDefaults() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (capacity == null || capacity < 1) {
      capacity = 4;
    }
  }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/WaitingListEntry.java`

```java
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/converter/ReservationStatusConverter.java`

```java
package com.lunaris.ansenuza.domain.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;
import java.util.Set;

/**
 * Conversor tolerante para el estado histórico de reserva, cuyo contrato público
 * continúa siendo String para no romper integraciones existentes.
 */
@Converter
public class ReservationStatusConverter implements AttributeConverter<String, String> {

    private static final String DEFAULT_STATUS = "PENDING";
    private static final Set<String> KNOWN_STATUSES = Set.of(
            DEFAULT_STATUS,
            "PENDING_PAYMENT",
            "PAYMENT_RECEIVED",
            "RECEIPT_UPLOADED",
            "RESERVED",
            "REJECTED",
            "CONFIRMED",
            "CANCELLED",
            "OPEN_RETURN",
            "PARTIALLY_COMPLETED",
            "COMPLETED");

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return normalize(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return normalize(dbData);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_STATUS;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return KNOWN_STATUSES.contains(normalized) ? normalized : DEFAULT_STATUS;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/converter/RoleConverter.java`

```java
package com.lunaris.ansenuza.domain.model.converter;

import com.lunaris.ansenuza.domain.model.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

/** Lee nombres históricos y persiste exclusivamente los roles canónicos del dominio. */
@Converter
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        String cleanedValue = dbData.trim().toUpperCase(Locale.ROOT);
        if (cleanedValue.startsWith("ROLE_")) cleanedValue = cleanedValue.substring(5);
        if ("DRIVER".equals(cleanedValue)) return Role.CHOFER;
        try {
            return Role.valueOf(cleanedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Rol persistido no reconocido.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/converter/TravelStatusConverter.java`

```java
package com.lunaris.ansenuza.domain.model.converter;

import com.lunaris.ansenuza.domain.model.Reservation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter
public class TravelStatusConverter
        implements AttributeConverter<Reservation.TravelStatus, String> {

    @Override
    public String convertToDatabaseColumn(Reservation.TravelStatus attribute) {
        return (attribute == null ? Reservation.TravelStatus.PENDING : attribute).name();
    }

    @Override
    public Reservation.TravelStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Reservation.TravelStatus.PENDING;
        }
        try {
            return Reservation.TravelStatus.valueOf(
                    dbData.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return Reservation.TravelStatus.PENDING;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/AccountRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<Account> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT DISTINCT a FROM Account a")
    List<Account> findAllWithRoles();

    boolean existsByUsernameIgnoreCase(String username);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/BusinessParameterRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.BusinessParameter;

@Repository
public interface BusinessParameterRepository extends JpaRepository<BusinessParameter, String> {
    
    // Spring traduce automáticamente 'findByParameterKey' a la columna 'parameter_key' en Postgres
    Optional<BusinessParameter> findByParameterKey(String parameterKey);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/CapacityLockRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.CapacityLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CapacityLockRepository extends JpaRepository<CapacityLock, String> {
    @Modifying
    @Query(value = "INSERT INTO reservation_capacity_locks(lock_key) VALUES (:lockKey) "
            + "ON CONFLICT (lock_key) DO NOTHING", nativeQuery = true)
    int ensureExists(@Param("lockKey") String lockKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CapacityLock c where c.lockKey = :lockKey")
    CapacityLock findForUpdate(@Param("lockKey") String lockKey);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/ChatMessageRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Trae el historial de mensajes de un teléfono ordenado del más viejo al más nuevo
    List<ChatMessage> findByPhoneNumberOrderByTimestampAsc(String phoneNumber);

    Optional<ChatMessage> findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc(String phoneNumber);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/ConversationSessionRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.ConversationSession;

public interface ConversationSessionRepository
        extends JpaRepository<ConversationSession, Long> {

    interface MonitorRow {
        Long getId();
        String getPhoneNumber();
        String getPassengerName();
        String getCurrentStep();
        boolean getBotPaused();
        LocalDateTime getLastInteraction();
        String getLastMessage();
    }

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT s.id AS id, s.phone_number AS phoneNumber,
               COALESCE(NULLIF(TRIM(CONCAT(CONCAT(p.first_name, ' '), p.last_name)), ''),
                        s.passenger_name, 'Cliente Anónimo') AS passengerName,
               s.current_step AS currentStep, COALESCE(s.bot_paused, false) AS botPaused,
               s.last_interaction AS lastInteraction, m.message_text AS lastMessage
        FROM conversation_sessions s
        LEFT JOIN (SELECT p.*, ROW_NUMBER() OVER (PARTITION BY phone ORDER BY id) AS rn
                   FROM passengers p) p ON p.phone = s.phone_number AND p.rn = 1
        LEFT JOIN (SELECT m.*, ROW_NUMBER() OVER (
                       PARTITION BY phone_number ORDER BY timestamp DESC, id DESC) AS rn
                   FROM chat_messages m) m ON m.phone_number = s.phone_number AND m.rn = 1
        ORDER BY COALESCE(m.timestamp, s.last_interaction) DESC NULLS LAST, s.id DESC
        """, nativeQuery = true)
    List<MonitorRow> findMonitorRows();

    Optional<ConversationSession>
    findByPhoneNumber(String phoneNumber);

    // 🧹 Sesiones del bot abandonadas: sin actividad reciente y que NO estén en manos de un operador.
    List<ConversationSession>
    findByBotPausedFalseAndLastInteractionBefore(LocalDateTime cutoff);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/DriverApplicationRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverApplicationRepository extends JpaRepository<DriverApplication, UUID> {
    Optional<DriverApplication> findFirstByPhone(String phone);

    List<DriverApplication> findByStatusOrderByCreatedAtAsc(DriverApplication.Status status);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/DriverRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // 🔥 Agregamos el import

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> { // 👈 Cambiado Long por UUID
    List<Driver> findByActiveTrue();
    Optional<Driver> findByAccountIdAndActiveTrue(UUID accountId);
    Optional<Driver> findFirstByPhone(String phone);
    Optional<Driver> findFirstByPhoneAndActiveTrue(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.id IN :ids ORDER BY d.id")
    List<Driver> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/FareRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.Fare;

@Repository
public interface FareRepository extends JpaRepository<Fare, UUID> {

    List<Fare> findAllByOrderByLocalityNameAsc();

    // 🌟 1. Trae solo los nombres de los pueblos comerciales activos para los menús
    @Query("SELECT f.localityName FROM Fare f WHERE f.amount > 0 ORDER BY f.amount DESC")
    List<String> findCommercialLocalities();

    // 🌟 2. Busca la tarifa por nombre de forma segura ignorando mayúsculas/minúsculas
    Optional<Fare> findByLocalityNameIgnoreCase(String localityName);

    Optional<Fare> findFirstByLocalityNameIgnoreCase(String localityName);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/InquiryRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Inquiry;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    long countByStatus(InquiryStatus status);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/InvoiceRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import com.lunaris.ansenuza.domain.model.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);

    Optional<Invoice> findFirstByReservationIdIn(java.util.List<UUID> reservationIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.reservationId = :reservationId")
    Optional<Invoice> findByReservationIdForUpdate(@Param("reservationId") UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :invoiceId")
    Optional<Invoice> findByIdForUpdate(@Param("invoiceId") UUID invoiceId);

    @Query("""
           SELECT i FROM Invoice i
           JOIN FETCH i.reservation r
           ORDER BY i.createdAt DESC
           """)
    List<Invoice> findAllIssuedWithReservation();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/LocalityRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.lunaris.ansenuza.domain.model.Locality;

public interface LocalityRepository extends JpaRepository<Locality, UUID> {

    Optional<Locality> findByName(String name);

    Optional<Locality> findFirstByNameIgnoreCase(String name);

    /**
     * Localidades publicables en el bot: existe una tarifa positiva asociada.
     * En el esquema actual una tarifa se considera activa mientras conserve un importe
     * positivo; no se agrega una columna para no alterar el contrato de la tabla fares.
     */
    @Query("""
            SELECT DISTINCT l
            FROM Locality l
            INNER JOIN Fare f ON TRIM(UPPER(l.name)) = TRIM(UPPER(f.localityName))
            WHERE f.amount IS NOT NULL AND f.amount > 0
            ORDER BY l.name ASC
            """)
    List<Locality> findAllWithActiveFare();

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/NewsBannerRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.NewsBanner;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsBannerRepository extends JpaRepository<NewsBanner, UUID> {

    List<NewsBanner> findAllByOrderByCreatedAtDesc();

    @Query("""
            select banner from NewsBanner banner
            where banner.active = true
              and (banner.validUntil is null or banner.validUntil >= :today)
            order by banner.createdAt desc
            """)
    List<NewsBanner> findActiveOn(@Param("today") LocalDate today);

    @Query("select distinct banner.eventType from NewsBanner banner "
            + "where banner.eventType is not null and banner.eventType <> ''")
    List<String> findDistinctEventTypes();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/OperatorNotificationPhoneRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OperatorNotificationPhoneRepository extends JpaRepository<OperatorNotificationPhone, UUID> {
    List<OperatorNotificationPhone> findByActiveTrueOrderByCreatedAtAsc();
    List<OperatorNotificationPhone> findAllByOrderByCreatedAtAsc();
    boolean existsByPhone(String phone);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/PassengerRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.Passenger;
import jakarta.persistence.LockModeType;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    Optional<Passenger> findFirstByPhone(String phoneNumber);

    Optional<Passenger> findByPhone(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.id = :id")
    Optional<Passenger> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Passenger p where p.phone = :phone")
    Optional<Passenger> findByPhoneForUpdate(@Param("phone") String phone);

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/PromotionRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import com.lunaris.ansenuza.domain.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    boolean existsByCode(String code);

    Optional<Promotion> findFirstByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select promotion from Promotion promotion where promotion.code = :code")
    Optional<Promotion> findByCodeForUpdate(@Param("code") String code);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/PromotionUsageRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.PromotionUsage;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

    @Query(value = """
            SELECT COUNT(*)
            FROM promotion_usages
            WHERE promotion_id = :promoId
              AND phone_number = :normalizedPhone
            """, nativeQuery = true)
    long countByPromotionAndNormalizedPhone(@Param("promoId") UUID promotionId,
            @Param("normalizedPhone") String normalizedPhone);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/ReservationEventRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;

@Repository
public interface ReservationEventRepository extends JpaRepository<ReservationEvent, UUID> {
    
    boolean existsByReservationIdAndEventTypeAndCreatedAtGreaterThanEqual(
            UUID reservationId, String eventType, java.time.LocalDateTime since);

    // Método clave para cuando armemos la pantalla /timeline de Martín
    List<ReservationEvent> findByReservationIdOrderByCreatedAtAsc(UUID reservationId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/ReservationRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByPassengerPhoneAndManualNotificationWaitingReplyTrue(String phone);

    @Query("""
            select r from Reservation r where r.manualNotificationPending = true
            and (r.manualNotificationAttemptAt is null or r.manualNotificationAttemptAt < :leaseExpired)
            and (r.manualNotificationWaitingReply = false or exists (
                select m.id from ChatMessage m where m.phoneNumber = r.passenger.phone
                and m.fromOperator = false and m.timestamp > :windowStart))
            order by r.createdAt
            """)
    List<Reservation> findRetryableManualNotifications(@Param("leaseExpired") LocalDateTime leaseExpired,
            @Param("windowStart") LocalDateTime windowStart, org.springframework.data.domain.Pageable pageable);


    @Query("""
           SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
           FROM Reservation r
           WHERE r.passenger.phone = :phone
             AND (r.promotionId = :promotionId OR r.promotionCode = :promotionCode)
             AND UPPER(COALESCE(r.status, '')) NOT IN
                 ('CANCELLED', 'CANCELED', 'REJECTED', 'EXPIRED')
           """)
    boolean existsActivePromotionUsageByPhone(
            @Param("phone") String phone,
            @Param("promotionId") UUID promotionId,
            @Param("promotionCode") String promotionCode);

    @Query("""
           SELECT r.id FROM Reservation r
           WHERE r.paymentVerified = false
             AND r.paymentExpiresAt IS NOT NULL
             AND r.paymentExpiresAt <= :now
             AND UPPER(r.status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION')
           ORDER BY r.paymentExpiresAt
           """)
    List<UUID> findExpiredPaymentCandidateIds(
            @Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @Query("""
           SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reservation r
           WHERE r.paymentReceiptUrl = :receiptUrl
             AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED')
             AND (:groupCode IS NULL OR r.bookingGroupCode IS NULL
                  OR r.bookingGroupCode <> :groupCode)
           """)
    boolean existsActiveReceiptInAnotherGroup(
            @Param("receiptUrl") String receiptUrl, @Param("groupCode") String groupCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id in :ids")
    List<Reservation> findAllByIdForUpdate(@Param("ids") List<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id IN :ids ORDER BY r.id")
    List<Reservation> findAllByIdInForUpdate(@Param("ids") List<UUID> ids);

    /** Reclamo atómico de un aviso: solamente una instancia puede obtener 1 fila. */
    @Modifying
    @Transactional
    @Query("UPDATE Reservation r SET r.returnAuditSentAt = :sentAt "
            + "WHERE r.id = :id AND (r.returnAuditSentAt IS NULL OR r.returnAuditSentAt < :dayStart)")
    int claimReturnAudit(@Param("id") UUID id, @Param("sentAt") LocalDateTime sentAt,
            @Param("dayStart") LocalDateTime dayStart);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.travelDate = :travelDate
           AND r.status = 'CONFIRMED'
           AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED
           AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.REALIZED
           """)
    List<Reservation> findConfirmedActiveByTravelDate(@Param("travelDate") LocalDate travelDate);

    @Query("""
           SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
           FROM Reservation r
           WHERE (LOWER(r.pickupLocality) = LOWER(:localityName)
                  OR LOWER(r.destination) = LOWER(:localityName))
           AND (r.status IS NULL OR UPPER(r.status) NOT IN ('CANCELLED', 'COMPLETED'))
           AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED
           """)
    boolean existsActiveByLocality(@Param("localityName") String localityName);

    // 🌟 1. LA FIRMA CRUCIAL: Soluciona los errores de compilación de Maven en el Service
    boolean existsByReservationCode(String reservationCode);

    // 🤖 2. BUSCADOR POR CÓDIGO: Necesario para que el Bot valide y procese la BAJA (Opción 5)
    Optional<Reservation> findByReservationCode(String reservationCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.reservationCode = :reservationCode")
    Optional<Reservation> findByReservationCodeForUpdate(
            @Param("reservationCode") String reservationCode);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.bookingGroupCode = :groupCode
              OR r.reservationCode = CONCAT(:groupCode, '-IDA')
              OR r.reservationCode = CONCAT(:groupCode, '-VUELTA')
           """)
    List<Reservation> findReservationGroup(@Param("groupCode") String groupCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT r FROM Reservation r
           WHERE r.bookingGroupCode = :groupCode
              OR r.reservationCode = CONCAT(:groupCode, '-IDA')
              OR r.reservationCode = CONCAT(:groupCode, '-VUELTA')
           ORDER BY r.reservationCode
           """)
    List<Reservation> findReservationGroupForUpdate(@Param("groupCode") String groupCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.bookingGroupCode = :groupCode ORDER BY r.createdAt, r.id")
    List<Reservation> findByBookingGroupCodeForUpdate(@Param("groupCode") String groupCode);

    // 📱 3. BUSCADOR POR TELÉFONO: Permite a la consulta del Bot (Opción 4) traer el historial por nro de celular
    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           ORDER BY r.travelDate ASC, r.departureSchedule ASC, r.createdAt DESC
           """)
    List<Reservation> findByPassengerPhone(@Param("phone") String phone);

    // 🤖 BUSCADOR PARA BOT: Trae las reservas activas confirmadas de un pasajero para poder listar en botones
    @Query("SELECT r FROM Reservation r WHERE r.passenger.phone = :phone AND r.status = :status")
    List<Reservation> findByPassengerPhoneAndStatus(@Param("phone") String phone, @Param("status") String status);

    // 🌟 4. LA SECUENCIA: Cuenta cuántas reservas hay en esa ruta exacta y fecha para armar el código base
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.pickupLocality = :origin AND r.destination = :dest AND r.travelDate = :date")
    long countSequenceByRouteAndDate(
        @Param("origin") String origin, 
        @Param("dest") String dest, 
        @Param("date") LocalDate date
    );

    @Query("""
           SELECT COALESCE(SUM(r.passengerCount), 0)
           FROM Reservation r
           WHERE r.pickupLocality = :pickupLocality
           AND r.destination = :destination
           AND r.travelDate = :travelDate
           AND r.status = 'CONFIRMED'
           """)
    Integer countConfirmedPassengersByRouteAndDate(
            @Param("pickupLocality") String pickupLocality,
            @Param("destination") String destination,
            @Param("travelDate") LocalDate travelDate);

    // 🔄 Métodos preexistentes del repositorio
    List<Reservation> findByTravelDate(LocalDate travelDate);

    /** Conteos operativos por persona: una reserva de ida y vuelta no duplica al pasajero. */
    @Query("SELECT COUNT(DISTINCT r.passenger.id) FROM Reservation r "
            + "WHERE r.travelDate = :today AND r.status <> 'CANCELLED'")
    long countDistinctPassengersByTravelDate(@Param("today") LocalDate today);

    @Query("SELECT COUNT(DISTINCT r.passenger.id) FROM Reservation r "
            + "WHERE r.travelDate = :today AND r.status <> 'CANCELLED' "
            + "AND r.paymentVerified = true")
    long countDistinctPaidPassengersByTravelDate(@Param("today") LocalDate today);

    @Query("SELECT COUNT(DISTINCT r.passenger.id) FROM Reservation r "
            + "WHERE r.travelDate = :today AND r.status <> 'CANCELLED' "
            + "AND r.paymentVerified = false")
    long countDistinctPendingPassengersByTravelDate(@Param("today") LocalDate today);

    @Query("""
           SELECT DISTINCT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           WHERE r.travelDate = :date
             AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'REJECTED', 'EXPIRED')
             AND (r.travelStatus IS NULL OR r.travelStatus NOT IN (
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.REALIZED,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.CANCELED,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.NO_SHOW))
           ORDER BY r.routeDirection ASC, r.departureSchedule ASC, r.createdAt ASC
           """)
    List<Reservation> findDailyManifest(@Param("date") LocalDate date);

    @Query("""
           SELECT DISTINCT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           LEFT JOIN FETCH r.driver
           WHERE r.travelDate BETWEEN :startDate AND :endDate
           ORDER BY r.travelDate ASC, r.departureSchedule ASC
           """)
    List<Reservation> findAgendaBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Reservation> findByTravelDateAndStatusNot(LocalDate travelDate, String status);

    @Query("""
           SELECT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           LEFT JOIN FETCH r.driver
           WHERE r.travelDate = :travelDate
           AND (r.status IS NULL OR UPPER(r.status) <> 'CANCELLED')
           AND (r.travelStatus IS NULL OR r.travelStatus NOT IN (
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.REALIZED,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.CANCELED,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.NO_SHOW))
           AND REPLACE(REPLACE(REPLACE(REPLACE(
               UPPER(COALESCE(r.departureSchedule, '03:00 AM')), ' ', ''), 'AM', ''), 'PM', ''), 'HS', '')
               = REPLACE(REPLACE(REPLACE(REPLACE(
                   UPPER(:schedule), ' ', ''), 'AM', ''), 'PM', ''), 'HS', '')
           AND ((:returnDirection = true
                 AND (LOWER(r.pickupLocality) LIKE '%córdoba%'
                      OR LOWER(r.pickupLocality) LIKE '%cordoba%'
                      OR LOWER(r.pickupLocality) LIKE '%aeropuerto%'
                      OR r.roundTrip = true
                      OR UPPER(TRIM(r.routeDirection)) = 'VUELTA'
                      OR UPPER(TRIM(r.reservationCode)) LIKE '%-VUELTA'))
                OR (:returnDirection = false
                 AND LOWER(r.pickupLocality) NOT LIKE '%córdoba%'
                 AND LOWER(r.pickupLocality) NOT LIKE '%cordoba%'
                 AND LOWER(r.pickupLocality) NOT LIKE '%aeropuerto%'
                 AND (LOWER(r.destination) LIKE '%córdoba%'
                      OR LOWER(r.destination) LIKE '%cordoba%'
                      OR LOWER(r.destination) LIKE '%aeropuerto%')))
           ORDER BY r.routeSequence ASC NULLS LAST, r.createdAt ASC
           """)
    List<Reservation> findActiveManifest(
            @Param("travelDate") LocalDate travelDate,
            @Param("schedule") String schedule,
            @Param("returnDirection") boolean returnDirection);

    List<Reservation> findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(
            Passenger passenger);

    List<Reservation> findByPassenger(Passenger passenger);

    @Query("""
           SELECT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           WHERE r.travelStatus =
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
           AND (r.returnAuditSentAt IS NULL OR r.returnAuditSentAt < :dayStart)
           AND (r.status IS NULL OR UPPER(r.status) <> 'CANCELLED')
           ORDER BY r.createdAt
           """)
    List<Reservation> findReturnScheduleAuditCandidates(
            @Param("dayStart") LocalDateTime dayStart);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           AND r.travelStatus =
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
           AND (r.status IS NULL OR UPPER(r.status) <> 'CANCELLED')
           ORDER BY r.createdAt DESC
           """)
    List<Reservation> findOpenReturnReservationsByPassengerPhone(@Param("phone") String phone);

    @Query("""
           SELECT COALESCE(SUM(CASE WHEN r.passengerCount IS NULL OR r.passengerCount < 1
                                   THEN 1 ELSE r.passengerCount END), 0)
           FROM Reservation r
           WHERE r.travelDate = :date
           AND SUBSTRING(COALESCE(r.departureSchedule, '03:00 AM'), 1, 5) = SUBSTRING(:schedule, 1, 5)
           AND (r.status IS NULL OR UPPER(r.status) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED'))
           AND (r.travelStatus IS NULL OR r.travelStatus NOT IN (
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.CANCELED,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.NO_SHOW,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED))
           """)
    long countReservedSeats(
            @Param("date") LocalDate date,
            @Param("schedule") String schedule);

    /** Return legs, including undated returns linked to today's outbound leg. */
    @Query("""
           SELECT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           WHERE (r.travelDate = :date OR
               (r.travelStatus = com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
                AND EXISTS (SELECT o.id FROM Reservation o
                    WHERE o.travelDate = :date
                    AND o.reservationCode = CONCAT(
                        SUBSTRING(r.reservationCode, 1, LENGTH(r.reservationCode) - 7), '-IDA')
                    AND UPPER(COALESCE(o.status, '')) NOT IN ('CANCELLED', 'CANCELED', 'EXPIRED', 'REJECTED'))))
           AND (UPPER(r.routeDirection) = 'VUELTA'
                OR LOWER(r.pickupLocality) LIKE '%cordoba%'
                OR LOWER(r.pickupLocality) LIKE '%córdoba%'
                OR LOWER(r.pickupLocality) LIKE '%aeropuerto%')
           AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'CANCELED', 'EXPIRED', 'REJECTED')
           AND (r.travelStatus IS NULL OR r.travelStatus NOT IN (
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.CANCELED,
               com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.NO_SHOW))
           """)
    List<Reservation> findReturnCapacityCandidates(@Param("date") LocalDate date);

    // 📊 Suma pasajeros reales para las tarjetas del panel ignorando los 'CANCELLED'
    @Query("SELECT COALESCE(SUM(r.passengerCount), 0) FROM Reservation r " +
           "WHERE r.travelDate = :fecha " +
           "AND r.notes LIKE %:horario% " +
           "AND r.status != 'CANCELLED'")
    int countPassengersByReturnDateAndNotesContaining(@Param("fecha") LocalDate fecha, @Param("horario") String horario);

    // 🧾 Listado para el panel de Facturación (reservas con pago confirmado)
    List<Reservation> findByStatus(String status);

    @Query("""
           SELECT r FROM Reservation r
           LEFT JOIN FETCH r.passenger
           WHERE r.paymentVerified = true
           AND r.requiresInvoice = true
           AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'REJECTED')
           AND r.id NOT IN (SELECT i.reservation.id FROM Invoice i)
           AND NOT EXISTS (
               SELECT groupedInvoice.id FROM Invoice groupedInvoice
               WHERE (r.bookingGroupCode IS NOT NULL
                      AND r.bookingGroupCode = groupedInvoice.reservation.bookingGroupCode)
                  OR (r.reservationCode LIKE '%-IDA'
                      AND groupedInvoice.reservation.reservationCode = CONCAT(
                          SUBSTRING(r.reservationCode, 1, LENGTH(r.reservationCode) - 4), '-VUELTA'))
                  OR (r.reservationCode LIKE '%-VUELTA'
                      AND groupedInvoice.reservation.reservationCode = CONCAT(
                          SUBSTRING(r.reservationCode, 1, LENGTH(r.reservationCode) - 7), '-IDA'))
           )
           """)
    List<Reservation> findPendingInvoiceReservations();

    // 💰 INGRESO DE DINERO: suma de todos los tramos confirmados; en ida y vuelta
    // cada tramo lleva la mitad del importe neto, por lo que la suma es el total cobrado.
    @Query("SELECT COALESCE(SUM(COALESCE(r.amount, 0) + COALESCE(r.extraAmount, 0)), 0) FROM Reservation r " +
           "WHERE r.paymentConfirmedAt >= :start AND r.paymentConfirmedAt < :end " +
           "AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'REJECTED')")
    BigDecimal sumConfirmedIncomeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.paymentConfirmedAt >= :start AND r.paymentConfirmedAt < :end " +
           "AND UPPER(COALESCE(r.status, '')) NOT IN ('CANCELLED', 'REJECTED') " +
           "AND (r.reservationCode IS NULL OR r.reservationCode NOT LIKE '%-VUELTA')")
    long countConfirmedIncomeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 💡 NUEVO MÉTODO FILTRADO: Para limpiar la grilla de vueltas abiertas en el controlador web
    @Query("""
           SELECT r FROM Reservation r
           WHERE (r.travelDate = :fechaCentinela
                  OR (r.travelDate IS NULL
                      AND r.travelStatus = com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN))
           AND r.driver IS NULL
           AND r.status <> 'CANCELLED'
           """)
    List<Reservation> findVueltasAbiertasActive(@Param("fechaCentinela") LocalDate fechaCentinela);

    @Query("""
           SELECT ret FROM Reservation ret
           WHERE ret.travelDate = :date
           AND ret.status <> 'CANCELLED'
           AND ret.reservationCode LIKE '%-VUELTA'
           AND EXISTS (
               SELECT outbound.id FROM Reservation outbound
               WHERE outbound.reservationCode = CONCAT(SUBSTRING(ret.reservationCode, 1, LENGTH(ret.reservationCode) - 7), '-IDA')
               AND outbound.travelStatus = :travelStatus
           )
           """)
    List<Reservation> findScheduledReturnsWithRealizedOutbound(
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.returnDate = :date
           AND r.travelStatus = :travelStatus
           AND r.status <> 'CANCELLED'
           """)
    List<Reservation> findRealizedOutboundReservationsWithReturnDate(
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           AND r.travelDate = :date
           AND r.status <> 'CANCELLED'
           AND r.reservationCode LIKE '%-VUELTA'
           ORDER BY r.createdAt DESC
           """)
    List<Reservation> findActiveReturnReservationsByPassengerPhoneAndDate(
            @Param("phone") String phone,
            @Param("date") LocalDate date);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.passenger.phone = :phone
           AND r.returnDate = :date
           AND r.travelStatus = :travelStatus
           AND r.status <> 'CANCELLED'
           ORDER BY r.createdAt DESC
           """)
    List<Reservation> findRealizedOutboundReservationsByPassengerPhoneAndReturnDate(
            @Param("phone") String phone,
            @Param("date") LocalDate date,
            @Param("travelStatus") TravelStatus travelStatus);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.travelDate BETWEEN :startDate AND :endDate
           AND r.status <> 'CANCELLED'
           ORDER BY r.travelDate ASC, r.departureSchedule ASC
           """)
    List<Reservation> findByDriverIdAndTravelDateBetween(
            @Param("driverId") UUID driverId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND (r.status IS NULL OR r.status <> 'CANCELLED')
           ORDER BY r.travelDate ASC, r.routeSequence ASC NULLS LAST,
                    r.departureSchedule ASC
           """)
    List<Reservation> findAllAssignedByDriverId(@Param("driverId") UUID driverId);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.travelDate = :travelDate
           AND r.status = 'CONFIRMED'
           AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
           ORDER BY r.routeSequence ASC NULLS LAST
           """)
    List<Reservation> findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
            @Param("driverId") UUID driverId, @Param("travelDate") LocalDate travelDate);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
             AND r.travelDate = :travelDate
             AND r.departureSchedule = :schedule
             AND r.routeDirection = :direction
             AND r.status = 'CONFIRMED'
             AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
           ORDER BY r.routeSequence ASC NULLS LAST
           """)
    List<Reservation> findByDriverAndRouteScope(
            @Param("driverId") UUID driverId,
            @Param("travelDate") LocalDate travelDate,
            @Param("schedule") String schedule,
            @Param("direction") String direction);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
           AND r.status = 'CONFIRMED'
           AND r.travelStatus <> com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.OPEN_RETURN
           AND (
               r.travelDate = :effectiveDate
               OR (r.reservationCode LIKE '%-VUELTA'
                   AND r.returnDate IS NOT NULL
                   AND r.returnDate = :effectiveDate)
           )
           ORDER BY r.routeSequence ASC NULLS LAST
           """)
    List<Reservation> findRouteByEffectiveDate(
            @Param("driverId") UUID driverId,
            @Param("effectiveDate") LocalDate effectiveDate);

    @Query("""
           SELECT r FROM Reservation r
           WHERE r.driver.id = :driverId
             AND r.routeSequence = :sequence
             AND r.routeDirection = :direction
             AND (r.travelDate = :effectiveDate
                  OR (r.returnDate = :effectiveDate AND r.reservationCode LIKE '%-VUELTA'))
             AND r.status = 'CONFIRMED'
             AND r.travelStatus NOT IN (
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.CANCELED,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.COMPLETED,
                 com.lunaris.ansenuza.domain.model.Reservation.TravelStatus.NO_SHOW)
           """)
    List<Reservation> findNextRoutePassenger(
            @Param("driverId") UUID driverId,
            @Param("effectiveDate") LocalDate effectiveDate,
            @Param("direction") String direction,
            @Param("sequence") int sequence);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/SystemConfigurationRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/VehicleRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleRepository
        extends JpaRepository<Vehicle, UUID> {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/repository/WaitingListRepository.java`

```java
package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    @Query("""
           SELECT COALESCE(SUM(entry.passengerCount), 0)
           FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
           AND entry.status = :status
           """)
    long sumPassengerCountByTravelDateAndStatus(
            @Param("travelDate") LocalDate travelDate, @Param("status") String status);

    List<WaitingListEntry> findByTravelDateAndStatusOrderByCreatedAtAsc(
            LocalDate travelDate, String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
             AND UPPER(entry.status) = UPPER(:status)
           ORDER BY entry.createdAt ASC
           """)
    List<WaitingListEntry> findByTravelDateAndNormalizedStatusOrderByCreatedAtAsc(
            @Param("travelDate") LocalDate travelDate, @Param("status") String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE UPPER(entry.status) = UPPER(:status)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findByNormalizedStatusOrderByCreatedAtDesc(
            @Param("status") String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt ASC
           """)
    List<WaitingListEntry> findByTravelDateAndActiveStatusOrderByCreatedAtAsc(
            @Param("travelDate") LocalDate travelDate);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE (entry.travelDate = :travelDate OR entry.travelDate IS NULL)
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findActiveWaitingForDateIncludingNull(
            @Param("travelDate") LocalDate travelDate);

    List<WaitingListEntry> findAllByOrderByCreatedAtDesc();

    @Query("select distinct entry.eventType from WaitingListEntry entry "
            + "where entry.eventType is not null and entry.eventType <> ''")
    List<String> findDistinctEventTypes();

    List<WaitingListEntry> findByStatusOrderByCreatedAtAsc(String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
              OR entry.status IS NULL
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findAllActiveWaitingOrderByCreatedAtDesc();

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE ((entry.eventType IS NOT NULL AND UPPER(entry.eventType) <> 'GENERAL')
                  OR entry.travelDate IS NULL)
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findActiveSpecialEventsOrderByCreatedAtDesc();

    @Query("""
           SELECT COALESCE(SUM(entry.passengerCount), 0) FROM WaitingListEntry entry
           WHERE UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
              OR entry.status IS NULL
           """)
    long countAllActiveWaiting();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entry FROM WaitingListEntry entry WHERE entry.id = :id")
    Optional<WaitingListEntry> findByIdForUpdate(@Param("id") Long id);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/ChatbotTelemetryAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.application.telemetry.*;
import com.lunaris.ansenuza.infrastructure.config.ChatbotAnalyticsProperties;
import com.lunaris.ansenuza.infrastructure.persistence.entity.*;
import com.lunaris.ansenuza.infrastructure.persistence.repository.*;
import com.lunaris.ansenuza.shared.PhoneUtils;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/** Best effort analytics: exceptions are caught OUTSIDE the independent transaction. */
@Component
@Slf4j
public class ChatbotTelemetryAdapter implements ChatbotTelemetryPort {
    private static final String SOURCE = "WHATSAPP";
    private final ChatbotAnalyticsSessionRepository sessions;
    private final ChatbotAnalyticsEventRepository events;
    private final ChatbotAnalyticsProperties properties;
    private final ApplicationEventPublisher publisher;
    private final TransactionTemplate write;
    private final TransactionTemplate read;
    private final Clock clock;
    private final AtomicLong failures = new AtomicLong();

    @Autowired
    public ChatbotTelemetryAdapter(ChatbotAnalyticsSessionRepository sessions,
            ChatbotAnalyticsEventRepository events, ChatbotAnalyticsProperties properties,
            ApplicationEventPublisher publisher, PlatformTransactionManager transactionManager) {
        this(sessions, events, properties, publisher, transactionManager, Clock.systemUTC());
    }

    ChatbotTelemetryAdapter(ChatbotAnalyticsSessionRepository sessions,
            ChatbotAnalyticsEventRepository events, ChatbotAnalyticsProperties properties,
            ApplicationEventPublisher publisher, PlatformTransactionManager transactionManager, Clock clock) {
        this.sessions = sessions;
        this.events = events;
        this.properties = properties;
        this.publisher = publisher;
        this.clock = clock;
        this.write = template(transactionManager, false);
        this.read = template(transactionManager, true);
    }

    private TransactionTemplate template(PlatformTransactionManager manager, boolean readOnly) {
        TransactionTemplate result = new TransactionTemplate(manager);
        result.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        result.setReadOnly(readOnly);
        // H2 en los tests puede tardar varios segundos en adquirir el lock
        // durante el arranque; no convertir ese retraso transitorio en NONE.
        result.setTimeout(30);
        return result;
    }

    @PostConstruct
    void checkConfiguration() {
        if (properties.isEnabled() && !enabled()) {
            log.warn("Chatbot analytics deshabilitado: configurar secreto HMAC de al menos 32 bytes, ambiente y duraciones válidas.");
        }
    }

    private boolean enabled() {
        return properties.isEnabled() && properties.getHmacSecret() != null
                && properties.getHmacSecret().getBytes(StandardCharsets.UTF_8).length >= 32
                && properties.getEnvironment() != null && !properties.getEnvironment().isBlank()
                && properties.getEnvironment().length() <= 20 && properties.getSubjectKeyVersion() > 0
                && properties.getInactivityTimeout() != null && properties.getInactivityTimeout().isPositive()
                && properties.getRetention() != null && properties.getRetention().isPositive();
    }

    String subjectKey(String phone) {
        return hmac(PhoneUtils.normalizeArgentinePhone(phone));
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo calcular la identidad analítica");
        }
    }

    @Override
    public ChatbotInteraction begin(String phone, String messageId, boolean test) {
        if (!enabled()) return ChatbotInteraction.NONE;
        try {
            String subject = subjectKey(phone);
            String key = hmac(properties.getEnvironment() + ":" + properties.getSubjectKeyVersion()
                    + ":" + subject + ":" + (messageId == null || messageId.isBlank() ? UUID.randomUUID() : messageId));
            boolean internal = test || !"prod".equals(properties.getEnvironment())
                    || properties.getInternalPhones().stream().filter(p -> p != null && !p.isBlank())
                            .anyMatch(p -> subjectKey(p).equals(subject));
            // A concurrent first message can win the partial unique index; retry in a NEW transaction.
            for (int attempt = 0; attempt < 2; attempt++) {
                try {
                    UUID id = write.execute(tx -> beginLocked(subject, key, internal));
                    return id == null ? ChatbotInteraction.NONE : new ChatbotInteraction(this, id, key);
                } catch (DataIntegrityViolationException collision) {
                    if (attempt == 1) throw collision;
                }
            }
        } catch (RuntimeException exception) {
            failed(exception);
        }
        return ChatbotInteraction.NONE;
    }

    private UUID beginLocked(String subject, String key, boolean test) {
        var previous = events.findFirstByDedupeKeyOrderByOccurredAtAsc(key);
        if (previous.isPresent()) return previous.get().getSessionId();
        Instant now = clock.instant();
        var session = active(subject).orElse(null);
        if (session != null && !session.getExpiresAt().isAfter(now)) {
            expireLocked(session);
            sessions.flush(); // Release the partial-index slot before inserting the next episode.
            session = null;
        }
        if (session != null && events.existsBySessionIdAndDedupeKey(session.getId(), key)) return session.getId();
        boolean created = session == null;
        if (created) {
            session = new ChatbotAnalyticsSession();
            session.setId(UUID.randomUUID());
            session.setSubjectKey(subject);
            session.setSubjectKeyVersion(properties.getSubjectKeyVersion());
            session.setEnvironment(properties.getEnvironment());
            session.setSource(SOURCE);
            session.setStartedAt(now);
            session.setLastMilestone(ChatbotEventType.SESSION_STARTED.name());
        }
        session.setTest(session.isTest() || test);
        session.setLastInteractionAt(now.isBefore(session.getLastInteractionAt() == null ? now : session.getLastInteractionAt())
                ? session.getLastInteractionAt() : now);
        session.setExpiresAt(session.getLastInteractionAt().plus(properties.getInactivityTimeout()));
        session = sessions.saveAndFlush(session);
        if (created) insert(session.getId(), ChatbotEventType.SESSION_STARTED, now, null, null, "start:" + session.getId());
        // The lookup is repeated after the row lock for concurrent duplicate messages.
        if (!events.existsBySessionIdAndDedupeKey(session.getId(), key)) {
            insert(session.getId(), ChatbotEventType.MESSAGE_RECEIVED, now, null, null, key);
        }
        return session.getId();
    }

    private java.util.Optional<ChatbotAnalyticsSession> active(String subject) {
        return sessions.findFirstByEnvironmentAndSourceAndSubjectKeyVersionAndSubjectKeyAndStatus(
                properties.getEnvironment(), SOURCE, properties.getSubjectKeyVersion(), subject, "ACTIVE");
    }

    @Override
    public void record(ChatbotSignal signal) {
        if (!enabled() || signal == null || signal.sessionId() == null) return;
        try {
            publisher.publishEvent(new PendingSignal(signal, clock.instant()));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public record PendingSignal(ChatbotSignal signal, Instant occurredAt) {}

    /** Works immediately without a transaction and after commit when business owns one. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void afterCommit(PendingSignal pending) {
        try {
            write.executeWithoutResult(tx -> recordLocked(pending));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    private void recordLocked(PendingSignal pending) {
        ChatbotSignal signal = pending.signal();
        var session = sessions.findForUpdate(signal.sessionId()).orElse(null);
        if (session == null) return; // Retention may have removed an old, delayed callback.
        String key = hmac(signal.type() == ChatbotEventType.BOOKING_CREATED
                ? "booking:" + signal.bookingGroupCode()
                : signal.interactionKey() + ":" + signal.type() + ":" + signal.step() + ":" + signal.reason());
        if (events.existsBySessionIdAndDedupeKey(session.getId(), key)) return;
        if (signal.type() == ChatbotEventType.BOOKING_CREATED
                && (signal.reservationId() == null || signal.bookingGroupCode() == null || signal.bookingGroupCode().isBlank())) {
            throw new IllegalArgumentException("Conversión sin referencia comercial");
        }
        insert(session.getId(), signal.type(), pending.occurredAt(), signal.step(), signal.reason(), key);
        if (signal.type() == ChatbotEventType.DRIVER_IDENTIFIED) session.setAudience("DRIVER");
        if (signal.type() == ChatbotEventType.PASSENGER_IDENTIFIED) session.setAudience("PASSENGER");
        if (!"ACTIVE".equals(session.getStatus())
                && (signal.type() != ChatbotEventType.BOOKING_CREATED || "COMPLETED".equals(session.getStatus()))) return;
        if (signal.type() == ChatbotEventType.STEP_CHANGED && signal.step() != null) session.setCurrentStep(signal.step());
        switch (signal.type()) {
            case BOOKING_STARTED -> {
                if (session.getBookingStartedAt() == null) session.setBookingStartedAt(pending.occurredAt());
            }
            case BOOKING_CREATED -> {
                session.setBookingGroupCode(signal.bookingGroupCode());
                session.setReservationId(signal.reservationId());
                session.setCompletedAt(pending.occurredAt());
                close(session, "COMPLETED", pending.occurredAt(), null);
            }
            case BOOKING_DECLINED -> close(session, "DECLINED", pending.occurredAt(), signal.reason());
            case HUMAN_HANDOFF -> close(session, "HANDED_OFF", pending.occurredAt(), signal.reason());
            case WAITLISTED -> close(session, "WAITLISTED", pending.occurredAt(), signal.reason());
            default -> { }
        }
        switch (signal.type()) {
            case BOOKING_STARTED, PRICE_REQUESTED, PRICE_SENT, ROUTE_SELECTED, DATE_SELECTED,
                    PASSENGER_DATA_COMPLETED, SUMMARY_SENT, BOOKING_CREATED -> session.setLastMilestone(signal.type().name());
            default -> { }
        }
    }

    private void insert(UUID sessionId, ChatbotEventType type, Instant at, String step, ChatbotReason reason, String key) {
        var event = new ChatbotAnalyticsEvent();
        event.setId(UUID.randomUUID());
        event.setSessionId(sessionId);
        event.setEventType(type.name());
        event.setOccurredAt(at);
        event.setStep(step);
        event.setReasonCode(reason == null ? null : reason.name());
        event.setDedupeKey(key);
        events.saveAndFlush(event);
    }

    private void close(ChatbotAnalyticsSession session, String status, Instant at, ChatbotReason reason) {
        session.setStatus(status);
        session.setEndedAt(at);
        session.setEndReason(reason == null ? null : reason.name());
    }

    private void expireLocked(ChatbotAnalyticsSession session) {
        insert(session.getId(), ChatbotEventType.SESSION_EXPIRED, session.getExpiresAt(),
                session.getCurrentStep(), ChatbotReason.INACTIVITY, "expire:" + session.getId());
        close(session, session.getBookingStartedAt() == null ? "EXPIRED" : "ABANDONED",
                session.getExpiresAt(), ChatbotReason.INACTIVITY);
    }

    @Override
    public void handoff(String phone) {
        if (!enabled()) return;
        try {
            String subject = subjectKey(phone);
            UUID id = write.execute(tx -> active(subject).map(ChatbotAnalyticsSession::getId).orElse(null));
            if (id != null) record(new ChatbotSignal(id, "operator:" + id,
                    ChatbotEventType.HUMAN_HANDOFF, null, ChatbotReason.OPERATOR, null, null));
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    @Scheduled(fixedDelayString = "${chatbot.analytics.cleanup-interval-ms:60000}")
    public void expireSessions() {
        if (!enabled()) return;
        try {
            List<UUID> ids = read.execute(tx -> sessions.findExpiredIds(clock.instant(), PageRequest.of(0, 200)));
            if (ids != null) for (UUID id : ids) {
                write.executeWithoutResult(tx -> sessions.findForUpdate(id).ifPresent(session -> {
                    if ("ACTIVE".equals(session.getStatus()) && !session.getExpiresAt().isAfter(clock.instant())) expireLocked(session);
                }));
            }
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    @Scheduled(fixedDelayString = "${chatbot.analytics.retention-interval-ms:86400000}")
    public void purgeExpiredRetention() {
        if (!enabled()) return;
        try {
            List<UUID> ids = read.execute(tx -> sessions.findRetainedIds(
                    clock.instant().minus(properties.getRetention()), PageRequest.of(0, 500)));
            if (ids != null) for (UUID id : ids) write.executeWithoutResult(tx -> {
                sessions.findForUpdate(id).ifPresent(session -> {
                    events.deleteBySessionId(id);
                    sessions.delete(session);
                });
            });
        } catch (RuntimeException exception) {
            failed(exception);
        }
    }

    public long getFailureCount() { return failures.get(); }

    private void failed(RuntimeException exception) {
        // Do not log messages/stack traces: JDBC errors may contain bound values.
        log.warn("Fallo de telemetría de chatbot; operación comercial preservada. tipo={}, total={}",
                exception.getClass().getSimpleName(), failures.incrementAndGet());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/JpaBankPaymentReservationAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.payment.BankPaymentReservationPort;
import com.lunaris.ansenuza.application.payment.ReservationPaymentCandidate;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBankPaymentReservationAdapter implements BankPaymentReservationPort {

    private final ReservationRepository repository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public JpaBankPaymentReservationAdapter(
            ReservationRepository repository,
            ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.repository = repository;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    @Override
    public Optional<ReservationPaymentCandidate> findByReservationCode(String reservationCode) {
        String groupCode = groupCode(reservationCode);
        if (groupCode != null) {
            List<Reservation> group = repository.findReservationGroupForUpdate(groupCode);
            return group.stream()
                    .filter(reservation -> reservationCode.equalsIgnoreCase(reservation.getReservationCode()))
                    .findFirst()
                    .map(selected -> new ReservationPaymentCandidate(selected.getId(), expectedTotal(group)));
        }

        return repository.findByReservationCodeForUpdate(reservationCode)
                .map(reservation -> new ReservationPaymentCandidate(
                        reservation.getId(), expectedTotal(List.of(reservation))));
    }

    @Override
    public void confirm(String reservationCode) {
        repository.findByReservationCode(reservationCode)
                .ifPresent(reservation -> confirmPaymentUseCase.execute(reservation.getId()));
    }

    private BigDecimal expectedTotal(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> nullSafe(reservation.getAmount())
                        .add(nullSafe(reservation.getExtraAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullSafe(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private String groupCode(String reservationCode) {
        if (reservationCode.endsWith("-IDA")) {
            return reservationCode.substring(0, reservationCode.length() - 4);
        }
        if (reservationCode.endsWith("-VUELTA")) {
            return reservationCode.substring(0, reservationCode.length() - 7);
        }
        return null;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/JpaPaymentAuditOutboxAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.application.payment.PaymentAuditOutboxPort;
import com.lunaris.ansenuza.application.payment.PaymentConfirmedEvent;
import com.lunaris.ansenuza.application.payment.PaymentDetectedAuditRecord;
import com.lunaris.ansenuza.infrastructure.persistence.entity.PaymentAuditOutboxEntity;
import com.lunaris.ansenuza.infrastructure.persistence.repository.PaymentAuditOutboxJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class JpaPaymentAuditOutboxAdapter implements PaymentAuditOutboxPort {

    private final PaymentAuditOutboxJpaRepository repository;
    private final ObjectMapper objectMapper;

    public JpaPaymentAuditOutboxAdapter(
            PaymentAuditOutboxJpaRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void appendAudit(PaymentDetectedAuditRecord record) {
        save(record.eventType(), record.transactionId(), record, record.occurredAt());
    }

    @Override
    public void appendConfirmed(PaymentConfirmedEvent event) {
        save("PAYMENT_CONFIRMED", event.transactionId(), event, event.occurredAt());
    }

    private void save(String eventType, String aggregateId, Object event, java.time.Instant occurredAt) {
        try {
            repository.save(new PaymentAuditOutboxEntity(
                    eventType, aggregateId, objectMapper.writeValueAsString(event), occurredAt));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize payment audit event", exception);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/ProcessedTransactionLedgerAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.application.payment.BankTransferNotification;
import com.lunaris.ansenuza.application.payment.ProcessedTransactionLedgerPort;
import com.lunaris.ansenuza.infrastructure.persistence.repository.ProcessedPaymentTransactionJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProcessedTransactionLedgerAdapter implements ProcessedTransactionLedgerPort {

    private final ProcessedPaymentTransactionJpaRepository repository;

    public ProcessedTransactionLedgerAdapter(ProcessedPaymentTransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean claim(BankTransferNotification notification) {
        return repository.claim(
                UUID.randomUUID(),
                notification.source(),
                notification.externalNotificationId(),
                notification.transactionId(),
                notification.reservationCode(),
                notification.amount(),
                notification.payerName(),
                notification.receivedAt()) == 1;
    }

    @Override
    public void recordOutcome(
            String source,
            String externalNotificationId,
            String status,
            UUID reservationId,
            BigDecimal expectedAmount,
            String detail) {
        repository.updateOutcome(source, externalNotificationId, status, reservationId,
                expectedAmount, detail, Instant.now());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/SpecialTripPersistenceAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import com.lunaris.ansenuza.infrastructure.persistence.mapper.SpecialTripPersistenceMapper;
import com.lunaris.ansenuza.infrastructure.persistence.repository.SpecialTripJpaRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SpecialTripPersistenceAdapter implements SpecialTripRepositoryPort {
    private final SpecialTripJpaRepository repository;
    private final SpecialTripPersistenceMapper mapper;

    @Override
    public SpecialTrip save(SpecialTrip specialTrip) {
        return mapper.toDomain(repository.save(mapper.toEntity(specialTrip)));
    }

    @Override
    public Optional<SpecialTrip> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SpecialTrip> findAll() {
        return repository.findAllByOrderByStartDateAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SpecialTrip> findActive() {
        return repository.findAllByActiveTrueOrderByStartDateAsc().stream().map(mapper::toDomain).toList();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/AssignedOrGeneratedUuid.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.UUID;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Preserves defensive UUID assignments while supporting Hibernate-generated IDs. */
@IdGeneratorType(AssignedOrGeneratedUuid.Generator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface AssignedOrGeneratedUuid {
    class Generator implements BeforeExecutionGenerator {
        @Override
        public Object generate(SharedSessionContractImplementor session, Object owner,
                Object currentValue, EventType eventType) {
            return currentValue == null ? UUID.randomUUID() : currentValue;
        }

        @Override
        public EnumSet<EventType> getEventTypes() {
            return EnumSet.of(EventType.INSERT);
        }

        @Override
        public boolean allowAssignedIdentifiers() {
            return true;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ChatbotAnalyticsEvent.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatbot_analytics_events",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "dedupe_key"}))
@Getter
@Setter
public class ChatbotAnalyticsEvent implements org.springframework.data.domain.Persistable<UUID> {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @AssignedOrGeneratedUuid
    private UUID id = UUID.randomUUID();
    @Column(name = "session_id", nullable = false) private UUID sessionId;
    @Column(nullable = false, length = 64) private String eventType;
    @Column(nullable = false) private Instant occurredAt;
    @Column(length = 64) private String step;
    @Column(length = 64) private String reasonCode;
    @Column(name = "dedupe_key", nullable = false, length = 128) private String dedupeKey;
    @Transient private boolean newEntity = true;
    @Override public boolean isNew() { return newEntity; }
    @PostLoad @PostPersist void markPersisted() { newEntity = false; }
    @PrePersist void initializeId() { if (id == null) id = UUID.randomUUID(); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ChatbotAnalyticsSession.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chatbot_analytics_sessions")
@Getter
@Setter
public class ChatbotAnalyticsSession implements org.springframework.data.domain.Persistable<UUID> {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @AssignedOrGeneratedUuid
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, length = 64) private String subjectKey;
    @Column(nullable = false) private short subjectKeyVersion;
    @Column(nullable = false, length = 20) private String environment;
    @Column(nullable = false, length = 20) private String source;
    @Column(name = "is_test", nullable = false) private boolean test;
    @Column(nullable = false, length = 20) private String audience = "UNKNOWN";
    @Column(nullable = false) private Instant startedAt;
    @Column(nullable = false) private Instant lastInteractionAt;
    @Column(nullable = false) private Instant expiresAt;
    private Instant bookingStartedAt;
    private Instant completedAt;
    private Instant endedAt;
    @Column(length = 64) private String currentStep;
    @Column(length = 64) private String lastMilestone;
    @Column(nullable = false, length = 24) private String status = "ACTIVE";
    @Column(length = 64) private String endReason;
    @Column(length = 40) private String bookingGroupCode;
    private UUID reservationId;
    @Transient private boolean newEntity = true;
    // An assigned UUID does not imply that the row already exists.
    @Override public boolean isNew() { return newEntity; }
    @PostLoad @PostPersist void markPersisted() { newEntity = false; }
    @PrePersist void initializeId() { if (id == null) id = UUID.randomUUID(); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/PaymentAuditOutboxEntity.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_audit_outbox")
public class PaymentAuditOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "aggregate_id", length = 120)
    private String aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean published;

    protected PaymentAuditOutboxEntity() {}

    public PaymentAuditOutboxEntity(String eventType, String aggregateId, String payload, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.createdAt = createdAt;
        this.published = false;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/ProcessedPaymentTransactionEntity.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_payment_transactions")
public class ProcessedPaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 40)
    private String source;

    @Column(name = "external_notification_id", nullable = false, length = 255)
    private String externalNotificationId;

    @Column(name = "transaction_id", nullable = false, length = 120)
    private String transactionId;

    @Column(name = "reservation_code", nullable = false, length = 40)
    private String reservationCode;

    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "received_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal receivedAmount;

    @Column(name = "expected_amount", precision = 14, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "payer_name", nullable = false, length = 180)
    private String payerName;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 500)
    private String detail;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/SpecialTripEntity.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "special_trips")
@Getter
@Setter
@NoArgsConstructor
public class SpecialTripEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 100)
    private String origin;
    @Column(length = 100)
    private String destination;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    @Column(name = "max_passengers")
    private Integer maxPassengers;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/entity/WhatsAppWebhookInboxEntity.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "whatsapp_webhook_inbox")
public class WhatsAppWebhookInboxEntity {

    @Id
    @Column(name = "message_id", length = 255, nullable = false)
    private String messageId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected WhatsAppWebhookInboxEntity() {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/mapper/SpecialTripPersistenceMapper.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.mapper;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.infrastructure.persistence.entity.SpecialTripEntity;
import org.springframework.stereotype.Component;

@Component
public class SpecialTripPersistenceMapper {
    public SpecialTripEntity toEntity(SpecialTrip domain) {
        SpecialTripEntity entity = new SpecialTripEntity();
        entity.setId(domain.id());
        entity.setTitle(domain.title());
        entity.setDescription(domain.description());
        entity.setOrigin(domain.origin());
        entity.setDestination(domain.destination());
        entity.setStartDate(domain.startDate());
        entity.setEndDate(domain.endDate());
        entity.setPrice(domain.price());
        entity.setMaxPassengers(domain.maxPassengers());
        entity.setImageUrl(domain.imageUrl());
        entity.setActive(domain.active());
        entity.setCreatedAt(domain.createdAt());
        return entity;
    }

    public SpecialTrip toDomain(SpecialTripEntity entity) {
        return new SpecialTrip(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getOrigin(),
                entity.getDestination(), entity.getStartDate(), entity.getEndDate(), entity.getPrice(),
                entity.getMaxPassengers(), entity.getImageUrl(), entity.isActive(), entity.getCreatedAt());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ChatbotAnalyticsEventRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ChatbotAnalyticsEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotAnalyticsEventRepository extends JpaRepository<ChatbotAnalyticsEvent, UUID> {
    boolean existsBySessionIdAndDedupeKey(UUID sessionId, String dedupeKey);
    Optional<ChatbotAnalyticsEvent> findFirstByDedupeKeyOrderByOccurredAtAsc(String dedupeKey);
    void deleteBySessionId(UUID sessionId);
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ChatbotAnalyticsSessionRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ChatbotAnalyticsSession;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatbotAnalyticsSessionRepository extends JpaRepository<ChatbotAnalyticsSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatbotAnalyticsSession> findFirstByEnvironmentAndSourceAndSubjectKeyVersionAndSubjectKeyAndStatus(
            String environment, String source, short version, String key, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ChatbotAnalyticsSession s where s.id = :id")
    Optional<ChatbotAnalyticsSession> findForUpdate(@Param("id") UUID id);

    @Query("select s.id from ChatbotAnalyticsSession s where s.status = 'ACTIVE' and s.expiresAt <= :now order by s.expiresAt")
    List<UUID> findExpiredIds(@Param("now") Instant now, Pageable page);

    @Query("select s.id from ChatbotAnalyticsSession s where s.endedAt < :cutoff order by s.endedAt")
    List<UUID> findRetainedIds(@Param("cutoff") Instant cutoff, Pageable page);
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/PaymentAuditOutboxJpaRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.PaymentAuditOutboxEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAuditOutboxJpaRepository
        extends JpaRepository<PaymentAuditOutboxEntity, UUID> {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/ProcessedPaymentTransactionJpaRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.ProcessedPaymentTransactionEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessedPaymentTransactionJpaRepository
        extends JpaRepository<ProcessedPaymentTransactionEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_payment_transactions
                (id, source, external_notification_id, transaction_id, reservation_code,
                 received_amount, payer_name, status, received_at)
            VALUES
                (:id, :source, :externalId, :transactionId, :reservationCode,
                 :amount, :payerName, 'RECEIVED', :receivedAt)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int claim(
            @Param("id") UUID id,
            @Param("source") String source,
            @Param("externalId") String externalId,
            @Param("transactionId") String transactionId,
            @Param("reservationCode") String reservationCode,
            @Param("amount") BigDecimal amount,
            @Param("payerName") String payerName,
            @Param("receivedAt") Instant receivedAt);

    @Modifying
    @Query("""
            UPDATE ProcessedPaymentTransactionEntity transaction
               SET transaction.status = :status,
                   transaction.reservationId = :reservationId,
                   transaction.expectedAmount = :expectedAmount,
                   transaction.detail = :detail,
                   transaction.processedAt = :processedAt
             WHERE transaction.source = :source
               AND transaction.externalNotificationId = :externalId
            """)
    int updateOutcome(
            @Param("source") String source,
            @Param("externalId") String externalId,
            @Param("status") String status,
            @Param("reservationId") UUID reservationId,
            @Param("expectedAmount") BigDecimal expectedAmount,
            @Param("detail") String detail,
            @Param("processedAt") Instant processedAt);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/SpecialTripJpaRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.SpecialTripEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialTripJpaRepository extends JpaRepository<SpecialTripEntity, Long> {
    List<SpecialTripEntity> findAllByOrderByStartDateAsc();
    List<SpecialTripEntity> findAllByActiveTrueOrderByStartDateAsc();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/persistence/repository/WhatsAppWebhookInboxRepository.java`

```java
package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.WhatsAppWebhookInboxEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WhatsAppWebhookInboxRepository
        extends JpaRepository<WhatsAppWebhookInboxEntity, String> {

    @Modifying
    @Query(value = """
            INSERT INTO whatsapp_webhook_inbox (message_id, received_at)
            VALUES (:messageId, :receivedAt)
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int claim(@Param("messageId") String messageId, @Param("receivedAt") Instant receivedAt);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/ReservationPersistenceAdapter.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence;

import com.lunaris.ansenuza.reservation.application.port.out.ReservationRepositoryPort;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.mapper.ReservationMapper;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.repository.SpringDataReservationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReservationPersistenceAdapter implements ReservationRepositoryPort {
    private final SpringDataReservationRepository repository;
    private final ReservationMapper mapper;
    public ReservationPersistenceAdapter(SpringDataReservationRepository repository, ReservationMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    @Override public Reservation save(Reservation reservation) { return mapper.toDomain(repository.save(mapper.toEntity(reservation))); }
    @Override @Transactional(readOnly = true) public Optional<Reservation> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override @Transactional(readOnly = true) public List<Reservation> findByPickupLocality(String locality) {
        return repository.findByPickupLocalityIgnoreCase(locality).stream().map(mapper::toDomain).toList();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/entity/ReservationEntity.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Representación exclusiva de persistencia de la tabla productiva existente. */
@Entity(name = "HexagonalReservationEntity")
@Table(name = "reservations")
public class ReservationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "passenger_id", nullable = false) private UUID passengerId;
    @Column(name = "driver_id") private UUID driverId;
    @Column(name = "travel_date") private LocalDate travelDate;
    @Column(name = "pickup_locality", nullable = false) private String pickupLocality;
    @Column(name = "pickup_address") private String pickupAddress;
    @Column(name = "destination", nullable = false) private String destination;
    @Column(name = "amount") private BigDecimal amount;
    @Column(name = "amount_is_group_total", nullable = false) private boolean amountIsGroupTotal;
    public boolean isAmountIsGroupTotal() { return amountIsGroupTotal; }
    public void setAmountIsGroupTotal(boolean value) { amountIsGroupTotal = value; }
    @Column(name = "round_trip") private Boolean roundTrip;
    @Column(name = "trip_type", length = 50) private String tripType;
    @Column(name = "return_date") private LocalDate returnDate;
    @Column(name = "extra_amount") private BigDecimal extraAmount;
    @Column(name = "promotion_code", length = 4) private String promotionCode;
    @Column(name = "promotion_id") private UUID promotionId;
    @Column(name = "promotion_discount_percentage") private Integer promotionDiscountPercentage;
    @Column(name = "discount_amount", nullable = false) private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "payment_verified", nullable = false) private Boolean paymentVerified;
    @Column(name = "status") private String status;
    @Column(name = "source", nullable = false, length = 20) private String source;
    @Column(name = "travel_status", nullable = false, length = 20) private String travelStatus;
    @Column(name = "notes") private String notes;
    @Column(name = "payment_receipt_url") private String paymentReceiptUrl;
    @Column(name = "waiting_list_entry_id") private Long waitingListEntryId;
    @Column(name = "payment_confirmed_at") private LocalDateTime paymentConfirmedAt;
    @Column(name = "companion_names", length = 500) private String companionNames;
    @Column(name = "passenger_count") private Integer passengerCount;
    @Column(name = "returned_passenger_count", nullable = false) private Integer returnedPassengerCount;
    @Column(name = "reservation_code", length = 20) private String reservationCode;
    @Column(name = "booking_group_code", length = 40) private String bookingGroupCode;
    @Column(name = "route_direction", length = 16) private String routeDirection;
    @Column(name = "return_audit_sent_at") private LocalDateTime returnAuditSentAt;
    @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "departure_schedule") private String departureSchedule;
    @Column(name = "route_sequence") private Integer routeSequence;
    @Column(name = "requires_invoice", nullable = false) private Boolean requiresInvoice;

    public ReservationEntity() { }

    @PrePersist
    void initializeDefaults() {
        if (id == null) id = UUID.randomUUID();
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;
        if (returnedPassengerCount == null || returnedPassengerCount < 0) returnedPassengerCount = 0;
        if (passengerCount == null || passengerCount < 1) passengerCount = 1;
        if (paymentVerified == null) paymentVerified = false;
        if (source == null) source = "MANUAL";
        if (travelStatus == null) travelStatus = "PENDING";
        if (requiresInvoice == null) requiresInvoice = true;
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public UUID getPassengerId(){return passengerId;} public void setPassengerId(UUID v){passengerId=v;}
    public UUID getDriverId(){return driverId;} public void setDriverId(UUID v){driverId=v;}
    public LocalDate getTravelDate(){return travelDate;} public void setTravelDate(LocalDate v){travelDate=v;}
    public String getPickupLocality(){return pickupLocality;} public void setPickupLocality(String v){pickupLocality=v;}
    public String getPickupAddress(){return pickupAddress;} public void setPickupAddress(String v){pickupAddress=v;}
    public String getDestination(){return destination;} public void setDestination(String v){destination=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public Boolean getRoundTrip(){return roundTrip;} public void setRoundTrip(Boolean v){roundTrip=v;}
    public String getTripType(){return tripType;} public void setTripType(String v){tripType=v;}
    public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;}
    public BigDecimal getExtraAmount(){return extraAmount;} public void setExtraAmount(BigDecimal v){extraAmount=v;}
    public String getPromotionCode(){return promotionCode;} public void setPromotionCode(String v){promotionCode=v;}
    public UUID getPromotionId(){return promotionId;} public void setPromotionId(UUID v){promotionId=v;}
    public Integer getPromotionDiscountPercentage(){return promotionDiscountPercentage;} public void setPromotionDiscountPercentage(Integer v){promotionDiscountPercentage=v;}
    public BigDecimal getDiscountAmount(){return discountAmount;} public void setDiscountAmount(BigDecimal v){discountAmount=v;}
    public Boolean getPaymentVerified(){return paymentVerified;} public void setPaymentVerified(Boolean v){paymentVerified=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getSource(){return source;} public void setSource(String v){source=v;}
    public String getTravelStatus(){return travelStatus;} public void setTravelStatus(String v){travelStatus=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public String getPaymentReceiptUrl(){return paymentReceiptUrl;} public void setPaymentReceiptUrl(String v){paymentReceiptUrl=v;}
    public Long getWaitingListEntryId(){return waitingListEntryId;} public void setWaitingListEntryId(Long v){waitingListEntryId=v;}
    public LocalDateTime getPaymentConfirmedAt(){return paymentConfirmedAt;} public void setPaymentConfirmedAt(LocalDateTime v){paymentConfirmedAt=v;}
    public String getCompanionNames(){return companionNames;} public void setCompanionNames(String v){companionNames=v;}
    public Integer getPassengerCount(){return passengerCount;} public void setPassengerCount(Integer v){passengerCount=v;}
    public Integer getReturnedPassengerCount(){return returnedPassengerCount;} public void setReturnedPassengerCount(Integer v){returnedPassengerCount=v;}
    public String getReservationCode(){return reservationCode;} public void setReservationCode(String v){reservationCode=v;}
    public String getBookingGroupCode(){return bookingGroupCode;} public void setBookingGroupCode(String v){bookingGroupCode=v;}
    public String getRouteDirection(){return routeDirection;} public void setRouteDirection(String v){routeDirection=v;}
    public LocalDateTime getReturnAuditSentAt(){return returnAuditSentAt;} public void setReturnAuditSentAt(LocalDateTime v){returnAuditSentAt=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public String getDepartureSchedule(){return departureSchedule;} public void setDepartureSchedule(String v){departureSchedule=v;}
    public Integer getRouteSequence(){return routeSequence;} public void setRouteSequence(Integer v){routeSequence=v;}
    public Boolean getRequiresInvoice(){return requiresInvoice;} public void setRequiresInvoice(Boolean v){requiresInvoice=v;}
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/mapper/ReservationMapper.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.mapper;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.domain.model.ReservationStatus;
import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationEntity toEntity(Reservation d) {
        ReservationEntity e = new ReservationEntity();
        e.setId(d.id()); e.setPassengerId(d.passengerId()); e.setDriverId(d.driverId()); e.setTravelDate(d.travelDate());
        e.setPickupLocality(d.pickupLocality()); e.setPickupAddress(d.pickupAddress()); e.setDestination(d.destination());
        e.setAmount(d.amount()); e.setAmountIsGroupTotal(d.amountIsGroupTotal()); e.setRoundTrip(d.roundTrip()); e.setTripType(d.tripType()); e.setReturnDate(d.returnDate());
        e.setExtraAmount(d.extraAmount()); e.setPromotionCode(d.promotionCode()); e.setPromotionId(d.promotionId());
        e.setPromotionDiscountPercentage(d.promotionDiscountPercentage()); e.setDiscountAmount(d.discountAmount());
        e.setPaymentVerified(d.paymentVerified()); e.setStatus(d.status().name()); e.setSource(d.source());
        e.setTravelStatus(d.travelStatus()); e.setNotes(d.notes()); e.setPaymentReceiptUrl(d.paymentReceiptUrl());
        e.setWaitingListEntryId(d.waitingListEntryId()); e.setPaymentConfirmedAt(d.paymentConfirmedAt());
        e.setCompanionNames(d.companionNames()); e.setPassengerCount(d.passengerCount());
        e.setReturnedPassengerCount(d.returnedPassengerCount()); e.setReservationCode(d.reservationCode());
        e.setBookingGroupCode(d.bookingGroupCode()); e.setRouteDirection(d.routeDirection());
        e.setReturnAuditSentAt(d.returnAuditSentAt()); e.setCreatedAt(d.createdAt()); e.setUpdatedAt(d.updatedAt());
        e.setDepartureSchedule(d.departureSchedule()); e.setRouteSequence(d.routeSequence()); e.setRequiresInvoice(d.requiresInvoice());
        return e;
    }

    public Reservation toDomain(ReservationEntity e) {
        return Reservation.builder(e.getPassengerId(), e.getPickupLocality(), e.getDestination())
                .id(e.getId()).driverId(e.getDriverId()).travelDate(e.getTravelDate()).pickupAddress(e.getPickupAddress())
                .amount(e.getAmount()).amountIsGroupTotal(e.isAmountIsGroupTotal()).roundTrip(e.getRoundTrip()).tripType(e.getTripType()).returnDate(e.getReturnDate())
                .extraAmount(e.getExtraAmount()).promotionCode(e.getPromotionCode()).promotionId(e.getPromotionId())
                .promotionDiscountPercentage(e.getPromotionDiscountPercentage()).discountAmount(e.getDiscountAmount())
                .paymentVerified(Boolean.TRUE.equals(e.getPaymentVerified())).status(ReservationStatus.fromPersistenceValue(e.getStatus()))
                .source(e.getSource()).travelStatus(e.getTravelStatus()).notes(e.getNotes()).paymentReceiptUrl(e.getPaymentReceiptUrl())
                .waitingListEntryId(e.getWaitingListEntryId()).paymentConfirmedAt(e.getPaymentConfirmedAt())
                .companionNames(e.getCompanionNames()).passengerCount(e.getPassengerCount() == null ? 1 : e.getPassengerCount())
                .returnedPassengerCount(e.getReturnedPassengerCount() == null ? 0 : e.getReturnedPassengerCount())
                .reservationCode(e.getReservationCode()).bookingGroupCode(e.getBookingGroupCode()).routeDirection(e.getRouteDirection())
                .returnAuditSentAt(e.getReturnAuditSentAt()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .departureSchedule(e.getDepartureSchedule()).routeSequence(e.getRouteSequence())
                .requiresInvoice(Boolean.TRUE.equals(e.getRequiresInvoice())).build();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/persistence/repository/SpringDataReservationRepository.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.repository;

import com.lunaris.ansenuza.reservation.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    List<ReservationEntity> findByPickupLocalityIgnoreCase(String locality);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Todos los escritores bloquean primero el viaje, luego los tramos por UUID ascendente. */
public interface CapacityRepository {
    record Trip(UUID id, int direction, String status, Instant closesAt) {}
    record Leg(UUID id, int ordinal) {}

    Optional<Trip> findTrip(UUID tripId);
    Optional<Trip> lockTrip(UUID tripId);
    List<Leg> findLegs(UUID tripId, List<Integer> ordinals);
    List<Leg> lockLegs(UUID tripId, List<Integer> ordinals);
    List<Integer> occupiedSeats(UUID legId);
    void insertReservation(UUID id, UUID tripId, Route route, PassengerHold passenger, Instant expiresAt);
    void insertSeat(UUID tripId, UUID legId, int seat, UUID reservationId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverOperationsRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverOperationsRepository {
    record Trip(UUID id, UUID driverId, String status, LocalDate date, Instant departureAt) {}
    record Ticket(UUID reservationId, UUID tripId, String status, Instant expiresAt, Instant consumedAt,
            Instant pickupAt, BigDecimal fare, BigDecimal commission, boolean approvedPayment) {}
    record RoutePassenger(UUID tripId, UUID reservationId, String name, String phone, String status,
            String pickupAddress, String dropoffAddress, Instant pickupAt, Instant dropoffAt) {}

    Optional<Trip> lockTrip(UUID tripId);
    Optional<Ticket> lockTicket(String tokenHash, UUID tripId);
    void consume(UUID reservationId, Instant now);
    void markCheckedIn(UUID reservationId, UUID driverId, Instant now);
    void earn(UUID reservationId, UUID driverId, BigDecimal amount, Instant now);
    List<RoutePassenger> routeSheet(UUID driverId, LocalDate date);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterurbanPaymentRepository {
    record Inbox(UUID id, String paymentId) {}
    record Reservation(UUID id, String status, BigDecimal fare, Instant holdExpiresAt, Instant qrExpiresAt,
            boolean completeSeats, String tripStatus) {}
    void enqueue(String eventKey, String paymentId);
    List<Inbox> pending();
    boolean lockPending(UUID inboxId);
    void lockPayment(String paymentId);
    List<Reservation> lockBooking(UUID bookingId);
    Optional<String> paymentStatus(String paymentId);
    Optional<UUID> paymentBooking(String paymentId);
    void recordPayment(PaymentQueryPort.Payment payment, UUID bookingId, String status);
    void markPaid(UUID reservationId);
    void revokeBooking(UUID bookingId, Instant now);
    void complete(UUID inboxId, String outcome);
    void retry(UUID inboxId);
    void review(String paymentId, String reason);
    boolean lockPaidReservation(UUID reservationId);
    boolean hasQr(UUID reservationId);
    void saveQr(UUID reservationId, String hash, Instant expiresAt, byte[] encryptedPng, String keyId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcCapacityRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** Sin @Repository: no se instancia si el módulo está apagado. */
public class JdbcCapacityRepository implements CapacityRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcCapacityRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<Trip> findTrip(UUID id) { return trip(id, false); }
    @Override public Optional<Trip> lockTrip(UUID id) { return trip(id, true); }

    private Optional<Trip> trip(UUID id, boolean lock) {
        return jdbc.query("SELECT id, direction, status, closes_at FROM interurban.trips WHERE id = :id"
                        + (lock ? " FOR UPDATE" : ""), Map.of("id", id),
                (rs, row) -> new Trip(rs.getObject("id", UUID.class), rs.getInt("direction"),
                        rs.getString("status"), rs.getTimestamp("closes_at").toInstant())).stream().findFirst();
    }

    @Override public List<Leg> findLegs(UUID id, List<Integer> ordinals) { return legs(id, ordinals, false); }
    @Override public List<Leg> lockLegs(UUID id, List<Integer> ordinals) { return legs(id, ordinals, true); }

    private List<Leg> legs(UUID id, List<Integer> ordinals, boolean lock) {
        return jdbc.query("""
                SELECT tl.id, cl.ordinal FROM interurban.trip_legs tl
                JOIN interurban.corridor_legs cl ON cl.id = tl.corridor_leg_id
                WHERE tl.trip_id = :id AND cl.ordinal IN (:ordinals)
                ORDER BY tl.id
                """ + (lock ? " FOR UPDATE OF tl" : ""), Map.of("id", id, "ordinals", ordinals),
                (rs, row) -> new Leg(rs.getObject("id", UUID.class), rs.getInt("ordinal")));
    }

    @Override public List<Integer> occupiedSeats(UUID legId) {
        return jdbc.queryForList("SELECT seat FROM interurban.leg_seats WHERE trip_leg_id = :id ORDER BY seat",
                Map.of("id", legId), Integer.class);
    }

    @Override public void insertReservation(UUID id, UUID tripId, Route route, PassengerHold p, Instant expiresAt) {
        jdbc.update("""
                INSERT INTO interurban.reservations
                (id, booking_id, trip_id, origin_stop, destination_stop, passenger_name, phone,
                 pickup_address, dropoff_address, fare_id, fare, commission, status, hold_expires_at)
                VALUES (:id, :booking, :trip, :origin, :destination, :name, :phone,
                        :pickup, :dropoff, :fareId, :fare, :commission, 'HELD', :expires)
                """, new MapSqlParameterSource().addValue("id", id).addValue("booking", p.bookingId())
                .addValue("trip", tripId).addValue("origin", route.origin()).addValue("destination", route.destination())
                .addValue("name", p.name()).addValue("phone", p.phone()).addValue("pickup", p.pickupAddress())
                .addValue("dropoff", p.dropoffAddress()).addValue("fareId", p.fareId()).addValue("fare", p.fare())
                .addValue("commission", p.commission()).addValue("expires", Timestamp.from(expiresAt)));
    }

    @Override public void insertSeat(UUID tripId, UUID legId, int seat, UUID reservationId) {
        jdbc.update("""
                INSERT INTO interurban.leg_seats(trip_id, trip_leg_id, seat, reservation_id)
                VALUES (:trip, :leg, :seat, :reservation)
                """, Map.of("trip", tripId, "leg", legId, "seat", seat, "reservation", reservationId));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcDriverOperationsRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcDriverOperationsRepository implements DriverOperationsRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public JdbcDriverOperationsRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<Trip> lockTrip(UUID tripId) {
        return jdbc.query("SELECT id,driver_id,status,service_date,departure_at FROM interurban.trips WHERE id=:id FOR UPDATE",
                Map.of("id", tripId), (rs, row) -> new Trip(rs.getObject("id", UUID.class), rs.getObject("driver_id", UUID.class),
                rs.getString("status"), rs.getObject("service_date", LocalDate.class), instant(rs, "departure_at"))).stream().findFirst();
    }

    @Override public Optional<Ticket> lockTicket(String hash, UUID tripId) {
        return jdbc.query("""
                SELECT r.id,r.trip_id,r.status,r.pickup_at,r.fare,r.commission,q.expires_at,q.consumed_at,
                       EXISTS(SELECT 1 FROM interurban.payments p WHERE p.booking_id=r.booking_id
                              AND p.status='APPROVED') AS approved_payment
                FROM interurban.reservations r JOIN interurban.qr_tokens q ON q.reservation_id=r.id
                WHERE q.token_hash=:hash AND r.trip_id=:trip FOR UPDATE OF r,q
                """, Map.of("hash", hash, "trip", tripId), (rs, row) -> new Ticket(rs.getObject("id", UUID.class),
                rs.getObject("trip_id", UUID.class), rs.getString("status"), instant(rs, "expires_at"),
                instant(rs, "consumed_at"), instant(rs, "pickup_at"), rs.getBigDecimal("fare"),
                rs.getBigDecimal("commission"), rs.getBoolean("approved_payment"))).stream().findFirst();
    }

    @Override public void consume(UUID id, Instant now) {
        if (jdbc.update("""
                UPDATE interurban.qr_tokens SET consumed_at=:now
                WHERE reservation_id=:id AND consumed_at IS NULL AND expires_at>:now
                """, Map.of("id", id, "now", Timestamp.from(now))) != 1) throw new QrAlreadyConsumedException();
    }

    @Override public void markCheckedIn(UUID id, UUID driverId, Instant now) {
        if (jdbc.update("""
                UPDATE interurban.reservations SET status='CHECKED_IN',checked_in_at=:now,checked_in_driver_id=:driver
                WHERE id=:id AND status='PAID'
                """, Map.of("id", id, "driver", driverId, "now", Timestamp.from(now))) != 1) throw new InvalidTripException();
    }

    @Override public void earn(UUID reservationId, UUID driverId, BigDecimal amount, Instant now) {
        // Sin ON CONFLICT DO NOTHING: una inconsistencia debe revertir el check-in completo.
        jdbc.update("""
                INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key,created_at)
                VALUES (:id,:driver,:reservation,'EARNED',:amount,:key,:now)
                """, Map.of("id", UUID.randomUUID(), "driver", driverId, "reservation", reservationId,
                "amount", amount, "key", "earned:" + reservationId, "now", Timestamp.from(now)));
    }

    @Override public List<RoutePassenger> routeSheet(UUID driverId, LocalDate date) {
        return jdbc.query("""
                SELECT r.trip_id,r.id,r.passenger_name,r.phone,r.status,r.pickup_address,r.dropoff_address,r.pickup_at,r.dropoff_at
                FROM interurban.reservations r JOIN interurban.trips t ON t.id=r.trip_id
                WHERE t.driver_id=:driver AND t.service_date=:date AND t.status IN ('ASSIGNED','COMPLETED')
                  AND (r.status='CHECKED_IN' OR (r.status='PAID' AND EXISTS
                      (SELECT 1 FROM interurban.payments p WHERE p.booking_id=r.booking_id AND p.status='APPROVED')))
                ORDER BY r.pickup_at NULLS LAST,r.trip_id,r.id
                """, Map.of("driver", driverId, "date", date), (rs, row) -> new RoutePassenger(rs.getObject("trip_id", UUID.class),
                rs.getObject("id", UUID.class), rs.getString("passenger_name"), rs.getString("phone"), rs.getString("status"),
                rs.getString("pickup_address"), rs.getString("dropoff_address"), instant(rs, "pickup_at"), instant(rs, "dropoff_at")));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/JdbcInterurbanPaymentRepository.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcInterurbanPaymentRepository implements InterurbanPaymentRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcInterurbanPaymentRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void enqueue(String eventKey, String paymentId) {
        jdbc.update("""
                INSERT INTO interurban.payment_inbox(id,event_id,payment_id) VALUES (:id,:event,:payment)
                ON CONFLICT (event_id) DO NOTHING
                """, Map.of("id", UUID.randomUUID(), "event", eventKey, "payment", paymentId));
    }

    @Override public List<Inbox> pending() {
        return jdbc.query("""
                SELECT id,payment_id FROM interurban.payment_inbox WHERE processed_at IS NULL
                ORDER BY attempts,received_at,id LIMIT 50
                """, Map.of(), (rs, row) -> new Inbox(rs.getObject("id", UUID.class), rs.getString("payment_id")));
    }

    @Override public boolean lockPending(UUID inboxId) {
        return !jdbc.queryForList("""
                SELECT id FROM interurban.payment_inbox WHERE id=:id AND processed_at IS NULL FOR UPDATE
                """, Map.of("id", inboxId), UUID.class).isEmpty();
    }

    @Override public void lockPayment(String paymentId) {
        // Serializa distintos eventos del mismo pago también entre réplicas.
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))",
                Map.of("key", "interurban-payment:" + paymentId), (rs, row) -> 1);
    }

    @Override public List<Reservation> lockBooking(UUID bookingId) {
        jdbc.queryForList("""
                SELECT t.id FROM interurban.trips t WHERE t.id IN
                    (SELECT trip_id FROM interurban.reservations WHERE booking_id=:booking)
                ORDER BY t.id FOR UPDATE OF t
                """, Map.of("booking", bookingId), UUID.class);
        return jdbc.query("""
                SELECT r.id,r.status,r.fare,r.hold_expires_at,t.status AS trip_status,
                       t.departure_at + INTERVAL '12 hours' AS qr_expires_at,
                       ((SELECT count(*) FROM interurban.leg_seats s WHERE s.reservation_id=r.id)
                         = abs(r.destination_stop-r.origin_stop)
                        AND (SELECT count(*) FROM interurban.leg_seats s
                             JOIN interurban.trip_legs tl ON tl.id=s.trip_leg_id
                             JOIN interurban.corridor_legs cl ON cl.id=tl.corridor_leg_id
                             WHERE s.reservation_id=r.id AND cl.ordinal > least(r.origin_stop,r.destination_stop)
                               AND cl.ordinal <= greatest(r.origin_stop,r.destination_stop))
                         = abs(r.destination_stop-r.origin_stop)) AS complete_seats
                FROM interurban.reservations r JOIN interurban.trips t ON t.id=r.trip_id
                WHERE r.booking_id=:booking ORDER BY r.id FOR UPDATE OF r
                """, Map.of("booking", bookingId), (rs, row) -> new Reservation(rs.getObject("id", UUID.class),
                rs.getString("status"), rs.getBigDecimal("fare"), rs.getTimestamp("hold_expires_at").toInstant(),
                rs.getTimestamp("qr_expires_at").toInstant(), rs.getBoolean("complete_seats"), rs.getString("trip_status")));
    }

    @Override public Optional<String> paymentStatus(String paymentId) {
        return jdbc.queryForList("SELECT status FROM interurban.payments WHERE payment_id=:id",
                Map.of("id", paymentId), String.class).stream().findFirst();
    }

    @Override public Optional<UUID> paymentBooking(String paymentId) {
        return jdbc.queryForList("SELECT booking_id FROM interurban.payments WHERE payment_id=:id",
                Map.of("id", paymentId), UUID.class).stream().findFirst();
    }

    @Override public void recordPayment(PaymentQueryPort.Payment p, UUID bookingId, String status) {
        jdbc.update("""
                INSERT INTO interurban.payments(id,payment_id,booking_id,amount,currency,status,verified_at)
                VALUES (:id,:payment,:booking,:amount,:currency,:status,CURRENT_TIMESTAMP)
                ON CONFLICT (payment_id) DO UPDATE SET status=EXCLUDED.status,verified_at=EXCLUDED.verified_at
                """, Map.of("id", UUID.randomUUID(), "payment", p.id(), "booking", bookingId,
                "amount", p.amount(), "currency", p.currency(), "status", status));
    }

    @Override public void markPaid(UUID reservationId) {
        if (jdbc.update("UPDATE interurban.reservations SET status='PAID' WHERE id=:id AND status='HELD'",
                Map.of("id", reservationId)) != 1) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }

    @Override public void revokeBooking(UUID bookingId, Instant now) {
        jdbc.update("""
                UPDATE interurban.qr_tokens SET expires_at=least(expires_at,:now)
                WHERE reservation_id IN (SELECT id FROM interurban.reservations WHERE booking_id=:booking)
                """, Map.of("now", Timestamp.from(now), "booking", bookingId));
        jdbc.update("""
                UPDATE interurban.reservations SET status='REFUNDED'
                WHERE booking_id=:booking AND status='PAID'
                """, Map.of("booking", bookingId));
    }

    @Override public void complete(UUID inboxId, String outcome) {
        jdbc.update("""
                UPDATE interurban.payment_inbox SET processed_at=CURRENT_TIMESTAMP,attempts=attempts+1,last_error=:outcome
                WHERE id=:id
                """, Map.of("id", inboxId, "outcome", outcome));
    }

    @Override public void retry(UUID inboxId) {
        jdbc.update("""
                UPDATE interurban.payment_inbox SET attempts=attempts+1,last_error='RETRY_REQUIRED'
                WHERE id=:id AND processed_at IS NULL
                """, Map.of("id", inboxId));
    }

    @Override public void review(String paymentId, String reason) {
        jdbc.update("""
                INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                VALUES (:id,:key,'PAYMENT_REVIEW_REQUIRED',:id,
                    jsonb_build_object('paymentId',CAST(:payment AS text),'reason',CAST(:reason AS text)))
                ON CONFLICT (event_key) DO NOTHING
                """, Map.of("id", UUID.randomUUID(), "key", "payment-review:" + paymentId + ":" + reason,
                "payment", paymentId, "reason", reason));
    }

    @Override public boolean lockPaidReservation(UUID reservationId) {
        return !jdbc.queryForList("SELECT id FROM interurban.reservations WHERE id=:id AND status='PAID' FOR UPDATE",
                Map.of("id", reservationId), UUID.class).isEmpty();
    }

    @Override public boolean hasQr(UUID reservationId) {
        return !jdbc.queryForList("SELECT reservation_id FROM interurban.qr_tokens WHERE reservation_id=:id",
                Map.of("id", reservationId), UUID.class).isEmpty();
    }

    @Override public void saveQr(UUID id, String hash, Instant expiresAt, byte[] png, String keyId) {
        jdbc.update("INSERT INTO interurban.qr_tokens(reservation_id,token_hash,expires_at) VALUES (:id,:hash,:expires)",
                Map.of("id", id, "hash", hash, "expires", Timestamp.from(expiresAt)));
        jdbc.update("INSERT INTO interurban.qr_artifacts(reservation_id,encrypted_png,key_id) VALUES (:id,:png,:key)",
                Map.of("id", id, "png", png, "key", keyId));
        jdbc.update("""
                INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                VALUES (:id,:key,'QR_READY',:reservation,jsonb_build_object('reservationId',CAST(:reservation AS text)))
                """, Map.of("id", UUID.randomUUID(), "key", "qr-ready:" + id, "reservation", id));
    }
}
```


## Archivo: `src/main/resources/db/migration/V100__add_missing_foreign_keys_and_indexes.sql`

```sql
-- V2 ya está ocupado por V2__drivers.sql y el historial llega hasta V99.
-- V100 evita versiones duplicadas y migraciones fuera de orden en Flyway.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'invoices'::regclass
          AND constraint_definition.confrelid = 'reservations'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'reservation_id'
    ) THEN
        ALTER TABLE invoices
            ADD CONSTRAINT fk_invoices_reservation
            FOREIGN KEY (reservation_id) REFERENCES reservations(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservation_events'::regclass
          AND constraint_definition.confrelid = 'reservations'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'reservation_id'
    ) THEN
        ALTER TABLE reservation_events
            ADD CONSTRAINT fk_reservation_events_reservation
            FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'promotion_usages'::regclass
          AND constraint_definition.confrelid = 'promotions'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'promotion_id'
    ) THEN
        ALTER TABLE promotion_usages
            ADD CONSTRAINT fk_promotion_usages_promotion
            FOREIGN KEY (promotion_id) REFERENCES promotions(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'passengers'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'passenger_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_passenger
            FOREIGN KEY (passenger_id) REFERENCES passengers(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'drivers'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'driver_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_driver
            FOREIGN KEY (driver_id) REFERENCES drivers(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = constraint_definition.conrelid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE constraint_definition.contype = 'f'
          AND constraint_definition.conrelid = 'reservations'::regclass
          AND constraint_definition.confrelid = 'promotions'::regclass
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'promotion_id'
    ) THEN
        ALTER TABLE reservations
            ADD CONSTRAINT fk_reservations_promotion
            FOREIGN KEY (promotion_id) REFERENCES promotions(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_conversation_sessions_phone_number
    ON conversation_sessions (phone_number);

CREATE INDEX IF NOT EXISTS idx_chat_messages_phone_number
    ON chat_messages (phone_number);

CREATE INDEX IF NOT EXISTS idx_passengers_phone
    ON passengers (phone);

CREATE INDEX IF NOT EXISTS idx_drivers_phone
    ON drivers (phone);

CREATE INDEX IF NOT EXISTS idx_reservations_travel_date_driver_status
    ON reservations (travel_date, driver_id, status);
```


## Archivo: `src/main/resources/db/migration/V101__extend_driver_applications.sql`

```sql
ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS locality VARCHAR(120);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS wants_direct_contact BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS insurance_file_url VARCHAR(500);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS green_card_file_url VARCHAR(500);

ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS criminal_record_file_url VARCHAR(500);

UPDATE driver_applications
SET locality = 'Sin especificar'
WHERE locality IS NULL OR BTRIM(locality) = '';

ALTER TABLE driver_applications
    ALTER COLUMN locality SET NOT NULL;
```


## Archivo: `src/main/resources/db/migration/V102__add_source_to_reservations_if_missing.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS source VARCHAR(50) DEFAULT 'WEB';
```


## Archivo: `src/main/resources/db/migration/V103__add_vehicle_year_to_driver_applications.sql`

```sql
ALTER TABLE driver_applications
    ADD COLUMN IF NOT EXISTS vehicle_year INTEGER;

UPDATE driver_applications
SET vehicle_year = 0
WHERE vehicle_year IS NULL;

ALTER TABLE driver_applications
    ALTER COLUMN vehicle_year SET NOT NULL;
```


## Archivo: `src/main/resources/db/migration/V104__add_trip_type_to_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS trip_type VARCHAR(50);

ALTER TABLE reservations ALTER COLUMN trip_type TYPE VARCHAR(50);
```


## Archivo: `src/main/resources/db/migration/V105__allow_company_vehicle_driver_applications.sql`

```sql
ALTER TABLE driver_applications
    ALTER COLUMN vehicle_year DROP NOT NULL;

ALTER TABLE driver_applications
    ALTER COLUMN license_plate DROP NOT NULL;
```


## Archivo: `src/main/resources/db/migration/V106__create_news_banners.sql`

```sql
CREATE TABLE news_banners (
    id UUID PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    title VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    valid_until DATE,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_news_banners_active_valid_until
    ON news_banners (active, valid_until);
```


## Archivo: `src/main/resources/db/migration/V107__create_waiting_list_entries.sql`

```sql
CREATE TABLE IF NOT EXISTS waiting_list_entries (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(30) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    travel_date DATE NOT NULL,
    pickup_locality VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    passenger_count INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_waiting_list_date_status
    ON waiting_list_entries (travel_date, status);
```


## Archivo: `src/main/resources/db/migration/V108__link_waiting_list_reengagement.sql`

```sql
ALTER TABLE conversation_sessions
    ADD COLUMN IF NOT EXISTS waiting_list_entry_id BIGINT;

ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS waiting_list_entry_id BIGINT;

ALTER TABLE reservations
    ADD CONSTRAINT fk_reservations_waiting_list_entry
    FOREIGN KEY (waiting_list_entry_id) REFERENCES waiting_list_entries(id);

CREATE INDEX IF NOT EXISTS idx_reservations_waiting_list_entry
    ON reservations (waiting_list_entry_id);
```


## Archivo: `src/main/resources/db/migration/V109__add_return_completion_tracking.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS returned_passenger_count INTEGER NOT NULL DEFAULT 0;

UPDATE reservations
SET returned_passenger_count = 0
WHERE returned_passenger_count IS NULL;
```


## Archivo: `src/main/resources/db/migration/V10__insert_airport_locality.sql`

```sql
INSERT INTO localities (name)
VALUES ('Aeropuerto Córdoba')
ON CONFLICT (name) DO NOTHING;
```


## Archivo: `src/main/resources/db/migration/V110__create_payment_email_audit.sql`

```sql
CREATE TABLE processed_payment_transactions (
    id                       UUID PRIMARY KEY,
    source                   VARCHAR(40) NOT NULL,
    external_notification_id VARCHAR(255) NOT NULL,
    transaction_id           VARCHAR(120) NOT NULL,
    reservation_code         VARCHAR(40) NOT NULL,
    reservation_id           UUID,
    received_amount          NUMERIC(14, 2) NOT NULL,
    expected_amount          NUMERIC(14, 2),
    payer_name               VARCHAR(180) NOT NULL,
    status                   VARCHAR(40) NOT NULL,
    detail                   VARCHAR(500),
    received_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at             TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_processed_payment_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE UNIQUE INDEX uq_processed_payment_external
    ON processed_payment_transactions(source, external_notification_id);

CREATE UNIQUE INDEX uq_processed_payment_transaction
    ON processed_payment_transactions(source, transaction_id);

CREATE INDEX idx_processed_payment_reservation
    ON processed_payment_transactions(reservation_id);

CREATE TABLE payment_audit_outbox (
    id           UUID PRIMARY KEY,
    event_type   VARCHAR(60) NOT NULL,
    aggregate_id VARCHAR(120),
    payload      TEXT NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published    BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payment_audit_outbox_pending
    ON payment_audit_outbox(published, created_at);
```


## Archivo: `src/main/resources/db/migration/V111__create_special_trips_table.sql`

```sql
CREATE TABLE special_trips (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    origin VARCHAR(100),
    destination VARCHAR(100),
    start_date DATE,
    end_date DATE,
    price DECIMAL(10,2),
    max_passengers INT,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_special_trips_active_start_date
    ON special_trips (active, start_date);
```


## Archivo: `src/main/resources/db/migration/V112__add_arrufo_locality_and_constraints.sql`

```sql
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition
          ON table_definition.oid = constraint_definition.conrelid
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = table_definition.oid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE table_definition.relname = 'localities'
          AND constraint_definition.contype = 'u'
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'name'
    ) THEN
        ALTER TABLE localities
            ADD CONSTRAINT uq_localities_name UNIQUE (name);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition
          ON table_definition.oid = constraint_definition.conrelid
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = table_definition.oid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE table_definition.relname = 'fares'
          AND constraint_definition.contype = 'u'
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'locality_name'
    ) THEN
        ALTER TABLE fares
            ADD CONSTRAINT uq_fares_locality_name UNIQUE (locality_name);
    END IF;
END $$;

INSERT INTO localities (name, kms_to_cordoba, minutes_from_origin)
VALUES ('Arrufó', 344, -35)
ON CONFLICT (name) DO UPDATE
SET kms_to_cordoba = EXCLUDED.kms_to_cordoba,
    minutes_from_origin = EXCLUDED.minutes_from_origin;

INSERT INTO fares (id, locality_name, amount)
VALUES (gen_random_uuid(), 'Arrufó', 105000.00)
ON CONFLICT (locality_name) DO UPDATE
SET amount = EXCLUDED.amount;
```


## Archivo: `src/main/resources/db/migration/V113__link_reservation_booking_groups.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS booking_group_code VARCHAR(40);

UPDATE reservations
SET booking_group_code = REGEXP_REPLACE(reservation_code, '-(IDA|VUELTA)$', '')
WHERE booking_group_code IS NULL
  AND reservation_code ~ '-(IDA|VUELTA)$';

CREATE INDEX IF NOT EXISTS idx_reservations_booking_group_code
    ON reservations(booking_group_code);
```


## Archivo: `src/main/resources/db/migration/V114__capacity_locks_and_route_scope.sql`

```sql
CREATE TABLE IF NOT EXISTS reservation_capacity_locks (
    lock_key VARCHAR(255) PRIMARY KEY
);

ALTER TABLE reservations ADD COLUMN IF NOT EXISTS route_direction VARCHAR(16);
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS return_audit_sent_at TIMESTAMP;

UPDATE reservations
SET route_direction = CASE
    WHEN LOWER(REPLACE(pickup_locality, 'ó', 'o')) LIKE '%cordoba%'
         AND LOWER(REPLACE(destination, 'ó', 'o')) NOT LIKE '%cordoba%' THEN 'VUELTA'
    WHEN LOWER(REPLACE(destination, 'ó', 'o')) LIKE '%cordoba%'
         AND LOWER(REPLACE(pickup_locality, 'ó', 'o')) NOT LIKE '%cordoba%' THEN 'IDA'
    ELSE route_direction
END
WHERE route_direction IS NULL;

ALTER TABLE reservations DROP CONSTRAINT IF EXISTS uk_reservations_driver_date_route_sequence;
CREATE UNIQUE INDEX IF NOT EXISTS uk_reservations_driver_route_scope
    ON reservations(driver_id, travel_date, departure_schedule, route_direction, route_sequence)
    WHERE driver_id IS NOT NULL AND route_sequence IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_driver_route_scope
    ON reservations(driver_id, travel_date, departure_schedule, route_direction);
```


## Archivo: `src/main/resources/db/migration/V115__extend_invoice_pdf_url.sql`

```sql
ALTER TABLE invoices
    ALTER COLUMN pdf_url TYPE VARCHAR(1000);
```


## Archivo: `src/main/resources/db/migration/V116__waiting_list_special_events.sql`

```sql
ALTER TABLE waiting_list_entries
    ALTER COLUMN travel_date DROP NOT NULL;

ALTER TABLE waiting_list_entries
    ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

ALTER TABLE waiting_list_entries
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_waiting_list_active_created
    ON waiting_list_entries(status, created_at DESC);
```


## Archivo: `src/main/resources/db/migration/V117__extend_news_banners_for_waiting_lists.sql`

```sql
ALTER TABLE news_banners
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS has_waiting_list BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE news_banners
SET event_type = UPPER(REGEXP_REPLACE(TRIM(title), '[^A-Za-z0-9]+', '_', 'g'))
WHERE event_type IS NULL OR TRIM(event_type) = '';

ALTER TABLE news_banners
    ALTER COLUMN event_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_news_banners_event_type
    ON news_banners (event_type);
```


## Archivo: `src/main/resources/db/migration/V118__optimize_fares_and_localities_indexes.sql`

```sql
UPDATE localities
SET name = TRIM(name);

UPDATE fares
SET locality_name = TRIM(locality_name);

CREATE INDEX IF NOT EXISTS idx_fares_locality_amount
    ON fares (UPPER(locality_name), amount);
```


## Archivo: `src/main/resources/db/migration/V119__financial_hardening_and_immutable_events.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS payment_expires_at TIMESTAMP WITHOUT TIME ZONE;

UPDATE reservations
SET payment_expires_at = created_at + INTERVAL '20 minutes'
WHERE payment_verified = FALSE
  AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION', 'PAYMENT_RECEIVED')
  AND payment_expires_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reservations_payment_expiration
    ON reservations(payment_expires_at)
    WHERE payment_verified = FALSE
      AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION', 'PAYMENT_RECEIVED');

-- Un comprobante puede compartirse entre los dos tramos del mismo grupo, pero no
-- reutilizarse como comprobante canónico de otra reserva activa.
CREATE OR REPLACE FUNCTION prevent_duplicate_active_receipt()
RETURNS trigger AS $$
BEGIN
    IF NEW.payment_receipt_url IS NOT NULL AND NEW.payment_receipt_url <> ''
       AND UPPER(COALESCE(NEW.status, '')) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED')
       AND EXISTS (
           SELECT 1 FROM reservations existing
           WHERE existing.id <> NEW.id
             AND existing.payment_receipt_url = NEW.payment_receipt_url
             AND UPPER(COALESCE(existing.status, '')) NOT IN ('CANCELLED', 'EXPIRED', 'REJECTED')
             AND (NEW.booking_group_code IS NULL
                  OR existing.booking_group_code IS NULL
                  OR existing.booking_group_code <> NEW.booking_group_code)
       ) THEN
        RAISE EXCEPTION 'El comprobante ya está vinculado a otra reserva activa';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_unique_active_receipt ON reservations;
CREATE TRIGGER trg_unique_active_receipt
BEFORE INSERT OR UPDATE OF payment_receipt_url, status, booking_group_code ON reservations
FOR EACH ROW EXECUTE FUNCTION prevent_duplicate_active_receipt();

CREATE OR REPLACE FUNCTION prevent_reservation_event_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'reservation_events es un registro inmutable';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservation_events_immutable ON reservation_events;
CREATE TRIGGER trg_reservation_events_immutable
BEFORE UPDATE OR DELETE ON reservation_events
FOR EACH ROW EXECUTE FUNCTION prevent_reservation_event_mutation();

CREATE OR REPLACE FUNCTION audit_reservation_mutation()
RETURNS trigger AS $$
BEGIN
    IF ROW(OLD.*) IS DISTINCT FROM ROW(NEW.*) THEN
        INSERT INTO reservation_events (
            id, reservation_id, event_type, description, created_at, triggered_by)
        VALUES (
            md5(random()::text || clock_timestamp()::text)::uuid,
            NEW.id,
            CASE
                WHEN OLD.status IS DISTINCT FROM NEW.status
                  OR OLD.travel_status IS DISTINCT FROM NEW.travel_status
                    THEN 'STATE_CHANGED'
                WHEN OLD.payment_verified IS DISTINCT FROM NEW.payment_verified
                    THEN 'PAYMENT_VERIFICATION_CHANGED'
                ELSE 'RESERVATION_MODIFIED'
            END,
            LEFT('Cambio BD: status=' || COALESCE(OLD.status, 'NULL') || '->'
                || COALESCE(NEW.status, 'NULL') || ', travel_status='
                || COALESCE(OLD.travel_status, 'NULL') || '->'
                || COALESCE(NEW.travel_status, 'NULL'), 500),
            NOW(),
            'DB_SYSTEM');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reservations_audit ON reservations;
CREATE TRIGGER trg_reservations_audit
AFTER UPDATE ON reservations
FOR EACH ROW EXECUTE FUNCTION audit_reservation_mutation();
```


## Archivo: `src/main/resources/db/migration/V11__add_reservation_fields_to_conversation.sql`

```sql
ALTER TABLE conversation_sessions
ADD COLUMN pickup_locality VARCHAR(100);

ALTER TABLE conversation_sessions
ADD COLUMN pickup_address VARCHAR(255);

ALTER TABLE conversation_sessions
ADD COLUMN destination VARCHAR(100);

ALTER TABLE conversation_sessions
ADD COLUMN travel_date DATE;
```


## Archivo: `src/main/resources/db/migration/V120__enforce_single_invoice_per_reservation.sql`

```sql
WITH duplicated_invoices AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY reservation_id
               ORDER BY created_at DESC NULLS LAST, id
           ) AS row_number
    FROM invoices
)
DELETE FROM invoices
WHERE id IN (
    SELECT id FROM duplicated_invoices WHERE row_number > 1
);

DROP INDEX IF EXISTS idx_invoices_reservation_id;

CREATE UNIQUE INDEX uk_invoices_reservation_id
    ON invoices(reservation_id);
```


## Archivo: `src/main/resources/db/migration/V121__track_balance_used_by_reservations.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS used_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00;

DROP INDEX IF EXISTS idx_reservations_payment_expiration;
CREATE INDEX IF NOT EXISTS idx_reservations_payment_expiration
    ON reservations(payment_expires_at)
    WHERE payment_verified = FALSE
      AND UPPER(status) IN ('PENDING_PAYMENT', 'PENDING_VERIFICATION');
```


## Archivo: `src/main/resources/db/migration/V122__create_whatsapp_webhook_inbox.sql`

```sql
CREATE TABLE IF NOT EXISTS whatsapp_webhook_inbox (
    message_id  VARCHAR(255) PRIMARY KEY,
    received_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_webhook_inbox_received_at
    ON whatsapp_webhook_inbox(received_at);
```


## Archivo: `src/main/resources/db/migration/V123__create_inquiries.sql`

```sql
CREATE TABLE inquiries (
    id UUID PRIMARY KEY,
    passenger_id UUID REFERENCES passengers(id),
    phone VARCHAR(255) NOT NULL,
    passenger_name VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'ARCHIVED')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_inquiries_created_at ON inquiries(created_at DESC);
CREATE INDEX idx_inquiries_status ON inquiries(status);
CREATE INDEX idx_inquiries_passenger_id ON inquiries(passenger_id);
```


## Archivo: `src/main/resources/db/migration/V124__operator_notification_phones.sql`

```sql
CREATE TABLE operator_notification_phones (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO operator_notification_phones (id, name, phone, active)
VALUES ('b653034c-f32f-4cf9-a6e1-4c53ea1c0c8d', 'Ignacio', '5493512282251', TRUE);
```


## Archivo: `src/main/resources/db/migration/V125__manual_bot_pause.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS manually_paused BOOLEAN NOT NULL DEFAULT false;
```


## Archivo: `src/main/resources/db/migration/V126__reservation_amount_scope.sql`

```sql
-- Los escritores actuales guardan importes por tramo. No se infiere duplicación por igualdad:
-- dos mitades iguales también representan una reserva correcta.
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS amount_is_group_total BOOLEAN NOT NULL DEFAULT false;
```


## Archivo: `src/main/resources/db/migration/V127__chatbot_analytics.sql`

```sql
CREATE TABLE chatbot_analytics_sessions (
    id UUID PRIMARY KEY,
    subject_key VARCHAR(64) NOT NULL,
    subject_key_version SMALLINT NOT NULL,
    environment VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    is_test BOOLEAN NOT NULL DEFAULT FALSE,
    audience VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',

    started_at TIMESTAMPTZ NOT NULL,
    last_interaction_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    booking_started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,

    current_step VARCHAR(64),
    last_milestone VARCHAR(64),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN (
            'ACTIVE', 'COMPLETED', 'ABANDONED', 'EXPIRED',
            'DECLINED', 'HANDED_OFF', 'WAITLISTED'
        )),
    end_reason VARCHAR(64),
    booking_group_code VARCHAR(40),
    reservation_id UUID,

    CHECK (last_interaction_at >= started_at),
    CHECK (expires_at >= last_interaction_at),
    CHECK ((status = 'ACTIVE') = (ended_at IS NULL)),
    CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_chatbot_active_subject
    ON chatbot_analytics_sessions (
        environment, source, subject_key_version, subject_key
    )
    WHERE status = 'ACTIVE';

CREATE INDEX idx_chatbot_sessions_started
    ON chatbot_analytics_sessions(started_at);

CREATE INDEX idx_chatbot_sessions_expiry
    ON chatbot_analytics_sessions(expires_at)
    WHERE status = 'ACTIVE';

CREATE TABLE chatbot_analytics_events (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL
        REFERENCES chatbot_analytics_sessions(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    step VARCHAR(64),
    reason_code VARCHAR(64),
    dedupe_key VARCHAR(128) NOT NULL,
    UNIQUE (session_id, dedupe_key)
);

CREATE INDEX idx_chatbot_events_session_time
    ON chatbot_analytics_events(session_id, occurred_at);
```


## Archivo: `src/main/resources/db/migration/V128__manual_reservation_notifications.sql`

```sql
ALTER TABLE reservations ADD COLUMN invoice_url VARCHAR(2048);
ALTER TABLE reservations ADD COLUMN manual_notification_pending BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN manual_notification_waiting_reply BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE reservations ADD COLUMN manual_notification_attempt_at TIMESTAMP;
ALTER TABLE invoices ADD COLUMN authorization_code VARCHAR(40);
CREATE INDEX idx_reservations_manual_notification_pending
    ON reservations (manual_notification_attempt_at, created_at)
    WHERE manual_notification_pending = TRUE;
```


## Archivo: `src/main/resources/db/migration/V129__add_interurban_corridor.sql`

```sql
-- Sólo objetos nuevos, aislados de public y de los repositorios de Córdoba.
-- Una reserva representa UN pasajero; booking_id agrupa acompañantes.
CREATE SCHEMA interurban;

CREATE TABLE interurban.corridor_legs (
    id UUID PRIMARY KEY,
    code VARCHAR(16) NOT NULL UNIQUE,
    ordinal SMALLINT NOT NULL UNIQUE CHECK (ordinal BETWEEN 1 AND 3),
    origin VARCHAR(40) NOT NULL,
    destination VARCHAR(40) NOT NULL
);
INSERT INTO interurban.corridor_legs VALUES
 ('00000000-0000-0000-0000-000000000001', 'SG-SUA', 1, 'San Guillermo', 'Suardi'),
 ('00000000-0000-0000-0000-000000000002', 'SUA-MOR', 2, 'Suardi', 'Morteros'),
 ('00000000-0000-0000-0000-000000000003', 'MOR-BRI', 3, 'Morteros', 'Brinkmann');

-- Paradas 0=SG, 1=SUA, 2=MOR, 3=BRI. Tarifas direccionales versionadas.
CREATE TABLE interurban.interurban_fares (
    id UUID PRIMARY KEY,
    origin_stop SMALLINT NOT NULL CHECK (origin_stop BETWEEN 0 AND 3),
    destination_stop SMALLINT NOT NULL CHECK (destination_stop BETWEEN 0 AND 3),
    valid_from DATE NOT NULL,
    fare NUMERIC(12,2) NOT NULL CHECK (fare >= 0),
    commission NUMERIC(12,2) NOT NULL CHECK (commission >= 0 AND commission <= fare),
    currency CHAR(3) NOT NULL DEFAULT 'ARS' CHECK (currency = 'ARS'),
    CHECK (origin_stop <> destination_stop),
    UNIQUE (origin_stop, destination_stop, valid_from)
);

CREATE TABLE interurban.trips (
    id UUID PRIMARY KEY,
    service_date DATE NOT NULL,
    departure_at TIMESTAMPTZ NOT NULL,
    closes_at TIMESTAMPTZ NOT NULL CHECK (closes_at <= departure_at),
    direction SMALLINT NOT NULL CHECK (direction IN (-1, 1)),
    status VARCHAR(24) NOT NULL CHECK (status IN ('OPEN', 'CLOSED', 'ASSIGNED', 'COMPLETED', 'CANCELLED')),
    driver_id UUID,
    vehicle_id UUID,
    assignment_source VARCHAR(24) CHECK (assignment_source IN ('LOCAL', 'POSITIONING', 'LUNARIS')),
    CHECK (status NOT IN ('ASSIGNED', 'COMPLETED') OR
        (driver_id IS NOT NULL AND vehicle_id IS NOT NULL AND assignment_source IS NOT NULL))
);
CREATE INDEX interurban_trip_schedule ON interurban.trips(service_date, direction, departure_at);

CREATE TABLE interurban.trip_legs (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    corridor_leg_id UUID NOT NULL REFERENCES interurban.corridor_legs(id),
    capacity SMALLINT NOT NULL DEFAULT 4 CHECK (capacity = 4),
    UNIQUE (trip_id, corridor_leg_id),
    UNIQUE (id, trip_id)
);

CREATE TABLE interurban.reservations (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    trip_id UUID NOT NULL REFERENCES interurban.trips(id),
    origin_stop SMALLINT NOT NULL CHECK (origin_stop BETWEEN 0 AND 3),
    destination_stop SMALLINT NOT NULL CHECK (destination_stop BETWEEN 0 AND 3),
    passenger_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    pickup_address VARCHAR(255) NOT NULL,
    dropoff_address VARCHAR(255) NOT NULL,
    pickup_at TIMESTAMPTZ,
    dropoff_at TIMESTAMPTZ,
    fare_id UUID NOT NULL REFERENCES interurban.interurban_fares(id),
    fare NUMERIC(12,2) NOT NULL CHECK (fare >= 0),
    commission NUMERIC(12,2) NOT NULL CHECK (commission >= 0 AND commission <= fare),
    status VARCHAR(24) NOT NULL CHECK (status IN ('HELD', 'PAID', 'CHECKED_IN', 'EXPIRED', 'CANCELLED', 'REFUNDED')),
    hold_expires_at TIMESTAMPTZ NOT NULL,
    checked_in_at TIMESTAMPTZ,
    checked_in_driver_id UUID,
    CHECK (origin_stop <> destination_stop),
    CHECK (status <> 'CHECKED_IN' OR (checked_in_at IS NOT NULL AND checked_in_driver_id IS NOT NULL)),
    UNIQUE (id, trip_id)
);
CREATE INDEX interurban_reservation_booking ON interurban.reservations(booking_id);
CREATE INDEX interurban_reservation_trip ON interurban.reservations(trip_id, pickup_at);
CREATE INDEX interurban_hold_expiry ON interurban.reservations(hold_expires_at) WHERE status = 'HELD';

-- La PK y el CHECK impiden un quinto pasajero incluso con escritores concurrentes.
-- No hay contador duplicado: la ocupación se deriva de estas filas.
CREATE TABLE interurban.leg_seats (
    trip_id UUID NOT NULL,
    trip_leg_id UUID NOT NULL,
    seat SMALLINT NOT NULL CHECK (seat BETWEEN 1 AND 4),
    reservation_id UUID NOT NULL,
    PRIMARY KEY (trip_leg_id, seat),
    UNIQUE (trip_leg_id, reservation_id),
    FOREIGN KEY (trip_leg_id, trip_id) REFERENCES interurban.trip_legs(id, trip_id),
    FOREIGN KEY (reservation_id, trip_id) REFERENCES interurban.reservations(id, trip_id)
);
CREATE INDEX interurban_seat_reservation ON interurban.leg_seats(reservation_id);

CREATE TABLE interurban.payment_inbox (
    id UUID PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL UNIQUE,
    payment_id VARCHAR(128) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    last_error VARCHAR(500)
);
CREATE TABLE interurban.payments (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(128) NOT NULL UNIQUE,
    booking_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    currency CHAR(3) NOT NULL CHECK (currency = 'ARS'),
    status VARCHAR(32) NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE interurban.qr_tokens (
    reservation_id UUID PRIMARY KEY REFERENCES interurban.reservations(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ
);

CREATE TABLE interurban.payout_orders (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    settlement_date DATE NOT NULL,
    amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    status VARCHAR(24) NOT NULL CHECK (status IN ('PENDING', 'SENDING', 'UNKNOWN', 'PAID', 'FAILED')),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    provider_reference VARCHAR(128) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (driver_id, settlement_date)
);
CREATE TABLE interurban.driver_ledger (
    id UUID PRIMARY KEY,
    driver_id UUID NOT NULL,
    reservation_id UUID REFERENCES interurban.reservations(id),
    payout_order_id UUID REFERENCES interurban.payout_orders(id),
    entry_type VARCHAR(24) NOT NULL CHECK (entry_type IN ('EARNED', 'REVERSAL', 'PAYOUT')),
    amount NUMERIC(14,2) NOT NULL,
    event_key VARCHAR(160) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK ((entry_type = 'EARNED' AND amount >= 0 AND reservation_id IS NOT NULL AND payout_order_id IS NULL)
        OR (entry_type = 'REVERSAL' AND amount <= 0 AND reservation_id IS NOT NULL AND payout_order_id IS NULL)
        OR (entry_type = 'PAYOUT' AND amount < 0 AND payout_order_id IS NOT NULL AND reservation_id IS NULL))
);
CREATE UNIQUE INDEX interurban_one_earning ON interurban.driver_ledger(reservation_id) WHERE entry_type = 'EARNED';
CREATE UNIQUE INDEX interurban_one_payout ON interurban.driver_ledger(payout_order_id) WHERE entry_type = 'PAYOUT';
CREATE INDEX interurban_driver_balance ON interurban.driver_ledger(driver_id, created_at);
CREATE TABLE interurban.payout_items (
    ledger_id UUID PRIMARY KEY REFERENCES interurban.driver_ledger(id),
    payout_order_id UUID NOT NULL REFERENCES interurban.payout_orders(id)
);
CREATE FUNCTION interurban.reject_ledger_mutation() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'driver_ledger es inmutable; registrar un asiento compensatorio';
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER interurban_immutable_ledger BEFORE UPDATE OR DELETE ON interurban.driver_ledger
    FOR EACH ROW EXECUTE FUNCTION interurban.reject_ledger_mutation();

CREATE TABLE interurban.outbox (
    id UUID PRIMARY KEY,
    event_key VARCHAR(160) NOT NULL UNIQUE,
    event_type VARCHAR(48) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0 CHECK (attempts >= 0)
);
CREATE INDEX interurban_pending_outbox ON interurban.outbox(created_at) WHERE delivered_at IS NULL;
```


## Archivo: `src/main/resources/db/migration/V12__add_passenger_name_to_conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions
ADD COLUMN passenger_name VARCHAR(150);

```


## Archivo: `src/main/resources/db/migration/V130__interurban_encrypted_qr_artifacts.sql`

```sql
-- Aditiva: V129 y las tablas de Córdoba permanecen intactas.
-- El PNG permite reentrega del mismo pase sin guardar el token en claro.
CREATE TABLE interurban.qr_artifacts (
    reservation_id UUID PRIMARY KEY REFERENCES interurban.qr_tokens(reservation_id),
    encrypted_png BYTEA NOT NULL,
    key_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX interurban_pending_payment_inbox
    ON interurban.payment_inbox(received_at) WHERE processed_at IS NULL;
```


## Archivo: `src/main/resources/db/migration/V131__interurban_settlement_breakdown.sql`

```sql
-- Desglose para nuevas liquidaciones. NULL identifica órdenes históricas sin snapshot.
ALTER TABLE interurban.payout_orders
    ADD COLUMN gross_amount NUMERIC(14,2),
    ADD COLUMN commission_amount NUMERIC(14,2),
    ADD COLUMN adjustment_amount NUMERIC(14,2),
    ADD CONSTRAINT interurban_payout_breakdown CHECK (
        (gross_amount IS NULL AND commission_amount IS NULL AND adjustment_amount IS NULL)
        OR (gross_amount IS NOT NULL AND commission_amount IS NOT NULL AND adjustment_amount IS NOT NULL
            AND gross_amount >= 0 AND commission_amount >= 0
            AND amount = gross_amount - commission_amount + adjustment_amount));
```


## Archivo: `src/main/resources/db/migration/V132__link_accounts_to_drivers_and_add_indexes.sql`

```sql
-- 1. Agregar columna account_id a public.drivers con restricción UNIQUE
ALTER TABLE public.drivers
    ADD COLUMN IF NOT EXISTS account_id UUID REFERENCES public.accounts(id);

ALTER TABLE public.drivers
    ADD CONSTRAINT uq_drivers_account UNIQUE (account_id);

-- 2. Índice para acelerar la resolución de chofer activo
CREATE INDEX IF NOT EXISTS idx_drivers_account_active
    ON public.drivers(account_id)
    WHERE active = TRUE;

-- 3. Índices de performance operativos para route-sheet
CREATE INDEX IF NOT EXISTS idx_interurban_trips_driver_date
    ON interurban.trips(driver_id, service_date)
    WHERE status IN ('ASSIGNED', 'COMPLETED');

CREATE INDEX IF NOT EXISTS idx_interurban_payments_approved_booking
    ON interurban.payments(booking_id)
    WHERE status = 'APPROVED';

-- 4. Unicidad de username insensible a mayúsculas
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_username_upper_unique
    ON public.accounts(UPPER(username));
```


## Archivo: `src/main/resources/db/migration/V14__add_round_trip_to_conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions
ADD COLUMN round_trip BOOLEAN;
```


## Archivo: `src/main/resources/db/migration/V15__add_invoice_fields_to_conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions
ADD COLUMN requires_invoice BOOLEAN;

ALTER TABLE conversation_sessions
ADD COLUMN cuil VARCHAR(20);
```


## Archivo: `src/main/resources/db/migration/V16__make_passenger_cuil_nullable.sql`

```sql
ALTER TABLE passengers
ALTER COLUMN cuil DROP NOT NULL;
```


## Archivo: `src/main/resources/db/migration/V17__add_status_to_reservations.sql`

```sql
ALTER TABLE reservations
ADD COLUMN status VARCHAR(50);

UPDATE reservations
SET status = 'PENDING_PAYMENT'
WHERE status IS NULL;
```


## Archivo: `src/main/resources/db/migration/V18__business_parameters.sql`

```sql
CREATE TABLE business_parameters (
    parameter_key VARCHAR(100) PRIMARY KEY,
    parameter_value VARCHAR(100) NOT NULL
);

INSERT INTO business_parameters(parameter_key, parameter_value)
VALUES ('ONE_WAY_EXTRA_AMOUNT', '8000');

INSERT INTO business_parameters(parameter_key, parameter_value)
VALUES ('PRICE_PER_KM', '1000');
```


## Archivo: `src/main/resources/db/migration/V19__INSERT_INTO_fares.sql`

```sql
INSERT INTO fares(id, locality_name, amount)
VALUES
(gen_random_uuid(),'San Guillermo',105000),
(gen_random_uuid(),'Suardi',99000),
(gen_random_uuid(),'Morteros',89000),
(gen_random_uuid(),'Brinkmann',85000),
(gen_random_uuid(),'Porteña',89000),
(gen_random_uuid(),'Freyre',89000),
(gen_random_uuid(),'La Paquita',72000),
(gen_random_uuid(),'Altos de Chipión',68000),
(gen_random_uuid(),'Balnearia',56000),
(gen_random_uuid(),'Miramar',62000);
```


## Archivo: `src/main/resources/db/migration/V1__initial_schema.sql`

```sql
CREATE TABLE passengers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    cuil VARCHAR(20) NOT NULL,
    phone VARCHAR(30),
    address VARCHAR(255),
    locality VARCHAR(100)
);
```


## Archivo: `src/main/resources/db/migration/V20__conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN return_date DATE;
```


## Archivo: `src/main/resources/db/migration/V21__passenger_count.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN passenger_count INT;
ALTER TABLE conversation_sessions ADD COLUMN companion_names VARCHAR(500);
```


## Archivo: `src/main/resources/db/migration/V22__add_companions_and_return_date.sql`

```sql
-- Agregamos las columnas solo si no existen previamente
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS passenger_count INT;
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS companion_names VARCHAR(500);
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS current_companion_index INT;
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS total_companions INT;

-- La fecha de regreso (por si no se creó antes)
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS return_date DATE;
```


## Archivo: `src/main/resources/db/migration/V23__ALTER_TABLE_RESERVATION.sql`

```sql
-- Agregamos la columna a la tabla de reservas si no existe
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS payment_receipt_url VARCHAR(500);
```


## Archivo: `src/main/resources/db/migration/V24__add_logistics_to_localities.sql`

```sql
-- Agregamos las columnas de logística interurbana
ALTER TABLE localities ADD COLUMN kms_to_cordoba INT DEFAULT 150;
ALTER TABLE localities ADD COLUMN minutes_from_origin INT DEFAULT 0;

-- Seteamos los datos reales del folleto y traza de la Ruta 17
UPDATE localities SET kms_to_cordoba = 280, minutes_from_origin = 0 WHERE name = 'San Guillermo';
UPDATE localities SET kms_to_cordoba = 260, minutes_from_origin = 20 WHERE name = 'Suardi';
UPDATE localities SET kms_to_cordoba = 240, minutes_from_origin = 40 WHERE name = 'Morteros';
UPDATE localities SET kms_to_cordoba = 224, minutes_from_origin = 55 WHERE name = 'Brinkmann';
UPDATE localities SET kms_to_cordoba = 206, minutes_from_origin = 70 WHERE name = 'Porteña';
UPDATE localities SET kms_to_cordoba = 179, minutes_from_origin = 95 WHERE name = 'Freyre';
UPDATE localities SET kms_to_cordoba = 164, minutes_from_origin = 110 WHERE name = 'La Paquita';
UPDATE localities SET kms_to_cordoba = 149, minutes_from_origin = 125 WHERE name = 'Altos de Chipión';
UPDATE localities SET kms_to_cordoba = 124, minutes_from_origin = 150 WHERE name = 'Balnearia';
UPDATE localities SET kms_to_cordoba = 137, minutes_from_origin = 165 WHERE name = 'Miramar';
```


## Archivo: `src/main/resources/db/migration/V25__companion_names.sql`

```sql
ALTER TABLE reservations ADD COLUMN companion_names VARCHAR(500);
```


## Archivo: `src/main/resources/db/migration/V26__Reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN passenger_count INTEGER DEFAULT 1;
```


## Archivo: `src/main/resources/db/migration/V27__add_bot_paused_to_conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN bot_paused BOOLEAN DEFAULT false;
```


## Archivo: `src/main/resources/db/migration/V28__create_chat_messages_table.sql`

```sql
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(30) NOT NULL,
    message_text TEXT NOT NULL,
    is_from_operator BOOLEAN NOT NULL DEFAULT false,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
```


## Archivo: `src/main/resources/db/migration/V29__add_reservation_code_to_reservations.sql`

```sql
ALTER TABLE reservations 
ADD COLUMN reservation_code VARCHAR(20) UNIQUE;
```


## Archivo: `src/main/resources/db/migration/V2__drivers.sql`

```sql
CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    active BOOLEAN NOT NULL,
    ranking INTEGER
);
```


## Archivo: `src/main/resources/db/migration/V30__enterprise_trazability_and_timestamps.sql`

```sql
-- 1. Agregamos los timestamps y el código (usando IF NOT EXISTS para la columna que ya tenés)
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS reservation_code VARCHAR(20) UNIQUE;
ALTER TABLE reservations ADD COLUMN created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();
ALTER TABLE reservations ADD COLUMN updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW();

-- 2. Creamos la tabla inmutable para auditoría por eventos (Timeline)
CREATE TABLE reservation_events (
    id UUID PRIMARY KEY,
    reservation_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    triggered_by VARCHAR(100) DEFAULT 'SYSTEM',
    CONSTRAINT fk_reservation_event FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);
```


## Archivo: `src/main/resources/db/migration/V31__Reservatoin_not_null_ALTER.sql`

```sql
ALTER TABLE reservations ALTER COLUMN travel_date DROP NOT NULL;
```


## Archivo: `src/main/resources/db/migration/V32__create_invoices_and_payment_timestamp.sql`

```sql
-- 🧾 Módulo de Facturación + registro de ingreso de dinero
-- 1. Marca temporal del momento exacto en que se confirma el pago (cuándo ingresó la plata)
ALTER TABLE reservations ADD COLUMN payment_confirmed_at TIMESTAMP;

-- 2. Registro de facturas emitidas (la factura la arma la operadora aparte y se sube en PDF)
CREATE TABLE invoices (
    id                  UUID PRIMARY KEY,
    reservation_id      UUID NOT NULL REFERENCES reservations(id),
    invoice_number      VARCHAR(40),
    passenger_name      VARCHAR(200),
    passenger_cuil      VARCHAR(20),
    amount              NUMERIC(12,2),
    pdf_url             VARCHAR(300),
    sent_via_whatsapp   BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at             TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_reservation_id ON invoices(reservation_id);
```


## Archivo: `src/main/resources/db/migration/V33__remove_unique_reservation_code.sql`

```sql
-- 🛠️ MIGRACIÓN FLYWAY: Removemos la restricción única del código de reserva
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_reservation_code_key;

-- Opcional: Si querés que las búsquedas sigan volando, le creamos un índice común (no único)
CREATE INDEX IF NOT EXISTS idx_reservations_reservation_code ON reservations(reservation_code);


ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS schedule_block VARCHAR(50);
ALTER TABLE conversation_sessions ADD COLUMN IF NOT EXISTS reservation_code VARCHAR(20);


-- 💰 Agregamos la columna de saldo a favor en la tabla de pasajeros con valor inicial 0
ALTER TABLE passengers ADD COLUMN IF NOT EXISTS current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00;
```


## Archivo: `src/main/resources/db/migration/V34__add_passenger_current_balance.sql`

```sql
-- 💰 Agregamos la columna de saldo a favor en la tabla de pasajeros con valor inicial 0
ALTER TABLE passengers ADD COLUMN IF NOT EXISTS current_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00;
```


## Archivo: `src/main/resources/db/migration/V35__add_assigned_operator_to_conversation_sessions.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN assigned_operator VARCHAR(255);
```


## Archivo: `src/main/resources/db/migration/V36__add_departure_schedule_to_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN departure_schedule VARCHAR(50);
```


## Archivo: `src/main/resources/db/migration/V37__add_requires_invoice_to_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN requires_invoice BOOLEAN DEFAULT FALSE;
```


## Archivo: `src/main/resources/db/migration/V38__add_travel_status_to_reservations.sql`

```sql
ALTER TABLE reservations
ADD COLUMN IF NOT EXISTS travel_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';
```


## Archivo: `src/main/resources/db/migration/V39__create_system_configurations.sql`

```sql
CREATE TABLE IF NOT EXISTS system_configurations (
    key VARCHAR PRIMARY KEY,
    value TEXT
);

INSERT INTO system_configurations (key, value) VALUES
    ('return.scheduler.time', '15:00'),
    ('return.message.header', 'Confirmación de vuelta'),
    ('return.message.body', 'Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?
Elegí una opción para que podamos organizar las butacas.'),
    ('return.button.yes.title', 'SÍ, VOLVER ✅'),
    ('return.button.later.title', 'OTRO DÍA 📅'),
    ('return.button.no.title', 'NO, CANCELAR ❌'),
    ('session.inactivity.timeout.minutes', '30')
ON CONFLICT (key) DO NOTHING;
```


## Archivo: `src/main/resources/db/migration/V3__vehicles.sql`

```sql
CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    plate VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL
);
```


## Archivo: `src/main/resources/db/migration/V40__add_driver_to_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS driver_id UUID;

-- Add constraint to reference the drivers table
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_reservations_driver'
    ) THEN
        ALTER TABLE reservations 
        ADD CONSTRAINT fk_reservations_driver 
        FOREIGN KEY (driver_id) 
        REFERENCES drivers(id);
    END IF;
END $$;
```


## Archivo: `src/main/resources/db/migration/V41__create_accounts.sql`

```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE account_roles (
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (account_id, role)
);
```


## Archivo: `src/main/resources/db/migration/V42__create_promotions.sql`

```sql
CREATE TABLE promotions (
    id UUID PRIMARY KEY,
    code VARCHAR(4) NOT NULL UNIQUE,
    discount_percentage INTEGER NOT NULL CHECK (discount_percentage BETWEEN 10 AND 100),
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_promotions_available_code ON promotions (code) WHERE used = FALSE;
```


## Archivo: `src/main/resources/db/migration/V43__add_promotion_fields.sql`

```sql
ALTER TABLE conversation_sessions ADD COLUMN promotion_code VARCHAR(4);
ALTER TABLE conversation_sessions ADD COLUMN promotion_discount_percentage INTEGER;

ALTER TABLE reservations ADD COLUMN promotion_code VARCHAR(4);
ALTER TABLE reservations ADD COLUMN discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;
```


## Archivo: `src/main/resources/db/migration/V44__add_promotion_expiration.sql`

```sql
ALTER TABLE promotions ADD COLUMN is_massive BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE promotions ADD COLUMN expires_at TIMESTAMP NULL;

CREATE TABLE promotion_usages (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    phone_number VARCHAR(30) NOT NULL,
    used_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number)
);

CREATE INDEX idx_promotion_usages_promotion ON promotion_usages (promotion_id);
```


## Archivo: `src/main/resources/db/migration/V45__normalize_massive_promotion_usages.sql`

```sql
ALTER TABLE promotion_usages RENAME COLUMN used_at TO created_at;

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, REGEXP_REPLACE(phone_number, '[^0-9]', '', 'g')
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

UPDATE promotion_usages
SET phone_number = REGEXP_REPLACE(phone_number, '[^0-9]', '', 'g');

ALTER TABLE promotion_usages
    ADD CONSTRAINT chk_promotion_usage_phone_digits
    CHECK (phone_number ~ '^[0-9]+$');
```


## Archivo: `src/main/resources/db/migration/V46__audit_promotions_on_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN promotion_id UUID;
ALTER TABLE reservations ADD COLUMN promotion_discount_percentage INTEGER;

UPDATE reservations reservation
SET promotion_id = promotion.id,
    promotion_discount_percentage = promotion.discount_percentage
FROM promotions promotion
WHERE reservation.promotion_code = promotion.code;

ALTER TABLE promotion_usages DROP CONSTRAINT IF EXISTS uk_promotion_usage_phone;

UPDATE promotion_usages
SET phone_number = REGEXP_REPLACE(phone_number, '^549', '54');

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, phone_number
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

ALTER TABLE promotion_usages
    ADD CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number);
```


## Archivo: `src/main/resources/db/migration/V47__add_dynamic_route_sequence.sql`

```sql
ALTER TABLE reservations ADD COLUMN route_sequence INTEGER;

WITH numbered_route AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY driver_id, travel_date
               ORDER BY departure_schedule NULLS LAST, created_at, id
           ) AS sequence
    FROM reservations
    WHERE driver_id IS NOT NULL
      AND status <> 'CANCELLED'
)
UPDATE reservations reservation
SET route_sequence = numbered_route.sequence
FROM numbered_route
WHERE reservation.id = numbered_route.id;

CREATE INDEX idx_reservations_driver_date_sequence
    ON reservations (driver_id, travel_date, route_sequence);
```


## Archivo: `src/main/resources/db/migration/V48__enforce_unique_route_sequence.sql`

```sql
ALTER TABLE promotion_usages
    DROP CONSTRAINT IF EXISTS uk_promotion_usage_phone;

UPDATE promotion_usages
SET phone_number = '54' || phone_number
WHERE LENGTH(phone_number) = 10
  AND phone_number NOT LIKE '54%';

WITH duplicated_usages AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY promotion_id, phone_number
               ORDER BY created_at, id
           ) AS row_number
    FROM promotion_usages
)
DELETE FROM promotion_usages
WHERE id IN (SELECT id FROM duplicated_usages WHERE row_number > 1);

ALTER TABLE promotion_usages
    ADD CONSTRAINT uk_promotion_usage_phone UNIQUE (promotion_id, phone_number);

UPDATE reservations
SET route_sequence = NULL
WHERE status = 'CANCELLED';

WITH numbered_route AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY driver_id, travel_date
               ORDER BY route_sequence NULLS LAST, departure_schedule NULLS LAST, created_at, id
           ) AS sequence
    FROM reservations
    WHERE driver_id IS NOT NULL
      AND status IS DISTINCT FROM 'CANCELLED'
)
UPDATE reservations reservation
SET route_sequence = numbered_route.sequence
FROM numbered_route
WHERE reservation.id = numbered_route.id;

ALTER TABLE reservations
    ADD CONSTRAINT uk_reservations_driver_date_route_sequence
    UNIQUE (driver_id, travel_date, route_sequence)
    DEFERRABLE INITIALLY DEFERRED;
```


## Archivo: `src/main/resources/db/migration/V49__create_driver_applications.sql`

```sql
CREATE TABLE driver_applications (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    vehicle_model VARCHAR(120) NOT NULL,
    license_plate VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_driver_applications_status
    ON driver_applications (status);
```


## Archivo: `src/main/resources/db/migration/V4__reservations.sql`

```sql
CREATE TABLE reservations (

    id UUID PRIMARY KEY,

    passenger_id UUID NOT NULL,

    travel_date DATE NOT NULL,

    pickup_locality VARCHAR(100) NOT NULL,

    pickup_address VARCHAR(255),

    destination VARCHAR(100) NOT NULL,

    payment_verified BOOLEAN NOT NULL,

    notes VARCHAR(500),

    CONSTRAINT fk_reservation_passenger
        FOREIGN KEY (passenger_id)
        REFERENCES passengers(id)
);
```


## Archivo: `src/main/resources/db/migration/V50__add_driver_current_location.sql`

```sql
ALTER TABLE drivers
    ADD COLUMN IF NOT EXISTS current_location_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS location_updated_at TIMESTAMP;
```


## Archivo: `src/main/resources/db/migration/V53__reset_admin_password.sql`

```sql
UPDATE accounts
SET password_hash = '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    active = true
WHERE upper(username) = upper('ignacio');

INSERT INTO accounts (id, username, display_name, password_hash, active)
SELECT
    'a1530000-0000-4000-8000-000000000001',
    'ignacio',
    'Ignacio',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    true
WHERE NOT EXISTS (
    SELECT 1
    FROM accounts
    WHERE upper(username) = upper('ignacio')
);

INSERT INTO account_roles (account_id, role)
SELECT id, 'ADMIN'
FROM accounts
WHERE upper(username) = upper('ignacio')
ON CONFLICT (account_id, role) DO NOTHING;
```


## Archivo: `src/main/resources/db/migration/V54__add_reservation_source.sql`

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'MANUAL';
```


## Archivo: `src/main/resources/db/migration/V55__add_trip_type_to_reservations.sql`

```sql
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS trip_type VARCHAR(20);

UPDATE reservations
SET trip_type = CASE
    WHEN COALESCE(round_trip, FALSE) = FALSE THEN 'ONE_WAY'
    WHEN travel_date IS NULL OR travel_status = 'OPEN_RETURN' THEN 'OPEN_RETURN'
    ELSE 'ROUND_TRIP'
END
WHERE trip_type IS NULL;
```


## Archivo: `src/main/resources/db/migration/V5__create_localities_and_fares.sql`

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE localities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE fares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    locality_name VARCHAR(100) NOT NULL,
    amount NUMERIC(12,2) NOT NULL
);
```


## Archivo: `src/main/resources/db/migration/V6__add_amount_to_reservations.sql`

```sql
ALTER TABLE reservations
ADD COLUMN amount NUMERIC(12,2);
```


## Archivo: `src/main/resources/db/migration/V7__add_round_trip_fields.sql`

```sql
ALTER TABLE reservations
ADD COLUMN round_trip BOOLEAN DEFAULT FALSE;

ALTER TABLE reservations
ADD COLUMN return_date DATE;

ALTER TABLE reservations
ADD COLUMN extra_amount NUMERIC(12,2);
```


## Archivo: `src/main/resources/db/migration/V8__insert_localities.sql`

```sql
INSERT INTO localities (name) VALUES
('San Guillermo'),
('Suardi'),
('Villa Trinidad'),
('Arrufó'),
('Morteros'),
('Brinkmann'),
('Porteña'),
('Seeber'),
('La Paquita'),
('Balnearia'),
('Miramar'),
('Marull'),
('Altos de Chipión'),
('Colonia Vignaud'),
('Freyre'),
('Devoto'),
('San Francisco'),
('Las Varillas'),
('Arroyito'),
('Rafaela'),
('Sunchales'),
('Córdoba'),
('Aeropuerto Córdoba')
ON CONFLICT (name) DO NOTHING;
```


## Archivo: `src/main/resources/db/migration/V99__reset_admin_password_force.sql`

```sql
DELETE FROM account_roles
WHERE account_id IN (
    SELECT id
    FROM accounts
    WHERE lower(username) = 'ignacio'
);

DELETE FROM accounts
WHERE lower(username) = 'ignacio';

INSERT INTO accounts (id, username, password_hash, active, display_name)
VALUES (
    'a1990000-0000-4000-8000-000000000001',
    'ignacio',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.',
    true,
    'Ignacio Admin'
);

INSERT INTO account_roles (account_id, role)
SELECT id, 'ADMIN'
FROM accounts
WHERE lower(username) = 'ignacio';
```


## Archivo: `src/main/resources/db/migration/V9__create_conversation_sessions.sql`

```sql
CREATE TABLE conversation_sessions (

    id BIGSERIAL PRIMARY KEY,

    phone_number VARCHAR(30) NOT NULL UNIQUE,

    current_step VARCHAR(50),

    last_interaction TIMESTAMP
);
```
