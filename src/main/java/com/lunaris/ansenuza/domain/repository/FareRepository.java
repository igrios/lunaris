package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.lunaris.ansenuza.domain.model.Fare;

@Repository
public interface FareRepository extends JpaRepository<Fare, UUID> {

    // 🌟 1. Trae solo los nombres de los pueblos comerciales activos para los menús
    @Query("SELECT f.localityName FROM Fare f WHERE f.amount > 0 ORDER BY f.amount DESC")
    List<String> findCommercialLocalities();

    // 🌟 2. Busca la tarifa por nombre de forma segura ignorando mayúsculas/minúsculas
    Optional<Fare> findByLocalityNameIgnoreCase(String localityName);

    Optional<Fare> findFirstByLocalityNameIgnoreCase(String localityName);
}
