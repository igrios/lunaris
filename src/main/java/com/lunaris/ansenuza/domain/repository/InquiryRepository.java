package com.lunaris.ansenuza.domain.repository;

import com.lunaris.ansenuza.domain.model.Inquiry;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    long countByStatus(InquiryStatus status);
}
