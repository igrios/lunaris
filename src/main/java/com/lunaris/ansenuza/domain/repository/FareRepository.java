package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.Fare;

public interface FareRepository
        extends JpaRepository<Fare, UUID> {

    Optional<Fare> findByLocalityName(String localityName);
}