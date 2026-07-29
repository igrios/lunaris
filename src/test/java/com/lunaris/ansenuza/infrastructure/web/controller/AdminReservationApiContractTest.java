package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.ReservationDriverAssignmentService;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminReservationApiContractTest {

    private ReservationDriverAssignmentService assignmentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        assignmentService = mock(ReservationDriverAssignmentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminReservationApiController(assignmentService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void assignsDriverUsingExactJsonContract() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Driver driver = new Driver();
        driver.setId(driverId);
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .driver(driver)
                .travelStatus(Reservation.TravelStatus.PENDING)
                .build();
        when(assignmentService.assign(reservationId, driverId))
                .thenReturn(Optional.of(reservation));

        mockMvc.perform(put("/api/admin/reservations/{id}/assign-driver", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"driverId\":\"" + driverId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.driverId").value(driverId.toString()))
                .andExpect(jsonPath("$.travelStatus").value("PENDING"));
    }

    @Test
    void assigningUnknownReservationOrDriverReturnsNotFound() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        when(assignmentService.assign(reservationId, driverId))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/admin/reservations/{id}/assign-driver", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"driverId\":\"" + driverId + "\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unassignsDriverWithoutBodyAndResetsTravelStatus() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .travelStatus(Reservation.TravelStatus.PENDING)
                .build();
        when(assignmentService.unassign(reservationId))
                .thenReturn(Optional.of(reservation));

        mockMvc.perform(put("/api/admin/reservations/{id}/unassign-driver", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.driverId").doesNotExist())
                .andExpect(jsonPath("$.travelStatus").value("PENDING"));
    }
}
