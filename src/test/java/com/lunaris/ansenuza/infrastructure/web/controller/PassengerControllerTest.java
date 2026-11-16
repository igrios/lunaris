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
