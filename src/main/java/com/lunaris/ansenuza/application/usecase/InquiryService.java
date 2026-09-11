package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.domain.exception.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InquiryService {
    private final InquiryRepository inquiries;
    private final PassengerRepository passengers;

    private final org.springframework.context.ApplicationEventPublisher events;

    public InquiryService(InquiryRepository inquiries, PassengerRepository passengers) {
        this(inquiries, passengers, event -> {});
    }

    @org.springframework.beans.factory.annotation.Autowired
    public InquiryService(InquiryRepository inquiries, PassengerRepository passengers,
            org.springframework.context.ApplicationEventPublisher events) {
        this.inquiries = inquiries;
        this.passengers = passengers;
        this.events = events;
    }

    public Inquiry register(String phone, String passengerName, String message) {
        if (phone == null || phone.isBlank() || message == null || message.isBlank()) {
            throw new DomainValidationException("El teléfono y el mensaje son obligatorios.");
        }
        Passenger passenger = passengers.findFirstByPhone(phone).orElse(null);
        String name = passenger == null ? passengerName
                : (passenger.getFirstName() + " " + passenger.getLastName()).trim();
        var now = ArgentinaTime.now();
        // El ID generado debe quedar nulo para que JPA persista una entidad nueva.
        Inquiry inquiry = Inquiry.builder().passenger(passenger)
                .phone(phone).passengerName(name).message(message.trim())
                .status(InquiryStatus.PENDING).createdAt(now).updatedAt(now).build();
        Inquiry saved = inquiries.save(inquiry);
        events.publishEvent(OperatorNotification.inquiry(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Inquiry> list() {
        return inquiries.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return inquiries.countByStatus(InquiryStatus.PENDING);
    }

    public void updateStatus(UUID id, InquiryStatus status) {
        if (status == null || status == InquiryStatus.PENDING) {
            throw new DomainValidationException("Seleccioná un estado válido para gestionar la consulta.");
        }
        Inquiry inquiry = inquiries.findById(id).orElseThrow(InquiryNotFoundException::new);
        inquiry.setStatus(status);
        inquiry.setUpdatedAt(ArgentinaTime.now());
        inquiries.save(inquiry);
    }
}
