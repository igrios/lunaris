package com.lunaris.ansenuza.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateDTO {
    // Datos del Pasajero
    private String firstName;
    private String lastName;
    private String phone;
    private String cuil; // DNI / CUIT de facturación

    // Datos del Viaje
    private LocalDate travelDate;
    private Boolean roundTrip;
    private LocalDate returnDate;
    private String departureSchedule; // Bloque horario

    // Trayectos
    private String pickupLocality;
    private String pickupAddress;
    private String destination;

    // Gestión de Asientos
    private Integer passengerCount;
    private List<String> companionNames;

    // Extras
    private Boolean paymentVerified;
    private String notes;
    private ReservationSource source;
}
