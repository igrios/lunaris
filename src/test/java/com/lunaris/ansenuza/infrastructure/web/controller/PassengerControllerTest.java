package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreatePassengerUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerDetailsService;
import com.lunaris.ansenuza.domain.exception.PassengerNotFoundException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PassengerControllerTest {
    @Test
    void consultaPorTelefonoDevuelveDatosONoContent() throws Exception {
        var service = mock(PassengerDetailsService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new PassengerController(service,
                mock(PassengerRepository.class), mock(CreatePassengerUseCase.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        when(service.findByPhone("456")).thenReturn(java.util.Optional.of(
                new PassengerDetailsService.PassengerLookup(UUID.randomUUID(), "Ana", "Pérez",
                        "123", "456", "San Martín 100", "Morteros", "Equipaje")));
        mvc.perform(get("/passengers/by-phone").param("phone", "456"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.cuil").value("123"))
                .andExpect(jsonPath("$.address").value("San Martín 100"))
                .andExpect(jsonPath("$.notes").value("Equipaje"))
                .andExpect(jsonPath("$.currentBalance").doesNotExist());
        mvc.perform(get("/passengers/by-phone").param("phone", "789"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get("/passengers/by-phone")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsPassengerNotesAndNotFoundStatus() throws Exception {
        var service = mock(PassengerDetailsService.class);
        var controller = new PassengerController(service, mock(PassengerRepository.class),
                mock(CreatePassengerUseCase.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler()).build();
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(Passenger.builder().id(id)
                .firstName("Ana").lastName("Pérez").cuil("123")
                .phone("456").address("San Martín 100").notes("Asistencia\nEquipaje").build());
        mvc.perform(get("/passengers/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("Asistencia\nEquipaje"))
                .andExpect(jsonPath("$.cuil").value("123"))
                .andExpect(jsonPath("$.address").value("San Martín 100"));
        when(service.findById(id)).thenThrow(new PassengerNotFoundException());
        mvc.perform(get("/passengers/{id}", id)).andExpect(status().isNotFound());
    }
}
