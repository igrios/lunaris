package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReservationForm {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    private String cuil;

@NotBlank(message = "El teléfono es obligatorio")
@Pattern(
    regexp = "^[0-9]{10,15}$",
    message = "Ingrese un teléfono válido"
)
private String phone;

    @NotNull(message = "La fecha del viaje es obligatoria")
    private LocalDate travelDate;

    @NotBlank(message = "La localidad de retiro es obligatoria")
    private String pickupLocality;

    @NotBlank(message = "La dirección de retiro es obligatoria")
    private String pickupAddress;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    private Boolean roundTrip = false;

    private LocalDate returnDate;

    private Boolean paymentVerified;

    private String notes;
}