package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.domain.model.Passenger;
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
                .id(id).reservationCode("RES-001").travelDate(fecha)
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez")
                        .phone("5493511234567").build())
                .notes("Lleva equipaje").build();
        when(repository.findDriverAgenda(fecha, fecha.plusDays(1), null))
                .thenReturn(List.of(reservation));

        var agenda = service.execute(fecha);

        assertEquals(1, agenda.size());
        assertEquals(id, agenda.getFirst().id());
        assertEquals("RES-001", agenda.getFirst().codigo());
        assertEquals(fecha, agenda.getFirst().fecha());
        assertEquals("Ana Pérez", agenda.getFirst().nombrePasajero());
        assertEquals("5493511234567", agenda.getFirst().telefono());
        assertEquals("Lleva equipaje", agenda.getFirst().comentarios());
        verify(repository).findDriverAgenda(fecha, fecha.plusDays(1), null);
    }

    @Test
    void toleraPasajeroAusenteYComentariosNulos() {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        when(repository.findDriverAgenda(fecha, fecha.plusDays(1), null))
                .thenReturn(List.of(Reservation.builder().travelDate(fecha).build()));
        var servicio = service.execute(fecha).getFirst();
        assertEquals("", servicio.nombrePasajero());
        assertEquals("", servicio.telefono());
        assertEquals("", servicio.comentarios());
    }

    @Test
    void toleraNombreParcialTelefonoNuloYComentariosVacios() {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        when(repository.findDriverAgenda(fecha, fecha.plusDays(1), null))
                .thenReturn(List.of(Reservation.builder().travelDate(fecha).notes("")
                        .passenger(Passenger.builder().lastName("Pérez").build()).build()));
        var servicio = service.execute(fecha).getFirst();
        assertEquals("Pérez", servicio.nombrePasajero());
        assertEquals("", servicio.telefono());
        assertEquals("", servicio.comentarios());
    }

    @Test
    void usaHoyPorDefectoSinFiltrarPorChofer() {
        LocalDate hoy = LocalDate.now();
        when(repository.findDriverAgenda(hoy, hoy.plusDays(1), null)).thenReturn(List.of());
        assertEquals(List.of(), service.execute(null));
        verify(repository).findDriverAgenda(hoy, hoy.plusDays(1), null);
    }
}
