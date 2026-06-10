package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateReservationForm {

    // Datos pasajero

    private String firstName;

    private String lastName;

    private String cuil;

    private String phone;

  
    // Datos viaje

    private LocalDate travelDate;

    private String pickupLocality;

    private String pickupAddress;

    private String destination;

    private Boolean paymentVerified;

    private String notes;
}