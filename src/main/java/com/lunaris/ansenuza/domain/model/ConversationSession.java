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

    // Nuevos campos para control de acompañantes individuales y plazas
    @Column(name = "passenger_count")
    private Integer passengerCount;

    @Column(name = "companion_names", length = 500)
    private String companionNames;

    @Column(name = "current_companion_index")
    private Integer currentCompanionIndex;

    @Column(name = "total_companions")
    private Integer totalCompanions;

    // Nuevo campo para retener la fecha de regreso temporalmente
    @Column(name = "return_date")
    private LocalDate returnDate;


    // 🆕 Agregamos el flag para mutear/pausar el bot sincronizado con la migración V27
    @Column(name = "bot_paused")
    private boolean botPaused = false;

// 🕒 Guarda temporalmente el bloque horario elegido (ej: "08:00 AM")
@Column(name = "schedule_block")
private String scheduleBlock;

// 📌 Guarda temporalmente el código base/nexo generado para el summary
@Column(name = "reservation_code")
private String reservationCode;

}
