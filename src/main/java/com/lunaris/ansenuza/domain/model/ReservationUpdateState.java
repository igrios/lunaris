package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

/** Revisión del agregado; se accede bajo el mutex GROUP de CapacityLockRepository. */
@Entity
@Table(name = "reservation_update_states")
@Getter @Setter @NoArgsConstructor
public class ReservationUpdateState {
    @Id
    @Column(name = "group_code", length = 64)
    private String groupCode;
    @Column(name = "primary_reservation_id", nullable = false)
    private UUID primaryReservationId;
    @Column(nullable = false)
    private int revision;
    @Column(nullable = false, length = 64)
    private String fingerprint;
}
