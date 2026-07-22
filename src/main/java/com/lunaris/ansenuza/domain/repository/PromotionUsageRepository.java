package com.lunaris.ansenuza.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.lunaris.ansenuza.domain.model.PromotionUsage;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {

    @Query(value = """
            SELECT COUNT(*)
            FROM promotion_usages
            WHERE promotion_id = :promoId
              AND phone_number = :normalizedPhone
            """, nativeQuery = true)
    long countByPromotionAndNormalizedPhone(@Param("promoId") UUID promotionId,
            @Param("normalizedPhone") String normalizedPhone);
}
