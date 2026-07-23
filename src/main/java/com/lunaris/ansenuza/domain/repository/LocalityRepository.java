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

  // 🎯 Consulta nativa para traer solo pueblos que tengan cargada al menos una tarifa comercial (ORDENADO ALFABÉTICAMENTE)
    @Query(value = "SELECT DISTINCT l.* FROM localities l INNER JOIN fares f ON l.name = f.locality_name ORDER BY l.name ASC", nativeQuery = true)
    List<Locality> findLocalitiesWithFares();
   

}
