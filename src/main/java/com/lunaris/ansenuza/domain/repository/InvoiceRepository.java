package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.lunaris.ansenuza.domain.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);

    @Query("""
           SELECT i FROM Invoice i
           JOIN FETCH i.reservation r
           ORDER BY i.createdAt DESC
           """)
    List<Invoice> findAllIssuedWithReservation();
}
