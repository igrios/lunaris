package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "assisted_reservation_drafts")
@Getter
@Setter
public class AssistedReservationDraft {
    @PrePersist
    void ensureId() {
        // El generador UUID de Hibernate se ejecuta antes de este callback.
        if (id == null) id = UUID.randomUUID();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
