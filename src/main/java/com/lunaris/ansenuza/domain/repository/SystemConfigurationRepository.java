package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
}
