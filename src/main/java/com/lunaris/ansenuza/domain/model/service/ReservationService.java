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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;
    private final PassengerRepository passengerRepository;
    private final OnboardPassengerUseCase onboardPassengerUseCase;

    @Transactional
    public List<Reservation> saveReservationFlow(Reservation mainReservation) {
        List<Reservation> savedReservations = new ArrayList<>();

        normalizePassengerName(mainReservation.getPassenger());
        boolean requiresInvoice = Boolean.TRUE.equals(mainReservation.getRequiresInvoice());
        mainReservation.setRequiresInvoice(requiresInvoice);

        // 1. Normalizamos la ruta y sus prefijos para que todos los canales
        // (web, panel y bot) compartan el mismo formato de código.
        String originClean = cleanLocality(mainReservation.getPickupLocality());
        String destClean = cleanLocality(mainReservation.getDestination());
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
        Passenger titular = mainReservation.getPassenger();
        BigDecimal saldoDisponible = titular.getCurrentBalance() != null ? titular.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal costoTotalFlujo = mainReservation.getAmount();

        if (saldoDisponible.compareTo(BigDecimal.ZERO) > 0) {
            if (saldoDisponible.compareTo(costoTotalFlujo) >= 0) {
                // El saldo cubre todo el viaje
                titular.setCurrentBalance(saldoDisponible.subtract(costoTotalFlujo));
                mainReservation.setAmount(BigDecimal.ZERO);
                mainReservation.setPaymentVerified(true);
                mainReservation.setStatus("CONFIRMED");
                mainReservation.setPaymentConfirmedAt(
                        com.lunaris.ansenuza.shared.ArgentinaTime.now());
            } else {
                // El saldo cubre una parte del viaje
                mainReservation.setAmount(costoTotalFlujo.subtract(saldoDisponible));
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

        // --- PROCESAMIENTO TRAMO: IDA ---
        mainReservation.setReservationCode(Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? codigoBase + "-IDA" : codigoBase);
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
                .description("Tramo de IDA registrado bajo el grupo " + codigoBase)
                .triggeredBy("API_SYSTEM").build();
        reservationEventRepository.save(eventIda);

        // --- PROCESAMIENTO TRAMO: VUELTA ---
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
            returnReservation.setReservationCode(codigoBase + "-VUELTA");
            returnReservation.setBookingGroupCode(codigoBase);
            returnReservation.setPaymentConfirmedAt(mainReservation.getPaymentConfirmedAt());
            returnReservation.setPaymentReceiptUrl(mainReservation.getPaymentReceiptUrl());

            Reservation savedReturn = reservationRepository.save(returnReservation);
            savedReservations.add(savedReturn);

            ReservationEvent eventVuelta = ReservationEvent.builder()
                    .reservationId(savedReturn.getId())
                    .eventType("RESERVATION_CREATED")
                    .description("Tramo de VUELTA registrado bajo el grupo " + codigoBase)
                    .triggeredBy("API_SYSTEM").build();
                    reservationEventRepository.save(eventVuelta);
        }

        return savedReservations;
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
    public void cancelReservation(UUID id, String triggeredBy) {
        reservationRepository.findByIdForUpdate(id).ifPresent(reservation -> {
            if (!"CANCELLED".equals(reservation.getStatus())) {
                assertNotCompleted(reservation);
                assertCancellationAllowed(reservation);

                if (isOutboundLeg(reservation) && isUsed(reservation)) {
                    List<Reservation> returnReservations = associatedReservations(reservation);
                    if (returnReservations.isEmpty()) {
                        throw new DomainValidationException(
                                "La ida ya fue utilizada y no posee una vuelta disponible para cancelar.");
                    }
                    returnReservations.forEach(returnReservation ->
                            cancelReturnOnly(returnReservation, reservation.getPassenger(), triggeredBy));
                    return;
                }
                
                Passenger passenger = reservation.getPassenger();
                BigDecimal saldoActual = passenger.getCurrentBalance() != null ? passenger.getCurrentBalance() : BigDecimal.ZERO;
                
                final BigDecimal[] totalReintegro = { BigDecimal.ZERO };

                // 🛡️ FILTRO DE SEGURIDAD: Solo computa dinero si el pago fue verificado o la reserva estaba confirmada
                boolean pagoRealizado = Boolean.TRUE.equals(reservation.getPaymentVerified()) || "CONFIRMED".equals(reservation.getStatus());

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
                        .eventType("RESERVATION_CANCELLED")
                        .description("Reserva " + reservation.getReservationCode() + " dada de baja. Pago verificado anteriormente: " + pagoRealizado)
                        .triggeredBy(triggeredBy)
                        .build();
                reservationEventRepository.save(cancelEvent);

                // 2. 🔄 CASCADA UNIDIRECCIONAL: Si se da de baja la IDA, cancelamos la VUELTA. Si se borra la VUELTA, la IDA queda intacta.
                if (isOutboundLeg(reservation)) {
                    associatedReservations(reservation).forEach(returnRes -> {
                        if (!"CANCELLED".equals(returnRes.getStatus())) {
                            assertCancellationAllowed(returnRes);
                            assertNotCompleted(returnRes);
                            boolean pagoVueltaRealizado = Boolean.TRUE.equals(returnRes.getPaymentVerified()) || "CONFIRMED".equals(returnRes.getStatus());
                            
                            returnRes.setStatus("CANCELLED");
                            returnRes.setTravelStatus(Reservation.TravelStatus.CANCELED);
                            
                            if (pagoVueltaRealizado && returnRes.getAmount() != null && returnRes.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                totalReintegro[0] = totalReintegro[0].add(refundableAmount(returnRes));
                            }
                            reservationRepository.saveAndFlush(returnRes);

                            ReservationEvent cancelReturnEvent = ReservationEvent.builder()
                                    .reservationId(returnRes.getId())
                                    .eventType("RESERVATION_CANCELLED")
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
                }
            }
        });
    }

    @Transactional
    public void cancelOneUnusedReturnSeat(UUID id, String triggeredBy) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
        assertNotCompleted(reservation);
        assertCancellationAllowed(reservation);
        if (!isReturnLeg(reservation)) {
            throw new DomainValidationException("La baja parcial solo se permite sobre una vuelta.");
        }
        int totalSeats = reservation.getTotalSeats();
        int returnedSeats = returnedSeats(reservation);
        if (totalSeats - returnedSeats <= 0) {
            throw new ReservationAlreadyCompletedException();
        }
        BigDecimal totalAmount = amount(reservation);
        BigDecimal seatAmount = totalAmount.divide(
                BigDecimal.valueOf(totalSeats), 2, RoundingMode.HALF_UP);
        reservation.setPassengerCount(totalSeats - 1);
        reservation.setAmount(totalAmount.subtract(seatAmount));
        if (returnedSeats > 0) {
            reservation.setTravelStatus(Reservation.TravelStatus.PARTIALLY_COMPLETED);
        }
        reservationRepository.saveAndFlush(reservation);
        credit(reservation.getPassenger(), isPaid(reservation) ? seatAmount : BigDecimal.ZERO);
        reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(id)
                .eventType("RETURN_SEAT_CANCELLED")
                .description("Cancelación de una plaza de vuelta no utilizada.")
                .triggeredBy(triggeredBy)
                .build());
    }

    private void cancelReturnOnly(Reservation returnReservation, Passenger passenger, String triggeredBy) {
        assertNotCompleted(returnReservation);
        assertCancellationAllowed(returnReservation);
        BigDecimal refund = isPaid(returnReservation)
                ? refundableAmount(returnReservation)
                : BigDecimal.ZERO;
        returnReservation.setStatus("CANCELLED");
        returnReservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
        reservationRepository.saveAndFlush(returnReservation);
        credit(passenger, refund);
        reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(returnReservation.getId())
                .eventType("RETURN_CANCELLED_AFTER_OUTBOUND")
                .description("Solo se canceló y acreditó la porción no utilizada de la vuelta.")
                .triggeredBy(triggeredBy)
                .build());
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
            return isUsed(reservation) ? BigDecimal.ZERO : amount(reservation);
        }
        int totalSeats = reservation.getTotalSeats();
        int unusedSeats = Math.max(0, totalSeats - returnedSeats(reservation));
        return amount(reservation)
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

    private boolean isPaid(Reservation reservation) {
        return Boolean.TRUE.equals(reservation.getPaymentVerified())
                || "CONFIRMED".equalsIgnoreCase(reservation.getStatus());
    }

    private boolean isUsed(Reservation reservation) {
        return reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || reservation.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.REALIZED
                || reservation.getTravelStatus() == Reservation.TravelStatus.COMPLETED
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

    private void assertCancellationAllowed(Reservation reservation) {
        Reservation.TravelStatus travelStatus = reservation.getTravelStatus();
        if (travelStatus == Reservation.TravelStatus.ROUTE_SENT
                || travelStatus == Reservation.TravelStatus.ONBOARD
                || travelStatus == Reservation.TravelStatus.IN_PROGRESS) {
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
            String paymentGroup = paymentGroupCode(reservation.getReservationCode());
            if (updatedData.getPaymentVerified() != null && paymentGroup != null) {
                List<Reservation> linked = reservationRepository.findReservationGroupForUpdate(paymentGroup);
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
}
