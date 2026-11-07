package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChoferAgendaServiceTest {
    private final ReservationRepository repository = mock(ReservationRepository.class);
    private final ChoferAgendaService service = new ChoferAgendaService(repository);

    @Test
    void consultaAgendaGlobalDelDiaSeleccionadoSinUsuario() {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        UUID id = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(id).reservationCode("RES-001").travelDate(fecha).build();
        when(repository.findDriverAgenda(fecha, fecha.plusDays(1), null))
                .thenReturn(List.of(reservation));

        var agenda = service.execute(fecha);

        assertEquals(1, agenda.size());
        assertEquals(id, agenda.getFirst().id());
        assertEquals("RES-001", agenda.getFirst().codigo());
        assertEquals(fecha, agenda.getFirst().fecha());
        verify(repository).findDriverAgenda(fecha, fecha.plusDays(1), null);
    }

    @Test
    void usaHoyPorDefectoSinFiltrarPorChofer() {
        LocalDate hoy = LocalDate.now();
        when(repository.findDriverAgenda(hoy, hoy.plusDays(1), null)).thenReturn(List.of());
        assertEquals(List.of(), service.execute(null));
        verify(repository).findDriverAgenda(hoy, hoy.plusDays(1), null);
    }
}
