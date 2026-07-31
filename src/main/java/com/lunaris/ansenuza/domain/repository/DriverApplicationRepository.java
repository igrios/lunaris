package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverApplicationRepository extends JpaRepository<DriverApplication, UUID> {
    Optional<DriverApplication> findFirstByPhone(String phone);

    List<DriverApplication> findByStatusOrderByCreatedAtAsc(DriverApplication.Status status);
}
