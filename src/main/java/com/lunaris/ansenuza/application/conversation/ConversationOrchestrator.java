package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.LiveChatPort;
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

    private final Map<String, ConversationStepHandler> handlers;
    private final ConversationSessionRepository conversationSessionRepository;
    private final LiveChatPort liveChat;
    private final OperationControlService operationControlService; // 👈 NUEVO SERVICIO INYECTADO
    private final ReservationCancellationService reservationCancellationService;
    private final DriverRepository driverRepository;
    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;

    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository conversationSessionRepository,
            LiveChatPort liveChat,
            OperationControlService operationControlService,
            ReservationCancellationService reservationCancellationService,
            DriverRepository driverRepository,
            ReservationRepository reservationRepository,
            WhatsAppService whatsAppService) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ConversationStepHandler::step, Function.identity()));
        this.conversationSessionRepository = conversationSessionRepository;
        this.liveChat = liveChat;
        this.operationControlService = operationControlService;
        this.reservationCancellationService = reservationCancellationService;
        this.driverRepository = driverRepository;
        this.reservationRepository = reservationRepository;
        this.whatsAppService = whatsAppService;
    }

    public void process(IncomingMessage message) {
        String raw = message.body();
        if (raw == null) {
            return;
        }
        String phoneNumber = message.from();
        String rawTrimmed = raw.trim();
        String body = rawTrimmed.toLowerCase();

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
        session.setLastInteraction(LocalDateTime.now());

        // 🌙 CONTROL DE JORNADA: Si la jornada humana terminó y el bot había quedado pausado,
        // lo despausamos automáticamente para que el cliente no quede hablando solo en la nada.
        if (session.isBotPaused() && !operationControlService.isHumanActionEnabled()) {
            log.info("[Jornada Finalizada] Forzando despause de bot para {} por cierre de atención humana.", phoneNumber);
            session.setBotPaused(false);
        }

        conversationSessionRepository.saveAndFlush(session);

        // 🚖 ACCIONES DEL CHOFER (Bypass del bot de pasajeros)
        if ("Ver Ruta".equalsIgnoreCase(rawTrimmed)) {
            handleVerRuta(phoneNumber);
            return;
        }

        if (rawTrimmed.startsWith("BOARD_ID_")) {
            handleBoardPassenger(phoneNumber, rawTrimmed);
            return;
        }

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

    private void handleVerRuta(String phone) {
        String normalizedPhone = normalizeWhatsAppNumber(phone);
        java.util.Optional<Driver> driverOpt = driverRepository.findByPhone(normalizedPhone);
        if (driverOpt.isEmpty()) {
            List<Driver> allDrivers = driverRepository.findAll();
            driverOpt = allDrivers.stream()
                    .filter(d -> normalizeWhatsAppNumber(d.getPhone()).equals(normalizedPhone))
                    .findFirst();
        }

        if (driverOpt.isEmpty()) {
            log.warn("[Driver Flow] No se encontró chofer con el celular: {}", phone);
            whatsAppService.sendMessage(phone, "Lo siento, no estás registrado como chofer en nuestro sistema.");
            return;
        }

        Driver driver = driverOpt.get();
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("America/Argentina/Cordoba"));
        List<Reservation> reservations = reservationRepository.findByDriverIdAndTravelDate(driver.getId(), today);

        if (reservations.isEmpty()) {
            whatsAppService.sendMessage(phone, "Hola " + driver.getFullName() + ", no tienes pasajeros asignados para el día de hoy.");
            return;
        }

        // Send interactive list of passengers
        List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (Reservation res : reservations) {
            String passengerName = res.getPassenger().getFirstName() + " " + res.getPassenger().getLastName();
            String description = res.getPickupLocality() + " -> " + res.getDestination();
            if (res.getTravelStatus() == Reservation.TravelStatus.BOARDED) {
                passengerName = "✅ " + passengerName;
                description = "[A bordo] " + description;
            } else {
                description = "[" + res.getPassengerCount() + " as.] " + description;
            }
            rows.add(java.util.Map.of(
                "id", "BOARD_ID_" + res.getId(),
                "title", passengerName,
                "description", description
            ));
        }

        java.util.Map<String, Object> section = java.util.Map.of(
            "title", "Pasajeros de Hoy",
            "rows", rows
        );

        whatsAppService.sendInteractiveList(
            phone,
            "Hoja de Ruta - Lunaris",
            "Hola " + driver.getFullName() + ". Selecciona un pasajero de la lista para marcarlo a bordo una vez que suba a la combi:",
            "Ver Pasajeros",
            List.of(section)
        );
    }

    private void handleBoardPassenger(String phone, String rawTrimmed) {
        String resIdStr = rawTrimmed.substring("BOARD_ID_".length());
        try {
            java.util.UUID reservationId = java.util.UUID.fromString(resIdStr);
            java.util.Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
            if (resOpt.isEmpty()) {
                whatsAppService.sendMessage(phone, "❌ No se encontró la reserva especificada.");
                return;
            }

            Reservation reservation = resOpt.get();
            reservation.setTravelStatus(Reservation.TravelStatus.BOARDED);
            reservationRepository.saveAndFlush(reservation);

            String passengerName = reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName();
            whatsAppService.sendMessage(phone, "✓ Pasajero [" + passengerName + "] marcado a bordo.");
        } catch (Exception e) {
            log.error("Error al marcar pasajero a bordo: ", e);
            whatsAppService.sendMessage(phone, "❌ Ocurrió un error al procesar el abordaje del pasajero.");
        }
    }
}
