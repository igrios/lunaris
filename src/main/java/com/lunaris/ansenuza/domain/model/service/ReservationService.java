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
        BigDecimal montoPorTramo = Boolean.TRUE.equals(mainReservation.getRoundTrip()) 
                ? mainReservation.getAmount().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : mainReservation.getAmount();
        BigDecimal descuentoPorTramo = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? mainReservation.getDiscountAmount().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : mainReservation.getDiscountAmount();

        // --- PROCESAMIENTO TRAMO: IDA ---
        mainReservation.setReservationCode(Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? codigoBase + "-IDA" : codigoBase);
        if (mainReservation.getStatus() == null) {
            mainReservation.setStatus(Boolean.TRUE.equals(mainReservation.getPaymentVerified()) ? "CONFIRMED" : "PENDING_PAYMENT");
        }
        if (Boolean.TRUE.equals(mainReservation.getPaymentVerified()) && mainReservation.getPaymentConfirmedAt() == null) {
            mainReservation.setPaymentConfirmedAt(
                    com.lunaris.ansenuza.shared.ArgentinaTime.now());
        }
        
        mainReservation.setAmount(montoPorTramo);
        mainReservation.setDiscountAmount(descuentoPorTramo);

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

            if (mainReservation.getReturnDate() != null) {
                returnReservation.setTravelDate(mainReservation.getReturnDate());
                returnReservation.setNotes("Vuelta vinculada al grupo " + codigoBase);
            } else {
                returnReservation.setTravelDate(LocalDate.of(2099, 12, 31));
                returnReservation.setNotes("🛑 VUELTA ABIERTA - Pendiente confirmar fecha. Grupo " + codigoBase);
            }

            returnReservation.setAmount(montoPorTramo);
            returnReservation.setDiscountAmount(descuentoPorTramo);
            returnReservation.setPromotionCode(mainReservation.getPromotionCode());
            returnReservation.setPromotionId(mainReservation.getPromotionId());
            returnReservation.setPromotionDiscountPercentage(mainReservation.getPromotionDiscountPercentage());
            returnReservation.setPassengerCount(mainReservation.getPassengerCount());
            returnReservation.setCompanionNames(mainReservation.getCompanionNames());
            returnReservation.setPaymentVerified(mainReservation.getPaymentVerified());
            returnReservation.setStatus(mainReservation.getStatus());
            returnReservation.setSource(mainReservation.getSource());
            returnReservation.setRoundTrip(true);
            returnReservation.setReservationCode(codigoBase + "-VUELTA");
            returnReservation.setPaymentConfirmedAt(mainReservation.getPaymentConfirmedAt());

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
        reservationRepository.findById(id).ifPresent(reservation -> {
            if (!"CANCELLED".equals(reservation.getStatus())) {
                
                Passenger passenger = reservation.getPassenger();
                BigDecimal saldoActual = passenger.getCurrentBalance() != null ? passenger.getCurrentBalance() : BigDecimal.ZERO;
                
                final BigDecimal[] totalReintegro = { BigDecimal.ZERO };

                // 🛡️ FILTRO DE SEGURIDAD: Solo computa dinero si el pago fue verificado o la reserva estaba confirmada
                boolean pagoRealizado = Boolean.TRUE.equals(reservation.getPaymentVerified()) || "CONFIRMED".equals(reservation.getStatus());

                // 1. Cancelamos la reserva actual seleccionada (Ida o Vuelta Abierta)
                reservation.setStatus("CANCELLED");
                reservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
                
                if (pagoRealizado && reservation.getAmount() != null && reservation.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    totalReintegro[0] = totalReintegro[0].add(reservation.getAmount());
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
                String codigoActual = reservation.getReservationCode();
                if (codigoActual != null && codigoActual.endsWith("-IDA")) {
                    String codigoVueltaBuscado = codigoActual.replace("-IDA", "-VUELTA");

                    reservationRepository.findByReservationCode(codigoVueltaBuscado).ifPresent(returnRes -> {
                        if (!"CANCELLED".equals(returnRes.getStatus())) {
                            
                            boolean pagoVueltaRealizado = Boolean.TRUE.equals(returnRes.getPaymentVerified()) || "CONFIRMED".equals(returnRes.getStatus());
                            
                            returnRes.setStatus("CANCELLED");
                            returnRes.setTravelStatus(Reservation.TravelStatus.CANCELED);
                            
                            if (pagoVueltaRealizado && returnRes.getAmount() != null && returnRes.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                                totalReintegro[0] = totalReintegro[0].add(returnRes.getAmount());
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
    public Reservation updateReservation(UUID id, Reservation updatedData, String triggeredBy) {
        return reservationRepository.findById(id).map(reservation -> {
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

            Reservation saved = reservationRepository.saveAndFlush(reservation);

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
