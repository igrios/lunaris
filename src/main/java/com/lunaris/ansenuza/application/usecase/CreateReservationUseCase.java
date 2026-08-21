package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.AirportTripDetector;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.shared.PhoneUtils;

@Service
public class CreateReservationUseCase {

    private final ReservationService reservationService;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final SameDayBookingPolicy sameDayBookingPolicy;
    private final PromotionService promotionService;

    public CreateReservationUseCase(ReservationService reservationService,
            PassengerRepository passengerRepository,
            PricingAndScheduleService pricingAndScheduleService,
            SameDayBookingPolicy sameDayBookingPolicy) {
        this(reservationService, passengerRepository, pricingAndScheduleService,
                sameDayBookingPolicy, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CreateReservationUseCase(ReservationService reservationService,
            PassengerRepository passengerRepository,
            PricingAndScheduleService pricingAndScheduleService,
            SameDayBookingPolicy sameDayBookingPolicy,
            PromotionService promotionService) {
        this.reservationService = reservationService;
        this.passengerRepository = passengerRepository;
        this.pricingAndScheduleService = pricingAndScheduleService;
        this.sameDayBookingPolicy = sameDayBookingPolicy;
        this.promotionService = promotionService;
    }

    @Value("${lunaris.trips.capacity:12}")
    private int tripCapacity = 12;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation execute(CreateReservationRequest request) {
        return execute(request, null);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation execute(CreateReservationRequest request, String paymentReceiptUrl) {
        validate(request);
        String departureSchedule = resolveSchedule(request.departureSchedule(), request.notes());
        sameDayBookingPolicy.validate(request.travelDate(), departureSchedule);

        Passenger passenger = resolvePassenger(request);

        // 🌟 Lógica del desplegable de asientos (Captura directa)
        int safePassengerCount = passengerCount(request.companionNames());
        long occupiedSeats = pricingAndScheduleService.countReservedSeats(
                request.travelDate(), departureSchedule);
        if (occupiedSeats + safePassengerCount > tripCapacity) {
            throw new SeatCapacityExceededException(
                    "No hay asientos suficientes para el turno seleccionado. Disponibles: "
                            + Math.max(0, tripCapacity - occupiedSeats) + ".");
        }
        // Las entradas públicas nunca pueden autoverificar un pago desde el payload.
        Boolean safePaymentVerified = false;
        boolean airportTrip = AirportTripDetector.isAirportTrip(
                effectivePickupLocality(request), effectiveDestination(request));
        String initialStatus = airportTrip ? "PENDING" : "PENDING_PAYMENT";

        // Centralizamos la cotización en el servicio de pricing para no duplicar reglas.
        TripType tripType = resolveTripType(request);
        boolean pairedTrip = tripType != TripType.ONE_WAY;
        BigDecimal computedAmount = airportTrip
                ? BigDecimal.ZERO
                : pricingAndScheduleService.calculateTripPrice(
                        effectivePickupLocality(request), pairedTrip, safePassengerCount);
        BigDecimal discountAmount = BigDecimal.ZERO;
        Promotion promotion = null;
        if (!airportTrip && request.promotionCode() != null && !request.promotionCode().isBlank()) {
            promotion = promotionService.requireAvailable(
                    request.promotionCode().trim(), passenger.getPhone());
            discountAmount = promotionService.calculateDiscount(
                    computedAmount, promotion.getDiscountPercentage());
            computedAmount = computedAmount.subtract(discountAmount).max(BigDecimal.ZERO);
        }

        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(request.travelDate())
                .pickupLocality(effectivePickupLocality(request))
                .pickupAddress(request.pickupAddress())
                .destination(effectiveDestination(request))
                .roundTrip(pairedTrip)
                .tripType(tripType)
                .returnDate(tripType == TripType.ROUND_TRIP ? request.returnDate() : null)
                .passengerCount(safePassengerCount)
                .companionNames(request.companionNames())
                .paymentVerified(safePaymentVerified)
                .requiresInvoice(true)
                .status(initialStatus)
                .source(request.source() != null ? request.source() : ReservationSource.WEB)
                .amount(computedAmount) // 🌟 Inyectamos el monto calculado automáticamente
                .discountAmount(discountAmount)
                .promotionCode(promotion != null ? promotion.getCode() : null)
                .promotionId(promotion != null ? promotion.getId() : null)
                .promotionDiscountPercentage(promotion != null ? promotion.getDiscountPercentage() : null)
                .notes(request.notes())
                .departureSchedule(departureSchedule)
                .paymentReceiptUrl(paymentReceiptUrl)
                .build();

        List<Reservation> savedReservations = reservationService.saveReservationFlow(reservation);
        
        return savedReservations.get(0);
    }

    private TripType resolveTripType(CreateReservationRequest request) {
        if (request.tripType() != null) {
            return request.tripType();
        }
        return Boolean.TRUE.equals(request.roundTrip())
                ? (request.returnDate() == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP)
                : TripType.ONE_WAY;
    }

    private int passengerCount(String companionNames) {
        if (companionNames == null || companionNames.isBlank()) {
            return 1;
        }
        return 1 + (int) java.util.Arrays.stream(companionNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .count();
    }

    private void validate(CreateReservationRequest request) {
        if (request == null || request.travelDate() == null) {
            throw new DomainValidationException("Pasajero y fecha de viaje son obligatorios.");
        }
        if (request.passengerId() == null
                && (request.fullName() == null || request.fullName().isBlank()
                || request.phone() == null || request.phone().isBlank())) {
            throw new DomainValidationException("Nombre y teléfono del pasajero son obligatorios.");
        }
        if (request.passengerId() == null && (request.cuilDni() == null
                || !request.cuilDni().replaceAll("[^0-9]", "").matches("[0-9]{7,11}"))) {
            throw new DomainValidationException("El DNI o CUIT debe contener entre 7 y 11 dígitos.");
        }
        if (effectivePickupLocality(request).length() < 3
                || effectiveDestination(request).length() < 3) {
            throw new DomainValidationException("Origen y destino deben tener al menos tres caracteres.");
        }
        if (effectivePickupLocality(request).equalsIgnoreCase(effectiveDestination(request))) {
            throw new DomainValidationException("Origen y destino deben ser diferentes.");
        }
        if (request.pickupAddress() == null || request.pickupAddress().isBlank()) {
            throw new DomainValidationException("La dirección de recogida es obligatoria.");
        }
        if (request.departureSchedule() == null || request.departureSchedule().isBlank()) {
            throw new DomainValidationException("El horario de salida es obligatorio.");
        }
        if (passengerCount(request.companionNames()) > 4) {
            throw new DomainValidationException("La cantidad de pasajeros debe estar entre 1 y 4.");
        }
    }

    private Passenger resolvePassenger(CreateReservationRequest request) {
        if (request.passengerId() != null) {
            return passengerRepository.findById(request.passengerId())
                    .map(passenger -> updatePassengerDetails(passenger, request))
                    .orElseGet(() -> resolvePassengerBySubmittedIdentity(request));
        }

        return resolvePassengerBySubmittedIdentity(request);
    }

    private Passenger resolvePassengerBySubmittedIdentity(CreateReservationRequest request) {
        if (request.phone() == null || request.phone().isBlank()) {
            throw new DomainValidationException(
                    "El pasajero indicado no existe y no se informó un teléfono para registrarlo.");
        }
        String phone = PhoneUtils.normalizeArgentinePhone(request.phone());
        if (request.fullName() == null || request.fullName().isBlank()) {
            return passengerRepository.findByPhone(phone)
                    .map(existing -> updatePassengerDetails(existing, request))
                    .orElseThrow(() -> new DomainValidationException(
                            "El pasajero indicado no existe y no se informó su nombre para registrarlo."));
        }
        NameParts name = splitFullName(request.fullName());
        return passengerRepository.findByPhone(phone)
                .map(existing -> updatePassengerDetails(repairIncompleteName(existing, name), request))
                .orElseGet(() -> passengerRepository.saveAndFlush(Passenger.builder()
                        .firstName(name.firstName())
                        .lastName(name.lastName())
                        .phone(phone)
                        .cuil(normalizeDocument(request.cuilDni()))
                        .address(normalizeNullable(request.pickupAddress()))
                        .locality(normalizeNullable(request.pickupLocality()))
                        .build()));
    }

    private Passenger updatePassengerDetails(Passenger passenger, CreateReservationRequest request) {
        boolean changed = false;
        String normalizedDocument = normalizeDocument(request.cuilDni());
        if (normalizedDocument != null && !normalizedDocument.equals(passenger.getCuil())) {
            passenger.setCuil(normalizedDocument);
            changed = true;
        }
        if (request.pickupAddress() != null && !request.pickupAddress().isBlank()
                && !request.pickupAddress().trim().equals(passenger.getAddress())) {
            passenger.setAddress(request.pickupAddress().trim());
            changed = true;
        }
        if (request.pickupLocality() != null && !request.pickupLocality().isBlank()
                && !request.pickupLocality().trim().equals(passenger.getLocality())) {
            passenger.setLocality(request.pickupLocality().trim());
            changed = true;
        }
        return changed ? passengerRepository.save(passenger) : passenger;
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeDocument(String value) {
        if (value == null || value.isBlank()) return null;
        return value.replaceAll("[^0-9]", "");
    }

    private Passenger repairIncompleteName(Passenger passenger, NameParts submittedName) {
        if (isMissingLastName(passenger.getLastName()) && !"Sin apellido".equals(submittedName.lastName())) {
            passenger.setFirstName(submittedName.firstName());
            passenger.setLastName(submittedName.lastName());
            return passengerRepository.save(passenger);
        }
        return passenger;
    }

    private boolean isMissingLastName(String lastName) {
        return lastName == null || lastName.isBlank() || "Sin apellido".equalsIgnoreCase(lastName.trim());
    }

    private NameParts splitFullName(String fullName) {
        String normalizedName = fullName.trim().replaceAll("\\s+", " ");
        int separator = normalizedName.indexOf(' ');
        return separator > 0
                ? new NameParts(normalizedName.substring(0, separator), normalizedName.substring(separator + 1))
                : new NameParts(normalizedName, "Sin apellido");
    }

    private record NameParts(String firstName, String lastName) {
    }

    private String effectivePickupLocality(CreateReservationRequest request) {
        return request.pickupLocality() == null || request.pickupLocality().isBlank()
                ? "Sin especificar" : request.pickupLocality().trim();
    }

    private String effectiveDestination(CreateReservationRequest request) {
        return request.destination() == null || request.destination().isBlank()
                ? "Sin especificar" : request.destination().trim();
    }

    private String resolveSchedule(String requestedSchedule, String notes) {
        if (requestedSchedule != null && !requestedSchedule.isBlank()) {
            return requestedSchedule.trim();
        }
        return notes != null && notes.contains("08:00") ? "08:00 AM" : "03:00 AM";
    }
}
