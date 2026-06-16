package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID; // 🔥 Importante sumar este import

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // 🔥 Cambiado para que procese UUIDs de Postgres
    private UUID id; // 👈 Cambiado de Long a UUID

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ranking")
    private Integer ranking;
}