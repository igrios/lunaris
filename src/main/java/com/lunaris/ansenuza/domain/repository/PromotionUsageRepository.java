package com.lunaris.ansenuza.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.PromotionUsage;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

    boolean existsByPromotionIdAndPhoneNumber(UUID promotionId, String phoneNumber);
}
