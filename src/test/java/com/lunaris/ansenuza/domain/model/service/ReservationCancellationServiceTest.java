package com.lunaris.ansenuza.domain.model.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReservationCancellationServiceTest {

    @Test
    void postponingKeepsOpenReturnAndMarksItAuditedToday() {
        ReservationRepository repository = mock(ReservationRepository.class);
        ReservationService reservationService = mock(ReservationService.class);
        ReservationCancellationService service =
                new ReservationCancellationService(repository, reservationService);
        Reservation openReturn = Reservation.builder()
                .travelStatus(Reservation.TravelStatus.OPEN_RETURN)
                .build();
        String phone = "5493511111111";
        when(repository.findActiveReturnReservationsByPassengerPhoneAndDate(
                phone, com.lunaris.ansenuza.shared.ArgentinaTime.today()))
                .thenReturn(List.of());
        when(repository.findRealizedOutboundReservationsByPassengerPhoneAndReturnDate(
                phone, com.lunaris.ansenuza.shared.ArgentinaTime.today(),
                Reservation.TravelStatus.REALIZED))
                .thenReturn(List.of());
        when(repository.findOpenReturnReservationsByPassengerPhone(phone))
                .thenReturn(List.of(openReturn));

        service.processReturnDecision(phone, ReservationCancellationService.RETURN_POSTPONE_ID);

        assertEquals(Reservation.TravelStatus.OPEN_RETURN, openReturn.getTravelStatus());
        assertNotNull(openReturn.getReturnAuditSentAt());
        assertEquals(com.lunaris.ansenuza.shared.ArgentinaTime.today(),
                openReturn.getReturnAuditSentAt().toLocalDate());
        assertTrue(service.isReturnDecision("return_postpone"));
        verify(repository).saveAndFlush(openReturn);
    }
}
