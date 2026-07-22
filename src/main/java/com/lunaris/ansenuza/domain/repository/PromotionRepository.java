package com.lunaris.ansenuza.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.lunaris.ansenuza.domain.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    boolean existsByCode(String code);

    Optional<Promotion> findFirstByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Promotion> findFirstByCodeAndUsedFalse(String code);
}
