package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.AssistedReservationDraft;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistedReservationDraftRepository extends JpaRepository<AssistedReservationDraft, UUID> {
    Optional<AssistedReservationDraft> findFirstByPhone(String phone);
    void deleteByPhone(String phone);
}
