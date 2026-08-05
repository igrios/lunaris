package com.lunaris.ansenuza.infrastructure.persistence.repository;

import com.lunaris.ansenuza.infrastructure.persistence.entity.PaymentAuditOutboxEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAuditOutboxJpaRepository
        extends JpaRepository<PaymentAuditOutboxEntity, UUID> {}
