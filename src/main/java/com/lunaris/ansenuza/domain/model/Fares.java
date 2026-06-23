package com.lunaris.ansenuza.domain.model;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class Fares {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "locality_name", nullable = false)
    private String localityName;

    @Column(nullable = false)
    private java.math.BigDecimal amount; // Mapea el valor monetario (105000, 99000, etc.)
}