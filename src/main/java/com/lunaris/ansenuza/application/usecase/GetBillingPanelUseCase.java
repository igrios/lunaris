package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Passenger;
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

        List<PendingInvoiceRow> pendientes = consolidatePendingInvoices(
                reservationRepository.findPendingInvoiceReservations());

        return new BillingPanelView(
                ingresoHoy != null ? ingresoHoy : BigDecimal.ZERO,
                countHoy,
                ingresoMes != null ? ingresoMes : BigDecimal.ZERO,
                countMes,
                pendientes,
                invoiceRepository.findAllByOrderByCreatedAtDesc()
        );
    }

    private List<PendingInvoiceRow> consolidatePendingInvoices(List<Reservation> reservations) {
        var groups = new LinkedHashMap<String, List<Reservation>>();
        for (Reservation reservation : reservations) {
            groups.compute(baseCode(reservation), (code, existing) -> {
                var legs = existing == null
                        ? new java.util.ArrayList<Reservation>()
                        : new java.util.ArrayList<>(existing);
                legs.add(reservation);
                return legs;
            });
        }
        return groups.values().stream()
                .filter(legs -> combinedAmount(legs).signum() > 0)
                .map(this::toRow)
                .toList();
    }

    private PendingInvoiceRow toRow(List<Reservation> legs) {
        Reservation primary = legs.stream()
                .filter(item -> item.getReservationCode() != null
                        && item.getReservationCode().endsWith("-IDA"))
                .findFirst()
                .orElse(legs.getFirst());
        Passenger passenger = legs.stream()
                .map(Reservation::getPassenger)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        String nombre = passenger == null
                ? "Pasajero sin vincular"
                : fullName(passenger);
        String rawDoc = passenger == null ? null : passenger.getCuil();
        String code = baseCode(primary);
        String route = primary.getPickupLocality() + " → " + primary.getDestination();
        if (legs.size() > 1) {
            route += " (Ida y Vuelta)";
        }
        return new PendingInvoiceRow(
                primary.getId(),
                code,
                nombre,
                passenger == null ? null : passenger.getPhone(),
                rawDoc,
                CuilCalculator.suggestCuil(rawDoc),
                combinedAmount(legs),
                primary.getTravelDate(),
                route
        );
    }

    private String baseCode(Reservation reservation) {
        if (reservation.getReservationCode() == null) {
            return "UUID:" + reservation.getId();
        }
        return reservation.getReservationCode().replaceFirst("-(IDA|VUELTA)$", "");
    }

    private BigDecimal combinedAmount(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::totalReservationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String fullName(Passenger passenger) {
        String firstName = passenger.getFirstName() == null ? "" : passenger.getFirstName().trim();
        String lastName = passenger.getLastName() == null ? "" : passenger.getLastName().trim();
        String name = (firstName + " " + lastName).trim();
        return name.isBlank() ? "Pasajero sin nombre" : name;
    }

    private BigDecimal totalReservationAmount(Reservation reservation) {
        BigDecimal amount = reservation.getAmount() == null ? BigDecimal.ZERO : reservation.getAmount();
        BigDecimal extraAmount = reservation.getExtraAmount() == null ? BigDecimal.ZERO : reservation.getExtraAmount();
        return amount.add(extraAmount);
    }

}
