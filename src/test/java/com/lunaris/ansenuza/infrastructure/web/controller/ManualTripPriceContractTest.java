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
}
