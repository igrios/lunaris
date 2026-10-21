package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.domain.model.TripCategory;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.web.mapper.ManualTripWebMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualTripPriceContractTest {
    @Test
    void jsonFormMapsAgreedTotalAndFreeLocationsToDomain() throws Exception {
        var form = new ObjectMapper().readValue("""
                {"tripCategory":"SPECIAL", "originCustom":"Suardi",
                 "destinationCustom":"Alta Gracia", "passengerCount":9,
                 "customPrice":83000.01, "customPriceIsTotal":true,
                 "departureSchedule":"08:00"}
                """, CreateReservationForm.class);
        var reservation = ManualTripWebMapper.toReservation(form);
        reservation.prepareSpecialTripCreation();
        assertThat(reservation.getTripCategory()).isEqualTo(TripCategory.SPECIAL);
        assertThat(reservation.getPickupLocality()).isEqualTo("Suardi");
        assertThat(reservation.getDestination()).isEqualTo("Alta Gracia");
        assertThat(reservation.getAmount()).isEqualByComparingTo("83000.01");
        assertThat(AdminReservationApiController.AdminReservationResponse.from(reservation)
                .customPriceIsTotal()).isTrue();
    }
    @Test
    void apiEditPreservesIdentityAndRegularCategoryAndReturnsOk() {
        var create = org.mockito.Mockito.mock(com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase.class);
        var payments = org.mockito.Mockito.mock(com.lunaris.ansenuza.application.usecase.MarkTripAsPaidService.class);
        var form = new CreateReservationForm();
        form.setReservationId(java.util.UUID.randomUUID());
        form.setTripCategory(TripCategory.REGULAR);
        form.setPaymentVerified(true);
        var saved = ManualTripWebMapper.toReservation(form);
        org.mockito.Mockito.when(create.execute(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(java.util.List.of(saved));
        var response = new ManualTripApiController(create, payments).create(form);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        var captured = org.mockito.ArgumentCaptor.forClass(com.lunaris.ansenuza.domain.model.Reservation.class);
        org.mockito.Mockito.verify(create).execute(captured.capture(), org.mockito.ArgumentMatchers.isNull());
        assertThat(captured.getValue().getId()).isEqualTo(form.getReservationId());
        assertThat(captured.getValue().getTripCategory()).isEqualTo(TripCategory.REGULAR);
        assertThat(captured.getValue().getPaymentVerified()).isTrue();
    }
}
