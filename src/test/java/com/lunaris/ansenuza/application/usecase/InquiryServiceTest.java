package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.domain.exception.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class InquiryServiceTest {
    private final InquiryRepository inquiries = mock(InquiryRepository.class);
    private final PassengerRepository passengers = mock(PassengerRepository.class);
    private final InquiryService service = new InquiryService(inquiries, passengers);

    @Test
    void publishesOneAlertForNewInquiry() {
        var events = mock(org.springframework.context.ApplicationEventPublisher.class);
        when(inquiries.save(any())).thenAnswer(i -> i.getArgument(0));
        new InquiryService(inquiries, passengers, events).register("5493512282251", "Ana", "Viaje para 8");
        var notification = org.mockito.ArgumentCaptor.forClass(OperatorNotification.class);
        verify(events).publishEvent(notification.capture());
        assertTrue(notification.getValue().message().contains("Ana (5493512282251)"));
        assertTrue(notification.getValue().message().contains("Viaje para 8"));
        verifyNoMoreInteractions(events);
    }

    @Test
    void registersPendingInquiryLinkedToExistingPassenger() {
        Passenger passenger = Passenger.builder().id(UUID.randomUUID()).firstName("Ana").lastName("Pérez").build();
        when(passengers.findFirstByPhone("5493515550101")).thenReturn(Optional.of(passenger));
        when(inquiries.save(any())).thenAnswer(i -> i.getArgument(0));
        Inquiry inquiry = service.register("5493515550101", null, " Viaje para 8 personas ");
        assertNull(inquiry.getId(), "El servicio debe delegar la generación del ID a JPA");
        assertSame(passenger, inquiry.getPassenger());
        assertEquals("Ana Pérez", inquiry.getPassengerName());
        assertEquals("Viaje para 8 personas", inquiry.getMessage());
        assertEquals(InquiryStatus.PENDING, inquiry.getStatus());
        assertNotNull(inquiry.getCreatedAt());
        assertEquals(inquiry.getCreatedAt(), inquiry.getUpdatedAt());
    }

    @Test
    void acceptsUnknownPassengerWithoutCreatingPassenger() {
        when(inquiries.save(any())).thenAnswer(i -> i.getArgument(0));
        Inquiry inquiry = service.register("5493515550101", null, "Consulta");
        assertNull(inquiry.getPassenger());
        assertEquals("https://wa.me/5493515550101", inquiry.getWhatsAppUrl());
        verify(passengers, never()).save(any());
    }

    @Test
    void rejectsEmptyMessageWithoutSaving() {
        assertThrows(DomainValidationException.class, () -> service.register("123", null, " "));
        verifyNoInteractions(inquiries, passengers);
    }

    @Test
    void updatesStatusAndTimestamp() {
        UUID id = UUID.randomUUID();
        Inquiry inquiry = Inquiry.builder().id(id).status(InquiryStatus.PENDING)
                .updatedAt(java.time.LocalDateTime.of(2020, 1, 1, 0, 0)).build();
        when(inquiries.findById(id)).thenReturn(Optional.of(inquiry));
        for (InquiryStatus status : List.of(InquiryStatus.IN_PROGRESS, InquiryStatus.RESOLVED, InquiryStatus.ARCHIVED)) {
            service.updateStatus(id, status);
            assertEquals(status, inquiry.getStatus());
        }
        assertTrue(inquiry.getUpdatedAt().getYear() > 2020);
        verify(inquiries, times(3)).save(inquiry);
    }

    @Test
    void rejectsMissingInquiryAndInvalidStatus() {
        assertThrows(InquiryNotFoundException.class, () -> service.updateStatus(UUID.randomUUID(), InquiryStatus.RESOLVED));
        assertThrows(DomainValidationException.class, () -> service.updateStatus(UUID.randomUUID(), InquiryStatus.PENDING));
        verify(inquiries, never()).save(any());
    }

    @Test
    void readsOrderedListAndPendingCount() {
        when(inquiries.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(inquiries.countByStatus(InquiryStatus.PENDING)).thenReturn(3L);
        assertTrue(service.list().isEmpty());
        assertEquals(3, service.pendingCount());
    }
}
