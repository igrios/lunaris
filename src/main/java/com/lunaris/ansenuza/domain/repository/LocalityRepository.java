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
     * Localidades publicables en el bot: existe una tarifa positiva asociada.
     * En el esquema actual una tarifa se considera activa mientras conserve un importe
     * positivo; no se agrega una columna para no alterar el contrato de la tabla fares.
     */
    @Query("""
            SELECT DISTINCT l
            FROM Locality l
            INNER JOIN Fare f ON TRIM(UPPER(l.name)) = TRIM(UPPER(f.localityName))
            WHERE f.amount IS NOT NULL AND f.amount > 0
            ORDER BY l.name ASC
            """)
    List<Locality> findAllWithActiveFare();

}
