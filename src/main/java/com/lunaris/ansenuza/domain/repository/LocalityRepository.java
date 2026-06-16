package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LocalityRepository
        extends JpaRepository<Locality, UUID> {

Optional<Locality> findByName(String name);

}