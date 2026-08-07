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
