package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;

class ReservationViewControllerTest {

    @Test
    void confirmingOpenReturnSendsWhatsAppConfirmation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        ReservationService reservationService = mock(ReservationService.class);
        ReservationViewController controller = new ReservationViewController(
                mock(PassengerRepository.class),
                mock(LocalityRepository.class),
                reservationService,
                reservations,
                mock(PricingAndScheduleService.class),
                drivers,
                whatsApp,
                mock(WaitingListService.class),
                mock(WaitingListConversionService.class),
                mock(WaitingListReengagementService.class));
        UUID reservationId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDate confirmedDate = LocalDate.of(2026, 8, 5);
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFullName("Juan Chofer");
        Reservation openReturn = Reservation.builder()
                .id(reservationId)
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .phone("5493511111111")
                        .build())
                .travelDate(LocalDate.of(2099, 12, 31))
                .reservationCode("MOR-COR-001-VUELTA")
                .passengerCount(1)
                .build();
        when(reservations.findById(reservationId)).thenReturn(Optional.of(openReturn));
        when(drivers.findById(driverId)).thenReturn(Optional.of(driver));
        when(reservations.saveAndFlush(openReturn)).thenReturn(openReturn);

        controller.updateFromPanel(
                reservationId,
                confirmedDate,
                "08:30",
                "Terminal",
                driverId,
                "CONFIRMED",
                "ONBOARD",
                1,
                "vueltas");

        verify(reservations).saveAndFlush(openReturn);
        assertEquals(confirmedDate, openReturn.getReturnDate());
        assertEquals("08:30", openReturn.getDepartureSchedule());
        verify(whatsApp).sendMessage(
                eq("5493511111111"),
                contains("Tu vuelta quedó confirmada para el " + confirmedDate));
        ArgumentCaptor<Reservation> updateCaptor =
                ArgumentCaptor.forClass(Reservation.class);
        verify(reservationService).updateReservation(
                eq(reservationId), updateCaptor.capture(), eq("ADMIN_PANEL"));
        assertEquals(
                Reservation.TravelStatus.ONBOARD,
                updateCaptor.getValue().getTravelStatus());
    }
}
