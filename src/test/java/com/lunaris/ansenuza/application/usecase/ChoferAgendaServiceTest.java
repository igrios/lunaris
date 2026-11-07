package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class ChoferAgendaServiceTest {
    private final ReservationRepository repository = mock(ReservationRepository.class);
    private final DriverAuthorizationService authorization = mock(DriverAuthorizationService.class);
    private final ChoferAgendaService service = new ChoferAgendaService(repository, authorization);

    @Test
    void choferSoloConsultaSusServiciosDelDiaSeleccionado() {
        LocalDate fecha = LocalDate.of(2026, 11, 7);
        UUID driverId = UUID.randomUUID();
        when(authorization.resolveActiveDriverId("chofer")).thenReturn(driverId);
        when(repository.findDriverAgenda(fecha, fecha.plusDays(1), driverId)).thenReturn(List.of());
        assertEquals(List.of(), service.execute(fecha, authentication("CHOFER")));
        verify(repository).findDriverAgenda(fecha, fecha.plusDays(1), driverId);
    }

    @Test
    void operadorConsultaAgendaCompletaYUsaHoyPorDefecto() {
        LocalDate hoy = LocalDate.now();
        when(repository.findDriverAgenda(hoy, hoy.plusDays(1), null)).thenReturn(List.of());
        assertEquals(List.of(), service.execute(null, authentication("OPERADOR")));
        verify(repository).findDriverAgenda(hoy, hoy.plusDays(1), null);
        verifyNoInteractions(authorization);
    }

    @Test
    void facturacionNoPuedeConsultarLaAgenda() {
        assertThrows(AccessDeniedException.class,
                () -> service.execute(LocalDate.now(), authentication("FACTURACION")));
        verifyNoInteractions(repository, authorization);
    }

    private UsernamePasswordAuthenticationToken authentication(String role) {
        return new UsernamePasswordAuthenticationToken("chofer", "", List.of(
                new SimpleGrantedAuthority("ROLE_" + role)));
    }
}
