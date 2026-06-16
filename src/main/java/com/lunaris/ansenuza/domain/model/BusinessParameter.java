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