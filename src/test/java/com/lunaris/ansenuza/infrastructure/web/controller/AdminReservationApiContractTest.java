package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lunaris.ansenuza.application.usecase.ReservationDriverAssignmentService;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import java.time.LocalDate;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminReservationApiContractTest {

    private ReservationDriverAssignmentService assignmentService;
    private ReservationRepository reservationRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        assignmentService = mock(ReservationDriverAssignmentService.class);
        reservationRepository = mock(ReservationRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminReservationApiController(assignmentService, reservationRepository))
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

    @Test
    void returnsEveryStoredReservationWithStatusAndDriverDetails() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFullName("Ana Chofer");
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .driver(driver)
                .status("CONFIRMED")
                .source(ReservationSource.WHATSAPP)
                .travelStatus(Reservation.TravelStatus.PENDING)
                .build();
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservationId.toString()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].source").value("WHATSAPP"))
                .andExpect(jsonPath("$[0].travelStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].driverId").value(driverId.toString()))
                .andExpect(jsonPath("$[0].driverName").value("Ana Chofer"));
    }

    @Test
    void returnsEveryReservationForRequestedTravelDateWithoutStatusOrSourceFiltering()
            throws Exception {
        LocalDate travelDate = LocalDate.of(2026, 8, 10);
        Reservation pendingWeb = Reservation.builder()
                .id(UUID.randomUUID())
                .travelDate(travelDate)
                .status("PENDING_PAYMENT")
                .source(ReservationSource.WEB)
                .build();
        Reservation confirmedManual = Reservation.builder()
                .id(UUID.randomUUID())
                .travelDate(travelDate)
                .status("CONFIRMED")
                .source(ReservationSource.MANUAL)
                .build();
        when(reservationRepository.findByTravelDate(travelDate))
                .thenReturn(List.of(pendingWeb, confirmedManual));

        mockMvc.perform(get("/api/admin/reservations").param("travelDate", travelDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].source").value("WEB"))
                .andExpect(jsonPath("$[1].source").value("MANUAL"));

        verify(reservationRepository).findByTravelDate(travelDate);
    }
}
