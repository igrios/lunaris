package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DriverRepository
        extends JpaRepository<Driver, UUID> {
}