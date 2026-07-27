package com.lunaris.ansenuza.application.conversation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.usecase.ProcessPromotionCommandUseCase;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.OperationControlService; // 👈 NUEVO IMPORT
import com.lunaris.ansenuza.domain.model.service.ReservationCancellationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.extern.slf4j.Slf4j;

/**
 * Orquestador de la máquina de estados conversacional del bot de WhatsApp.
 *
 * <p>Centraliza las responsabilidades transversales (carga/creación de sesión, reflejo
 * en el chat en vivo, bypass de bot pausado y detección de saludos) y delega cada paso
 * concreto en el {@link ConversationStepHandler} correspondiente.
 */
@Service
@Slf4j
public class ConversationOrchestrator {

    private static final String BOARD_ID_PREFIX = "BOARD_ID_";
    private static final String BOARD_PREFIX = "BOARD_";
    private static final String ONBOARD_PREFIX = "ONBOARD_";
    private static final String ONBOARD_COLON_PREFIX = "ONBOARD:";

    private final Map<String, ConversationStepHandler> handlers;
    private final ConversationSessionRepository conversationSessionRepository;
    private final LiveChatPort liveChat;
    private final OperationControlService operationControlService; // 👈 NUEVO SERVICIO INYECTADO
    private final ReservationCancellationService reservationCancellationService;
    private final DriverRepository driverRepository;
    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final ProcessPromotionCommandUseCase processPromotionCommandUseCase;
    private final OnboardPassengerUseCase onboardPassengerUseCase;

    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository conversationSessionRepository,
            LiveChatPort liveChat,
            OperationControlService operationControlService,
            ReservationCancellationService reservationCancellationService,
            DriverRepository driverRepository,
            ReservationRepository reservationRepository,
            WhatsAppService whatsAppService,
            ProcessPromotionCommandUseCase processPromotionCommandUseCase,
            OnboardPassengerUseCase onboardPassengerUseCase) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ConversationStepHandler::step, Function.identity()));
        this.conversationSessionRepository = conversationSessionRepository;
        this.liveChat = liveChat;
        this.operationControlService = operationControlService;
        this.reservationCancellationService = reservationCancellationService;
        this.driverRepository = driverRepository;
        this.reservationRepository = reservationRepository;
        this.whatsAppService = whatsAppService;
        this.processPromotionCommandUseCase = processPromotionCommandUseCase;
        this.onboardPassengerUseCase = onboardPassengerUseCase;
    }

    public void process(IncomingMessage message) {
        String raw = message.body();
        if (raw == null) {
            return;
        }
        String phoneNumber = message.from();
        String rawTrimmed = raw.trim();
        String body = rawTrimmed.toLowerCase();

        Optional<Driver> routeDriver = findDriverByPhone(phoneNumber);
        if (routeDriver.isPresent() && isDriverRouteCommand(rawTrimmed)) {
            handleVerRuta(phoneNumber, routeDriver.get());
            return;
        }

        if (message.type() == IncomingMessage.MessageType.LOCATION) {
            Optional<Driver> activeDriver = findActiveDriverByPhone(phoneNumber);
            if (activeDriver.isPresent()) {
                Driver driver = activeDriver.get();
                driver.setCurrentLocationUrl(message.pickupAddress());
                driver.setLocationUpdatedAt(
                        com.lunaris.ansenuza.shared.ArgentinaTime.now());
                driverRepository.saveAndFlush(driver);
                whatsAppService.sendMessage(
                        phoneNumber, "✓ Ubicación del chofer actualizada.");
                return;
            }
        }

        if (processPromotionCommandUseCase.isPromotionCommand(rawTrimmed)) {
            liveChat.recordIncomingMessage(phoneNumber, rawTrimmed);
            processPromotionCommandUseCase.execute(phoneNumber, rawTrimmed);
            return;
        }

        Optional<UUID> boardingReservationId =
                extractBoardingReservationId(message, rawTrimmed);
        if (boardingReservationId.isPresent()) {
            log.info(
                    "[Driver Flow] Boarding action received. phone={}, reservationId={}, type={}",
                    phoneNumber, boardingReservationId.get(), message.type());
            handleBoardPassenger(phoneNumber, boardingReservationId.get());
            return;
        }

        // ⚖️ LOAD BALANCER: Si la sesión es nueva, le asignamos el operador con menos carga activa
        ConversationSession session = conversationSessionRepository
                .findByPhoneNumber(phoneNumber).orElseGet(() -> {
                    // Calculamos cuál operador está más libre mediante el balanceador
                    String operadorAsignado = operationControlService.getOperatorWithLeastLoad();
                    log.info("[Load Balancer] Asignando nuevo chat de {} al operador: {}", phoneNumber, operadorAsignado);
                    
                    ConversationSession newSession = ConversationSession.builder()
                            .phoneNumber(phoneNumber)
                            .currentStep("START")
                            .botPaused(false)
                            .assignedOperator(operadorAsignado) // 👈 ¡ACTIVO! Enlazado a la migración V35
                            .build();
                    return conversationSessionRepository.saveAndFlush(newSession);
                });

        // Reflejamos el mensaje del cliente en la sala de chat humana (persistencia + WebSocket)
        liveChat.recordIncomingMessage(phoneNumber, raw.trim());

        // Marcamos actividad en cada mensaje para que el scheduler pueda detectar sesiones abandonadas
        session.setLastInteraction(com.lunaris.ansenuza.shared.ArgentinaTime.now());

        // 🌙 CONTROL DE JORNADA: Si la jornada humana terminó y el bot había quedado pausado,
        // lo despausamos automáticamente para que el cliente no quede hablando solo en la nada.
        if (session.isBotPaused() && !operationControlService.isHumanActionEnabled()) {
            log.info("[Jornada Finalizada] Forzando despause de bot para {} por cierre de atención humana.", phoneNumber);
            session.setBotPaused(false);
        }

        conversationSessionRepository.saveAndFlush(session);

        if (reservationCancellationService.isReturnDecision(rawTrimmed)) {
            reservationCancellationService.processReturnDecision(phoneNumber, rawTrimmed);
            log.info("[Bot] Decisión de vuelta '{}' procesada para {}.", rawTrimmed, phoneNumber);
            return;
        }

        if (session.isBotPaused()) {
            log.info("[Bypass] Bot muteado para {}. Derivando mensaje a la sala de chat humana.",
                    phoneNumber);
            return;
        }

        boolean isGreeting = "hola".equals(body) || "buen dia".equals(body)
                || "buenas".equals(body) || "menu".equals(body) || "reinicio".equals(body);

        String currentStep = session.getCurrentStep();
        String effectiveStep =
                (currentStep == null || "START".equals(currentStep) || isGreeting) ? "START"
                        : currentStep;

        ConversationStepHandler handler = handlers.get(effectiveStep);
        if (handler == null) {
            log.warn("[Bot] No hay handler registrado para el paso '{}' (teléfono {}).",
                    effectiveStep, phoneNumber);
            return;
        }

        handler.handle(session, message);
    }

    private String normalizeWhatsAppNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        return clean.startsWith("549") ? "54" + clean.substring(3) : clean;
    }

    private String truncateSafe(String text, int maxLength) {
        if (text == null) return "";
        text = text.trim();
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private boolean isDriverRouteCommand(String payload) {
        String normalized = Normalizer.normalize(payload, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return "ver ruta".equals(normalized)
                || "mis viajes".equals(normalized)
                || "agenda".equals(normalized)
                || "mi agenda".equals(normalized)
                || "ver agenda".equals(normalized);
    }

    private Optional<Driver> findDriverByPhone(String phone) {
        String normalizedPhone = normalizeWhatsAppNumber(phone);
        return driverRepository.findFirstByPhone(normalizedPhone)
                .or(() -> driverRepository.findAll().stream()
                        .filter(driver -> normalizeWhatsAppNumber(driver.getPhone())
                                .equals(normalizedPhone))
                        .findFirst());
    }

    private void handleVerRuta(String phone, Driver driver) {
        List<Reservation> reservations = reservationRepository
                .findAllAssignedByDriverId(driver.getId());

        if (reservations.isEmpty()) {
            whatsAppService.sendMessage(phone, "ℹ️ No tenés viajes asignados.");
            return;
        }

        reservations.sort(Comparator
                .comparing(Reservation::getTravelDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getRouteSequence,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getDepartureSchedule,
                        Comparator.nullsLast(Comparator.naturalOrder())));

        StringBuilder routeMessage = new StringBuilder("🗺️ Hoja de ruta\n\n");
        for (int index = 0; index < reservations.size(); index++) {
            Reservation reservation = reservations.get(index);
            String passengerName = reservation.getPassenger().getFirstName() + " "
                    + reservation.getPassenger().getLastName();
            String schedule = reservation.getDepartureSchedule() == null
                    || reservation.getDepartureSchedule().isBlank()
                            ? "Horario a confirmar"
                            : reservation.getDepartureSchedule().trim();

            routeMessage.append(index + 1).append(". 👤 ").append(passengerName).append(" (")
                    .append(schedule).append(" hs)\n")
                    .append("📞 ").append(reservation.getPassenger().getPhone())
                    .append(" | 📍 ").append(pickupLocation(reservation))
                    .append(" ➔ ").append(reservation.getDestination()).append("\n\n");
        }
        routeMessage.append("📍 Mapa: ").append(buildGoogleMapsUrl(reservations));
        whatsAppService.sendMessage(phone, routeMessage.toString());

        // Segundo mensaje: lista de onboarding para confirmar abordajes.
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Reservation res : reservations) {
            String passengerName = res.getPassenger().getFirstName() + " " + res.getPassenger().getLastName();
            String scheduleShort = res.getDepartureSchedule() == null || res.getDepartureSchedule().isBlank()
                    ? "S/H"
                    : res.getDepartureSchedule().trim();
            rows.add(java.util.Map.of(
                "id", ONBOARD_PREFIX + res.getId(),
                "title", truncateSafe(scheduleShort + " - " + passengerName, 24),
                "description", truncateSafe("Confirmar a bordo", 72)
            ));
        }

        java.util.Map<String, Object> section = java.util.Map.of(
            "title", "Onboarding",
            "rows", rows
        );

        whatsAppService.sendInteractiveList(
            phone,
            "Onboarding",
            "Seleccioná un pasajero para confirmar que está a bordo.",
            "Ver Pasajeros",
            List.of(section)
        );
    }

    private String pickupLocation(Reservation reservation) {
        if (reservation.getPickupAddress() == null || reservation.getPickupAddress().isBlank()) {
            return reservation.getPickupLocality();
        }
        if (reservation.getPickupAddress().startsWith("https://maps.google.com/?q=")) {
            return reservation.getPickupAddress();
        }
        return reservation.getPickupAddress() + ", " + reservation.getPickupLocality();
    }

    private String buildGoogleMapsUrl(List<Reservation> reservations) {
        Reservation firstReservation = reservations.get(0);
        Reservation lastReservation = reservations.get(reservations.size() - 1);
        List<String> waypoints = reservations.stream()
                .skip(1)
                .map(this::pickupLocation)
                .map(GoogleMapsParameterFormatter::normalize)
                .limit(9)
                .toList();

        return "https://www.google.com/maps/dir/?api=1&origin="
                + encodeMapParameter(pickupLocation(firstReservation))
                + "&destination=" + encodeMapParameter(lastReservation.getDestination())
                + "&waypoints=" + encodeMapParameter(String.join("|", waypoints))
                + "&travelmode=driving";
    }

    private String encodeMapParameter(String value) {
        return GoogleMapsParameterFormatter.encode(value);
    }

    private Optional<UUID> extractBoardingReservationId(
            IncomingMessage message, String rawPayload) {
        String candidate = null;
        if (rawPayload.regionMatches(
                true, 0, BOARD_ID_PREFIX, 0, BOARD_ID_PREFIX.length())) {
            candidate = rawPayload.substring(BOARD_ID_PREFIX.length());
        } else if (rawPayload.regionMatches(
                true, 0, ONBOARD_PREFIX, 0, ONBOARD_PREFIX.length())) {
            candidate = rawPayload.substring(ONBOARD_PREFIX.length());
        } else if (rawPayload.regionMatches(
                true, 0, ONBOARD_COLON_PREFIX, 0, ONBOARD_COLON_PREFIX.length())) {
            candidate = rawPayload.substring(ONBOARD_COLON_PREFIX.length());
        } else if (rawPayload.regionMatches(
                true, 0, BOARD_PREFIX, 0, BOARD_PREFIX.length())) {
            candidate = rawPayload.substring(BOARD_PREFIX.length());
        } else if (message.type() == IncomingMessage.MessageType.INTERACTIVE
                && isActiveDriverPhone(message.from())
                && isUuid(rawPayload)) {
            candidate = rawPayload.trim();
        }
        if (candidate == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(candidate.trim()));
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[Driver Flow] Invalid boarding action payload. phone={}, payload={}",
                    message.from(), rawPayload);
            return Optional.empty();
        }
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isActiveDriverPhone(String phone) {
        return findActiveDriverByPhone(phone).isPresent();
    }

    private Optional<Driver> findActiveDriverByPhone(String phone) {
        String normalizedPhone = normalizeWhatsAppNumber(phone);
        Optional<Driver> exactMatch = driverRepository.findFirstByPhone(normalizedPhone);
        if (exactMatch.filter(Driver::isActive).isPresent()) {
            return exactMatch;
        }
        return driverRepository.findByActiveTrue().stream()
                .filter(driver -> normalizeWhatsAppNumber(driver.getPhone())
                        .equals(normalizedPhone))
                .findFirst();
    }

    private void handleBoardPassenger(String phone, UUID reservationId) {
        try {
            Reservation reservation = onboardPassengerUseCase.execute(reservationId, phone);

            String passengerName = reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName();
            whatsAppService.sendMessage(phone, "✓ Pasajero [" + passengerName + "] marcado a bordo.");
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[Driver Flow] Boarding reservation not found. phone={}, reservationId={}",
                    phone, reservationId);
            whatsAppService.sendMessage(phone, "❌ No se encontró la reserva especificada.");
        } catch (Exception e) {
            log.error("Error al marcar pasajero a bordo: ", e);
            whatsAppService.sendMessage(phone, "❌ Ocurrió un error al procesar el abordaje del pasajero.");
        }
    }

}
