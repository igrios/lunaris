package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.PassengerContactTemplate;
import com.lunaris.ansenuza.domain.model.Reservation;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ManualReservationNotificationServiceTest {
    @Test
    void missingDetailsInviteFreeReplyAndPaymentWithoutStrictForms() {
        var reservation = Reservation.builder().passengerCount(3).paymentVerified(false).build();
        assertThat(ManualReservationNotificationService.pendingDetails(reservation))
                .contains("dirección exacta de retiro", "nombres de tus acompañantes", "registrar el pago",
                        "responder libremente", "operadores", "no necesitás completar un formulario");
    }

    @Test
    void paidSoloPassengerDoesNotGetFalsePendingRequests() {
        var reservation = Reservation.builder().passengerCount(1).pickupAddress("Belgrano 100")
                .paymentVerified(true).build();
        assertThat(ManualReservationNotificationService.pendingDetails(reservation))
                .doesNotContain("pendiente", "acompañantes", "registrar el pago")
                .contains("responder libremente");
    }

    @Test
    void approvedTemplateKeepsSingleNormalizedNameParameter() {
        assertThat(PassengerContactTemplate.NAME).isEqualTo("contacto_pasajero");
        assertThat(PassengerContactTemplate.parameters(" Ana\n Pérez ")).containsExactly("Ana Pérez");
        assertThat(PassengerContactTemplate.parameters(null)).containsExactly("Pasajero");
    }
}
