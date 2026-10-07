package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReservationForm extends ManualReservationOptions {

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

    private String pickupLocality;

    @NotBlank(message = "La dirección de retiro es obligatoria")
    private String pickupAddress;

    private String destination;

    private Boolean roundTrip = false;

    private LocalDate returnDate;

    // 🕒 Horario de salida (igual que el bot): "03:00 AM" (primer turno) u "08:00 AM" (segundo turno)
    private String departureSchedule;

    private Boolean paymentVerified;

    // 🌟 NUEVOS CAMPOS AGREGADOS PARA LA GESTIÓN DE ASIENTOS Y ACOMPAÑANTES
    @NotNull(message = "La cantidad de pasajeros es obligatoria")
    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.Max(value = 4, message = "La cantidad de pasajeros no puede superar 4")
    private Integer passengerCount;

    private Boolean requiresInvoice = false;
}
