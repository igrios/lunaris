package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import lombok.*;

/** Resultado inmutable de una operación confirmada, para reintentos idempotentes. */
@Entity
@Table(name = "reservation_update_operations")
@Getter @Setter @NoArgsConstructor
public class ReservationUpdateOperation {
    @Id
    @Column(name = "operation_key", length = 64)
    private String operationKey;
    @Column(name = "group_code", nullable = false, length = 64)
    private String groupCode;
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;
    @Column(name = "before_json", nullable = false, columnDefinition = "text")
    private String beforeJson;
    @Column(name = "triggered_by", nullable = false, length = 100)
    private String triggeredBy;
    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    @Column(name = "result_json", nullable = false, columnDefinition = "text")
    private String resultJson;
}
