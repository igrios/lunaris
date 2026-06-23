package com.lunaris.ansenuza.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lunaris.ansenuza.domain.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    boolean existsByReservationId(UUID reservationId);

    Optional<Invoice> findByReservationId(UUID reservationId);

    List<Invoice> findAllByOrderByCreatedAtDesc();
}
