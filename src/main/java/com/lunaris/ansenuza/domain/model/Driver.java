package com.lunaris.ansenuza.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "active")
    private boolean active;

    @Column(name = "ranking")
    private Integer ranking;

    @Column(name = "current_location_url", length = 500)
    private String currentLocationUrl;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @PrePersist
    void initializeId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
