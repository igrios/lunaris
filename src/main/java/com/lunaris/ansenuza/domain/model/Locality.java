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
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    // 🛣️ Kilómetros reales desde esta localidad hasta Córdoba Capital
    @Column(name = "kms_to_cordoba")
    private Integer kmsToCordoba;

    // ⏱️ Minutos de viaje acumulados desde el inicio del recorrido en Morteros
    @Column(name = "minutes_from_origin")
    private Integer minutesFromOrigin;
}