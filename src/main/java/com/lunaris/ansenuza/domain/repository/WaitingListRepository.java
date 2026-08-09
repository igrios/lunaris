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

    @Query("""
           SELECT COALESCE(SUM(entry.passengerCount), 0)
           FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
           AND entry.status = :status
           """)
    long sumPassengerCountByTravelDateAndStatus(
            @Param("travelDate") LocalDate travelDate, @Param("status") String status);

    List<WaitingListEntry> findByTravelDateAndStatusOrderByCreatedAtAsc(
            LocalDate travelDate, String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
             AND UPPER(entry.status) = UPPER(:status)
           ORDER BY entry.createdAt ASC
           """)
    List<WaitingListEntry> findByTravelDateAndNormalizedStatusOrderByCreatedAtAsc(
            @Param("travelDate") LocalDate travelDate, @Param("status") String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE UPPER(entry.status) = UPPER(:status)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findByNormalizedStatusOrderByCreatedAtDesc(
            @Param("status") String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE entry.travelDate = :travelDate
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt ASC
           """)
    List<WaitingListEntry> findByTravelDateAndActiveStatusOrderByCreatedAtAsc(
            @Param("travelDate") LocalDate travelDate);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE (entry.travelDate = :travelDate OR entry.travelDate IS NULL)
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findActiveWaitingForDateIncludingNull(
            @Param("travelDate") LocalDate travelDate);

    List<WaitingListEntry> findAllByOrderByCreatedAtDesc();

    @Query("select distinct entry.eventType from WaitingListEntry entry "
            + "where entry.eventType is not null and entry.eventType <> ''")
    List<String> findDistinctEventTypes();

    List<WaitingListEntry> findByStatusOrderByCreatedAtAsc(String status);

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
              OR entry.status IS NULL
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findAllActiveWaitingOrderByCreatedAtDesc();

    @Query("""
           SELECT entry FROM WaitingListEntry entry
           WHERE ((entry.eventType IS NOT NULL AND UPPER(entry.eventType) <> 'GENERAL')
                  OR entry.travelDate IS NULL)
             AND (UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
                  OR entry.status IS NULL)
           ORDER BY entry.createdAt DESC
           """)
    List<WaitingListEntry> findActiveSpecialEventsOrderByCreatedAtDesc();

    @Query("""
           SELECT COALESCE(SUM(entry.passengerCount), 0) FROM WaitingListEntry entry
           WHERE UPPER(entry.status) IN ('WAITING', 'PENDING', 'PENDIENTE', 'NEW')
              OR entry.status IS NULL
           """)
    long countAllActiveWaiting();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT entry FROM WaitingListEntry entry WHERE entry.id = :id")
    Optional<WaitingListEntry> findByIdForUpdate(@Param("id") Long id);
}
