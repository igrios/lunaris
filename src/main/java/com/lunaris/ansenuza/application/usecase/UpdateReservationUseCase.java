package com.lunaris.ansenuza.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.application.dto.*;
import com.lunaris.ansenuza.domain.exception.*;
import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.model.service.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Edición del agregado: mutex de grupo, filas ordenadas, recursos ordenados y pasajero. */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateReservationUseCase {
    private final ReservationRepository reservations;
    private final ReservationUpdateStateRepository states;
    private final ReservationUpdateOperationRepository operations;
    private final ReservationEventRepository audit;
    private final ReservationUpdateCapacityService capacity;
    private final ReservationUpdatePricingService pricing;
    private final EntityManager entityManager;
    private final Validator validator;
    private final ObjectMapper json;
    private final ApplicationEventPublisher events;

    /** Inicializa/sincroniza la revisión incluso para escritores legados que no usan este caso de uso. */
    public UpdateReservationResult getState(UUID reservationId) {
        LockedGroup locked = lockGroup(reservationId);
        synchronizeRevision(locked);
        List<Reservation> active = locked.legs().stream().filter(ReservationUpdateCapacityService::active).toList();
        return result(locked, null, new ReservationUpdatePricingService.Outcome(
                ReservationUpdatePricingService.grossTotal(active), BookingInvoiceAmount.total(active),
                BigDecimal.ZERO, ReservationUpdatePricingService.pending(active), BigDecimal.ZERO));
    }

    public UpdateReservationResult execute(UUID reservationId, UpdateReservationRequest request) {
        return execute(reservationId, request, "ADMIN_PANEL");
    }

    public UpdateReservationResult execute(UUID reservationId, UpdateReservationRequest request, String triggeredBy) {
        validateRequest(request, triggeredBy);
        LockedGroup locked = lockGroup(reservationId);
        String operationKey = hash(locked.code() + "\n" + request.operationId());
        String requestHash = hash(serialize(request));
        var replay = operations.findById(operationKey);
        if (replay.isPresent()) {
            if (!replay.get().getRequestHash().equals(requestHash)) {
                throw new ReservationUpdateConflictException("El identificador de operación ya fue usado con otra solicitud.");
            }
            return deserialize(replay.get().getResultJson());
        }
        synchronizeRevision(locked);
        if (locked.state().getRevision() != request.expectedRevision()) {
            throw new ReservationUpdateConflictException("La reserva cambió. Recargá sus datos antes de editar.");
        }
        List<Reservation> before = locked.legs();
        var oldActive = before.stream().filter(ReservationUpdateCapacityService::active).toList();
        String beforeJson = serialize(result(locked, null, new ReservationUpdatePricingService.Outcome(
                ReservationUpdatePricingService.grossTotal(oldActive), BookingInvoiceAmount.total(oldActive),
                BigDecimal.ZERO, ReservationUpdatePricingService.pending(oldActive), BigDecimal.ZERO)));
        assertEditable(before);
        Reservation primary = before.stream().filter(r -> r.getId().equals(locked.state().getPrimaryReservationId()))
                .findFirst().orElseThrow(() -> new ReservationUpdateConflictException("El grupo perdió su tramo principal."));
        if (!ReservationUpdateCapacityService.active(primary)) {
            throw new DomainValidationException("No se puede editar un grupo con el tramo principal cancelado.");
        }
        List<Reservation> proposed = new ArrayList<>(before.stream().map(r -> r.toBuilder().build()).toList());
        Reservation proposedPrimary = proposed.stream().filter(r -> r.getId().equals(primary.getId())).findFirst().orElseThrow();
        ReturnAction action = request.returnChange() == null ? ReturnAction.KEEP : request.returnChange().action();
        Reservation added = planReturn(proposed, proposedPrimary, request.returnChange(), locked.code());
        validateTopology(proposed, proposedPrimary);
        if (action != ReturnAction.KEEP) synchronizeTripType(proposed, proposedPrimary);
        if (action != ReturnAction.KEEP) capacity.validateReplacement(before, proposed, primary.getTravelDate());
        var price = pricing.apply(before, proposed, proposedPrimary, request.pricing(),
                action == ReturnAction.ADD || action == ReturnAction.REMOVE);
        String oldFingerprint = fingerprint(before);
        String plannedFingerprint = fingerprint(proposed);
        boolean changed = !oldFingerprint.equals(plannedFingerprint);
        if (changed) {
            if (added != null) {
                // persist genera el UUID antes de save; el fallback respeta instalaciones legadas.
                added.setId(null);
                entityManager.persist(added);
                if (added.getId() == null) added.setId(UUID.randomUUID());
            }
            // Las copias se fusionan sólo después de validar todo el plan y los recursos.
            proposed = reservations.saveAllAndFlush(proposed);
            locked.legs().clear();
            locked.legs().addAll(proposed);
            locked.state().setRevision(Math.incrementExact(locked.state().getRevision()));
            locked.state().setFingerprint(fingerprint(proposed));
        }
        UpdateReservationResult result = result(locked, request.operationId(), price);
        ReservationUpdateOperation operation = new ReservationUpdateOperation();
        operation.setOperationKey(operationKey);
        operation.setGroupCode(locked.code());
        operation.setRequestHash(requestHash);
        operation.setBeforeJson(beforeJson);
        operation.setTriggeredBy(triggeredBy);
        operation.setResultJson(serialize(result));
        states.saveAndFlush(locked.state());
        operations.save(operation);
        if (changed) {
            ReservationEvent event = ReservationEvent.builder()
                    .reservationId(primary.getId()).eventType("RESERVATION_UPDATED")
                    .description("Operación " + request.operationId() + "; acción " + action
                            + "; tramo " + (added != null ? added.getId() : request.returnChange() == null
                                    ? "-" : request.returnChange().targetLegId())
                            + "; revisión " + result.revision() + "; total " + price.total()
                            + "; diferencia " + price.difference() + "; crédito " + price.credited()
                            + "; pendiente " + price.pending())
                    .triggeredBy(triggeredBy).build();
            entityManager.persist(event);
            if (event.getId() == null) throw new IllegalStateException("No se generó la identidad del evento.");
            audit.save(event);
            // OperatorNotificationService ya despacha este snapshot con AFTER_COMMIT.
            events.publishEvent(new OperatorNotification("🚌 RESERVA ACTUALIZADA: " + locked.code()
                    + "\nOperación: " + request.operationId() + "\nAcción: " + action
                    + "\nTotal: $" + price.total() + "\nPendiente: $" + price.pending()));
        }
        return result;
    }

    private record LockedGroup(String code, List<Reservation> legs, ReservationUpdateState state) {}

    private LockedGroup lockGroup(UUID id) {
        if (id == null) throw new ReservationNotFoundException();
        String code = reservations.findBillingGroupCodeById(id).orElse("UUID:" + id);
        capacity.lock("GROUP|" + code);
        // La proyección escalar bloquea sólo reservas, sin locks implícitos sobre relaciones EAGER.
        List<Reservation> group = new ArrayList<>(reservations.findUpdateGroupIdsForUpdate(code, id).stream()
                .map(legId -> entityManager.find(Reservation.class, legId)).toList());
        if (group.stream().noneMatch(r -> id.equals(r.getId()))) throw new ReservationNotFoundException();
        // También refresca entidades cargadas previamente por el llamador en esta transacción.
        group.forEach(entityManager::refresh);
        ReservationUpdateState state = states.findById(code).orElseGet(() -> {
            ReservationUpdateState created = new ReservationUpdateState();
            created.setGroupCode(code);
            Reservation root = group.stream().filter(r -> code.equals(r.getReservationCode())).findFirst()
                    .orElseGet(() -> inferPrimary(group));
            created.setPrimaryReservationId(root.getId());
            created.setFingerprint(fingerprint(group));
            return states.save(created);
        });
        return new LockedGroup(code, group, state);
    }

    private Reservation inferPrimary(List<Reservation> group) {
        var ordered = group.stream().sorted(Comparator.comparing(Reservation::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Reservation::getId)).toList();
        if (ordered.size() > 1 && Objects.equals(ordered.get(0).getCreatedAt(), ordered.get(1).getCreatedAt())) {
            throw new DomainValidationException("No se puede identificar la ida del grupo legado. Revisá su vinculación antes de editar.");
        }
        return ordered.getFirst();
    }

    private void synchronizeRevision(LockedGroup group) {
        String fingerprint = fingerprint(group.legs());
        if (!fingerprint.equals(group.state().getFingerprint())) {
            group.state().setRevision(Math.incrementExact(group.state().getRevision()));
            group.state().setFingerprint(fingerprint);
        }
    }

    private Reservation planReturn(List<Reservation> proposed, Reservation primary,
            ReturnChangeRequest change, String code) {
        if (change == null || change.action() == ReturnAction.KEEP) return null;
        Reservation target;
        if (change.action() == ReturnAction.ADD) {
            if (change.targetLegId() != null || proposed.stream().anyMatch(r -> !r.getId().equals(primary.getId())
                    && ReservationUpdateCapacityService.active(r))) {
                throw new DomainValidationException("El grupo ya tiene un regreso activo o se indicó un tramo para el alta.");
            }
            target = primary.toBuilder().id(null).createdAt(null).updatedAt(null).driver(null)
                    .reservationCode("R" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "-VUELTA")
                    .bookingGroupCode(code).pickupLocality(primary.getDestination()).destination(primary.getPickupLocality())
                    .originCustom(primary.getDestinationCustom()).destinationCustom(primary.getOriginCustom())
                    .pickupAddress(null).routeDirection("IDA".equals(primary.getRouteDirection()) ? "VUELTA" : "IDA")
                    .amount(BigDecimal.ZERO).extraAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                    .usedBalance(BigDecimal.ZERO).recognizedPaidAmount(BigDecimal.ZERO)
                    .paymentVerified(false).paymentStatus(PaymentStatus.PENDING).paymentReceiptUrl(null)
                    .paymentConfirmedAt(null).paymentExpiresAt(null).status("CONFIRMED")
                    .manualNotificationAttemptAt(null).manualNotificationPending(false).manualNotificationWaitingReply(false)
                    .manualConfirmationSentAt(null).manualInvoiceDeliveredUrl(null).routeSequence(null)
                    .returnedPassengerCount(0).returnAuditSentAt(null).waitingListEntryId(null).build();
            // Identidad provisional del plan: se reemplaza por el UUID JPA al persistir.
            target.setId(UUID.randomUUID());
            if (primary.getBookingGroupCode() == null || primary.getBookingGroupCode().isBlank()) {
                primary.setBookingGroupCode(code);
            }
            proposed.add(target);
        } else {
            UUID targetId;
            try { targetId = UUID.fromString(Objects.toString(change.targetLegId(), "")); }
            catch (IllegalArgumentException exception) { throw new DomainValidationException("Indicá el UUID del tramo de regreso."); }
            target = proposed.stream().filter(r -> targetId.equals(r.getId()) && !primary.getId().equals(r.getId()))
                    .findFirst().orElseThrow(() -> new DomainValidationException("El regreso no pertenece al grupo."));
            if (!ReservationUpdateCapacityService.active(target)) throw new DomainValidationException("El regreso ya está dado de baja.");
        }
        if (change.action() == ReturnAction.REMOVE) {
            target.setStatus("CANCELLED");
            target.setTravelStatus(Reservation.TravelStatus.CANCELED);
            target.setRouteSequence(null);
            return null;
        }
        validateReturn(change, primary);
        boolean scheduled = change.mode() == ReturnMode.SCHEDULED;
        target.setTravelDate(scheduled ? change.date() : null);
        target.setReturnDate(change.date());
        target.setDepartureSchedule(scheduled ? change.customTime() : null);
        target.setFlexibleReturnTime(change.mode() == ReturnMode.FLEXIBLE);
        target.setReturnTimePreference(change.timePreference() == null ? null : change.timePreference().trim());
        target.setTravelStatus(scheduled ? Reservation.TravelStatus.SCHEDULED : Reservation.TravelStatus.OPEN_RETURN);
        target.setRoundTrip(true);
        target.setTripType(scheduled ? TripType.ROUND_TRIP : TripType.OPEN_RETURN);
        target.validateFlexibleReturn();
        return change.action() == ReturnAction.ADD ? target : null;
    }

    private void validateReturn(ReturnChangeRequest change, Reservation primary) {
        if (change.mode() == null) throw new DomainValidationException("Indicá la modalidad de regreso.");
        if (change.date() != null && (LocalDate.of(2099, 12, 31).equals(change.date())
                || primary.getTravelDate() != null && change.date().isBefore(primary.getTravelDate()))) {
            throw new DomainValidationException("La fecha de regreso es inválida o anterior a la ida.");
        }
        if (change.mode() == ReturnMode.SCHEDULED) {
            if (change.date() == null || change.customTime() == null) {
                throw new DomainValidationException("El regreso programado requiere fecha y horario HH:mm.");
            }
            if (change.date().equals(primary.getTravelDate()) && primary.getDepartureSchedule() != null
                    && change.customTime().compareTo(ReturnCapacityPolicy.normalizeSchedule(primary.getDepartureSchedule())) < 0) {
                throw new DomainValidationException("El regreso no puede salir antes de la ida.");
            }
        } else if (change.customTime() != null) {
            throw new DomainValidationException("Una vuelta abierta o flexible no admite horario fijo.");
        }
        if (change.mode() == ReturnMode.OPEN && change.date() != null) {
            throw new DomainValidationException("Usá FLEXIBLE para indicar una fecha estimada sin horario.");
        }
        if (change.mode() == ReturnMode.FLEXIBLE && change.date() == null
                && (change.timePreference() == null || change.timePreference().isBlank())) {
            throw new DomainValidationException("La vuelta flexible requiere una fecha estimada o preferencia.");
        }
    }

    private void validateTopology(List<Reservation> group, Reservation primary) {
        var active = group.stream().filter(ReservationUpdateCapacityService::active).toList();
        if (active.stream().anyMatch(r -> !Objects.equals(r.getPassenger().getId(), primary.getPassenger().getId())
                || r.getPassengerCount() != null && (r.getPassengerCount() < 1 || r.getPassengerCount() > 9))) {
            throw new DomainValidationException("El grupo contiene pasajeros o cantidades incompatibles.");
        }
        var returns = active.stream().filter(r -> !primary.getId().equals(r.getId())).toList();
        if (returns.stream().anyMatch(r -> !Objects.equals(r.getPickupLocality(), primary.getDestination())
                || !Objects.equals(r.getDestination(), primary.getPickupLocality()))
                || returns.stream().mapToInt(Reservation::getTotalSeats).sum() > primary.getTotalSeats()) {
            throw new DomainValidationException("Los regresos del grupo no coinciden con la ruta o los asientos de la ida.");
        }
    }

    private void synchronizeTripType(List<Reservation> group, Reservation primary) {
        var returns = group.stream().filter(ReservationUpdateCapacityService::active)
                .filter(r -> !primary.getId().equals(r.getId())).toList();
        primary.setRoundTrip(!returns.isEmpty());
        primary.setTripType(returns.isEmpty() ? TripType.ONE_WAY
                : returns.stream().anyMatch(r -> r.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN)
                        ? TripType.OPEN_RETURN : TripType.ROUND_TRIP);
        primary.setReturnDate(returns.size() == 1 ? returns.getFirst().getReturnDate() : null);
        primary.setFlexibleReturnTime(returns.size() == 1 && returns.getFirst().isFlexibleReturnTime());
        primary.setReturnTimePreference(returns.size() == 1 ? returns.getFirst().getReturnTimePreference() : null);
    }

    private void assertEditable(List<Reservation> group) {
        for (Reservation leg : group) {
            if (leg.isInvoiceIssued() || leg.getInvoiceUrl() != null) {
                throw new DomainValidationException("No se puede editar un grupo facturado.");
            }
            if (!ReservationUpdateCapacityService.active(leg)) continue;
            if (leg.getDriver() != null || Set.of(Reservation.TravelStatus.COMPLETED, Reservation.TravelStatus.REALIZED,
                    Reservation.TravelStatus.IN_PROGRESS, Reservation.TravelStatus.ONBOARD, Reservation.TravelStatus.BOARDED,
                    Reservation.TravelStatus.ONBOARDED, Reservation.TravelStatus.ROUTE_SENT,
                    Reservation.TravelStatus.PARTIALLY_COMPLETED).contains(leg.getTravelStatus() == null
                            ? Reservation.TravelStatus.PENDING : leg.getTravelStatus())
                    || "COMPLETED".equalsIgnoreCase(leg.getStatus())
                    || leg.getTravelDate() != null && leg.getTravelDate().isBefore(ArgentinaTime.today())
                    || leg.getReturnedPassengerCount() != null && leg.getReturnedPassengerCount() > 0) {
                throw new DomainValidationException("El grupo tiene tramos asignados, utilizados o en curso.");
            }
            if (leg.getManualNotificationAttemptAt() != null && leg.getManualNotificationAttemptAt()
                    .isAfter(ArgentinaTime.now().minusMinutes(5))) {
                throw new ReservationUpdateConflictException("El grupo tiene un aviso en curso. Reintentá más tarde.");
            }
        }
    }

    private void validateRequest(UpdateReservationRequest request, String actor) {
        if (request == null) throw new DomainValidationException("La solicitud es obligatoria.");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new DomainValidationException(violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().findFirst().orElseThrow());
        if (request.operationId().length() > 128 || actor == null || actor.isBlank() || actor.length() > 100) {
            throw new DomainValidationException("El identificador de operación o el operador son inválidos.");
        }
    }

    private UpdateReservationResult result(LockedGroup group, String operationId, ReservationUpdatePricingService.Outcome price) {
        var legs = group.legs().stream().map(r -> new UpdateReservationResult.Leg(r.getId(), r.getReservationCode(),
                ReservationUpdateCapacityService.active(r), r.getTravelDate(), r.getReturnDate(), r.getDepartureSchedule(),
                r.isFlexibleReturnTime(), r.getReturnTimePreference(), r.getTravelStatus(), r.getTotalSeats(), r.getAmount(),
                r.getExtraAmount(), r.getUsedBalance(), r.recognizedPayment(), Boolean.TRUE.equals(r.getPaymentVerified()))).toList();
        return new UpdateReservationResult(group.code(), group.state().getRevision(), operationId, legs,
                price.total(), price.invoiceTotal(), price.difference(), price.pending(), price.credited());
    }

    private String fingerprint(List<Reservation> group) {
        return hash(serialize(group.stream().sorted(Comparator.comparing(Reservation::getId)).map(r -> canonicalValues(
                r.getId(), r.getPassenger().getId(), r.getDriver() == null ? null : r.getDriver().getId(),
                r.getReservationCode(), r.getBookingGroupCode(), r.getTravelDate(), r.getDepartureSchedule(),
                r.getPickupLocality(), r.getDestination(), r.getPickupAddress(), r.getPassengerCount(), r.getCompanionNames(),
                r.getRoundTrip(), r.getTripType(), r.getReturnDate(), r.isFlexibleReturnTime(), r.getReturnTimePreference(),
                r.getTravelStatus(), r.getStatus(), r.getReturnedPassengerCount(), r.getTripCategory(),
                r.getOriginCustom(), r.getDestinationCustom(), r.getCustomPrice(), r.isCustomPriceIsTotal(),
                r.getAmount(), r.isAmountIsGroupTotal(), r.getExtraAmount(), r.getDiscountAmount(), r.getUsedBalance(),
                r.getPromotionId(), r.getPromotionCode(), r.getPromotionDiscountPercentage(), r.getPaymentVerified(),
                r.getPaymentStatus(), r.getRecognizedPaidAmount(), r.getRequiresInvoice(), r.isInvoiceIssued(), r.getInvoiceUrl(),
                r.getPaymentReceiptUrl(), r.getNotes(), r.getRouteDirection(), r.getRouteSequence())).toList()));
    }

    private List<Object> canonicalValues(Object... values) {
        return Arrays.stream(values).map(value -> value instanceof BigDecimal amount
                ? amount.stripTrailingZeros() : value).toList();
    }

    private String serialize(Object value) {
        try { return json.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("No se pudo serializar la operación de reserva.", exception); }
    }

    private UpdateReservationResult deserialize(String value) {
        try { return json.readValue(value, UpdateReservationResult.class); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("No se pudo recuperar la operación de reserva.", exception); }
    }

    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
