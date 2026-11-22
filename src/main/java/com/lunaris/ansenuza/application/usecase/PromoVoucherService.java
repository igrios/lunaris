package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.PaymentStatus;
import com.lunaris.ansenuza.domain.model.PromoVoucher;
import com.lunaris.ansenuza.domain.model.PromoVoucher.Status;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PromoVoucherRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromoVoucherService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");
    private final PromoVoucherRepository vouchers;
    private final CreateManualReservationUseCase manualReservations;
    private final LocalityRepository localities;
    private final ScheduleService schedules;
    private final ReservationRepository reservations;

    public record Registration(String firstName, String lastName, String phone, String cuil,
            BigDecimal paidAmount, String receiptUrl, String notes) {}

    @Transactional
    public PromoVoucher register(Registration input) {
        String firstName = required(input.firstName(), "nombre", 100);
        String lastName = required(input.lastName(), "apellido", 100);
        String phone = PhoneUtils.normalizeArgentinePhone(input.phone());
        String cuil = required(input.cuil(), "DNI/CUIL", 20).replaceAll("[\\s-]", "");
        if (!cuil.matches("[0-9]{7,8}|[0-9]{11}")) {
            throw new DomainValidationException("Ingresá un DNI o CUIL válido.");
        }
        validateAmount(input.paidAmount());
        String receiptUrl = validateReceipt(input.receiptUrl());
        String notes = optional(input.notes(), "observaciones", 500);
        return vouchers.save(PromoVoucher.builder()
                .number("LUN-PROMO-" + String.format(Locale.ROOT, "%06d", vouchers.nextNumber()))
                .firstName(firstName).lastName(lastName).phone(phone).cuil(cuil)
                .paidAmount(input.paidAmount()).receiptUrl(receiptUrl).notes(notes)
                .createdAt(ArgentinaTime.now()).build());
    }

    public PromoVoucher find(String number) {
        return vouchers.findByNumber(normalizeNumber(number)).orElseThrow(PromoVoucherService::missingPromo);
    }

    public Optional<Reservation> linkedReservation(PromoVoucher promo) {
        return Optional.ofNullable(promo.getReservationId()).flatMap(reservations::findById);
    }

    public Page<PromoVoucher> search(Status status, String query, int page) {
        String search = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        String phone = search.matches("[+()0-9 .-]+") ? search.replaceAll("\\D", "") : "";
        if (phone.length() >= 10) {
            try {
                phone = PhoneUtils.normalizeArgentinePhone(phone);
            } catch (DomainValidationException ignored) {
                // Conserva la búsqueda literal para teléfonos incompletos.
            }
        }
        return vouchers.search(status, search, phone, PageRequest.of(Math.max(page, 0), 25));
    }

    @Transactional
    public Reservation redeem(String number, Reservation trip, String returnSchedule) {
        PromoVoucher promo = lockVoucher(number);
        promo.requireAvailable();
        if (!Boolean.TRUE.equals(trip.getRoundTrip())) trip.setReturnDate(null);
        validateTrip(trip, returnSchedule);
        BigDecimal total = trip.isSpecialTrip() ? trip.getCustomPrice() : trip.getAmount();
        validateAmount(total);
        if (total.compareTo(promo.getPaidAmount()) < 0) {
            throw new DomainValidationException("El importe total del viaje debe cubrir el monto abonado de la promo.");
        }

        trip.setPassenger(Passenger.builder().firstName(promo.getFirstName()).lastName(promo.getLastName())
                .phone(promo.getPhone()).cuil(promo.getCuil()).build());
        trip.setRouteDirection(null);
        trip.setPickupAddress(optional(trip.getPickupAddress(), "dirección de retiro", 255));
        trip.setDiscountAmount(BigDecimal.ZERO);
        trip.setExtraAmount(BigDecimal.ZERO);
        trip.setPaymentReceiptUrl(null);
        trip.setPaymentVerified(false);
        trip.setPaymentStatus(PaymentStatus.PENDING);
        trip.setRequiresInvoice(true);
        String notes = optional(trip.getNotes(), "observaciones", 455);
        trip.setNotes("Canje de promo " + promo.getNumber() + (notes == null ? "" : " | " + notes));

        List<Reservation> legs = manualReservations.execute(trip, returnSchedule);
        applyPayment(promo, total, legs);
        Reservation primary = legs.getFirst();
        promo.redeem(primary.getId(), ArgentinaTime.now());
        return primary;
    }

    @Transactional
    public void cancel(String number) {
        lockVoucher(number).cancel();
    }

    private void validateTrip(Reservation trip, String returnSchedule) {
        if (trip.getId() != null || trip.getTripCategory() == null || trip.getTravelDate() == null
                || trip.getTravelDate().isBefore(ArgentinaTime.now().toLocalDate())) {
            throw new DomainValidationException("Seleccioná una fecha vigente para el nuevo viaje.");
        }
        if (trip.getPassengerCount() == null || trip.getPassengerCount() < 1 || trip.getPassengerCount() > 9) {
            throw new DomainValidationException("Ingresá entre 1 y 9 pasajeros.");
        }
        requireTime(trip.getDepartureSchedule());
        if (Boolean.TRUE.equals(trip.getRoundTrip()) && trip.getReturnDate() != null) {
            requireTime(returnSchedule);
        }
        if (trip.isSpecialTrip()) {
            trip.setCustomPriceIsTotal(true);
            return;
        }
        List<String> activeLocalities = localities.findAllWithActiveFare().stream()
                .map(locality -> locality.getName()).toList();
        String origin = trip.getPickupLocality();
        String destination = trip.getDestination();
        if (!isValidLocality(activeLocalities, origin) || !isValidLocality(activeLocalities, destination)
                || origin.equalsIgnoreCase(destination)
                || (TripRouteCalculatorService.isCordoba(origin) && TripRouteCalculatorService.isCordoba(destination))) {
            throw new DomainValidationException("Seleccioná localidades habilitadas y distintas.");
        }
        if (!isScheduled(origin, trip.getTravelDate(), trip.getDepartureSchedule())) {
            throw new DomainValidationException("Seleccioná un horario habilitado para la salida.");
        }
        if (Boolean.TRUE.equals(trip.getRoundTrip()) && trip.getReturnDate() != null
                && !isScheduled(destination, trip.getReturnDate(), returnSchedule)) {
            throw new DomainValidationException("Seleccioná un horario habilitado para el regreso.");
        }
    }

    private static boolean isValidLocality(List<String> localities, String name) {
        return name != null && ("Córdoba".equals(name) || localities.contains(name));
    }

    private boolean isScheduled(String origin, LocalDate date, String time) {
        var availableSchedules = TripRouteCalculatorService.isCordoba(origin)
                ? schedules.getReturnSchedulesForWeb(date) : schedules.getSchedulesForWeb(origin, date);
        return availableSchedules.stream().anyMatch(schedule -> schedule.id().equals(time));
    }

    private void applyPayment(PromoVoucher promo, BigDecimal total, List<Reservation> legs) {
        BigDecimal remaining = promo.getPaidAmount();
        boolean paidInFull = remaining.compareTo(total) == 0;
        // Cada tramo conserva su parte de la seña y el último absorbe el redondeo.
        for (Reservation leg : legs) {
            BigDecimal applied = leg == legs.getLast() ? remaining
                    : promo.getPaidAmount().multiply(leg.getAmount()).divide(total, 2, RoundingMode.HALF_UP);
            remaining = remaining.subtract(applied);
            leg.setRecognizedPaidAmount(applied);
            leg.setPaymentReceiptUrl(promo.getReceiptUrl());
            leg.setPaymentVerified(paidInFull);
            if (paidInFull) leg.setPaymentConfirmedAt(promo.getCreatedAt());
            leg.synchronizePaymentAndBilling();
        }
    }

    private PromoVoucher lockVoucher(String number) {
        return vouchers.findByNumberForUpdate(normalizeNumber(number)).orElseThrow(PromoVoucherService::missingPromo);
    }

    private static DomainValidationException missingPromo() {
        return new DomainValidationException("No se encontró el número de promo.");
    }

    private static String normalizeNumber(String number) {
        return number.strip().toUpperCase(Locale.ROOT);
    }

    private static void requireTime(String time) {
        if (time == null || !time.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) {
            throw new DomainValidationException("Seleccioná un horario válido para cada tramo programado.");
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(MAX_AMOUNT) > 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new DomainValidationException("Ingresá un importe positivo con hasta ocho enteros y dos decimales.");
        }
    }

    private static String required(String value, String field, int maxLength) {
        String text = optional(value, field, maxLength);
        if (text == null) throw new DomainValidationException("Completá el campo " + field + ".");
        return text;
    }

    private static String optional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String text = value.strip();
        if (text.length() > maxLength) {
            throw new DomainValidationException("El campo " + field + " admite hasta " + maxLength + " caracteres.");
        }
        return text;
    }

    private static String validateReceipt(String value) {
        String url = optional(value, "comprobante", 500);
        if (url == null) return null;
        try {
            URI uri = URI.create(url);
            if (uri.getHost() != null && ("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))) return url;
        } catch (IllegalArgumentException ignored) {
            // La respuesta de validación es la misma para toda URL inválida.
        }
        throw new DomainValidationException("Ingresá una URL válida para el comprobante.");
    }
}
