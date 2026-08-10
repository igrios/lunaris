package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.lunaris.ansenuza.domain.model.Locality;

public interface LocalityRepository extends JpaRepository<Locality, UUID> {

    Optional<Locality> findByName(String name);

    Optional<Locality> findFirstByNameIgnoreCase(String name);

    /** Devuelve exclusivamente localidades con una tarifa comercial activa y positiva. */
    @Query("""
            SELECT DISTINCT l
            FROM Locality l
            INNER JOIN Fare f ON UPPER(l.name) = UPPER(f.localityName)
            WHERE f.amount IS NOT NULL AND f.amount > 0
            ORDER BY l.name ASC
            """)
    List<Locality> findLocalitiesWithFares();

}
