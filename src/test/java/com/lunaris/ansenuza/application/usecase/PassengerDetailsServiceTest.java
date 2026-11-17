package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.PassengerNotFoundException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.UpdatePassengerRequest;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PassengerDetailsServiceTest {
    private final PassengerRepository repository = mock(PassengerRepository.class);
    private final PassengerDetailsService service = new PassengerDetailsService(repository);

    @Test
    void buscaTelefonoNormalizadoYDevuelveDatosParaAutocompletar() {
        when(repository.findFirstByPhone("+54 9 351 1234567")).thenReturn(Optional.empty());
        when(repository.findFirstByPhone("543511234567")).thenReturn(Optional.of(Passenger.builder()
                .firstName("Ana").lastName("Pérez").cuil("123").phone("543511234567")
                .address("San Martín 100").locality("Córdoba").notes("Equipaje").build()));
        var result = service.findByPhone(" +54 9 351 1234567 ").orElseThrow();
        assertEquals("Ana", result.firstName());
        assertEquals("Pérez", result.lastName());
        assertEquals("123", result.cuil());
        assertEquals("San Martín 100", result.address());
        assertEquals("Equipaje", result.notes());
        verify(repository, never()).save(any());
    }

    @Test
    void telefonoInexistenteNoCreaPasajeroYRechazaConsultaVacia() {
        assertTrue(service.findByPhone("543511234567").isEmpty());
        assertThrows(DomainValidationException.class, () -> service.findByPhone(" "));
        assertThrows(DomainValidationException.class, () -> service.findByPhone(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updatesNotesAndContactWithoutChangingBalance() {
        UUID id = UUID.randomUUID();
        Passenger passenger = Passenger.builder().id(id).notes("Anterior")
                .currentBalance(new BigDecimal("2500")).build();
        when(repository.findById(id)).thenReturn(Optional.of(passenger));
        when(repository.save(passenger)).thenReturn(passenger);
        var request = new UpdatePassengerRequest(" Ana ", " Pérez ", "123", "456",
                "San Martín 100", "Morteros", "Equipaje grande\nRequiere asistencia");
        Passenger result = service.update(id, request);
        assertEquals("Ana", result.getFirstName());
        assertEquals("Pérez", result.getLastName());
        assertEquals(request.notes(), result.getNotes());
        assertEquals(request.address(), result.getAddress());
        assertEquals(request.phone(), result.getPhone());
        assertEquals(new BigDecimal("2500"), result.getCurrentBalance());
        assertSame(result, service.findById(id));
        service.update(id, new UpdatePassengerRequest("Ana", "Pérez", "123", "456", "", "", ""));
        assertEquals("", passenger.getNotes());
    }

    @Test
    void rejectsMissingPassengerAndInvalidNames() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(PassengerNotFoundException.class, () -> service.findById(id));
        assertThrows(PassengerNotFoundException.class, () -> service.update(id,
                new UpdatePassengerRequest("Ana", "Pérez", null, null, null, null, null)));
        assertThrows(DomainValidationException.class, () -> service.update(id,
                new UpdatePassengerRequest(" ", "Pérez", null, null, null, null, null)));
        verify(repository, never()).save(any());
    }
}
