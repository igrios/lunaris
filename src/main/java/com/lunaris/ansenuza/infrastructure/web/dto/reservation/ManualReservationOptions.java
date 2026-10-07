package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.lunaris.ansenuza.domain.model.TripCategory;
import com.lunaris.ansenuza.domain.model.PaymentStatus;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Campos opcionales del operador; null en amount solicita la tarifa vigente. */
@Getter
@Setter
public class ManualReservationOptions {
    @NotNull
    private TripCategory tripCategory =
            TripCategory.REGULAR;
    @Size(max = 100)
    private String originCustom;
    @Size(max = 100)
    private String destinationCustom;
    @Digits(integer = 8, fraction = 2)
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal customPrice;
    @NotNull
    private PaymentStatus paymentStatus =
            PaymentStatus.PENDING;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal extraAmount;
    @Size(max = 500)
    private String companionNames;
    private String routeDirection;
    private String notes;
    private String returnDepartureSchedule;
}
