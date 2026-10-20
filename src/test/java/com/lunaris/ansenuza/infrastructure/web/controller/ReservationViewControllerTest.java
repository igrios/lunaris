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
    void specialEntryPreselectsFreeTripAndDateWithoutDependingOnFixedFares() throws Exception {
        LocalityRepository localities = mock(LocalityRepository.class);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(formController(localities)).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/reservations/new")
                        .param("tripCategory", "SPECIAL").param("travelDate", "2030-01-02"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("reservation-form"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("initialTripCategory", "SPECIAL"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("initialTravelDate", LocalDate.of(2030, 1, 2)));
        org.mockito.Mockito.verifyNoInteractions(localities);
    }

    @Test
    void sessionLinkPreloadsSpecialFormAndPassengerFromDatabase() throws Exception {
        var monitor = mock(com.lunaris.ansenuza.application.usecase.BotMonitorService.class);
        var passengers = mock(PassengerRepository.class);
        var localities = mock(LocalityRepository.class);
        var session = com.lunaris.ansenuza.domain.model.ConversationSession.builder()
                .id(42L).phoneNumber("5493511234567").passengerName("Ana Pérez")
                .tripCategory(com.lunaris.ansenuza.domain.model.TripCategory.SPECIAL)
                .originCustom("Suardi").destinationCustom("Alta Gracia")
                .travelDate(LocalDate.of(2030, 1, 2)).passengerCount(4)
                .customPrice(new java.math.BigDecimal("83000.01")).build();
        when(monitor.specialTrip(42L)).thenReturn(session);
        when(passengers.findFirstByPhone(session.getPhoneNumber())).thenReturn(Optional.of(
                Passenger.builder().firstName("Ana").lastName("Pérez").cuil("27123456789").build()));
        var controller = new ReservationViewController(monitor, passengers, localities,
                mock(ReservationService.class), mock(ReservationRepository.class),
                mock(PricingAndScheduleService.class), mock(DriverRepository.class), mock(WhatsAppService.class),
                mock(WaitingListService.class), mock(WaitingListConversionService.class),
                mock(WaitingListReengagementService.class),
                mock(com.lunaris.ansenuza.application.usecase.NewsBannerService.class));
        var model = new org.springframework.ui.ExtendedModelMap();
        assertEquals("reservation-form", controller.newReservation(
                com.lunaris.ansenuza.domain.model.TripCategory.REGULAR, 42L, null, model));
        var form = (com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm)
                model.get("reservation");
        assertEquals("SPECIAL", model.get("initialTripCategory"));
        assertEquals("Ana", form.getFirstName());
        assertEquals("Pérez", form.getLastName());
        assertEquals(session.getPhoneNumber(), form.getPhone());
        assertEquals("Suardi", form.getOriginCustom());
        assertEquals("Alta Gracia", form.getDestinationCustom());
        assertEquals(session.getTravelDate(), form.getTravelDate());
        assertEquals(4, form.getPassengerCount());
        assertEquals(session.getCustomPrice(), form.getCustomPrice());
        org.assertj.core.api.Assertions.assertThat(form.isCustomPriceIsTotal()).isTrue();
        assertEquals(com.lunaris.ansenuza.domain.model.PaymentStatus.PENDING, form.getPaymentStatus());
        org.mockito.Mockito.verifyNoInteractions(localities);
        when(passengers.findFirstByPhone(session.getPhoneNumber())).thenReturn(Optional.empty());
        controller.newReservation(com.lunaris.ansenuza.domain.model.TripCategory.REGULAR, 42L, null, model);
        form = (com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm) model.get("reservation");
        assertEquals("Ana", form.getFirstName());
        assertEquals("Pérez", form.getLastName());
    }

    @Test
    void standardEntryKeepsRegularMode() throws Exception {
        LocalityRepository localities = mock(LocalityRepository.class);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(formController(localities)).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/reservations/new"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attribute("initialTripCategory", "REGULAR"));
        verify(localities).findAllWithActiveFare();
    }

    private ReservationViewController formController(LocalityRepository localities) {
        return new ReservationViewController(mock(com.lunaris.ansenuza.application.usecase.BotMonitorService.class), mock(PassengerRepository.class), localities,
                mock(ReservationService.class), mock(ReservationRepository.class),
                mock(PricingAndScheduleService.class), mock(DriverRepository.class), mock(WhatsAppService.class),
                mock(WaitingListService.class), mock(WaitingListConversionService.class),
                mock(WaitingListReengagementService.class),
                mock(com.lunaris.ansenuza.application.usecase.NewsBannerService.class));
    }

    @Test
    void confirmingOpenReturnSendsWhatsAppConfirmation() {
        ReservationRepository reservations = mock(ReservationRepository.class);
        DriverRepository drivers = mock(DriverRepository.class);
        WhatsAppService whatsApp = mock(WhatsAppService.class);
        ReservationService reservationService = mock(ReservationService.class);
        ReservationViewController controller = new ReservationViewController(mock(com.lunaris.ansenuza.application.usecase.BotMonitorService.class),
                mock(PassengerRepository.class),
                mock(LocalityRepository.class),
                reservationService,
                reservations,
                mock(PricingAndScheduleService.class),
                drivers,
                whatsApp,
                mock(WaitingListService.class),
                mock(WaitingListConversionService.class),
                mock(WaitingListReengagementService.class),
                mock(com.lunaris.ansenuza.application.usecase.NewsBannerService.class));
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
        assertEquals(confirmedDate, openReturn.getTravelDate());
        assertEquals("08:30 AM", openReturn.getDepartureSchedule());
        assertEquals(Reservation.TravelStatus.PENDING, openReturn.getTravelStatus());
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

    @Test
    void normalizesStandardOpenReturnShiftFormats() {
        assertEquals("03:00 AM", ReservationViewController.normalizeDepartureSchedule("03:00"));
        assertEquals("08:00 AM", ReservationViewController.normalizeDepartureSchedule("08:00 AM"));
        assertEquals("18:00 PM", ReservationViewController.normalizeDepartureSchedule("18:00"));
    }
}
