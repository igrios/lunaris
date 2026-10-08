package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class AgendaManifestPdfControllerTest {
    @Mock
    private ReservationRepository reservations;
    @InjectMocks
    private AgendaViewController controller;

    @Test
    void exportsRegularAndSpecialQueriesForSelectedDate() {
        LocalDate date = LocalDate.of(2026, 10, 8);
        when(reservations.findDailyManifest(date)).thenReturn(List.of());
        when(reservations.findSpecialAgendaTrips(date)).thenReturn(List.of(
                Reservation.builder().reservationCode("SPECIAL-DAY").passengerCount(5).build()));

        var response = controller.dailyManifestPdf(date);

        verify(reservations).findDailyManifest(date);
        verify(reservations).findSpecialAgendaTrips(date);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(new String(response.getBody(), StandardCharsets.ISO_8859_1))
                .contains("08/10/2026", "Viajes Especiales", "SPECIAL-DAY");
    }
}
