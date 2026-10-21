package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManualReservationConsistencyTest {
    @Test void paidAndConfirmedStatesCannotOptOutOfBilling() {
        var paid = Reservation.builder().paymentStatus(PaymentStatus.PAID).paymentVerified(false).requiresInvoice(false).build();
        paid.synchronizePaymentAndBilling();
        assertThat(paid.getPaymentVerified()).isTrue();
        assertThat(paid.getRequiresInvoice()).isTrue();
        assertThat(paid.getPaymentConfirmedAt()).isNotNull();
        var confirmed = Reservation.builder().status("CONFIRMED").requiresInvoice(false).build();
        confirmed.synchronizePaymentAndBilling();
        assertThat(confirmed.getRequiresInvoice()).isTrue();
        assertThat(confirmed.getPaymentVerified()).isFalse();
    }

    @Test void explicitIdentityChoosesUpdateAndReceiptCountsAsOperatorValidation() {
        var passengers = mock(PassengerRepository.class);
        var reservations = mock(ReservationService.class);
        var passenger = Passenger.builder().firstName("Ana").lastName("Pérez").phone("543511234567").build();
        var input = Reservation.builder().id(UUID.randomUUID()).passenger(passenger)
                .tripCategory(TripCategory.REGULAR).paymentReceiptUrl("https://receipt.test/selected.pdf")
                .requiresInvoice(false).build();
        when(passengers.findFirstByPhone(anyString())).thenReturn(Optional.of(passenger));
        when(passengers.saveAndFlush(passenger)).thenReturn(passenger);
        when(reservations.updateManualReservationFlow(input, null)).thenReturn(List.of(input));
        new CreateManualReservationUseCase(passengers, reservations).execute(input, null);
        verify(reservations).updateManualReservationFlow(input, null);
        verify(reservations, never()).saveManualReservationFlow(any(), any());
        assertThat(input.getTripCategory()).isEqualTo(TripCategory.REGULAR);
        assertThat(input.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(input.getPaymentVerified()).isTrue();
        assertThat(input.getRequiresInvoice()).isTrue();
    }
}
