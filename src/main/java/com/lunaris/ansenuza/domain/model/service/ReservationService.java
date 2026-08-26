package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;

@Service
@Slf4j
public class ReservationService {

    public record CancellationResult(boolean paymentVerified, BigDecimal creditedAmount) {
        public CancellationResult {
            creditedAmount = creditedAmount == null ? BigDecimal.ZERO : creditedAmount;
        }
    }

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;
    private final PassengerRepository passengerRepository;
    private final OnboardPassengerUseCase onboardPassengerUseCase;
    private final CapacityLockRepository capacityLockRepository;

    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository,
            PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase) {
        this(reservationRepository, reservationEventRepository, passengerRepository,
                onboardPassengerUseCase, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository,
            PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase,
            CapacityLockRepository capacityLockRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationEventRepository = reservationEventRepository;
        this.passengerRepository = passengerRepository;
        this.onboardPassengerUseCase = onboardPassengerUseCase;
        this.capacityLockRepository = capacityLockRepository;
    }

    @Transactional
    public List<Reservation> saveReservationFlow(Reservation mainReservation) {
        return saveReservationFlow(mainReservation, null);
    }

    @Transactional
    public List<Reservation> saveReservationFlow(
            Reservation mainReservation, String returnDepartureSchedule) {
        List<Reservation> savedReservations = new ArrayList<>();

        lockAndValidateCapacity(mainReservation);
        if (Boolean.TRUE.equals(mainReservation.getRoundTrip())
                && mainReservation.getReturnDate() != null) {
            lockAndValidateCapacity(mainReservation.getReturnDate(), returnDepartureSchedule,
                    mainReservation.getDestination(), mainReservation.getTotalSeats());
        }

        normalizePassengerName(mainReservation.getPassenger());
        boolean requiresInvoice = Boolean.TRUE.equals(mainReservation.getRequiresInvoice());
        mainReservation.setRequiresInvoice(requiresInvoice);

        // 1. Normalizamos la ruta y sus prefijos para que todos los canales
        // (web, panel y bot) compartan el mismo formato de código.
        String originClean = cleanLocality(mainReservation.getPickupLocality());
        String destClean = cleanLocality(mainReservation.getDestination());
        String outboundDirection = routeDirection(originClean, destClean);
        mainReservation.setRouteDirection(outboundDirection);
        String routePrefix = localityPrefix(originClean) + "-" + localityPrefix(destClean);

        // 2. Obtenemos la secuencia estimada para el Nexo de Grupo unificado
        long currentCount = reservationRepository.countSequenceByRouteAndDate(originClean, destClean, mainReservation.getTravelDate());
        long nextSequence = currentCount + 1;

        // 3. 🛡️ BUCLE DEFENSIVO ANTI-COLISIÓN (Código base de grupo compartido)
        String codigoBase = String.format("%s-%03d", routePrefix, nextSequence);
        while (reservationRepository.existsByReservationCode(codigoBase)
                || reservationRepository.existsByReservationCode(codigoBase + "-IDA")
                || reservationRepository.existsByReservationCode(codigoBase + "-VUELTA")) {
            nextSequence++;
            codigoBase = String.format("%s-%03d", routePrefix, nextSequence);
        }

        // 💳 PASO CRÍTICO DE CUENTA CORRIENTE: Evaluar y aplicar saldo a favor del Pasajero Titular
        Passenger titular = lockPassenger(mainReservation.getPassenger());
        mainReservation.setPassenger(titular);
        BigDecimal saldoDisponible = titular.getCurrentBalance() != null ? titular.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal costoTotalFlujo = amountWithExtras(mainReservation);

        if (saldoDisponible.compareTo(BigDecimal.ZERO) > 0
                && costoTotalFlujo.compareTo(BigDecimal.ZERO) > 0) {
            if (saldoDisponible.compareTo(costoTotalFlujo) >= 0) {
                // El saldo cubre todo el viaje
                titular.setCurrentBalance(saldoDisponible.subtract(costoTotalFlujo));
                mainReservation.setAmount(BigDecimal.ZERO);
                mainReservation.setExtraAmount(BigDecimal.ZERO);
                mainReservation.setPaymentVerified(true);
                mainReservation.setStatus("CONFIRMED");
                mainReservation.setPaymentConfirmedAt(
                        com.lunaris.ansenuza.shared.ArgentinaTime.now());
            } else {
                // El saldo cubre una parte del viaje
                BigDecimal saldoRestante = costoTotalFlujo.subtract(saldoDisponible);
                BigDecimal extraAmount = mainReservation.getExtraAmount() == null
                        ? BigDecimal.ZERO : mainReservation.getExtraAmount();
                BigDecimal saldoRestantePositivo = saldoRestante.max(BigDecimal.ZERO);
                BigDecimal extraRestante = extraAmount.min(saldoRestantePositivo);
                mainReservation.setAmount(saldoRestantePositivo.subtract(extraRestante));
                mainReservation.setExtraAmount(extraRestante);
                titular.setCurrentBalance(BigDecimal.ZERO);
            }
            passengerRepository.saveAndFlush(titular);
        }

        // Dividimos el costo equitativamente por tramo usando el enum RoundingMode
        BigDecimal montoIda = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? mainReservation.getAmount().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : mainReservation.getAmount();
        BigDecimal montoVuelta = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? mainReservation.getAmount().subtract(montoIda)
                : BigDecimal.ZERO;
        BigDecimal descuentoIda = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? mainReservation.getDiscountAmount().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : mainReservation.getDiscountAmount();
        BigDecimal descuentoVuelta = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? mainReservation.getDiscountAmount().subtract(descuentoIda)
                : BigDecimal.ZERO;

        // --- PROCESAMIENTO TRAMO INICIAL ---
        mainReservation.setReservationCode(Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? codigoBase + "-" + outboundDirection : codigoBase);
        mainReservation.setBookingGroupCode(codigoBase);
        if (mainReservation.getStatus() == null) {
            mainReservation.setStatus(Boolean.TRUE.equals(mainReservation.getPaymentVerified()) ? "CONFIRMED" : "PENDING_PAYMENT");
        }
        if (Boolean.TRUE.equals(mainReservation.getPaymentVerified()) && mainReservation.getPaymentConfirmedAt() == null) {
            mainReservation.setPaymentConfirmedAt(
                    com.lunaris.ansenuza.shared.ArgentinaTime.now());
        }
        
        mainReservation.setAmount(montoIda);
        mainReservation.setDiscountAmount(descuentoIda);

        Reservation savedMain = reservationRepository.save(mainReservation);
        savedReservations.add(savedMain);

        ReservationEvent eventIda = ReservationEvent.builder()
                .reservationId(savedMain.getId())
                .eventType("RESERVATION_CREATED")
                .description("Tramo de " + outboundDirection
                        + " registrado bajo el grupo " + codigoBase)
                .triggeredBy("API_SYSTEM").build();
        reservationEventRepository.save(eventIda);

        // --- PROCESAMIENTO TRAMO INVERSO ---
        if (Boolean.TRUE.equals(mainReservation.getRoundTrip())) {
            Reservation returnReservation = new Reservation();
            returnReservation.setPassenger(mainReservation.getPassenger());
            returnReservation.setPickupLocality(mainReservation.getDestination()); 
            returnReservation.setDestination(mainReservation.getPickupLocality()); 

            if (mainReservation.getTripType() != TripType.OPEN_RETURN
                    && mainReservation.getReturnDate() != null) {
                returnReservation.setTravelDate(mainReservation.getReturnDate());
                returnReservation.setNotes("Vuelta vinculada al grupo " + codigoBase);
            } else {
                returnReservation.setTravelDate(null);
                returnReservation.setTravelStatus(Reservation.TravelStatus.OPEN_RETURN);
                returnReservation.setNotes("🛑 VUELTA ABIERTA - Pendiente confirmar fecha. Grupo " + codigoBase);
            }

            returnReservation.setAmount(montoVuelta);
            returnReservation.setDiscountAmount(descuentoVuelta);
            returnReservation.setPromotionCode(mainReservation.getPromotionCode());
            returnReservation.setPromotionId(mainReservation.getPromotionId());
            returnReservation.setPromotionDiscountPercentage(mainReservation.getPromotionDiscountPercentage());
            returnReservation.setPassengerCount(mainReservation.getPassengerCount());
            returnReservation.setCompanionNames(mainReservation.getCompanionNames());
            returnReservation.setPaymentVerified(mainReservation.getPaymentVerified());
            returnReservation.setRequiresInvoice(requiresInvoice);
            returnReservation.setStatus(mainReservation.getStatus());
            returnReservation.setSource(mainReservation.getSource());
            returnReservation.setRoundTrip(true);
            returnReservation.setTripType(mainReservation.getTripType());
            String returnDirection = oppositeDirection(outboundDirection);
            returnReservation.setRouteDirection(returnDirection);
            returnReservation.setReservationCode(codigoBase + "-" + returnDirection);
            returnReservation.setBookingGroupCode(codigoBase);
            returnReservation.setPaymentConfirmedAt(mainReservation.getPaymentConfirmedAt());
            returnReservation.setPaymentReceiptUrl(mainReservation.getPaymentReceiptUrl());
            returnReservation.setDepartureSchedule(returnDepartureSchedule);

            Reservation savedReturn = reservationRepository.save(returnReservation);
            savedReservations.add(savedReturn);

            ReservationEvent eventVuelta = ReservationEvent.builder()
                    .reservationId(savedReturn.getId())
                    .eventType("RESERVATION_CREATED")
                    .description("Tramo de " + returnDirection
                            + " registrado bajo el grupo " + codigoBase)
                    .triggeredBy("API_SYSTEM").build();
                    reservationEventRepository.save(eventVuelta);
        }

        return savedReservations;
    }

    private String routeDirection(String pickupLocality, String destination) {
        boolean fromCordoba = TripRouteCalculatorService.isCordoba(pickupLocality);
        boolean toCordoba = TripRouteCalculatorService.isCordoba(destination);
        if (fromCordoba == toCordoba) {
            // Los viajes especiales (por ejemplo, aeropuerto) pueden no pertenecer al
            // corredor regular. Conservamos el comportamiento legado IDA.
            return "IDA";
        }
        return fromCordoba ? "VUELTA" : "IDA";
    }

    private String oppositeDirection(String direction) {
        return "IDA".equals(direction) ? "VUELTA" : "IDA";
    }

    /** Revalida dentro de la transacción de escritura, cubriendo también el API web. */
    private void lockAndValidateCapacity(Reservation reservation) {
        if (capacityLockRepository == null || reservation.getTravelDate() == null) {
            return;
        }
        lockAndValidateCapacity(reservation.getTravelDate(), reservation.getDepartureSchedule(),
                reservation.getPickupLocality(), reservation.getTotalSeats());
    }

    private void lockAndValidateCapacity(LocalDate travelDate, String departureSchedule,
            String pickupLocality, int requestedSeats) {
        if (capacityLockRepository == null || travelDate == null) return;
        String schedule = departureSchedule == null
                || departureSchedule.isBlank()
                ? "03:00 AM" : departureSchedule.trim();
        String direction = TripRouteCalculatorService.isCordoba(pickupLocality)
                ? "RETURN" : "OUTBOUND";
        String key = travelDate + "|" + schedule.toLowerCase(java.util.Locale.ROOT)
                + "|" + direction;
        capacityLockRepository.ensureExists(key);
        if (capacityLockRepository.findForUpdate(key) == null) {
            throw new DomainValidationException("No se pudo bloquear la capacidad del turno.");
        }
        long occupied = reservationRepository.countReservedSeats(travelDate, schedule);
        if (occupied + requestedSeats > 12) {
            throw new com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException(
                    "No hay asientos suficientes para el turno seleccionado.");
        }
    }

    @Transactional
    public Reservation verifyPayment(UUID id) {
        Reservation initial = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
        String groupCode = paymentGroupCode(initial.getReservationCode());
        List<Reservation> group = groupCode == null
                ? List.of(reservationRepository.findByIdForUpdate(id)
                        .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id)))
                : reservationRepository.findReservationGroupForUpdate(groupCode);
        Reservation selected = group.stream().filter(item -> id.equals(item.getId())).findFirst()
                .orElseThrow(() -> new IllegalStateException("El grupo de reserva está incompleto."));
        LocalDateTime confirmedAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        group.forEach(reservation -> {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
            reservation.setPaymentConfirmedAt(confirmedAt);
        });
        reservationRepository.saveAllAndFlush(group);
        return selected;
    }

    private String paymentGroupCode(String reservationCode) {
        if (reservationCode == null || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) return null;
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }

    private String cleanLocality(String locality) {
        return locality == null ? "" : locality.trim();
    }

    private String localityPrefix(String locality) {
        if (locality == null || locality.isBlank()) {
            return "LUN";
        }
        String normalized = Normalizer.normalize(locality, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z]", "")
                .toUpperCase();
        if (normalized.isEmpty()) {
            return "LUN";
        }
        return normalized.substring(0, Math.min(3, normalized.length()));
    }

    private void normalizePassengerName(Passenger passenger) {
        if (passenger == null || passenger.getFirstName() == null) {
            return;
        }
        boolean missingLastName = passenger.getLastName() == null
                || passenger.getLastName().isBlank()
                || "Sin apellido".equalsIgnoreCase(passenger.getLastName().trim());
        String fullName = passenger.getFirstName().trim().replaceAll("\\s+", " ");
        int separator = fullName.lastIndexOf(' ');
        if (missingLastName && separator > 0) {
            passenger.setFirstName(fullName.substring(0, separator));
            passenger.setLastName(fullName.substring(separator + 1));
            passengerRepository.save(passenger);
        }
    }

    // 🗑️ BAJA LÓGICA ATÓMICA CON CASCADA INTELIGENTE (PROTEGE LA IDA SI SE CANCELA LA VUELTA)
    @Transactional
    public CancellationResult cancelReservation(UUID id, String triggeredBy) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
        if (!"CANCELLED".equals(reservation.getStatus())) {
                assertNotCompleted(reservation);
                assertCancellationAllowed(reservation, triggeredBy);

                if (isOutboundLeg(reservation) && isUsed(reservation)) {
                    List<Reservation> returnReservations = associatedReservations(reservation);
                    if (returnReservations.isEmpty()) {
                        throw new DomainValidationException(
                                "La ida ya fue utilizada y no posee una vuelta disponible para cancelar.");
                    }
                    BigDecimal credited = returnReservations.stream()
                            .map(returnReservation -> cancelReturnOnly(
                                    returnReservation, reservation.getPassenger(), triggeredBy))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CancellationResult(credited.signum() > 0, credited);
                }
                
                Passenger passenger = lockPassenger(reservation.getPassenger());
                reservation.setPassenger(passenger);
                BigDecimal saldoActual = passenger.getCurrentBalance() != null ? passenger.getCurrentBalance() : BigDecimal.ZERO;
                
                final BigDecimal[] totalReintegro = { BigDecimal.ZERO };

                // EVALUACIÓN ESTRICTA: Solo la bandera payment_verified == true autoriza el reembolso
                boolean pagoRealizado = Boolean.TRUE.equals(reservation.getPaymentVerified());
                if (!pagoRealizado) {
                    log.warn("[CANCEL] Cancelación SIN reembolso para reserva {}. Motivo: payment_verified es FALSE (Estado actual: {})",
                            reservation.getReservationCode(), reservation.getStatus());
                }

                // 1. Cancelamos la reserva actual seleccionada (Ida o Vuelta Abierta)
                reservation.setStatus("CANCELLED");
                reservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
                
                if (pagoRealizado && reservation.getAmount() != null && reservation.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    totalReintegro[0] = totalReintegro[0].add(refundableAmount(reservation));
                }
                reservationRepository.saveAndFlush(reservation);

                // Registro del evento
                ReservationEvent cancelEvent = ReservationEvent.builder()
                        .reservationId(reservation.getId())
                        .eventType(pagoRealizado ? "CANCELLED_CREDIT_ACCRUED"
                                : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                        .description("Reserva " + reservation.getReservationCode() + " dada de baja. Pago verificado anteriormente: " + pagoRealizado)
                        .triggeredBy(triggeredBy)
                        .build();
                reservationEventRepository.save(cancelEvent);

                // 2. 🔄 CASCADA UNIDIRECCIONAL: Si se da de baja la IDA, cancelamos la VUELTA. Si se borra la VUELTA, la IDA queda intacta.
                if (isOutboundLeg(reservation)) {
                    associatedReservations(reservation).forEach(returnRes -> {
                        if (!"CANCELLED".equals(returnRes.getStatus())) {
                            assertCancellationAllowed(returnRes, triggeredBy);
                            assertNotCompleted(returnRes);
                            boolean pagoVueltaRealizado = Boolean.TRUE.equals(returnRes.getPaymentVerified());
                            
                            returnRes.setStatus("CANCELLED");
                            returnRes.setTravelStatus(Reservation.TravelStatus.CANCELED);
                            
                            if (pagoVueltaRealizado && returnRes.getAmount() != null && returnRes.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                totalReintegro[0] = totalReintegro[0].add(refundableAmount(returnRes));
                            }
                            reservationRepository.saveAndFlush(returnRes);

                            ReservationEvent cancelReturnEvent = ReservationEvent.builder()
                                    .reservationId(returnRes.getId())
                                    .eventType(pagoVueltaRealizado ? "CANCELLED_CREDIT_ACCRUED"
                                            : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                                    .description("Cancelación automática de VUELTA por baja de IDA. Pago verificado: " + pagoVueltaRealizado)
                                    .triggeredBy(triggeredBy)
                                    .build();
                            reservationEventRepository.save(cancelReturnEvent);
                        }
                    });
                }

                // 3. 💳 ACREDITACIÓN CONTROLADA: Sumamos reintegros validados a la cuenta corriente (Corregido Typo)
                if (totalReintegro[0].compareTo(BigDecimal.ZERO) > 0) {
                    passenger.setCurrentBalance(saldoActual.add(totalReintegro[0]));
                    passengerRepository.saveAndFlush(passenger);
                    log.info("[CANCEL] Reembolso acreditado: {} a pasajero {}",
                            totalReintegro[0], passenger.getPhone());
                }
                return new CancellationResult(pagoRealizado, totalReintegro[0]);
        }
        return new CancellationResult(false, BigDecimal.ZERO);
    }

    @Transactional
    public void cancelOneUnusedReturnSeat(UUID id, String triggeredBy) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
        assertNotCompleted(reservation);
        assertCancellationAllowed(reservation, triggeredBy);
        if (!isReturnLeg(reservation)) {
            throw new DomainValidationException("La baja parcial solo se permite sobre una vuelta.");
        }
        int totalSeats = reservation.getTotalSeats();
        int returnedSeats = returnedSeats(reservation);
        if (totalSeats - returnedSeats <= 0) {
            throw new ReservationAlreadyCompletedException();
        }
        BigDecimal totalAmount = amountWithExtras(reservation);
        BigDecimal seatAmount = totalAmount.divide(
                BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);
        Passenger passenger = lockPassenger(reservation.getPassenger());
        reservation.setPassenger(passenger);
        reservation.setPassengerCount(totalSeats - 1);
        BigDecimal remainingAmount = totalAmount.subtract(seatAmount);
        BigDecimal existingExtra = reservation.getExtraAmount() == null
                ? BigDecimal.ZERO : reservation.getExtraAmount();
        BigDecimal remainingExtra = existingExtra.min(remainingAmount.max(BigDecimal.ZERO));
        reservation.setAmount(remainingAmount.subtract(remainingExtra));
        reservation.setExtraAmount(remainingExtra);
        if (returnedSeats > 0) {
            reservation.setTravelStatus(Reservation.TravelStatus.PARTIALLY_COMPLETED);
        }
        reservationRepository.saveAndFlush(reservation);
        boolean refundable = isRefundEligible(reservation);
        credit(passenger, refundable ? seatAmount : BigDecimal.ZERO);
        reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(id)
                .eventType(refundable ? "RETURN_SEAT_CANCELLED"
                        : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                .description(refundable
                        ? "Cancelación de una plaza de vuelta no utilizada."
                        : "Plaza cancelada sin saldo a favor porque el pago no estaba verificado.")
                .triggeredBy(triggeredBy)
                .build());
    }

    private BigDecimal cancelReturnOnly(
            Reservation returnReservation, Passenger passenger, String triggeredBy) {
        assertNotCompleted(returnReservation);
        assertCancellationAllowed(returnReservation, triggeredBy);
        boolean refundable = isRefundEligible(returnReservation);
        BigDecimal refund = refundable
                ? refundableAmount(returnReservation)
                : BigDecimal.ZERO;
        returnReservation.setStatus("CANCELLED");
        returnReservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
        reservationRepository.saveAndFlush(returnReservation);
        credit(passenger, refund);
        reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(returnReservation.getId())
                .eventType(refundable ? "RETURN_CANCELLED_AFTER_OUTBOUND"
                        : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                .description(refundable
                        ? "Solo se canceló y acreditó la porción no utilizada de la vuelta."
                        : "La vuelta se canceló sin saldo a favor porque el pago no estaba verificado.")
                .triggeredBy(triggeredBy)
                .build());
        if (refundable && refund.signum() > 0) {
            log.info("Saldo de {} acreditado al pasajero {}", refund, passenger.getPhone());
        } else if (!refundable) {
            log.info("Reserva {} cancelada SIN reembolso porque el pago no estaba verificado (payment_verified=false).",
                    returnReservation.getReservationCode());
        }
        return refund;
    }

    private List<Reservation> associatedReservations(Reservation parent) {
        List<Reservation> grouped = parent.getBookingGroupCode() == null
                || parent.getBookingGroupCode().isBlank()
                ? List.of()
                : reservationRepository.findByBookingGroupCodeForUpdate(parent.getBookingGroupCode());
        List<Reservation> associated = grouped.stream()
                .filter(candidate -> !java.util.Objects.equals(candidate.getId(), parent.getId()))
                .filter(candidate -> !"CANCELLED".equalsIgnoreCase(candidate.getStatus()))
                .toList();
        if (!associated.isEmpty()) {
            return associated;
        }
        String code = parent.getReservationCode();
        if (code == null || !code.endsWith("-IDA")) {
            return List.of();
        }
        return reservationRepository.findByReservationCode(code.replace("-IDA", "-VUELTA"))
                .filter(candidate -> !"CANCELLED".equalsIgnoreCase(candidate.getStatus()))
                .stream()
                .toList();
    }

    private BigDecimal refundableAmount(Reservation reservation) {
        if (!isReturnLeg(reservation)) {
            return isUsed(reservation) ? BigDecimal.ZERO : amountWithExtras(reservation);
        }
        int totalSeats = reservation.getTotalSeats();
        int unusedSeats = Math.max(0, totalSeats - returnedSeats(reservation));
        return amountWithExtras(reservation)
                .multiply(BigDecimal.valueOf(unusedSeats))
                .divide(BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);
    }

    private void credit(Passenger passenger, BigDecimal amount) {
        if (passenger == null || amount.signum() <= 0) {
            return;
        }
        BigDecimal balance = passenger.getCurrentBalance() == null
                ? BigDecimal.ZERO
                : passenger.getCurrentBalance();
        passenger.setCurrentBalance(balance.add(amount));
        passengerRepository.saveAndFlush(passenger);
    }

    private BigDecimal amount(Reservation reservation) {
        return reservation.getAmount() == null ? BigDecimal.ZERO : reservation.getAmount();
    }

    private int returnedSeats(Reservation reservation) {
        return reservation.getReturnedPassengerCount() == null
                ? 0
                : Math.max(0, reservation.getReturnedPassengerCount());
    }

    /**
     * Un comprobante cargado o un estado de pago no prueban que el dinero haya sido
     * validado. Sólo paymentVerified=true habilita una acreditación.
     */
    public boolean isRefundEligible(Reservation reservation) {
        return reservation != null && Boolean.TRUE.equals(reservation.getPaymentVerified());
    }

    private Passenger lockPassenger(Passenger passenger) {
        if (passenger == null || passenger.getId() == null) {
            return passenger;
        }
        // En una transacción real la consulta bloquea la fila; si el adaptador no
        // devuelve una entidad (por ejemplo, una reserva recién creada en el mismo
        // flujo), conservamos la instancia administrada para no perder el saldo.
        return passengerRepository.findByIdForUpdate(passenger.getId()).orElse(passenger);
    }

    private BigDecimal amountWithExtras(Reservation reservation) {
        return amount(reservation).add(reservation.getExtraAmount() == null
                ? BigDecimal.ZERO : reservation.getExtraAmount());
    }

    private boolean isUsed(Reservation reservation) {
        return reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || reservation.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.REALIZED
                || reservation.getTravelStatus() == Reservation.TravelStatus.COMPLETED
                || reservation.getTravelStatus() == Reservation.TravelStatus.REALIZED
                || reservation.getTravelDate() != null
                && reservation.getTravelDate().isBefore(
                        com.lunaris.ansenuza.shared.ArgentinaTime.today());
    }

    private boolean isOutboundLeg(Reservation reservation) {
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().endsWith("-IDA");
    }

    private boolean isReturnLeg(Reservation reservation) {
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().endsWith("-VUELTA");
    }

    private void assertNotCompleted(Reservation reservation) {
        if ("COMPLETED".equalsIgnoreCase(reservation.getStatus())
                || reservation.getTravelStatus() == Reservation.TravelStatus.COMPLETED
                || isReturnLeg(reservation)
                && returnedSeats(reservation) >= reservation.getTotalSeats()) {
            throw new ReservationAlreadyCompletedException();
        }
    }

    private void assertCancellationAllowed(Reservation reservation, String triggeredBy) {
        Reservation.TravelStatus travelStatus = reservation.getTravelStatus();
        if (travelStatus == Reservation.TravelStatus.ONBOARD
                || travelStatus == Reservation.TravelStatus.BOARDED
                || travelStatus == Reservation.TravelStatus.ONBOARDED
                || travelStatus == Reservation.TravelStatus.IN_PROGRESS
                || travelStatus == Reservation.TravelStatus.COMPLETED
                || travelStatus == Reservation.TravelStatus.REALIZED) {
            if ("BOT_WHATSAPP".equalsIgnoreCase(triggeredBy)) {
                throw new DomainValidationException(
                        "⚠️ Ya te encontrás a bordo o tu viaje ya finalizó. No es posible cancelar este servicio.");
            }
            throw new IllegalStateException(
                    "No se puede cancelar la reserva porque la ruta ya fue enviada al chofer o el viaje está en curso.");
        }
        if (travelStatus == Reservation.TravelStatus.ROUTE_SENT || reservation.getDriver() != null) {
            if ("BOT_WHATSAPP".equalsIgnoreCase(triggeredBy)) {
                throw new DomainValidationException(
                        "⚠️ Tu viaje ya fue asignado al chofer y la ruta está en curso. "
                                + "Para cancelar o modificar tu reserva, por favor comunicate con un operador.");
            }
            throw new IllegalStateException(
                    "No se puede cancelar la reserva porque la ruta ya fue enviada al chofer o el viaje está en curso.");
        }
    }

    @Transactional
    public Reservation updateReservation(UUID id, Reservation updatedData, String triggeredBy) {
        return reservationRepository.findById(id).map(reservation -> {
            assertNotCompleted(reservation);
            Reservation.TravelStatus requestedTravelStatus = updatedData.getTravelStatus();
            StringBuilder auditoriaDesc = new StringBuilder("Campos modificados: ");
            LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

            if (updatedData.getTravelDate() != null && !updatedData.getTravelDate().equals(reservation.getTravelDate())) {
                auditoriaDesc.append(String.format("[Fecha: %s -> %s] ", reservation.getTravelDate(), updatedData.getTravelDate()));
                reservation.setTravelDate(updatedData.getTravelDate());
                
                if (!updatedData.getTravelDate().equals(fechaCentinela) && reservation.getNotes() != null) {
                    reservation.setNotes(reservation.getNotes().replace("🛑 VUELTA ABIERTA - Pendiente confirmar fecha.", "🔄 Vuelta agendada:"));
                }
            }

            if (updatedData.getPickupAddress() != null) reservation.setPickupAddress(updatedData.getPickupAddress());
            if (updatedData.getPassengerCount() != null) reservation.setPassengerCount(updatedData.getPassengerCount());
            if (updatedData.getCompanionNames() != null) reservation.setCompanionNames(updatedData.getCompanionNames());
            if (updatedData.getAmount() != null) reservation.setAmount(updatedData.getAmount());
            
            if (updatedData.getPaymentVerified() != null) {
                reservation.setPaymentVerified(updatedData.getPaymentVerified());
                if (Boolean.TRUE.equals(updatedData.getPaymentVerified())) {
                    reservation.setStatus("CONFIRMED");
                    if (reservation.getPaymentConfirmedAt() == null) {
                        reservation.setPaymentConfirmedAt(
                                com.lunaris.ansenuza.shared.ArgentinaTime.now());
                    }
                }
            }
            if (updatedData.getStatus() != null) reservation.setStatus(updatedData.getStatus());
            if (updatedData.getNotes() != null) reservation.setNotes(updatedData.getNotes());
            if (requestedTravelStatus != null
                    && requestedTravelStatus != Reservation.TravelStatus.ONBOARD) {
                reservation.setTravelStatus(requestedTravelStatus);
            }

            Reservation saved;
            String paymentGroup = reservation.getBookingGroupCode() != null
                    && !reservation.getBookingGroupCode().isBlank()
                            ? reservation.getBookingGroupCode()
                            : paymentGroupCode(reservation.getReservationCode());
            if (updatedData.getPaymentVerified() != null && paymentGroup != null) {
                List<Reservation> lockedLinked = reservationRepository
                        .findByBookingGroupCodeForUpdate(paymentGroup);
                if (lockedLinked.isEmpty()) {
                    lockedLinked = reservationRepository.findReservationGroupForUpdate(paymentGroup);
                }
                final List<Reservation> linked = lockedLinked;
                linked.forEach(item -> {
                    item.setPaymentVerified(reservation.getPaymentVerified());
                    item.setStatus(reservation.getStatus());
                    item.setPaymentConfirmedAt(reservation.getPaymentConfirmedAt());
                });
                reservationRepository.saveAllAndFlush(linked);
                saved = linked.stream().filter(item -> id.equals(item.getId()))
                        .findFirst().orElse(reservation);
            } else {
                saved = reservationRepository.saveAndFlush(reservation);
            }

            ReservationEvent updateEvent = ReservationEvent.builder()
                    .reservationId(saved.getId())
                    .eventType("RESERVATION_UPDATED")
                    .description(auditoriaDesc.toString())
                    .triggeredBy(triggeredBy)
                    .build();
            reservationEventRepository.save(updateEvent);

            if (requestedTravelStatus == Reservation.TravelStatus.ONBOARD
                    && saved.getTravelStatus() != Reservation.TravelStatus.ONBOARD) {
                return onboardPassengerUseCase.updateTravelStatus(
                        saved.getId(), Reservation.TravelStatus.ONBOARD);
            }
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con ID: " + id));
    }

    /** Registra la tarifa acordada por un operador para un viaje inicialmente a cotizar. */
    @Transactional
    public Reservation updateAgreedAmount(UUID id, BigDecimal agreedAmount) {
        if (agreedAmount == null || agreedAmount.signum() <= 0) {
            throw new DomainValidationException("El importe acordado debe ser mayor a cero.");
        }
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
        assertNotCompleted(reservation);
        if (!AirportTripDetector.isAirportTrip(
                reservation.getPickupLocality(), reservation.getDestination())
                && (reservation.getAmount() == null || reservation.getAmount().signum() != 0)
                && !"PENDING".equalsIgnoreCase(reservation.getStatus())) {
            throw new DomainValidationException(
                    "La edición rápida sólo está habilitada para viajes especiales pendientes de cotización.");
        }
        reservation.setAmount(agreedAmount);
        if ("PENDING".equalsIgnoreCase(reservation.getStatus())) {
            reservation.setStatus("PENDING_PAYMENT");
        }
        return reservationRepository.saveAndFlush(reservation);
    }
}
