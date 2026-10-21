package com.lunaris.ansenuza.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.TripCategory;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssistedReservationDraftService {
    private final com.lunaris.ansenuza.domain.repository.AssistedReservationDraftRepository drafts;
    private final ConversationSessionRepository sessions;
    private final ChatMessageRepository messages;
    private final PassengerRepository passengers;
    private final AssistedChatService assisted;
    private final ObjectMapper json;

    public record Prefill(CreateReservationForm form, String sourceMessage, String detectedPhone) {}

    @Transactional(readOnly = true)
    public Prefill prefill(String phone) {
        var session = sessions.findByPhoneNumber(phone).orElse(null);
        CreateReservationForm form;
        try {
            var draft = drafts.findFirstByPhone(com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phone)).orElse(null);
            form = draft != null ? json.readValue(draft.getContent(), CreateReservationForm.class)
                    : initialForm(phone, session);
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("No se pudo recuperar el borrador de reserva.");
        }
        // Los borradores históricos también conservan ahora la identidad de la reserva editada.
        var group = assisted.editableGroup(phone);
        if (!group.isEmpty() && form.getReservationId() == null) form.setReservationId(group.getFirst().getId());
        String text = messages.findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDescIdDesc(phone)
                .map(message -> message.getMessageText()).orElse(null);
        var suggestions = FreeTextReservationExtractor.extract(text);
        if (blank(form.getPickupAddress())) form.setPickupAddress(suggestions.pickupAddress());
        if (blank(form.getNotes())) form.setNotes(suggestions.references());
        return new Prefill(form, text, suggestions.phone());
    }

    @Transactional
    public void save(String sourcePhone, CreateReservationForm form) {
        String draftPhone = com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(sourcePhone);
        String payload;
        try {
            // Guardar la selección no modifica el pago real de la reserva.
            if (form.getTripCategory() == null) form.setTripCategory(TripCategory.REGULAR);
            payload = json.writeValueAsString(form);
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("No se pudo guardar el borrador de reserva.");
        }
        if (payload.length() > 16000) throw new DomainValidationException("El borrador es demasiado extenso.");
        var draft = drafts.findFirstByPhone(draftPhone).orElseGet(
                com.lunaris.ansenuza.domain.model.AssistedReservationDraft::new);
        draft.setPhone(draftPhone);
        draft.setContent(payload);
        draft.setUpdatedAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        drafts.saveAndFlush(draft);
    }

    private CreateReservationForm initialForm(String phone, ConversationSession session) {
        var data = assisted.prefill(phone);
        var form = new CreateReservationForm();
        form.setPhone(phone);
        form.setPickupAddress(data.getPickupAddress());
        form.setPickupLocality(data.getPickupLocality());
        form.setDestination(data.getDestination());
        form.setTravelDate(data.getTravelDate());
        form.setReturnDate(data.getReturnDate());
        form.setRoundTrip(data.getRoundTrip());
        form.setPassengerCount(data.getPassengerCount());
        form.setDepartureSchedule(data.getScheduleBlock());
        form.setCompanionNames(data.getCompanionNames());
        form.setRequiresInvoice(data.getRequiresInvoice());
        form.setCuil(data.getCuil());
        passengers.findFirstByPhone(phone).ifPresentOrElse(passenger -> {
            form.setFirstName(passenger.getFirstName());
            form.setLastName(passenger.getLastName());
            form.setCuil(passenger.getCuil());
        }, () -> {
            if (!blank(data.getPassengerName())) {
                String[] name = data.getPassengerName().trim().split("\\s+", 2);
                form.setFirstName(name[0]);
                if (name.length > 1) form.setLastName(name[1]);
            }
        });
        if (session != null && session.getTripCategory() == TripCategory.SPECIAL) {
            form.setTripCategory(TripCategory.SPECIAL);
            form.setOriginCustom(session.getOriginCustom());
            form.setDestinationCustom(session.getDestinationCustom());
            form.setCustomPrice(session.getCustomPrice());
            form.setCustomPriceIsTotal(true);
        }
        var group = assisted.editableGroup(phone);
        if (!group.isEmpty()) {
            var trip = group.getFirst();
            form.setReservationId(trip.getId());
            form.setTripCategory(trip.getTripCategory());
            form.setOriginCustom(trip.getOriginCustom());
            form.setDestinationCustom(trip.getDestinationCustom());
            form.setCustomPrice(trip.getCustomPrice());
            form.setCustomPriceIsTotal(trip.isCustomPriceIsTotal());
            form.setPickupLocality(trip.getPickupLocality());
            form.setDestination(trip.getDestination());
            form.setPickupAddress(trip.getPickupAddress());
            form.setTravelDate(trip.getTravelDate());
            form.setRoundTrip(group.size() == 2);
            form.setReturnDate(group.size() == 2 ? group.getLast().getTravelDate() : null);
            form.setReturnDepartureSchedule(group.size() == 2 ? group.getLast().getDepartureSchedule() : null);
            form.setDepartureSchedule(trip.getDepartureSchedule());
            form.setPassengerCount(trip.getPassengerCount());
            form.setNotes(trip.getNotes());
            form.setCompanionNames(trip.getCompanionNames());
            form.setRouteDirection(trip.getRouteDirection());
            form.setPaymentVerified(trip.getPaymentVerified());
            form.setPaymentStatus(trip.getPaymentStatus());
            form.setPaymentReceiptUrl(trip.getPaymentReceiptUrl());
            form.setRequiresInvoice(trip.getRequiresInvoice());
            form.setAmount(com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.total(group)
                    .subtract(group.stream().map(r -> r.getExtraAmount() == null ? java.math.BigDecimal.ZERO : r.getExtraAmount())
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)));
            form.setExtraAmount(group.stream().map(r -> r.getExtraAmount() == null ? java.math.BigDecimal.ZERO : r.getExtraAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            form.setDiscountAmount(group.stream().map(r -> r.getDiscountAmount() == null ? java.math.BigDecimal.ZERO : r.getDiscountAmount())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        }
        return form;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
