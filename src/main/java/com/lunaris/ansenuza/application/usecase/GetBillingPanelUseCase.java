package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.CuilCalculator;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.billing.BillingPanelView;
import com.lunaris.ansenuza.infrastructure.web.dto.billing.PendingInvoiceRow;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBillingPanelUseCase {

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;

    public BillingPanelView execute() {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();

        BigDecimal ingresoHoy = reservationRepository.sumConfirmedIncomeBetween(startOfDay, startOfTomorrow);
        long countHoy = reservationRepository.countConfirmedIncomeBetween(startOfDay, startOfTomorrow);
        BigDecimal ingresoMes = reservationRepository.sumConfirmedIncomeBetween(startOfMonth, startOfNextMonth);
        long countMes = reservationRepository.countConfirmedIncomeBetween(startOfMonth, startOfNextMonth);

        // Pendientes de factura: se toma exclusivamente la ida como cabecera del grupo
        // para facturar el importe neto realmente cobrado por todo el viaje.
        List<PendingInvoiceRow> pendientes = reservationRepository.findByStatus("CONFIRMED").stream()
                .filter(r -> r.getReservationCode() == null || r.getReservationCode().endsWith("-IDA"))
                .filter(r -> groupTotalAmount(r).signum() > 0)
                .filter(r -> !invoiceRepository.existsByReservationId(r.getId()))
                .map(this::toRow)
                .toList();

        return new BillingPanelView(
                ingresoHoy != null ? ingresoHoy : BigDecimal.ZERO,
                countHoy,
                ingresoMes != null ? ingresoMes : BigDecimal.ZERO,
                countMes,
                pendientes,
                invoiceRepository.findAllByOrderByCreatedAtDesc()
        );
    }

    private PendingInvoiceRow toRow(Reservation r) {
        String nombre = r.getPassenger().getFirstName() + " " + r.getPassenger().getLastName();
        String rawDoc = r.getPassenger().getCuil();
        String route = r.getPickupLocality() + " → " + r.getDestination();
        return new PendingInvoiceRow(
                r.getId(),
                r.getReservationCode(),
                nombre,
                r.getPassenger().getPhone(),
                rawDoc,
                CuilCalculator.suggestCuil(rawDoc),
                groupTotalAmount(r),
                r.getTravelDate(),
                route
        );
    }

    private BigDecimal totalReservationAmount(Reservation reservation) {
        BigDecimal amount = reservation.getAmount() == null ? BigDecimal.ZERO : reservation.getAmount();
        BigDecimal extraAmount = reservation.getExtraAmount() == null ? BigDecimal.ZERO : reservation.getExtraAmount();
        return amount.add(extraAmount);
    }

    private BigDecimal groupTotalAmount(Reservation reservation) {
        if (reservation.getReservationCode() == null) {
            return totalReservationAmount(reservation);
        }
        String groupCode = reservation.getReservationCode().replaceFirst("-(IDA|VUELTA)$", "");
        return reservationRepository.findReservationGroup(groupCode).stream()
                .map(this::totalReservationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
