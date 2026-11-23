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
    private final InvoiceRepository invoices;
    private final LocalityRepository localities;

    public record PanelState(Reservation selected, Reservation primary, List<Reservation> returns,
            int revision, boolean operationalOnly, boolean showDirectSchedule,
            BigDecimal total, BigDecimal paidAmount, List<String> localities) {}

    public PanelState getPanelState(UUID reservationId) {
        LockedGroup group = lockGroup(reservationId);
        synchronizeRevision(group);
        Reservation primary = primary(group);
        Reservation selected = leg(group.legs(), reservationId);
        var active = group.legs().stream().filter(ReservationUpdateCapacityService::active).toList();
        boolean restricted = operationalOnly(group.legs(), primary, selected);
        return new PanelState(selected, primary,
                active.stream().filter(r -> r != primary).toList(),
                group.state().getRevision(), restricted,
                !isOpenReturn(selected) || restricted && canScheduleOpen(selected, primary),
                ReservationUpdatePricingService.grossTotal(active),
                active.stream().map(Reservation::recognizedPayment).reduce(BigDecimal.ZERO, BigDecimal::add),
                selected.isSpecialTrip() ? List.of() : activeLocalities());
    }

    public UpdateReservationResult editFromPanel(UUID reservationId, PanelReservationEdit edit, String operator) {
        validate(edit);
        return execute(reservationId, edit.request(), operator, edit);
    }

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
        return execute(reservationId, request, triggeredBy, null);
    }

    private UpdateReservationResult execute(UUID reservationId, UpdateReservationRequest request, String triggeredBy,
            PanelReservationEdit edit) {
        validateRequest(request, triggeredBy);
        LockedGroup locked = lockGroup(reservationId);
        String operationKey = hash(locked.code() + "\n" + request.operationId());
        String requestHash = hash(edit == null ? serialize(request)
                : serialize(List.of(reservationId, edit)));
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
        Reservation primary = primary(locked);
        boolean restricted = edit != null && operationalOnly(before, primary, leg(before, reservationId));
        if (edit == null) {
            assertEditable(before);
            if (!ReservationUpdateCapacityService.active(primary)) {
                throw new DomainValidationException("No se puede editar un grupo con el tramo principal cancelado.");
            }
        }
        if (restricted && edit.changesCommercialFields()) {
            throw new DomainValidationException("Esta reserva admite únicamente correcciones de origen, fecha, hora, dirección y observaciones.");
        }
        List<Reservation> proposed = new ArrayList<>(before.stream().map(r -> r.toBuilder().build()).toList());
        Reservation proposedPrimary = leg(proposed, primary.getId());
        boolean scheduleChanged = edit != null && applyPanelEdit(edit, proposed, proposedPrimary, reservationId, restricted);
        ReturnAction action = request.returnChange() == null ? ReturnAction.KEEP : request.returnChange().action();
        if (edit != null && request.returnChange() != null && request.returnChange().date() != null
                && request.returnChange().date().isBefore(ArgentinaTime.today())) {
            throw new DomainValidationException("Indicá una fecha vigente para el regreso.");
        }
        Reservation added = planReturn(proposed, proposedPrimary, request.returnChange(), locked.code());
        if (!restricted) validateTopology(proposed, proposedPrimary);
        if (edit != null && !restricted && (action != ReturnAction.KEEP || scheduleChanged)) {
            validatePanelDates(proposed, proposedPrimary);
        }
        if (action != ReturnAction.KEEP) {
            synchronizeTripType(proposed, proposedPrimary);
        }
        if (edit == null && action != ReturnAction.KEEP) {
            capacity.validateReplacement(before, proposed, primary.getTravelDate());
        } else if (edit != null && capacityChanged(before, proposed)) {
            capacity.validateReplacement(before.stream().filter(this::occupiesCapacity).toList(),
                    proposed.stream().filter(this::occupiesCapacity).toList(),
                    primary.getTravelDate(), proposedPrimary.getTravelDate());
        }
        var price = pricing.apply(before, proposed, proposedPrimary, request.pricing(),
                action == ReturnAction.ADD || action == ReturnAction.REMOVE);
        if (edit != null && edit.paidAmount() != null) price = pricing.recordPayment(proposed, edit.paidAmount(), price);
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
                                    ? reservationId : request.returnChange().targetLegId())
                            + "; revisión " + result.revision() + "; total " + price.total()
                            + "; diferencia " + price.difference() + "; crédito " + price.credited()
                            + "; pendiente " + price.pending())
                    .triggeredBy(triggeredBy).build();
            entityManager.persist(event);
            if (event.getId() == null) throw new IllegalStateException("No se generó la identidad del evento.");
            audit.save(event);
            if (edit == null) events.publishEvent(new OperatorNotification("🚌 RESERVA ACTUALIZADA: " + locked.code()
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

    private Reservation primary(LockedGroup group) {
        return leg(group.legs(), group.state().getPrimaryReservationId());
    }

    private Reservation leg(List<Reservation> group, UUID id) {
        return group.stream().filter(r -> id.equals(r.getId())).findFirst()
                .orElseThrow(() -> new ReservationUpdateConflictException("El tramo ya no pertenece al grupo."));
    }

    private boolean operationalOnly(List<Reservation> group, Reservation primary, Reservation selected) {
        return !ReservationUpdateCapacityService.active(primary) || !ReservationUpdateCapacityService.active(selected)
                || group.stream().anyMatch(r -> r.isInvoiceIssued() || r.getInvoiceUrl() != null
                        || ReservationUpdateCapacityService.active(r) && usedOrDispatched(r))
                || invoices.existsByReservationIdIn(group.stream().map(Reservation::getId).toList());
    }

    private boolean usedOrDispatched(Reservation leg) {
        return leg.getDriver() != null || "COMPLETED".equalsIgnoreCase(leg.getStatus())
                || leg.getTravelStatus() != null && switch (leg.getTravelStatus()) {
                    case COMPLETED, REALIZED, PARTIALLY_COMPLETED, IN_PROGRESS, ONBOARD, BOARDED, ONBOARDED, ROUTE_SENT -> true;
                    default -> false;
                }
                || leg.getTravelDate() != null && leg.getTravelDate().isBefore(ArgentinaTime.today())
                || leg.getReturnedPassengerCount() != null && leg.getReturnedPassengerCount() > 0;
    }

    private boolean occupiesCapacity(Reservation leg) {
        return ReservationUpdateCapacityService.active(leg)
                && !"COMPLETED".equalsIgnoreCase(leg.getStatus())
                && leg.getTravelStatus() != Reservation.TravelStatus.COMPLETED
                && leg.getTravelStatus() != Reservation.TravelStatus.REALIZED;
    }

    private boolean capacityChanged(List<Reservation> before, List<Reservation> proposed) {
        return !capacitySignature(before).equals(capacitySignature(proposed));
    }

    private List<List<Object>> capacitySignature(List<Reservation> legs) {
        return legs.stream().filter(this::occupiesCapacity).filter(r -> !r.isSpecialTrip())
                .map(r -> canonicalValues(r.getId(), r.getTravelDate(), r.getDepartureSchedule(),
                        r.getPickupLocality(), r.getTotalSeats(), r.getTravelStatus(), r.isFlexibleReturnTime()))
                .toList();
    }

    private boolean applyPanelEdit(PanelReservationEdit edit, List<Reservation> group, Reservation primary,
            UUID selectedId, boolean restricted) {
        Reservation selected = leg(group, selectedId);
        boolean scheduleChanged = edit.travelDate() != null && !edit.travelDate().equals(selected.getTravelDate())
                || edit.departureSchedule() != null && !edit.departureSchedule().equals(normalizedSchedule(selected));
        if (scheduleChanged) {
            if (isOpenReturn(selected)) {
                if (!restricted || !canScheduleOpen(selected, primary)) {
                    throw new DomainValidationException("Usá la modalidad de regreso para programar una vuelta abierta.");
                }
                if (edit.travelDate() == null || edit.travelDate().isBefore(ArgentinaTime.today())) {
                    throw new DomainValidationException("Indicá una fecha vigente y una hora para el regreso.");
                }
                planReturn(group, primary, new ReturnChangeRequest(ReturnAction.UPDATE, selected.getId().toString(),
                        ReturnMode.SCHEDULED, edit.travelDate(), edit.departureSchedule(), null), primary.getBookingGroupCode());
                synchronizeTripType(group, primary);
            } else {
                applyScheduleCorrection(edit, group, selected, primary, restricted);
            }
        }
        if (edit.pickupAddress() != null) selected.setPickupAddress(blankToNull(edit.pickupAddress()));
        if (edit.notes() != null) selected.setNotes(blankToNull(edit.notes()));
        if (restricted) {
            applyOperationalOrigin(edit.origin(), selected);
        } else {
            applyCommercialFields(edit, group, primary, selected);
        }
        return scheduleChanged;
    }

    private void applyScheduleCorrection(PanelReservationEdit edit, List<Reservation> group,
            Reservation selected, Reservation primary, boolean restricted) {
        if (edit.travelDate() != null) {
            if (LocalDate.of(2099, 12, 31).equals(edit.travelDate())
                    || !restricted && edit.travelDate().isBefore(ArgentinaTime.today())) {
                throw new DomainValidationException("La fecha del viaje es inválida.");
            }
            if (!edit.travelDate().equals(selected.getTravelDate())) selected.setRouteSequence(null);
            selected.setTravelDate(edit.travelDate());
            if (selected != primary) {
                selected.setReturnDate(edit.travelDate());
                if (group.stream().filter(r -> r != primary)
                        .filter(ReservationUpdateCapacityService::active).count() == 1
                        && ReservationUpdateCapacityService.active(selected)) {
                    primary.setReturnDate(edit.travelDate());
                }
            }
        }
        if (edit.departureSchedule() != null && !edit.departureSchedule().equals(normalizedSchedule(selected))) {
            selected.setDepartureSchedule(edit.departureSchedule());
        }
    }

    private void applyOperationalOrigin(String input, Reservation selected) {
        if (input == null) return;
        String origin = input.strip();
        if (Objects.equals(origin, selected.getPickupLocality())) return;
        if (origin.isBlank() || origin.equalsIgnoreCase(selected.getDestination())
                || !selected.isSpecialTrip() && !activeLocalities().contains(origin)) {
            throw new DomainValidationException("Elegí un origen habilitado y distinto del destino.");
        }
        boolean fromCordoba = TripRouteCalculatorService.isCordoba(origin);
        if (fromCordoba != TripRouteCalculatorService.isCordoba(selected.getPickupLocality())) {
            selected.setRouteDirection(fromCordoba ? "VUELTA" : "IDA");
        }
        selected.setPickupLocality(origin);
        if (selected.isSpecialTrip()) selected.setOriginCustom(origin);
    }

    private void applyCommercialFields(PanelReservationEdit edit, List<Reservation> group, Reservation primary,
            Reservation selected) {
        boolean returningSelection = selected != primary;
        String requestedOrigin = returningSelection ? edit.destination() : edit.origin();
        String requestedDestination = returningSelection ? edit.origin() : edit.destination();
        String origin = requestedOrigin == null ? primary.getPickupLocality() : requestedOrigin.strip();
        String destination = requestedDestination == null ? primary.getDestination() : requestedDestination.strip();
        boolean routeChanged = !Objects.equals(origin, primary.getPickupLocality())
                || !Objects.equals(destination, primary.getDestination());
        if (routeChanged) {
            if (origin.isBlank() || destination.isBlank() || origin.equalsIgnoreCase(destination)
                    || !primary.isSpecialTrip() && !activeRoute(origin, destination)) {
                throw new DomainValidationException("Elegí un origen y un destino distintos y habilitados.");
            }
            boolean reverse = TripRouteCalculatorService.isCordoba(origin);
            for (Reservation leg : group) {
                if (!ReservationUpdateCapacityService.active(leg)) continue;
                boolean returning = leg != primary;
                leg.setPickupLocality(returning ? destination : origin);
                leg.setDestination(returning ? origin : destination);
                leg.setRouteDirection(reverse == returning ? "IDA" : "VUELTA");
                leg.setRouteSequence(null);
                if (leg.isSpecialTrip()) {
                    leg.setOriginCustom(leg.getPickupLocality());
                    leg.setDestinationCustom(leg.getDestination());
                }
            }
        }
        if (edit.seats() != null && edit.seats() != primary.getTotalSeats()) {
            if (edit.seats() < 1 || edit.seats() > 9) {
                throw new DomainValidationException("La cantidad de pasajeros debe estar entre 1 y 9.");
            }
            int previousSeats = primary.getTotalSeats();
            primary.setPassengerCount(edit.seats());
            var returns = group.stream().filter(r -> r != primary)
                    .filter(ReservationUpdateCapacityService::active).toList();
            if (returns.size() == 1 && returns.getFirst().getTotalSeats() == previousSeats) {
                returns.getFirst().setPassengerCount(edit.seats());
            }
        }
        if (edit.companionNames() != null) {
            String companions = blankToNull(edit.companionNames());
            if (!Objects.equals(companions, primary.getCompanionNames())) {
                group.stream().filter(ReservationUpdateCapacityService::active)
                        .forEach(leg -> leg.setCompanionNames(companions));
            }
        }
    }

    private boolean isOpenReturn(Reservation leg) {
        return leg.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                || LocalDate.of(2099, 12, 31).equals(leg.getTravelDate());
    }

    private boolean canScheduleOpen(Reservation selected, Reservation primary) {
        return selected != primary && ReservationUpdateCapacityService.active(selected) && !usedOrDispatched(selected);
    }

    private void validatePanelDates(List<Reservation> group, Reservation primary) {
        if (primary.getTravelDate() == null) return;
        String departure = normalizedSchedule(primary);
        for (Reservation leg : group) {
            if (leg == primary || !ReservationUpdateCapacityService.active(leg)) continue;
            LocalDate date = leg.getTravelDate();
            if (date == null || LocalDate.of(2099, 12, 31).equals(date)) date = leg.getReturnDate();
            if (date == null || LocalDate.of(2099, 12, 31).equals(date)) continue;
            String time = normalizedSchedule(leg);
            if (date.isBefore(primary.getTravelDate()) || date.equals(primary.getTravelDate())
                    && departure != null && time != null && time.compareTo(departure) < 0) {
                throw new DomainValidationException("El regreso no puede salir antes de la ida.");
            }
        }
    }

    private static String blankToNull(String value) {
        return value.isBlank() ? null : value.strip();
    }

    private boolean activeRoute(String origin, String destination) {
        var names = activeLocalities();
        return names.contains(origin) && names.contains(destination);
    }

    private List<String> activeLocalities() {
        var names = new LinkedHashSet<String>();
        names.add("Córdoba");
        localities.findAllWithActiveFare().forEach(locality -> names.add(locality.getName()));
        return List.copyOf(names);
    }

    public static String normalizedSchedule(Reservation leg) {
        if (leg.getDepartureSchedule() == null || leg.getDepartureSchedule().isBlank()) return null;
        try {
            return ReturnCapacityPolicy.normalizeSchedule(leg.getDepartureSchedule());
        } catch (java.time.format.DateTimeParseException invalidLegacySchedule) {
            return null;
        }
    }

    private void assertEditable(List<Reservation> group) {
        for (Reservation leg : group) {
            if (leg.isInvoiceIssued() || leg.getInvoiceUrl() != null) {
                throw new DomainValidationException("No se puede editar un grupo facturado.");
            }
            if (!ReservationUpdateCapacityService.active(leg)) continue;
            if (usedOrDispatched(leg)) {
                throw new DomainValidationException("El grupo tiene tramos asignados, utilizados o en curso.");
            }
            if (leg.getManualNotificationAttemptAt() != null && leg.getManualNotificationAttemptAt()
                    .isAfter(ArgentinaTime.now().minusMinutes(5))) {
                throw new ReservationUpdateConflictException("El grupo tiene un aviso en curso. Reintentá más tarde.");
            }
        }
    }

    private void validateRequest(UpdateReservationRequest request, String actor) {
        validate(request);
        if (request.operationId().length() > 128 || actor == null || actor.isBlank() || actor.length() > 100) {
            throw new DomainValidationException("El identificador de operación o el operador son inválidos.");
        }
    }

    private void validate(Object request) {
        if (request == null) throw new DomainValidationException("La solicitud es obligatoria.");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new DomainValidationException(violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().findFirst().orElseThrow());
    }

    private UpdateReservationResult result(LockedGroup group, String operationId, ReservationUpdatePricingService.Outcome price) {
        var legs = group.legs().stream().map(r -> new UpdateReservationResult.Leg(r.getId(), r.getReservationCode(),
                ReservationUpdateCapacityService.active(r), r.getTravelDate(), r.getReturnDate(), r.getDepartureSchedule(),
                r.isFlexibleReturnTime(), r.getReturnTimePreference(), r.getTravelStatus(), r.getTotalSeats(), r.getAmount(),
                r.getExtraAmount(), r.getUsedBalance(), r.recognizedPayment(), Boolean.TRUE.equals(r.getPaymentVerified()),
                r.getPickupLocality(), r.getDestination(), r.getPickupAddress(), r.getNotes(), r.getCompanionNames())).toList();
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
