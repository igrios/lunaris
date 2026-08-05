package com.lunaris.ansenuza.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JpaBankPaymentReservationAdapterTest {

    @Test
    void expectedTotalIncludesEveryLegAmountAndExtraAmount() {
        UUID selectedId = UUID.randomUUID();
        Reservation outbound = Reservation.builder()
                .id(selectedId)
                .reservationCode("MOR-COR-001-IDA")
                .amount(new BigDecimal("5000.00"))
                .extraAmount(new BigDecimal("250.00"))
                .build();
        Reservation inbound = Reservation.builder()
                .id(UUID.randomUUID())
                .reservationCode("MOR-COR-001-VUELTA")
                .amount(new BigDecimal("5000.00"))
                .extraAmount(new BigDecimal("250.00"))
                .build();
        ReservationRepository repository = mock(ReservationRepository.class);
        when(repository.findReservationGroupForUpdate("MOR-COR-001"))
                .thenReturn(List.of(outbound, inbound));

        var adapter = new JpaBankPaymentReservationAdapter(
                repository, mock(ConfirmPaymentUseCase.class));
        var candidate = adapter.findByReservationCode("MOR-COR-001-IDA").orElseThrow();

        assertEquals(selectedId, candidate.reservationId());
        assertEquals(new BigDecimal("10500.00"), candidate.expectedTotal());
    }
}
