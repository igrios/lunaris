package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OperatorNotificationPhoneRepository extends JpaRepository<OperatorNotificationPhone, UUID> {
    List<OperatorNotificationPhone> findByActiveTrueOrderByCreatedAtAsc();
    List<OperatorNotificationPhone> findAllByOrderByCreatedAtAsc();
    boolean existsByPhone(String phone);
}
