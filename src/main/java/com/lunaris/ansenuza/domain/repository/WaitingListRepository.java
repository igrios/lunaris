package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, Long> {

    List<WaitingListEntry> findByTravelDateAndStatusOrderByCreatedAtAsc(
            LocalDate travelDate, String status);

    List<WaitingListEntry> findAllByOrderByCreatedAtDesc();
}
