package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.NewsBanner;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsBannerRepository extends JpaRepository<NewsBanner, UUID> {

    List<NewsBanner> findAllByOrderByCreatedAtDesc();

    @Query("""
            select banner from NewsBanner banner
            where banner.active = true
              and (banner.validUntil is null or banner.validUntil >= :today)
            order by banner.createdAt desc
            """)
    List<NewsBanner> findActiveOn(@Param("today") LocalDate today);

    @Query("select distinct banner.eventType from NewsBanner banner "
            + "where banner.eventType is not null and banner.eventType <> ''")
    List<String> findDistinctEventTypes();
}
