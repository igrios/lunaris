package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.dto.*;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationUpdateCapacityService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationUpdatePricingService {
    private final PricingAndScheduleService pricing;
    private final PassengerRepository passengers;
    private final EntityManager entityManager;

    public record Outcome(BigDecimal total, BigDecimal invoiceTotal, BigDecimal difference,
            BigDecimal pending, BigDecimal credited) {}

    @Transactional
    public Outcome apply(List<Reservation> before, List<Reservation> proposed, Reservation primary,
            PricingChangeRequest request, boolean topologyChanged) {
        List<Reservation> oldActive = before.stream().filter(ReservationUpdateCapacityService::active).toList();
        List<Reservation> active = proposed.stream().filter(ReservationUpdateCapacityService::active).toList();
        BigDecimal previous = grossTotal(oldActive);
        if (request == null && !topologyChanged) {
            return new Outcome(previous, BookingInvoiceAmount.total(active), BigDecimal.ZERO,
                    pending(active), BigDecimal.ZERO);
        }
        if (oldActive.stream().anyMatch(Reservation::isAmountIsGroupTotal)) {
            throw new DomainValidationException("Normalizá los importes legados del grupo antes de editar su tarifa.");
        }
        BigDecimal extras = oldActive.stream().map(r -> zero(r.getExtraAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount;
        BigDecimal base;
        if (request != null && request.mode() == PricingMode.AGREED_TOTAL) {
            validateMoney(request.agreedAmount());
            base = request.agreedAmount().subtract(extras);
            discount = BigDecimal.ZERO;
            if (base.signum() < 0) throw new DomainValidationException("El total acordado no puede ser menor a los extras existentes.");
        } else {
            if (request != null && request.agreedAmount() != null) {
                throw new DomainValidationException("RECALCULATE no admite un monto acordado.");
            }
            base = calculateBase(active, primary);
            Integer percent = primary.getPromotionDiscountPercentage();
            if (percent != null && (percent < 0 || percent > 100)) {
                throw new DomainValidationException("El descuento promocional es inválido.");
            }
            discount = percent == null ? oldActive.stream().map(r -> zero(r.getDiscountAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    : base.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (discount.signum() < 0 || discount.compareTo(base) > 0) {
                throw new DomainValidationException("El descuento supera la nueva tarifa.");
            }
            base = base.subtract(discount);
        }
        BigDecimal total = base.add(extras).setScale(2, RoundingMode.HALF_UP);
        validateMoney(total);
        BigDecimal usedBefore = oldActive.stream().map(r -> zero(r.getUsedBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidBefore = oldActive.stream().map(Reservation::recognizedPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal used = usedBefore.min(total);
        BigDecimal paid = paidBefore.min(total.subtract(used));
        BigDecimal credited = usedBefore.subtract(used).add(paidBefore.subtract(paid));
        if (credited.signum() > 0) {
            Passenger passenger = passengers.findByIdForUpdate(primary.getPassenger().getId())
                    .orElseThrow(() -> new DomainValidationException("El pasajero no existe."));
            // Puede haberse cargado por EAGER antes de esperar el lock de otra acreditación.
            entityManager.refresh(passenger);
            passenger.setCurrentBalance(zero(passenger.getCurrentBalance()).add(credited));
            passengers.save(passenger);
        }
        int weight = active.stream().mapToInt(Reservation::getTotalSeats).sum();
        BigDecimal remainingBase = base;
        BigDecimal remainingDiscount = discount;
        for (int i = 0; i < active.size(); i++) {
            Reservation leg = active.get(i);
            BigDecimal legBase = i == active.size() - 1 ? remainingBase
                    : share(base, leg.getTotalSeats(), weight);
            BigDecimal legDiscount = i == active.size() - 1 ? remainingDiscount
                    : share(discount, leg.getTotalSeats(), weight);
            remainingBase = remainingBase.subtract(legBase);
            remainingDiscount = remainingDiscount.subtract(legDiscount);
            BigDecimal legExtra = leg.getId().equals(primary.getId()) ? extras : BigDecimal.ZERO;
            BigDecimal legUsed = used.min(legBase.add(legExtra));
            used = used.subtract(legUsed);
            BigDecimal usedOnBase = legUsed.min(legBase);
            leg.setAmount(legBase.subtract(usedOnBase));
            leg.setExtraAmount(legExtra.subtract(legUsed.subtract(usedOnBase)));
            leg.setUsedBalance(legUsed);
            leg.setDiscountAmount(legDiscount);
            leg.setAmountIsGroupTotal(false);
            BigDecimal net = leg.getAmount().add(leg.getExtraAmount());
            BigDecimal legPaid = paid.min(net);
            paid = paid.subtract(legPaid);
            leg.setRecognizedPaidAmount(legPaid);
            leg.setPaymentVerified(legPaid.compareTo(net) >= 0);
            leg.setPaymentStatus(leg.getPaymentVerified() ? PaymentStatus.PAID : PaymentStatus.PENDING);
            // La deuda parcial no vence automáticamente una reserva que ya tiene pagos reconocidos.
            leg.setPaymentExpiresAt(null);
            if (!leg.getPaymentVerified()) leg.setPaymentConfirmedAt(null);
            if (request != null && request.mode() == PricingMode.AGREED_TOTAL) {
                leg.setPromotionId(null);
                leg.setPromotionCode(null);
                leg.setPromotionDiscountPercentage(null);
                if (leg.isSpecialTrip()) {
                    leg.setCustomPriceIsTotal(true);
                    leg.setCustomPrice(base);
                }
            }
        }
        return new Outcome(total, BookingInvoiceAmount.total(active), total.subtract(previous),
                pending(active), credited);
    }

    public Outcome recordPayment(List<Reservation> group, BigDecimal submitted, Outcome price) {
        List<Reservation> active = group.stream().filter(ReservationUpdateCapacityService::active).toList();
        if (active.stream().anyMatch(Reservation::isAmountIsGroupTotal)) {
            throw new DomainValidationException("Normalizá los importes legados del grupo antes de registrar el cobro.");
        }
        BigDecimal recorded = active.stream().map(Reservation::recognizedPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal additional = submitted.subtract(recorded);
        if (additional.signum() < 0 || additional.compareTo(pending(active)) > 0) {
            throw new DomainValidationException("El pago acumulado debe estar entre lo ya cobrado y el total a cobrar después del saldo a favor.");
        }
        for (Reservation leg : active) {
            if (additional.signum() == 0) break;
            BigDecimal current = leg.recognizedPayment();
            BigDecimal net = zero(leg.getAmount()).add(zero(leg.getExtraAmount()));
            BigDecimal applied = additional.min(net.subtract(current).max(BigDecimal.ZERO));
            if (applied.signum() == 0) continue;
            leg.setRecognizedPaidAmount(current.add(applied));
            leg.setPaymentVerified(current.add(applied).compareTo(net) == 0);
            leg.synchronizePaymentAndBilling();
            additional = additional.subtract(applied);
        }
        return new Outcome(price.total(), price.invoiceTotal(), price.difference(), pending(active), price.credited());
    }

    private BigDecimal calculateBase(List<Reservation> active, Reservation primary) {
        if (primary.isSpecialTrip()) {
            if (primary.getCustomPrice() == null || primary.getCustomPrice().signum() <= 0) {
                throw new DomainValidationException("El viaje especial requiere un precio personalizado.");
            }
            return primary.isCustomPriceIsTotal() ? primary.getCustomPrice()
                    : primary.getCustomPrice().multiply(BigDecimal.valueOf(primary.getTotalSeats()));
        }
        int returning = active.stream().filter(r -> !r.getId().equals(primary.getId()))
                .mapToInt(Reservation::getTotalSeats).sum();
        int oneWay = primary.getTotalSeats() - returning;
        BigDecimal total = BigDecimal.ZERO;
        if (returning > 0) total = pricing.calculateReservationAmount(primary.getPickupLocality(),
                primary.getDestination(), true, returning);
        if (oneWay > 0) total = total.add(pricing.calculateReservationAmount(primary.getPickupLocality(),
                primary.getDestination(), false, oneWay));
        return total;
    }

    private static BigDecimal share(BigDecimal value, int seats, int totalSeats) {
        // Redondear hacia abajo evita que múltiples splits consuman más centavos que el total.
        return value.multiply(BigDecimal.valueOf(seats)).divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.DOWN);
    }

    public static BigDecimal grossTotal(List<Reservation> legs) {
        return BookingInvoiceAmount.total(legs).add(legs.stream().map(r -> zero(r.getUsedBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static BigDecimal pending(List<Reservation> legs) {
        return legs.stream().map(r -> zero(r.getAmount()).add(zero(r.getExtraAmount()))
                .subtract(r.recognizedPayment()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void validateMoney(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("99999999.99")) > 0
                || value.stripTrailingZeros().scale() > 2) {
            throw new DomainValidationException("El monto debe ser no negativo, con hasta ocho enteros y dos decimales.");
        }
    }

    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
