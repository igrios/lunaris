package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import com.lunaris.ansenuza.domain.model.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.reservationId = :reservationId")
    Optional<Invoice> findByReservationIdForUpdate(@Param("reservationId") UUID reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.id = :invoiceId")
    Optional<Invoice> findByIdForUpdate(@Param("invoiceId") UUID invoiceId);

    @Query("""
           SELECT i FROM Invoice i
           JOIN FETCH i.reservation r
           ORDER BY i.createdAt DESC
           """)
    List<Invoice> findAllIssuedWithReservation();
}
