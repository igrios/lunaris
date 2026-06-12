package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


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

    private Boolean roundTrip = false;

    private LocalDate returnDate;

    private Boolean paymentVerified;

    private String notes;
}
