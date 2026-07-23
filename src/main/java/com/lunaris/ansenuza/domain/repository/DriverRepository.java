package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // 🔥 Agregamos el import

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> { // 👈 Cambiado Long por UUID
    List<Driver> findByActiveTrue();
    Optional<Driver> findFirstByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Driver d WHERE d.id IN :ids ORDER BY d.id")
    List<Driver> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
