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

    @Column(name = "reservation_code")
    private String reservationCode;

    // ⚖️ 🆕 NUEVO CAMPO: Persistencia del operador asignado por el Load Balancer
    @Column(name = "assigned_operator")
    private String assignedOperator;

}