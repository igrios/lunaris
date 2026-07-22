package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // 🔥 Agregamos el import

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> { // 👈 Cambiado Long por UUID
    List<Driver> findByActiveTrue();
    Optional<Driver> findFirstByPhone(String phone);
}
