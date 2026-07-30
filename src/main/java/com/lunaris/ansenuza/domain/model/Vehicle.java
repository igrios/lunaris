package com.lunaris.ansenuza.domain.model;

import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String plate;

  @Builder.Default
  private Integer capacity = 4;

  private Boolean active;

  @PrePersist
  void applyDefaults() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    if (capacity == null || capacity < 1) {
      capacity = 4;
    }
  }
}
