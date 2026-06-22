package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalityRepository extends JpaRepository<Locality, UUID> {

    Optional<Locality> findByName(String name);

    // 🎯 Consulta nativa para traer solo pueblos que tengan cargada al menos una tarifa comercial
    @Query(value = "SELECT DISTINCT l.* FROM localities l INNER JOIN fares f ON l.name = f.locality_name", nativeQuery = true)
    List<Locality> findLocalitiesWithFares();
}