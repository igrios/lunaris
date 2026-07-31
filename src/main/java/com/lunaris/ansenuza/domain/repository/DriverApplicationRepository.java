package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverApplicationRepository extends JpaRepository<DriverApplication, UUID> {
    List<DriverApplication> findByStatusOrderByCreatedAtAsc(DriverApplication.Status status);
}
