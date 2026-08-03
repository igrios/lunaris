package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    List<WaitingListEntry> findByTravelDateAndStatusOrderByCreatedAtAsc(
            LocalDate travelDate, String status);

    List<WaitingListEntry> findAllByOrderByCreatedAtDesc();

    List<WaitingListEntry> findByStatusOrderByCreatedAtAsc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entry FROM WaitingListEntry entry WHERE entry.id = :id")
    Optional<WaitingListEntry> findByIdForUpdate(@Param("id") Long id);
}
