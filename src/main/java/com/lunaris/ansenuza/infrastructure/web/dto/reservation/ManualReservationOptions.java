package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Campos opcionales del operador; null en amount solicita la tarifa vigente. */
@Getter
@Setter
public class ManualReservationOptions {
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal extraAmount;
    private String companionNames;
    private String routeDirection;
    private String notes;
    private String returnDepartureSchedule;
}
