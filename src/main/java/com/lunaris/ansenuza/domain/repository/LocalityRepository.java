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

    /**
     * Devuelve todas las localidades, tengan o no una tarifa comercial explícita.
     * El LEFT JOIN conserva la compatibilidad con los consumidores que también consultan tarifas.
     */
    @Query("""
            SELECT DISTINCT l
            FROM Locality l
            LEFT JOIN Fare f ON UPPER(l.name) = UPPER(f.localityName)
            ORDER BY l.name ASC
            """)
    List<Locality> findLocalitiesWithFares();

}
