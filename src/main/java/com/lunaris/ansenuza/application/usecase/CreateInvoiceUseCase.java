package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.CuilCalculator;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Crea de forma idempotente el registro de factura al verificarse un pago. */
@Service
@RequiredArgsConstructor
public class CreateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice execute(Reservation reservation) {
        return execute(reservation, List.of(reservation));
    }

    @Transactional
    public Invoice execute(Reservation reservation, List<Reservation> reservationGroup) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("La reserva debe estar persistida antes de facturarla.");
        }
        return invoiceRepository.findByReservationId(reservation.getId())
                .orElseGet(() -> invoiceRepository.save(Invoice.builder()
                        .reservationId(reservation.getId())
                        .invoiceNumber(nextInvoiceNumber())
                        .passengerName(passengerName(reservation))
                        .passengerCuil(reservation.getPassenger() == null
                                ? null
                                : CuilCalculator.suggestCuil(reservation.getPassenger().getCuil()))
                        .amount(reservationGroup.stream()
                                .map(this::totalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .sentViaWhatsapp(false)
                        .build()));
    }

    private String nextInvoiceNumber() {
        return String.format("F-%d-%05d", Year.now().getValue(), invoiceRepository.count() + 1);
    }

    private String passengerName(Reservation reservation) {
        if (reservation.getPassenger() == null) {
            return null;
        }
        return (reservation.getPassenger().getFirstName() + " "
                + reservation.getPassenger().getLastName()).trim();
    }

    private BigDecimal totalAmount(Reservation reservation) {
        BigDecimal amount = reservation.getAmount() == null ? BigDecimal.ZERO : reservation.getAmount();
        BigDecimal extra = reservation.getExtraAmount() == null
                ? BigDecimal.ZERO
                : reservation.getExtraAmount();
        return amount.add(extra);
    }
}
