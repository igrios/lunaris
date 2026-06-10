package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleRepository
        extends JpaRepository<Vehicle, UUID> {
}