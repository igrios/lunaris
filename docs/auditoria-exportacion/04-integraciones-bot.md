# 4. INTEGRACIONES EXTERNAS / BOT

78 archivos. Rutas relativas a la raíz del repositorio. Código original, sin reformatear.

La agrupación es funcional: no modifica paquetes. Cada archivo aparece una sola vez; los archivos con responsabilidades mixtas se asignan a su función principal.

## Índice

- `src/main/java/com/lunaris/ansenuza/application/conversation/BotRoute.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationOrchestrator.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationPresenter.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationSessionCleanupScheduler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationStepHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/FechaParser.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/GoogleMapsParameterFormatter.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/IncomingMessage.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/PassengerAddressResolver.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/WaitingListCapacityGuard.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskAddressTextHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskCompanionsCountHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDateHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDestinationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDniHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskIndividualCompanionHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskInterurbanDestinationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskLocalityHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskNameHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskPromotionCodeHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskReturnDateHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskReturnDateTypeHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskTownDestinationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskTripTypeHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AwaitingPaymentHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/CancelReservationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmAddressHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmWaitingListBookingHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/InterurbanConfirmationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/MainMenuHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/MarketingConfirmationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ReturnWindowSelectionHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/SelectScheduleHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/StartHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/WaitingForInquiryMessageHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/conversation/steps/WaitingListConfirmationHandler.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/BankEmailProcessingResult.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/BankPaymentReservationPort.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/BankTransferNotification.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/PaymentAuditOutboxPort.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/PaymentConfirmedEvent.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/PaymentDetectedAuditRecord.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/ProcessBankEmailService.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/ProcessBankEmailUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/ProcessedTransactionLedgerPort.java`
- `src/main/java/com/lunaris/ansenuza/application/payment/ReservationPaymentCandidate.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/WhatsAppWebhookInboxService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/WhatsAppConversationWindowService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/mail/MercadoPagoImapAdapterConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/mail/MercadoPagoImapProperties.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/parser/MercadoPagoEmailParser.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/chat/WebSocketLiveChatAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/CloudinaryConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/WebSocketConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryDriverDocumentStorageAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryInvoiceStorageService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryNewsBannerStorageAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryReceiptStorageAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalDriverDocumentStorageAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalInvoiceStorageService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalReceiptStorageService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ChatWebSocketController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppSimulatorDevController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppSimulatorDevViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppWebhookController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessageDispatcher.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessagingAdapter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppServiceDevMock.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppWebhookParser.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/payment/ExistingBankPaymentGatewayAdapter.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoPaymentClient.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoSignatureValidator.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoWebhookController.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoWebhookService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/SimulatedDriverPayoutAdapter.java`

## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/BotRoute.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;

public final class BotRoute {
    private BotRoute() {}

    public static boolean fromCordoba(String locality) {
        return locality != null && Normalizer.normalize(locality.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").equalsIgnoreCase("Cordoba");
    }

    public static List<Locality> destinations(LocalityRepository repository) {
        return repository.findAllWithActiveFare().stream()
                .filter(locality -> !fromCordoba(locality.getName()))
                .sorted(Comparator.comparing(Locality::getName))
                .toList();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationOrchestrator.java`

```java
package com.lunaris.ansenuza.application.conversation;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

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
import com.lunaris.ansenuza.application.usecase.CompleteTripUseCase;
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
import org.springframework.beans.factory.annotation.Autowired;

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
    private static final String ADDRESS_LOCATION_STEP = "ASK_ADDRESS_TEXT";
    private static final List<String> ADDRESS_LOCATION_STEPS = List.of(
            ADDRESS_LOCATION_STEP,
            "AWAITING_PICKUP_ADDRESS",
            "CONFIRM_ADDRESS",
            "CONFIRM_ADDRESS_BUTTONS");

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
    private final CompleteTripUseCase completeTripUseCase;
    private final com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry;

    @Autowired
    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository conversationSessionRepository,
            LiveChatPort liveChat,
            OperationControlService operationControlService,
            ReservationCancellationService reservationCancellationService,
            DriverRepository driverRepository,
            ReservationRepository reservationRepository,
            WhatsAppService whatsAppService,
            ProcessPromotionCommandUseCase processPromotionCommandUseCase,
            OnboardPassengerUseCase onboardPassengerUseCase,
            CompleteTripUseCase completeTripUseCase,
            com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry) {
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
        this.completeTripUseCase = completeTripUseCase;
        this.telemetry = telemetry;
    }

    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository sessions, LiveChatPort liveChat,
            OperationControlService operations, ReservationCancellationService cancellations,
            DriverRepository drivers, ReservationRepository reservations, WhatsAppService whatsApp,
            ProcessPromotionCommandUseCase promotions, OnboardPassengerUseCase onboarding,
            CompleteTripUseCase completeTrip) {
        this(handlerList, sessions, liveChat, operations, cancellations, drivers, reservations,
                whatsApp, promotions, onboarding, completeTrip,
                com.lunaris.ansenuza.application.port.ChatbotTelemetryPort.NOOP);
    }

    public ConversationOrchestrator(List<ConversationStepHandler> handlerList,
            ConversationSessionRepository conversationSessionRepository, LiveChatPort liveChat,
            OperationControlService operationControlService,
            ReservationCancellationService reservationCancellationService, DriverRepository driverRepository,
            ReservationRepository reservationRepository, WhatsAppService whatsAppService,
            ProcessPromotionCommandUseCase processPromotionCommandUseCase,
            OnboardPassengerUseCase onboardPassengerUseCase) {
        this(handlerList, conversationSessionRepository, liveChat, operationControlService,
                reservationCancellationService, driverRepository, reservationRepository, whatsAppService,
                processPromotionCommandUseCase, onboardPassengerUseCase, null);
    }

    public void process(IncomingMessage message) {
        try {
            IncomingMessage tracked = message.telemetry().active() ? message
                    : message.withTelemetry(telemetry.begin(message.from(), message.messageId(), message.analyticsTest()));
            try {
                processMessage(tracked);
            } catch (RuntimeException exception) {
                tracked.telemetry().emit(FLOW_BLOCKED, null, PROCESSING_FAILED);
                throw exception;
            }
        } finally {
            liveChat.conversationChanged();
        }
    }

    private void processMessage(IncomingMessage message) {
        String raw = message.body();
        if (raw == null) {
            message.telemetry().emit(INPUT_REJECTED, null, UNSUPPORTED_MESSAGE);
            return;
        }
        String phoneNumber = message.from();
        String rawTrimmed = raw.trim();
        String body = rawTrimmed.toLowerCase();

        // La identidad operativa tiene prioridad absoluta: un chofer activo nunca
        // debe consultar ni reutilizar una ConversationSession de pasajero.
        Optional<Driver> activeDriver = findActiveDriverByPhone(phoneNumber);
        if (activeDriver.isPresent()) {
            message.telemetry().emit(DRIVER_IDENTIFIED, null);
            handleDriverFlow(phoneNumber, activeDriver.get(), message, rawTrimmed);
            return;
        }

        Optional<ConversationSession> locationSession = Optional.empty();
        if (message.type() == IncomingMessage.MessageType.LOCATION) {
            locationSession = conversationSessionRepository.findByPhoneNumber(phoneNumber);
        }

        // Se conserva la consulta de agenda para choferes registrados temporalmente
        // inactivos, sin permitir que otros mensajes salteen el flujo de pasajeros.
        if (isDriverRouteCommand(rawTrimmed)) {
            Optional<Driver> routeDriver = findDriverByPhone(phoneNumber);
            if (routeDriver.isPresent()) {
                message.telemetry().emit(DRIVER_IDENTIFIED, null);
                handleVerRuta(phoneNumber, routeDriver.get());
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
        ConversationSession session = (message.type() == IncomingMessage.MessageType.LOCATION
                ? locationSession
                : conversationSessionRepository.findByPhoneNumber(phoneNumber)).orElseGet(() -> {
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

        message.telemetry().emit(PASSENGER_IDENTIFIED, session.getCurrentStep());

        // Reflejamos el mensaje del cliente en la sala de chat humana (persistencia + WebSocket)
        liveChat.recordIncomingMessage(phoneNumber, raw.trim());

        // Marcamos actividad en cada mensaje para que el scheduler pueda detectar sesiones abandonadas
        session.setLastInteraction(com.lunaris.ansenuza.shared.ArgentinaTime.now());

        // 🌙 CONTROL DE JORNADA: Si la jornada humana terminó y el bot había quedado pausado,
        // lo despausamos automáticamente para que el cliente no quede hablando solo en la nada.
        if (session.isBotPaused() && !session.isManuallyPaused() && !operationControlService.isHumanActionEnabled()) {
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
            message.telemetry().emit(HUMAN_HANDOFF, session.getCurrentStep(), OPERATOR);
            log.info("[Bypass] Bot muteado para {}. Derivando mensaje a la sala de chat humana.",
                    phoneNumber);
            return;
        }

        boolean isGreeting = !"WAITING_FOR_INQUIRY_MESSAGE".equals(session.getCurrentStep())
                && ("hola".equals(body) || "buen dia".equals(body)
                || "buenas".equals(body) || "menu".equals(body) || "reinicio".equals(body));

        String currentStep = session.getCurrentStep();
        String effectiveStep =
                (currentStep == null || "START".equals(currentStep) || isGreeting) ? "START"
                        : currentStep;
        if (message.type() == IncomingMessage.MessageType.LOCATION
                && ADDRESS_LOCATION_STEPS.contains(effectiveStep)) {
            effectiveStep = ADDRESS_LOCATION_STEP;
        }

        ConversationStepHandler handler = handlers.get(effectiveStep);
        if (handler == null) {
            log.warn("[Bot] No hay handler registrado para el paso '{}' (teléfono {}).",
                    effectiveStep, phoneNumber);
            resetToStart(session);
            handler = handlers.get("START");
            if (handler == null) {
                log.error("[Bot] No hay handler START para recuperar la sesión de {}.",
                        phoneNumber);
                return;
            }
        }

        message.telemetry().emit(STEP_CHANGED, effectiveStep);
        handler.handle(session, message);
        message.telemetry().emit(STEP_CHANGED, session.getCurrentStep());
        if ("WAITING_LIST".equals(session.getCurrentStep())) {
            message.telemetry().emit(FLOW_BLOCKED, effectiveStep, NO_CAPACITY);
            message.telemetry().emit(WAITLISTED, effectiveStep, NO_CAPACITY);
        }
    }

    private void resetToStart(ConversationSession session) {
        session.setCurrentStep("START");
        session.setPickupLocality(null);
        session.setPassengerName(null);
        session.setPickupAddress(null);
        session.setDestination(null);
        session.setRoundTrip(null);
        session.setTravelDate(null);
        session.setReturnDate(null);
        session.setRequiresInvoice(null);
        session.setCuil(null);
        session.setPromotionCode(null);
        session.setPromotionDiscountPercentage(null);
        session.setPassengerCount(null);
        session.setCompanionNames(null);
        session.setCurrentCompanionIndex(null);
        session.setTotalCompanions(null);
        session.setScheduleBlock(null);
        session.setReservationCode(null);
        session.setWaitingListEntryId(null);
        session.setBotPaused(false);
        conversationSessionRepository.saveAndFlush(session);
    }

    private String normalizeWhatsAppNumber(String phone) {
        try {
            return com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phone);
        } catch (com.lunaris.ansenuza.domain.exception.DomainValidationException exception) {
            return phone == null ? "" : phone.replaceAll("[^0-9]", "");
        }
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
                || "view route".equals(normalized)
                || "mis viajes".equals(normalized)
                || "agenda".equals(normalized)
                || "mi agenda".equals(normalized)
                || "ver agenda".equals(normalized);
    }

    private Optional<Driver> findDriverByPhone(String phone) {
        String digitsOnlyPhone = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        String normalizedPhone = normalizeWhatsAppNumber(phone);
        return driverRepository.findFirstByPhone(digitsOnlyPhone)
                .or(() -> digitsOnlyPhone.equals(normalizedPhone)
                        ? Optional.empty()
                        : driverRepository.findFirstByPhone(normalizedPhone))
                .or(() -> driverRepository.findAll().stream()
                        .filter(driver -> normalizeWhatsAppNumber(driver.getPhone())
                                .equals(normalizedPhone))
                        .findFirst());
    }

    private void handleDriverFlow(
            String phone, Driver driver, IncomingMessage message, String rawPayload) {
        if (message.type() == IncomingMessage.MessageType.LOCATION) {
            driver.setCurrentLocationUrl(message.pickupAddress());
            driver.setLocationUpdatedAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
            driverRepository.saveAndFlush(driver);
            whatsAppService.sendMessage(phone, "✓ Ubicación del chofer actualizada.");
            return;
        }

        if (rawPayload.regionMatches(true, 0, "COMPLETE_TRIP_", 0, "COMPLETE_TRIP_".length())) {
            handleCompleteTrip(phone, driver, rawPayload.substring("COMPLETE_TRIP_".length()));
            return;
        }

        Optional<UUID> boardingReservationId = message.type() == IncomingMessage.MessageType.INTERACTIVE
                && isUuid(rawPayload)
                        ? Optional.of(UUID.fromString(rawPayload.trim()))
                        : extractBoardingReservationId(message, rawPayload);
        if (boardingReservationId.isPresent()) {
            log.info(
                    "[Driver Flow] Boarding action received. phone={}, reservationId={}, type={}",
                    phone, boardingReservationId.get(), message.type());
            handleBoardPassenger(phone, boardingReservationId.get());
            return;
        }

        if (isDriverRouteCommand(rawPayload)) {
            handleVerRuta(phone, driver);
            return;
        }

        whatsAppService.sendMessage(
                phone,
                "🚐 Menú de chofer\n\nEscribí *VER RUTA* para consultar tus viajes asignados.");
    }

    private void handleCompleteTrip(String phone, Driver driver, String payload) {
        try {
            String[] parts = payload.split("_", 3);
            if (parts.length != 3 || !driver.getId().equals(UUID.fromString(parts[0]))) {
                throw new IllegalArgumentException("payload");
            }
            completeTripUseCase.execute(driver.getId(), java.time.LocalDate.parse(parts[1]), parts[2]);
            whatsAppService.sendMessage(phone, "✅ Viaje finalizado correctamente. ¡Gracias!");
        } catch (Exception ex) {
            log.warn("[Driver Flow] No se pudo finalizar la hoja de ruta: {}", ex.getMessage());
            whatsAppService.sendMessage(phone, "❌ No se pudo finalizar el viaje. Comunicate con un operador.");
        }
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

    private Optional<Driver> findActiveDriverByPhone(String phone) {
        String digitsOnlyPhone = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        String normalizedPhone = normalizeWhatsAppNumber(phone);
        Optional<Driver> exactMatch = driverRepository.findFirstByPhoneAndActiveTrue(digitsOnlyPhone)
                .or(() -> digitsOnlyPhone.equals(normalizedPhone)
                        ? Optional.empty()
                        : driverRepository.findFirstByPhoneAndActiveTrue(normalizedPhone));
        if (exactMatch.isPresent()) {
            return exactMatch;
        }
        return driverRepository.findByActiveTrue().stream()
                .filter(driver -> normalizeWhatsAppNumber(driver.getPhone())
                        .equals(normalizedPhone))
                .findFirst();
    }

    private void handleBoardPassenger(String phone, UUID reservationId) {
        try {
            Optional<Reservation> current = reservationRepository.findById(reservationId);
            if (current.isPresent() && isBoardingClosed(current.get())) {
                log.warn(
                        "[Driver Flow] Boarding ignored for closed reservation. "
                                + "phone={}, reservationId={}, status={}, travelStatus={}",
                        phone,
                        reservationId,
                        current.get().getStatus(),
                        current.get().getTravelStatus());
                whatsAppService.sendMessage(
                        phone, "Esta reserva ya se encuentra abordada o finalizada.");
                return;
            }
            onboardPassengerUseCase.execute(reservationId, phone);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[Driver Flow] Boarding reservation not found. phone={}, reservationId={}",
                    phone, reservationId);
            whatsAppService.sendMessage(phone, "❌ No se encontró la reserva especificada.");
        } catch (IllegalStateException exception) {
            log.warn(
                    "[Driver Flow] Boarding rejected. phone={}, reservationId={}, reason={}",
                    phone, reservationId, exception.getMessage());
            whatsAppService.sendMessage(
                    phone, "Esta reserva ya se encuentra abordada o finalizada.");
        } catch (Exception e) {
            log.error("Error al marcar pasajero a bordo: ", e);
            whatsAppService.sendMessage(phone, "❌ Ocurrió un error al procesar el abordaje del pasajero.");
        }
    }

    private boolean isBoardingClosed(Reservation reservation) {
        Reservation.TravelStatus travelStatus = reservation.getTravelStatus();
        if (travelStatus == Reservation.TravelStatus.ONBOARD
                || travelStatus == Reservation.TravelStatus.BOARDED
                || travelStatus == Reservation.TravelStatus.ONBOARDED
                || travelStatus == Reservation.TravelStatus.REALIZED
                || travelStatus == Reservation.TravelStatus.CANCELED
                || travelStatus == Reservation.TravelStatus.NO_SHOW) {
            return true;
        }
        String status = reservation.getStatus();
        return !"CONFIRMED".equalsIgnoreCase(status)
                && !"PENDING".equalsIgnoreCase(status);
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationPresenter.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;

/**
 * Construye y envía los mensajes de presentation reutilizados por varios pasos del bot
 * (listado de localidades y resumen del itinerario), evitando duplicar lógica entre handlers.
 */
@Component
public class ConversationPresenter {

    private final LocalityRepository localityRepository;
    private final PassengerRepository passengerRepository; // 💳 Inyectamos el repositorio para leer la billetera virtual
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;
    private final com.lunaris.ansenuza.service.interurban.InterurbanCatalog interurban;



    public ConversationPresenter(LocalityRepository localityRepository, PassengerRepository passengerRepository, PricingAndScheduleService pricingAndScheduleService, MessagingPort messaging) {
        this(localityRepository, passengerRepository, pricingAndScheduleService, messaging, java.util.Optional.empty());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ConversationPresenter(LocalityRepository localityRepository, PassengerRepository passengerRepository, PricingAndScheduleService pricingAndScheduleService, MessagingPort messaging,
            java.util.Optional<com.lunaris.ansenuza.service.interurban.InterurbanCatalog> interurban) {
        this.localityRepository = localityRepository;
        this.passengerRepository = passengerRepository;
        this.pricingAndScheduleService = pricingAndScheduleService;
        this.messaging = messaging;
        this.interurban = interurban.orElse(null);
    }

    public void sendAllLocalitiesList(String phoneNumber, String saludo) {
        List<Locality> localities = localityRepository.findAllWithActiveFare().stream()
                .filter(locality -> !BotRoute.fromCordoba(locality.getName()))
                .toList();
        if (interurban != null) {
            var available = new java.util.ArrayList<>(localities);
            for (String origin : interurban.origins()) {
                if (available.stream().noneMatch(l -> l.getName().equalsIgnoreCase(origin))) {
                    available.add(Locality.builder().name(origin).build());
                }
            }
            localities = List.copyOf(available);
        }

        StringBuilder menu = new StringBuilder(saludo)
                .append("📍 *¿Desde qué localidad salís?*\n\n");
        int index = 1;
        for (Locality locality : localities) {
            menu.append("*").append(index).append(")* ").append(locality.getName()).append("\n");
            index++;
        }
        menu.append("*").append(index).append(")* Córdoba\n");
        menu.append("\n*0)* Volver al Menú Principal\n\n_Respondé escribiendo únicamente el número que corresponda a tu localidad de origen._");
        messaging.sendText(phoneNumber, menu.toString());
    }

    public void sendReservationSummaryWithButtons(String phoneNumber, ConversationSession session) {
        sendReservationSummaryWithButtons(phoneNumber, session,
                com.lunaris.ansenuza.application.telemetry.ChatbotInteraction.NONE);
    }

    public void sendReservationSummaryWithButtons(String phoneNumber, ConversationSession session,
            com.lunaris.ansenuza.application.telemetry.ChatbotInteraction telemetry) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dates = "*Ida:* " + session.getTravelDate().format(formatter);

        if (Boolean.TRUE.equals(session.getRoundTrip())) {
            if (session.getReturnDate() != null) {
                dates += " | *Vuelta:* " + session.getReturnDate().format(formatter);
            } else {
                dates += " | *Vuelta:* 🔄 _ABIERTA_";
            }
        }

        // 🕒 REPARACIÓN FASE 3: Obtenemos el bloque de horario real guardado en la sesión
        String blockInfo = session.getScheduleBlock() != null ? session.getScheduleBlock() : "03:00 AM";
        
        // Limpiamos el string para el calculador (ej: de "08:00 AM" a "08:00")
        String rawHour = blockInfo.replace(" AM", "").replace(" PM", "").trim();

        String estimatedPickupTime = pricingAndScheduleService.calculateEstimatedPickupTime(
                session.getPickupLocality(), rawHour);

        int totalAsientos =
                session.getPassengerCount() != null ? session.getPassengerCount() : 1;

        // 💰 Cálculo del precio bruto base del viaje
        BigDecimal priceBase = pricingAndScheduleService.calculateTripPrice(
                BotRoute.fromCordoba(session.getPickupLocality())
                        ? session.getDestination() : session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

        // 💳 Verificamos el saldo corriente a favor del pasajero
        BigDecimal saldoAplicado = BigDecimal.ZERO;
        Passenger passenger = passengerRepository.findByPhone(phoneNumber).orElse(null);
        if (passenger != null && passenger.getCurrentBalance() != null) {
            saldoAplicado = passenger.getCurrentBalance();
        }

        // Calculamos el neto final a pagar (sin bajar de cero)
        BigDecimal totalNeto = priceBase.subtract(saldoAplicado);
        if (totalNeto.compareTo(BigDecimal.ZERO) < 0) {
            totalNeto = BigDecimal.ZERO;
        }

        // Si el saldo cubrió más del costo, mostramos solo lo que se restó de forma efectiva
        if (saldoAplicado.compareTo(priceBase) > 0) {
            saldoAplicado = priceBase;
        }

        BigDecimal descuentoPromo = BigDecimal.ZERO;
        if (session.getPromotionDiscountPercentage() != null) {
            descuentoPromo = priceBase.multiply(BigDecimal.valueOf(session.getPromotionDiscountPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            totalNeto = totalNeto.subtract(descuentoPromo).max(BigDecimal.ZERO);
        }

        String paxLine = session.getPassengerName();
        if (session.getCompanionNames() != null && !session.getCompanionNames().isBlank()) {
            paxLine += "\n👥 *Acompañantes:* " + session.getCompanionNames();
        }

        // Si la sesión ya precalculó el código base o nexo temporal lo exponemos, sino dejamos el marcador
        String displayCode = session.getReservationCode() != null ? session.getReservationCode().replace("-IDA", "") : "Pendiente asignación";

        String summary = """
                📌 *Nro. de Grupo/Reserva:* %s
                👤 *Pasajero titular:* %s
                🔢 *Asientos a ocupar:* %d
                📍 *Origen:* %s (%s)
                🎯 *Destino:* %s
                🕒 *Horario de cabecera:* %s
                ⏱ *Hora de retiro por tu domicilio:* %s
                🔄 *Modalidad:* %s
                📅 %s
                🧾 *Documento Factura:* %s
                💵 *Precio Base del Viaje:* $%,.2f
                📉 *Saldo a Favor Aplicado:* -$%,.2f
                🎟️ *Descuento promocional:* -$%,.2f
                💰 *Total Neto a Transferir:* $%,.2f
                """.formatted(displayCode, paxLine, totalAsientos, session.getPickupLocality(),
                session.getPickupAddress(), session.getDestination(), blockInfo,
                estimatedPickupTime,
                Boolean.TRUE.equals(session.getRoundTrip()) ? "Ida y vuelta" : "Solo ida",
                dates, session.getCuil(), priceBase, saldoAplicado, descuentoPromo, totalNeto);

        telemetry.sendButtons(messaging, phoneNumber, "Verificación del Itinerario", summary,
                List.of(new Button("confirm_ok", "Confirmar 👍"),
                        new Button("confirm_cancel", "Cancelar ❌")),
                com.lunaris.ansenuza.application.telemetry.ChatbotEventType.SUMMARY_SENT, session.getCurrentStep());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationSessionCleanupScheduler.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🧹 Tarea programada que cierra las sesiones del bot de WhatsApp que quedaron a mitad de camino
 * y sin respuesta del cliente. Así un pasajero que abandonó la conversación arranca de cero
 * (menú principal) la próxima vez, en vez de quedar atrapado en un paso intermedio.
 *
 * <p>Se excluyen las sesiones con {@code botPaused = true}: esas están siendo atendidas por un
 * operador humano y no deben tocarse.
 *
 * <p>Parametrizable vía {@code application.yaml}:
 * <ul>
 *   <li>{@code bot.session.inactive-minutes} (default 30): minutos sin actividad para considerar abandonada.</li>
 *   <li>{@code bot.session.cleanup-interval-ms} (default 600000 = 10 min): cada cuánto corre la limpieza.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConversationSessionCleanupScheduler {

    private static final String SESSION_INACTIVITY_TIMEOUT_KEY = "session.inactivity.timeout.minutes";
    private static final long DEFAULT_INACTIVE_MINUTES = 30;

    private final ConversationSessionRepository conversationSessionRepository;
    private final SystemConfigurationService configurationService;

    @Scheduled(fixedDelayString = "${bot.session.cleanup-interval-ms:600000}")
    @Transactional
    public void purgeInactiveSessions() {
        long inactiveMinutes = resolveInactiveMinutes();
        LocalDateTime cutoff =
                com.lunaris.ansenuza.shared.ArgentinaTime.now().minusMinutes(inactiveMinutes);
        List<ConversationSession> abandonadas =
                conversationSessionRepository.findByBotPausedFalseAndLastInteractionBefore(cutoff);

        if (abandonadas.isEmpty()) {
            return;
        }

        conversationSessionRepository.deleteAll(abandonadas);
        log.info("[Cleanup] Se cerraron {} sesiones de bot inactivas (sin actividad > {} min).",
                abandonadas.size(), inactiveMinutes);
    }

    private long resolveInactiveMinutes() {
        String configuredValue = configurationService.getValue(
                SESSION_INACTIVITY_TIMEOUT_KEY,
                String.valueOf(DEFAULT_INACTIVE_MINUTES));
        try {
            long minutes = Long.parseLong(configuredValue.trim());
            return minutes > 0 ? minutes : DEFAULT_INACTIVE_MINUTES;
        } catch (NumberFormatException e) {
            log.warn("[Cleanup] Timeout de inactividad inválido '{}'. Usando {} min.",
                    configuredValue, DEFAULT_INACTIVE_MINUTES);
            return DEFAULT_INACTIVE_MINUTES;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/ConversationStepHandler.java`

```java
package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.domain.model.ConversationSession;

/**
 * Estrategia que resuelve un único paso (currentStep) de la máquina de estados
 * conversacional del bot. Cada implementación es responsable de un solo estado,
 * evitando la god class original.
 *
 * <p>El {@link ConversationOrchestrator} registra todos los handlers por su
 * {@link #step()} y rutea cada mensaje entrante al que corresponda.
 */
public interface ConversationStepHandler {

    /** Identificador del paso (currentStep) que este handler atiende. */
    String step();

    /** Procesa el mensaje entrante para la sesión dada (mutando y persistiendo la sesión). */
    void handle(ConversationSession session, IncomingMessage message);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/FechaParser.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Optional;

public class FechaParser {

    private static final DateTimeFormatter FORMATEADOR_FLEXIBLE = new DateTimeFormatterBuilder()
            .appendPattern("[d][dd]") // Día de 1 o 2 dígitos
            .appendPattern("[/][-]")  // Soporta barra o guion
            .appendPattern("[M][MM]") // Mes de 1 o 2 dígitos
            .appendPattern("[/][-]")  // Soporta barra o guion
            .appendValueReduced(ChronoField.YEAR, 2, 4, 2000) // Año de 2 cifras (26 -> 2026) o 4 cifras (2026)
            .toFormatter();

    public static Optional<LocalDate> parsear(String textoUsuario) {
        if (textoUsuario == null) return Optional.empty();
        try {
            String textoLimpio = textoUsuario.trim();
            return Optional.of(LocalDate.parse(textoLimpio, FORMATEADOR_FLEXIBLE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/GoogleMapsParameterFormatter.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Reservation;

public final class GoogleMapsParameterFormatter {

    private GoogleMapsParameterFormatter() {
    }

    public static String encode(String location) {
        String normalized = normalize(location);
        return URLEncoder.encode(normalized, StandardCharsets.UTF_8)
                .replace("%2C", ",");
    }

    public static String buildDirectionsUrl(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return "https://www.google.com/maps/dir/?api=1&destination=Cordoba";
        }
        List<String> pickups = reservations.stream()
                .filter(java.util.Objects::nonNull)
                .filter(Reservation::isScheduledConfirmedTrip)
                .map(GoogleMapsParameterFormatter::pickupLocation)
                .filter(location -> !location.isBlank())
                .toList();
        if (pickups.isEmpty()) {
            return "https://www.google.com/maps/dir/?api=1&destination=Cordoba";
        }
        StringBuilder url = new StringBuilder(
                "https://www.google.com/maps/dir/?api=1&origin=")
                .append(encode(pickups.getFirst()))
                .append("&destination=Cordoba");
        if (pickups.size() > 1) {
            url.append("&waypoints=")
                    .append(encode(String.join("|", pickups.subList(1, pickups.size())))
                            .replace("%7C", "|"));
        }
        return url.toString();
    }

    private static String pickupLocation(Reservation reservation) {
        String address = normalize(reservation.getPickupAddress());
        if (address.matches("-?\\d+(?:\\.\\d+)?,-?\\d+(?:\\.\\d+)?")) {
            return address;
        }
        String locality = reservation.getPickupLocality() == null
                || reservation.getPickupLocality().isBlank()
                ? "Córdoba"
                : reservation.getPickupLocality().trim();
        return (address.isBlank() ? locality : address + ", " + locality)
                + ", Córdoba, Argentina";
    }

    static String normalize(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String trimmed = location.trim();
        try {
            URI uri = URI.create(trimmed);
            if (uri.getHost() == null || !uri.getHost().endsWith("google.com")) {
                return trimmed;
            }
            return Arrays.stream(uri.getRawQuery() == null ? new String[0] : uri.getRawQuery().split("&"))
                    .map(parameter -> parameter.split("=", 2))
                    .filter(parts -> parts.length == 2 && "q".equals(parts[0]))
                    .map(parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8))
                    .findFirst()
                    .orElse("");
        } catch (IllegalArgumentException ignored) {
            return trimmed;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/IncomingMessage.java`

```java
package com.lunaris.ansenuza.application.conversation;

/**
 * Mensaje entrante ya normalizado y desacoplado del formato crudo del webhook de Meta.
 *
 * @param from    número de teléfono normalizado del remitente
 * @param type    tipo de mensaje recibido
 * @param body    texto del mensaje (para TEXT) o id del botón pulsado (para INTERACTIVE); puede ser null
 * @param mediaId id o URL del adjunto multimedia (para IMAGE o DOCUMENT); puede ser null
 * @param latitude latitud compartida (para LOCATION); puede ser null
 * @param longitude longitud compartida (para LOCATION); puede ser null
 */
public record IncomingMessage(
        String messageId, String from, MessageType type, String body, String mediaId,
        Double latitude, Double longitude,
        com.lunaris.ansenuza.application.telemetry.ChatbotInteraction telemetry, boolean analyticsTest) {

    public IncomingMessage {
        if (telemetry == null) telemetry = com.lunaris.ansenuza.application.telemetry.ChatbotInteraction.NONE;
    }

    public IncomingMessage(String messageId, String from, MessageType type, String body, String mediaId,
            Double latitude, Double longitude) {
        this(messageId, from, type, body, mediaId, latitude, longitude,
                com.lunaris.ansenuza.application.telemetry.ChatbotInteraction.NONE, false);
    }

    public IncomingMessage withTelemetry(com.lunaris.ansenuza.application.telemetry.ChatbotInteraction context) {
        return context == null || !context.active() ? this
                : new IncomingMessage(messageId, from, type, body, mediaId, latitude, longitude, context, analyticsTest);
    }

    public IncomingMessage asAnalyticsTest() {
        return new IncomingMessage(messageId, from, type, body, mediaId, latitude, longitude, telemetry, true);
    }

    public enum MessageType {
        TEXT, IMAGE, DOCUMENT, INTERACTIVE, LOCATION, OTHER
    }

    public IncomingMessage(String from, MessageType type, String body, String mediaId) {
        this(null, from, type, body, mediaId, null, null);
    }

    public IncomingMessage(String from, MessageType type, String body, String mediaId,
            Double latitude, Double longitude) {
        this(null, from, type, body, mediaId, latitude, longitude);
    }

    public boolean isImageWithMedia() {
        return type == MessageType.IMAGE && mediaId != null;
    }

    public boolean isMediaWithResource() {
        return (type == MessageType.IMAGE || type == MessageType.DOCUMENT)
                && mediaId != null && !mediaId.isBlank();
    }

    public String pickupAddress() {
        if (type == MessageType.LOCATION && latitude != null && longitude != null) {
            return "https://maps.google.com/?q=" + latitude + "," + longitude;
        }
        return body == null ? null : body.trim();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/PassengerAddressResolver.java`

```java
package com.lunaris.ansenuza.application.conversation;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/**
 * Decide el siguiente paso de la conversación según conozcamos o no el domicilio habitual
 * del pasajero: si ya tenemos dirección en la misma localidad, ofrece confirmarla; si no,
 * pide la dirección por texto. Reutilizado al terminar la carga de acompañantes.
 */
@Component
@RequiredArgsConstructor
public class PassengerAddressResolver {

    private final PassengerRepository passengerRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    public void resolve(String phoneNumber, ConversationSession session) {
        Optional<Passenger> passengerOpt = passengerRepository.findByPhone(phoneNumber);
        if (passengerOpt.isPresent() && passengerOpt.get().getAddress() != null
                && passengerOpt.get().getLocality() != null) {
            Passenger p = passengerOpt.get();
            if (session.getPickupLocality().equalsIgnoreCase(p.getLocality())) {
                session.setPickupAddress(p.getAddress());
                session.setCurrentStep("CONFIRM_ADDRESS_BUTTONS");
                conversationSessionRepository.saveAndFlush(session);

                messaging.sendButtons(phoneNumber, "Dirección de Retiro",
                        "📍 *Detectamos tu domicilio habitual en " + p.getLocality() + ":*\n"
                                + p.getAddress() + "\n\n¿Pasamos a buscarte por acá?",
                        List.of(new Button("addr_yes", "Sí, pasar por acá ✅"),
                                new Button("addr_no", "Nueva Dirección 🏠")));
                return;
            }
        }
        session.setCurrentStep("ASK_ADDRESS_TEXT");
        conversationSessionRepository.saveAndFlush(session);
        messaging.requestLocation(phoneNumber,
                "🏠 Escribí la calle y número donde pasamos a buscarte en "
                        + session.getPickupLocality()
                        + " (ej.: Av. San Martín 450), o tocá el botón para compartir tu ubicación.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/WaitingListCapacityGuard.java`

```java
package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import com.lunaris.ansenuza.domain.model.service.ReturnCapacityPolicy;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitingListCapacityGuard {

    private final ReservationRepository reservationRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final WaitingListService waitingListService;
    private final CapacityLockRepository capacityLockRepository;

    public WaitingListCapacityGuard(ReservationRepository reservationRepository,
            SystemConfigurationService systemConfigurationService,
            ConversationSessionRepository conversationSessionRepository,
            MessagingPort messaging, WaitingListService waitingListService) {
        this(reservationRepository, systemConfigurationService, conversationSessionRepository,
                messaging, waitingListService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WaitingListCapacityGuard(ReservationRepository reservationRepository,
            SystemConfigurationService systemConfigurationService,
            ConversationSessionRepository conversationSessionRepository,
            MessagingPort messaging, WaitingListService waitingListService,
            CapacityLockRepository capacityLockRepository) {
        this.reservationRepository = reservationRepository;
        this.systemConfigurationService = systemConfigurationService;
        this.conversationSessionRepository = conversationSessionRepository;
        this.messaging = messaging;
        this.waitingListService = waitingListService;
        this.capacityLockRepository = capacityLockRepository;
    }

    @Transactional
    public boolean offerWaitingListWhenFull(ConversationSession session) {
        int requestedSeats = session.getPassengerCount() == null
                ? 1 : Math.max(1, session.getPassengerCount());
        String schedule = session.getScheduleBlock() == null
                || session.getScheduleBlock().isBlank()
                ? "03:00 AM" : session.getScheduleBlock().trim();
        // La fila se bloquea dentro de la misma transacción que crea la reserva.
        // Los tests unitarios antiguos no proveen el repositorio, por eso conservan
        // el comportamiento de conteo sin persistencia.
        if (capacityLockRepository != null && session.getTravelDate() != null) {
            String direction = isCordoba(session.getPickupLocality()) ? "RETURN" : "OUTBOUND";
            String key = session.getTravelDate() + "|" + ("RETURN".equals(direction) ? "DAY" : ReturnCapacityPolicy.normalizeSchedule(schedule)) + "|" + direction;
            capacityLockRepository.ensureExists(key);
            if (capacityLockRepository.findForUpdate(key) == null) {
                throw new IllegalStateException("No se pudo bloquear la capacidad del turno.");
            }
        }
        boolean returning = isCordoba(session.getPickupLocality());
        int maxCapacity = ReturnCapacityPolicy.CAPACITY;
        long available = returning
                ? ReturnCapacityPolicy.availableSeats(reservationRepository, session.getTravelDate(), schedule)
                : maxCapacity - reservationRepository.countReservedSeats(session.getTravelDate(), schedule);

        if (requestedSeats <= available) {
            return false;
        }

        waitingListService.join(session);
        session.setCurrentStep("WAITING_LIST");
        messaging.sendText(session.getPhoneNumber(),
                "⏳ La unidad principal de " + maxCapacity + " pasajeros para el " + schedule
                        + " está completa. Te agregamos a la Lista de Espera y te avisaremos "
                        + "por WhatsApp cuando se libere un lugar.");
        conversationSessionRepository.delete(session);
        conversationSessionRepository.flush();
        return true;
    }

    private static boolean isCordoba(String locality) {
        return com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.isCordoba(locality);
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskAddressTextHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.UpdatePassengerAddressUseCase;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/** ASK_ADDRESS_TEXT: guarda la dirección de retiro ingresada manualmente y pide el destino. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AskAddressTextHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final UpdatePassengerAddressUseCase updatePassengerAddressUseCase;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_ADDRESS_TEXT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String pickupAddress = message.pickupAddress();
        if (pickupAddress == null || pickupAddress.isBlank()) {
            messaging.requestLocation(phoneNumber,
                    "🏠 Enviá calle y número, o tocá el botón para compartir tu ubicación.");
            return;
        }
        String normalizedAddress = pickupAddress.trim();
        try {
            updatePassengerAddressWithRetry(
                    phoneNumber, normalizedAddress, session.getPickupLocality());

            session.setPickupAddress(normalizedAddress);
            boolean cordobaOrigin = com.lunaris.ansenuza.application.conversation.BotRoute
                    .fromCordoba(session.getPickupLocality());
            session.setCurrentStep(cordobaOrigin ? "ASK_TRIP_TYPE" : "ASK_DESTINATION");
            conversationSessionRepository.saveAndFlush(session);

            if (cordobaOrigin) {
                messaging.sendButtons(phoneNumber, "Dirección de ascenso registrada",
                        "✅ *Registramos tu punto de ascenso:*\n" + normalizedAddress
                                + "\n\n🔄 *¿Qué tipo de viaje vas a realizar?*",
                        List.of(new Button("trip_ida", "Solo ida ➡️"),
                                new Button("trip_completo", "Ida y vuelta 🔄")));
            } else {
                messaging.sendButtons(phoneNumber, "Dirección actualizada",
                        "✅ *Actualizamos tu dirección de retiro:*\n"
                                + normalizedAddress + "\n\n"
                                + "🎯 *¿Hacia dónde viajás en Córdoba?*",
                        List.of(new Button("dest_aeropuerto", "Aeropuerto Cba ✈️"),
                                new Button("dest_capital", "Córdoba Capital 🏢")));
            }
        } catch (RuntimeException exception) {
            log.warn("No se pudo actualizar la dirección del pasajero {}.", phoneNumber, exception);
            session.setCurrentStep("ASK_ADDRESS_TEXT");
            messaging.requestLocation(phoneNumber,
                    "⚠️ No pudimos guardar esa dirección. Enviá nuevamente calle y número, "
                            + "o compartí tu ubicación.");
        }
    }

    private void updatePassengerAddressWithRetry(
            String phoneNumber, String address, String locality) {
        try {
            updatePassengerAddressUseCase.update(phoneNumber, address, locality);
        } catch (ObjectOptimisticLockingFailureException exception) {
            log.warn("Conflicto concurrente actualizando la dirección del pasajero {}. Reintentando con una lectura limpia.",
                    phoneNumber);
            updatePassengerAddressUseCase.update(phoneNumber, address, locality);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskCompanionsCountHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.PassengerAddressResolver;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_COMPANIONS_COUNT: cantidad de acompañantes (0 a 3); ramifica a carga individual o dirección. */
@Component
@RequiredArgsConstructor
public class AskCompanionsCountHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerAddressResolver passengerAddressResolver;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_COMPANIONS_COUNT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        try {
            int count = Integer.parseInt(body);
            if (count < 0 || count > 3) {
                message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
                messaging.sendText(phoneNumber,
                        "❌ Podés registrar hasta un máximo de 3 acompañantes directos. Ingresá entre 0 y 3:");
                return;
            }

            if (count == 0) {
                session.setPassengerCount(1);
                session.setCompanionNames(null);
                passengerAddressResolver.resolve(phoneNumber, session);
            } else {
                session.setTotalCompanions(count);
                session.setPassengerCount(1);
                session.setCurrentStep("ASK_INDIVIDUAL_COMPANION");
                session.setCompanionNames("");
                
                // 🛠️ CORRECCIÓN: Usamos el campo semántico correcto en lugar de setCuil("1")
                session.setCurrentCompanionIndex(1);
                
                conversationSessionRepository.saveAndFlush(session);
                messaging.sendText(phoneNumber,
                        "👤 *Ingresá Nombre y Apellido de tu acompañante 1:*");
            }
        } catch (Exception e) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "⚠️ Respondé únicamente con el número digital (Ej: 2).");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDateHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.FechaParser;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService; // 👈 NUEVO IMPORT
import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DATE: valida la fecha de ida de forma flexible; deriva a fecha de regreso (ida y vuelta) o a facturación. */
@Component
@RequiredArgsConstructor
public class AskDateHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService; // 👈 CONTROL DE JORNADA INYECTADO
    private final SameDayBookingPolicy sameDayBookingPolicy;

    @Override
    public String step() {
        return "ASK_DATE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();

        String rawBody = message.body() == null ? "" : message.body().trim().toLowerCase();
        if ("return_other_date".equals(rawBody) || "otra fecha".equals(rawBody)
                || "otra_fecha".equals(rawBody)) {
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "✍️ *Por favor, ingresá la fecha en la que querés viajar* "
                            + "(ejemplo: 12/08 o 12 de agosto).");
            return;
        }

        // Usamos el parseador flexible para extraer la fecha sin importar los ceros o el año corto
        Optional<LocalDate> fechaParseada = FechaParser.parsear(message.body());

        if (fechaParseada.isEmpty()) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "❌ *Formato erróneo.* Por favor, indicá la fecha de tu viaje "
                            + "(por ejemplo: 12/08/2026):");
            return;
        }

        LocalDate travelDate = fechaParseada.get();
        LocalDate hoy = com.lunaris.ansenuza.shared.ArgentinaTime.today();

        // Mantenemos tu validación de negocio intacta
        if (travelDate.isBefore(hoy)) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "❌ La fecha no puede ser anterior a hoy. Reingresá:");
            return;
        }

        try {
            sameDayBookingPolicy.validate(travelDate, session.getScheduleBlock());
        } catch (SameDayBookingClosedException exception) {
            message.telemetry().emit(FLOW_BLOCKED, step(), DATE_CUTOFF);
            messaging.sendText(phoneNumber, exception.getMessage());
            return;
        }

        // El corte para mañana aplica a pueblos; las salidas desde Córdoba quedan exceptuadas.
        if (travelDate.equals(hoy.plusDays(1))
                && !BotRoute.fromCordoba(session.getPickupLocality())
                && operationControlService.isPastCutoffTime()) {
            message.telemetry().emit(FLOW_BLOCKED, step(), DATE_CUTOFF);
            messaging.sendText(phoneNumber,
                    "⏱️ *Logística Cerrada para Mañana.*\n\nTe recordamos que las reservas para viajar al día siguiente cierran estrictamente a las *19:00 Hs* para poder asignar unidades y garantizar el descanso reglamentario de nuestros choferes. 🚐💤\n\nPor favor, ingresá una fecha alternativa a partir de pasados mañana:");
            return;
        }
        
        session.setTravelDate(travelDate);
        message.telemetry().emit(DATE_SELECTED, step());

        if (Boolean.TRUE.equals(session.getRoundTrip())) {
            session.setCurrentStep("ASK_RETURN_DATE_TYPE");
            conversationSessionRepository.saveAndFlush(session);

            messaging.sendButtons(phoneNumber, "Fecha de Regreso",
                    "📅 *¿Cuándo programamos el regreso?*\n\nSi todavía no sabés el día exacto, podés dejar la fecha abierta y coordinarla más adelante con Martín.",
                    List.of(new Button("return_same_day", "Vuelvo en el día"),
                            new Button("return_choose_date", "Otra fecha"),
                            new Button("return_open", "Fecha abierta")));
        } else {
            session.setCurrentStep("ASK_DNI_REQUIRED");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDestinationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DESTINATION: registra el destino en Córdoba (Capital o Aeropuerto) y pide la modalidad. */
@Component
@RequiredArgsConstructor
public class AskDestinationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_DESTINATION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        String dest = "dest_aeropuerto".equals(body) ? "Aeropuerto Córdoba"
                : "dest_capital".equals(body) ? "Córdoba" : null;
        if (dest == null) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            return;
        }

        session.setDestination(dest);
        message.telemetry().emit(ROUTE_SELECTED, step());
        session.setCurrentStep("ASK_TRIP_TYPE");
        conversationSessionRepository.saveAndFlush(session);

        messaging.sendButtons(phoneNumber, "Modalidad", "🔄 *¿Qué tipo de viaje vas a realizar?*",
                List.of(new Button("trip_ida", "Solo ida ➡️"),
                        new Button("trip_completo", "Ida y vuelta 🔄")));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskDniHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_DNI_REQUIRED: valida el DNI/CUIT fiscal y muestra el resumen del itinerario para confirmar. */
@Component
@RequiredArgsConstructor
public class AskDniHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_DNI_REQUIRED";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        String cleanDni = body.replaceAll("[^0-9]", "");
        if (cleanDni.length() < 7 || cleanDni.length() > 11) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "❌ *DNI o CUIT inválido.* Verificá el número e ingresalo nuevamente sin guiones:");
            return;
        }
        session.setCuil(cleanDni);
        session.setCurrentStep("ASK_PROMOTION_CODE");
        conversationSessionRepository.saveAndFlush(session);
        message.telemetry().emit(PASSENGER_DATA_COMPLETED, step());
        messaging.sendText(phoneNumber,
                "🎟️ Si tenés un código promocional de 4 dígitos, ingresalo ahora. Si no tenés, escribí *SIN PROMO*.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskIndividualCompanionHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.PassengerAddressResolver;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_INDIVIDUAL_COMPANION: acumula los nombres de cada acompañante uno por uno. */
@Component
@RequiredArgsConstructor
public class AskIndividualCompanionHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerAddressResolver passengerAddressResolver;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_INDIVIDUAL_COMPANION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String currentName = message.body() == null ? "" : message.body().trim().replaceAll("\\s+", " ");
        if (currentName.isBlank()) {
            messaging.sendText(phoneNumber, "⚠️ Ingresá el nombre y apellido del acompañante.");
            return;
        }

        java.util.List<String> companions = parseCompanions(session.getCompanionNames());
        boolean duplicate = companions.stream().anyMatch(name -> name.equalsIgnoreCase(currentName));
        int expectedCompanions = Math.max(0, Math.min(3,
                session.getTotalCompanions() == null ? 0 : session.getTotalCompanions()));
        if (!duplicate && companions.size() < expectedCompanions && companions.size() < 3) {
            companions.add(currentName);
        }
        session.setCompanionNames(String.join(", ", companions));
        session.setPassengerCount(Math.min(4, 1 + companions.size()));

        if (companions.size() >= expectedCompanions) {
            session.setCurrentCompanionIndex(null);
            passengerAddressResolver.resolve(phoneNumber, session);
        } else {
            int nextIndex = companions.size() + 1;
            session.setCurrentCompanionIndex(nextIndex);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido de tu acompañante " + nextIndex + ":*");
        }
    }

    private java.util.List<String> parseCompanions(String accumulated) {
        if (accumulated == null || accumulated.isBlank()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.stream(accumulated.split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .distinct()
                .limit(3)
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskInterurbanDestinationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.service.interurban.InterurbanCatalog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class AskInterurbanDestinationHandler implements ConversationStepHandler {
    private final InterurbanCatalog catalog;
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;
    private final LocalityRepository localities;
    private final AskLocalityHandler conventional;

    @Override
    public String step() { return "ASK_INTERURBAN_DESTINATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String input = message.body().trim();
        if ("0".equals(input)) {
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "1) Reservar un viaje\n2) Ver precios\n3) Operador");
            return;
        }
        if ("c".equalsIgnoreCase(input)) {
            var origins = localities.findAllWithActiveFare().stream()
                    .filter(l -> !BotRoute.fromCordoba(l.getName())).toList();
            for (int i = 0; i < origins.size(); i++) {
                if (origins.get(i).getName().equalsIgnoreCase(session.getPickupLocality())) {
                    conventional.handleConventional(session, new IncomingMessage(message.from(),
                            IncomingMessage.MessageType.TEXT, Integer.toString(i + 1), null)
                            .withTelemetry(message.telemetry()));
                    return;
                }
            }
            messaging.sendText(session.getPhoneNumber(), "No hay tarifa convencional para este origen. Elegí un destino interurbano o 0 para volver.");
            return;
        }
        var selected = catalog.destinations(session.getPickupLocality()).stream()
                .filter(d -> ("i_" + d.stop()).equalsIgnoreCase(input)).findFirst();
        if (selected.isEmpty()) {
            messaging.sendText(session.getPhoneNumber(), "Destino inválido. Respondé con el código del destino o 0 para volver.");
            return;
        }
        var destination = selected.get();
        session.setDestination(destination.name());
        // No reutilizar ASK_DATE/CONFIRM: persisten reservas y capacidad del esquema public.
        session.setCurrentStep("ASK_INTERURBAN_CONFIRMATION");
        sessions.saveAndFlush(session);
        messaging.sendButtons(session.getPhoneNumber(), "Viaje interurbano",
                "💰 *%s → %s*: $%,.2f ARS por pasajero, solo ida. Tarifa vigente de referencia.\n¿Querés coordinar la reserva con un operador? Confirmará horario, disponibilidad y tarifa para tu fecha."
                        .formatted(session.getPickupLocality(), destination.name(), destination.fare()),
                java.util.List.of(new com.lunaris.ansenuza.application.port.Button("interurban_reserve", "Coordinar reserva"),
                        new com.lunaris.ansenuza.application.port.Button("interurban_cancel", "Volver al menú")));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskLocalityHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import lombok.extern.slf4j.Slf4j;

/** ASK_LOCALITY: el pasajero elige su localidad de origen y recibe la cotización base. */
@Component
@Slf4j
public class AskLocalityHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final LocalityRepository localityRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final MessagingPort messaging;
    private final com.lunaris.ansenuza.service.interurban.InterurbanCatalog interurban;



    public AskLocalityHandler(ConversationSessionRepository conversationSessionRepository, LocalityRepository localityRepository, PricingAndScheduleService pricingAndScheduleService, MessagingPort messaging) {
        this(conversationSessionRepository, localityRepository, pricingAndScheduleService, messaging, java.util.Optional.empty());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AskLocalityHandler(ConversationSessionRepository conversationSessionRepository, LocalityRepository localityRepository, PricingAndScheduleService pricingAndScheduleService, MessagingPort messaging,
            java.util.Optional<com.lunaris.ansenuza.service.interurban.InterurbanCatalog> interurban) {
        this.conversationSessionRepository = conversationSessionRepository;
        this.localityRepository = localityRepository;
        this.pricingAndScheduleService = pricingAndScheduleService;
        this.messaging = messaging;
        this.interurban = interurban.orElse(null);
    }

    @Override
    public String step() {
        return "ASK_LOCALITY";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        handle(session, message, true);
    }

    void handleConventional(ConversationSession session, IncomingMessage message) {
        handle(session, message, false);
    }

    private void handle(ConversationSession session, IncomingMessage message, boolean routeInterurban) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("0".equals(body)) {
            session.setCurrentStep("MAIN_MENU");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    " En qué te podemos ayudar hoy? Por favor, elegí una opción enviando el número:\n\n1️⃣ Reservar un viaje\n2️⃣ Ver Precios\n3️⃣ Operador");
            return;
        }
        try {
            int option = Integer.parseInt(body);
            List<Locality> localities = localityRepository.findAllWithActiveFare().stream()
                .filter(locality -> !BotRoute.fromCordoba(locality.getName()))
                .toList();
        if (interurban != null) {
            var available = new java.util.ArrayList<>(localities);
            for (String origin : interurban.origins()) {
                if (available.stream().noneMatch(l -> l.getName().equalsIgnoreCase(origin))) {
                    available.add(Locality.builder().name(origin).build());
                }
            }
            localities = List.copyOf(available);
        }


            if (option == localities.size() + 1) {
                session.setPickupLocality("Córdoba");
                // La dirección concreta se solicita en el paso común ASK_ADDRESS_TEXT.
                session.setPickupAddress(null);
                session.setDestination(null);
                session.setCurrentStep("ASK_TOWN_DESTINATION");
                conversationSessionRepository.saveAndFlush(session);
                StringBuilder menu = new StringBuilder("🎯 *¿A qué localidad viajás desde Córdoba?*\n\n");
                List<Locality> destinations = BotRoute.destinations(localityRepository);
                for (int i = 0; i < destinations.size(); i++) {
                    menu.append(i + 1).append(") ").append(destinations.get(i).getName()).append("\n");
                }
                messaging.sendText(phoneNumber, menu.append("\nRespondé con el número del destino.").toString());
                return;
            }

            if (option < 1 || option > localities.size()) {
                message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
                messaging.sendText(phoneNumber,
                        "❌ Selección inválida. Ingresá un número de la lista o *0* para volver.");
                return;
            }

            Locality selected = localities.get(option - 1);
            var destinations = routeInterurban && interurban != null
                    ? interurban.destinations(selected.getName()) : List.<com.lunaris.ansenuza.service.interurban.InterurbanCatalog.Destination>of();
            if (!destinations.isEmpty()) {
                session.setPickupLocality(selected.getName());
                session.setDestination(null);
                session.setPickupAddress(null);
                session.setCurrentStep("ASK_INTERURBAN_DESTINATION");
                conversationSessionRepository.saveAndFlush(session);
                StringBuilder menu = new StringBuilder("🎯 *¿A dónde viajás?*\n\n");
                for (var destination : destinations) {
                    menu.append("i_").append(destination.stop()).append(") ").append(destination.name()).append("\n");
                }
                menu.append("c) Córdoba / Aeropuerto\n\nRespondé con el código del destino (por ejemplo i_3) o 0 para volver.");
                messaging.sendText(phoneNumber, menu.toString());
                return;
            }
            BigDecimal baseFare;

            try {
                baseFare = pricingAndScheduleService.calculateTripPrice(selected.getName(), true, 1);
            } catch (IllegalArgumentException ex) {
                message.telemetry().emit(FLOW_BLOCKED, step(), NO_FARE);
                log.warn("Falta tarifa en base para la localidad seleccionada: {}",
                        selected.getName());
                messaging.sendText(phoneNumber,
                        "⚠️ Lo sentimos, actualmente *no hay tarifa para esa ciudad* de forma automatizada.\n\nPor favor, ingresá *0* para volver o respondé *Hola* para coordinar con un operador.");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }

            session.setPickupLocality(selected.getName());
            session.setCurrentStep("ASK_MARKETING_CONFIRMATION");
            conversationSessionRepository.saveAndFlush(session);

            String primerHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(selected.getName(), "03:00");
            String segundoHorario = pricingAndScheduleService
                    .calculateEstimatedPickupTime(selected.getName(), "08:00");

            int lugaresDisponibles = new java.util.Random().nextInt(4) + 1;

            String text = """
                    💰 *Tarifa base para %s:*
                    El valor de referencia (Ida y Vuelta) es de *$%,.0f*.

                    ⏱️ *Horarios de paso por tu localidad:*
                    • Primer horario: *%s*
                    • Segundo horario: *%s*

                    🚨 *¡ATENCIÓN!:* Para viajar en las próximas unidades solo nos quedan *%d lugares disponibles* en la flota compartida.

                    ¿Deseás realizar tu reserva ahora mismo?
                    """
                    .formatted(selected.getName(), baseFare, primerHorario, segundoHorario,
                            lugaresDisponibles);

            message.telemetry().sendButtons(messaging, phoneNumber, "LUNARIS - Cotización", text,
                    List.of(new Button("yes_reserve", "Reservar ✅"),
                            new Button("no_cancel", "En otro momento ❌")), PRICE_SENT, session.getCurrentStep());
            return;
        } catch (NumberFormatException e) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "⚠️ Por favor, respondé únicamente con el número correlativo de tu localidad o *0* para volver.");
            return;
        } catch (Exception e) {
            message.telemetry().emit(FLOW_BLOCKED, step(), PROCESSING_FAILED);
            log.error("Error en ASK_LOCALITY: ", e);
            return;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskNameHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_NAME: captura el nombre del pasajero titular (cuando no estaba registrado). */
@Component
@RequiredArgsConstructor
public class AskNameHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_NAME";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        session.setPassengerName(message.body().trim());
        session.setCurrentStep("ASK_COMPANIONS_COUNT");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(phoneNumber,
                "🔢 *Escribí cuántas personas viajan con vos, o 0 si estás solo (0)*");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskPromotionCodeHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AskPromotionCodeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PromotionService promotionService;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;
    private final WaitingListCapacityGuard capacityGuard;

    @Override
    public String step() {
        return "ASK_PROMOTION_CODE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim();
        if ("SIN PROMO".equalsIgnoreCase(response) || "NO".equalsIgnoreCase(response)) {
            session.setPromotionCode(null);
            session.setPromotionDiscountPercentage(null);
        } else if (response.matches("\\d{4}")) {
            try {
                Promotion promotion = promotionService.requireAvailable(response, session.getPhoneNumber());
                session.setPromotionCode(promotion.getCode());
                session.setPromotionDiscountPercentage(promotion.getDiscountPercentage());
            } catch (IllegalArgumentException exception) {
                message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
                messaging.sendText(session.getPhoneNumber(), "❌ " + exception.getMessage()
                        + ". Ingresá otro código de 4 dígitos o escribí *SIN PROMO*.");
                return;
            }
        } else {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(session.getPhoneNumber(), "Ingresá un código promocional válido de 4 dígitos o escribí *SIN PROMO*.");
            return;
        }

        session.setCurrentStep("ASK_CONFIRMATION");
        conversationSessionRepository.saveAndFlush(session);
        if (capacityGuard.offerWaitingListWhenFull(session)) {
            return;
        }
        presenter.sendReservationSummaryWithButtons(session.getPhoneNumber(), session, message.telemetry());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskReturnDateHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.FechaParser; // Importamos tu parseador flexible
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_RETURN_DATE: valida la fecha de regreso fijada de forma flexible y avanza a la facturación. */
@Component
@RequiredArgsConstructor
public class AskReturnDateHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_RETURN_DATE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();

        // Parseamos usando la lógica flexible que acepta año corto y ceros omitidos
        Optional<LocalDate> fechaParseada = FechaParser.parsear(message.body());

        if (fechaParseada.isEmpty()) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "❌ *Formato erróneo.* Por favor, indicá la fecha de tu regreso "
                            + "(por ejemplo: 12/08/2026):");
            return;
        }

        LocalDate returnDate = fechaParseada.get();

        // Mantenemos la validación temporal de tu regla de negocio
        if (returnDate.isBefore(session.getTravelDate())) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber,
                    "❌ El regreso no puede ser anterior al viaje de ida.");
            return;
        }

        session.setReturnDate(returnDate);
        message.telemetry().emit(DATE_SELECTED, step());
        session.setCurrentStep("ASK_DNI_REQUIRED");
        conversationSessionRepository.saveAndFlush(session);
        
        messaging.sendText(phoneNumber,
                "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskReturnDateTypeHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_RETURN_DATE_TYPE: el pasajero elige entre fijar fecha de regreso o dejar la vuelta abierta. */
@Component
@RequiredArgsConstructor
public class AskReturnDateTypeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_RETURN_DATE_TYPE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("return_same_day".equals(body)) {
            session.setReturnDate(session.getTravelDate());
            message.telemetry().emit(DATE_SELECTED, step());
            advanceToBilling(session, phoneNumber);
            return;
        }
        if ("return_today".equals(body) || "hoy".equals(body)
                || "return_tomorrow".equals(body) || "mañana".equals(body)) {
            boolean tomorrow = "return_tomorrow".equals(body) || "mañana".equals(body);
            LocalDate selected = com.lunaris.ansenuza.shared.ArgentinaTime.today()
                    .plusDays(tomorrow ? 1 : 0);
            if (session.getTravelDate() != null && selected.isBefore(session.getTravelDate())) {
                message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
                messaging.sendText(phoneNumber,
                        "❌ La fecha de regreso no puede ser anterior al viaje de ida. Elegí otra opción:");
                return;
            }
            session.setReturnDate(selected);
            message.telemetry().emit(DATE_SELECTED, step());
            advanceToBilling(session, phoneNumber);
            return;
        }
        if ("return_choose_date".equals(body) || "return_fixed".equals(body)
                || "otra fecha".equals(body) || "otra_fecha".equals(body)) {
            session.setCurrentStep("ASK_RETURN_DATE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "✍️ *Por favor, ingresá la fecha deseada* (ejemplo: 12/08 o 12 de agosto).\n\n"
                            + (BotRoute.fromCordoba(session.getPickupLocality())
                                    ? "El horario de regreso desde el pueblo se coordinará con un operador."
                                    : "Ventanas desde Córdoba: 14:00 a 15:00 hs o 17:30 a 18:00 hs."));
            return;
        }
        if ("return_open".equals(body)) {
            session.setReturnDate(null);
            message.telemetry().emit(DATE_SELECTED, step());
            advanceToBilling(session, phoneNumber);
            return;
        }
    }

    private void advanceToBilling(ConversationSession session, String phoneNumber) {
        session.setCurrentStep("ASK_DNI_REQUIRED");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(phoneNumber,
                "🧾 *Para emitir la facturación fiscal obligatoria:*\n\nIngresá tu número de DNI o CUIT (solo números):");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskTownDestinationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.util.List;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;

@Component
@RequiredArgsConstructor
public class AskTownDestinationHandler implements ConversationStepHandler {
    private final LocalityRepository localities;
    private final ConversationSessionRepository sessions;
    private final PricingAndScheduleService pricing;
    private final MessagingPort messaging;

    @Override
    public String step() { return "ASK_TOWN_DESTINATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        List<Locality> destinations = BotRoute.destinations(localities);
        int index;
        try {
            index = Integer.parseInt(message.body() == null ? "" : message.body().trim()) - 1;
        } catch (NumberFormatException exception) {
            index = -1;
        }
        if (index < 0 || index >= destinations.size()) {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(session.getPhoneNumber(), "Ingresá un número válido de la lista de destinos.");
            return;
        }
        session.setDestination(destinations.get(index).getName());
        session.setCurrentStep("ASK_MARKETING_CONFIRMATION");
        sessions.saveAndFlush(session);
        java.math.BigDecimal fare;
        try {
            fare = pricing.calculateTripPrice(session.getDestination(), true, 1);
        } catch (IllegalArgumentException exception) {
            message.telemetry().emit(FLOW_BLOCKED, step(), NO_FARE);
            throw exception;
        }
        message.telemetry().emit(ROUTE_SELECTED, step());
        message.telemetry().sendButtons(messaging, session.getPhoneNumber(), "LUNARIS - Cotización",
                "💰 Tarifa de referencia (ida y vuelta) a " + session.getDestination()
                        + ": $" + fare + ". ¿Deseás reservar?",
                List.of(new Button("yes_reserve", "Reservar ✅"), new Button("no_cancel", "En otro momento ❌")), PRICE_SENT, session.getCurrentStep());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AskTripTypeHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** ASK_TRIP_TYPE: define si el viaje es solo ida o ida y vuelta, y pide la fecha de ida. */
@Component
@RequiredArgsConstructor
public class AskTripTypeHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final SameDayBookingPolicy sameDayBookingPolicy;
    private final OperationControlService operationControlService;

    @Override
    public String step() {
        return "ASK_TRIP_TYPE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("trip_ida".equals(body)) {
            session.setRoundTrip(false);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            sendDateOptions(session, "📅 *¿Qué día es el viaje de ida?*");
        } else if ("trip_completo".equals(body)) {
            session.setRoundTrip(true);
            session.setCurrentStep("ASK_DATE");
            conversationSessionRepository.saveAndFlush(session);
            sendDateOptions(session,
                    "📅 *Perfecto, ida y vuelta.* ¿Qué día es el viaje de ida?");
        }
    }

    private void sendDateOptions(ConversationSession session, String prompt) {
        String phoneNumber = session.getPhoneNumber();
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        DateTimeFormatter payloadFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Button> options = new ArrayList<>();
        if (!sameDayBookingPolicy.isTodayClosed(session.getScheduleBlock())) {
            options.add(new Button(today.format(payloadFormat),
                    "Hoy (" + today.format(payloadFormat) + ")"));
        }
        LocalDate candidate = today.plusDays(1);
        if (!BotRoute.fromCordoba(session.getPickupLocality())
                && operationControlService.isPastCutoffTime()) {
            candidate = candidate.plusDays(1);
        }
        if (options.size() < 3) {
            String formattedDate = candidate.format(payloadFormat);
            String title = candidate.equals(today.plusDays(1))
                    ? "Mañana" : formattedDate;
            options.add(new Button(formattedDate, title));
        }
        if (options.size() < 3) {
            options.add(new Button("return_other_date", "Otra fecha"));
        }
        messaging.sendButtons(phoneNumber, "Fecha del viaje",
                prompt + "\n\nElegí una opción o seleccioná *Otra fecha* para escribirla.", options);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/AwaitingPaymentHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AwaitingPaymentHandler implements ConversationStepHandler {

    private final MessagingPort messaging;

    @Override
    public String step() {
        return "AWAITING_PAYMENT";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        messaging.sendText(session.getPhoneNumber(),
                "Estamos esperando tu comprobante. Enviá una foto para que podamos verificar el pago.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/CancelReservationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CancelReservationHandler implements ConversationStepHandler {

    private static final String DISPATCHED_MESSAGE =
            "⚠️ Tu viaje ya fue asignado al chofer y la ruta está en curso. "
                    + "Para cancelar o modificar tu reserva, por favor comunicate con un operador.";

    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "WAITING_CANCEL_CODE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String input = message.body().trim().toUpperCase();

        System.out.println("[BOT-CANCEL] Entrando al handler. Input recibido: " + input);

        if ("MENÚ".equals(input) || "MENU".equals(input)) {
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "Volviendo al menú principal...");
            return;
        }

        // Si viene del menú principal, mandamos a mostrar las opciones directamente
        if ("5".equals(input) || input.contains("CANCELAR")) {
            mostrarBotoneraOCodigo(session, phoneNumber);
            return;
        }

        Optional<Reservation> optRes = reservationRepository.findByReservationCode(input);

        if (optRes.isPresent()) {
            Reservation res = optRes.get();

            if (!res.getPassenger().getPhone().equals(phoneNumber)) {
                messaging.sendText(phoneNumber, "⚠️ El viaje seleccionado no corresponde a tu número por seguridad.");
                return;
            }

            if (res.getTravelStatus() == Reservation.TravelStatus.COMPLETED
                    || res.getTravelStatus() == Reservation.TravelStatus.REALIZED
                    || "COMPLETED".equalsIgnoreCase(res.getStatus())) {
                messaging.sendText(phoneNumber,
                        "⚠️ Ya te encontrás a bordo o tu viaje ya finalizó. No es posible cancelar este servicio.");
                return;
            }
            if (res.getDriver() != null
                    || res.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                messaging.sendText(phoneNumber, DISPATCHED_MESSAGE);
                return;
            }

            ReservationService.CancellationResult cancellationResult;

            try {
                cancellationResult = reservationService.cancelReservation(
                        res.getId(), "BOT_WHATSAPP");
            } catch (ReservationAlreadyCompletedException exception) {
                messaging.sendText(phoneNumber,
                        "⚠️ Ya te encontrás a bordo o tu viaje ya finalizó. No es posible cancelar este servicio.");
                return;
            } catch (IllegalStateException exception) {
                messaging.sendText(phoneNumber,
                        "⚠️ Tu viaje ya se encuentra en proceso o la ruta fue asignada al chofer, "
                                + "por lo que no es posible realizar la cancelación por este medio. "
                                + "Comunícate con un operador.");
                return;
            } catch (DomainValidationException exception) {
                String detail = exception.getMessage();
                messaging.sendText(phoneNumber,
                        detail != null && detail.startsWith("⚠️") ? detail : "⚠️ " + detail);
                return;
            }

            if (!cancellationResult.paymentVerified()) {
                messaging.sendText(phoneNumber,
                        "Tu reserva " + input + " fue cancelada. Dado que el comprobante de pago no estaba verificado por la administración, no se acreditó saldo a favor.");
            } else {
                messaging.sendText(phoneNumber,
                        "Tu reserva " + input + " fue cancelada. Se acreditaron $"
                                + cancellationResult.creditedAmount().toPlainString()
                                + " en tu saldo a favor.");
            }

            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
        } else {
            messaging.sendText(phoneNumber, "⚠️ Código inválido.");
            mostrarBotoneraOCodigo(session, phoneNumber);
        }
    }

    private void mostrarBotoneraOCodigo(ConversationSession session, String phoneNumber) {
        List<Reservation> todasLasReservas = reservationRepository.findByPassengerPhone(phoneNumber);
        List<Reservation> reservasActivas = todasLasReservas.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !"COMPLETED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getTravelStatus() != Reservation.TravelStatus.COMPLETED)
                .toList();

        if (reservasActivas.isEmpty()) {
            messaging.sendText(phoneNumber, "⚠️ No registrás ningún viaje activo o próximo para poder cancelar.\n\nEscribí *Menú* para volver.");
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            return;
        }

        List<Button> botonesAEnviar = new ArrayList<>();
        int limite = Math.min(reservasActivas.size(), 3);

        for (int i = 0; i < limite; i++) {
            Reservation r = reservasActivas.get(i);
            String codigo = r.getReservationCode().trim();
            
            // 🛡️ SOLUCIÓN AL ERROR 400 DE META: El label del botón jamás supera los 20 caracteres
            String label = codigo.length() > 20 ? codigo.substring(0, 20) : codigo;
            botonesAEnviar.add(new Button(codigo, label));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 *Gestión de Cancelaciones* 🚐\n\n");
        sb.append("Seleccioná cuál de tus próximos viajes deseas dar de baja automáticamente.\n");
        sb.append("Por favor, *escribí el código* del viaje a cancelar:\n\n");
        for (int i = 0; i < limite; i++) {
            Reservation r = reservasActivas.get(i);
            sb.append(String.format("🔹 *%s* (%s ➡️ %s)\n", r.getReservationCode(), r.getPickupLocality(), r.getDestination()));
        }
        sb.append("\nEscribí *Menú* para regresar.");

        try {
            messaging.sendButtons(
                    phoneNumber,
                    "Gestión de Cancelaciones 🚐",
                    "Seleccioná de la pantalla cuál de tus próximos viajes deseas dar de baja:",
                    botonesAEnviar
            );
        } catch (Exception e) {
            messaging.sendText(phoneNumber, sb.toString());
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmAddressHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;

/** CONFIRM_ADDRESS_BUTTONS: confirma el domicilio habitual detectado o pide uno nuevo. */
@Component
@RequiredArgsConstructor
public class ConfirmAddressHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "CONFIRM_ADDRESS_BUTTONS";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("addr_yes".equals(body)) {
            session.setCurrentStep("ASK_DESTINATION");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendButtons(phoneNumber, "Destino en Córdoba",
                    "🎯 *¿Hacia dónde viajás en Córdoba?*",
                    List.of(new Button("dest_aeropuerto", "Aeropuerto Cba ✈️"),
                            new Button("dest_capital", "Córdoba Capital 🏢")));
            return;
        }
        if ("addr_no".equals(body)) {
            session.setCurrentStep("ASK_ADDRESS_TEXT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.requestLocation(phoneNumber,
                    "🏠 Escribí la nueva calle y número para el retiro, o tocá el botón para compartir tu ubicación.");
            return;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmWaitingListBookingHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConfirmWaitingListBookingHandler implements ConversationStepHandler {

    private final WaitingListConversionService conversionService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "CONFIRMING_WAITING_LIST_BOOKING";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim().toLowerCase();
        if ("confirm_waiting_list".equals(response)) {
            try {
                var reservation = conversionService.beginPayment(session.getWaitingListEntryId());
                message.telemetry().emit(BOOKING_STARTED, step());
                if (reservation != null) message.telemetry().bookingCreated(
                        step(), reservation.getBookingGroupCode(), reservation.getId());
            } catch (SeatCapacityExceededException exception) {
                message.telemetry().emit(FLOW_BLOCKED, step(), NO_CAPACITY);
                messaging.sendText(session.getPhoneNumber(),
                        "Disculpá, en este momento el cupo sigue completo. "
                                + "Te avisaremos apenas se confirme un nuevo coche de refuerzo.");
                return;
            }
            session.setCurrentStep("AWAITING_PAYMENT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), """
                    ✅ *Reservamos temporalmente tu lugar.*

                    Para confirmarlo, realizá la transferencia y enviá por acá una foto del comprobante.

                    • *Titular:* Martín Fernando Manuel Cuestaz
                    • *Alias:* cuestazm.bna
                    • *CBU:* 01103739330037363119529
                    """);
            return;
        }
        if ("reject_waiting_list".equals(response)) {
            message.telemetry().emit(BOOKING_DECLINED, step(), USER_DECLINED);
            conversionService.cancel(session.getWaitingListEntryId());
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(),
                    "Entendido. Cancelamos tu lugar en la lista de espera.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ConfirmationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.conversation.WaitingListCapacityGuard;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.AirportTripDetector;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** ASK_CONFIRMATION: confirma o cancela la reserva; al confirmar persiste pasajero + reserva(s). */
@Component
@RequiredArgsConstructor
public class ConfirmationHandler implements ConversationStepHandler {

    private static final String CBU_BANNER_IMAGE_URL =
            "https://res.cloudinary.com/dgrwrcb5p/image/upload/CBU_MARTIN_nxpvk8.jpg";

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final PromotionService promotionService;
    private final ReservationService reservationService;
    private final MessagingPort messaging;
    private final WaitingListCapacityGuard capacityGuard;

    @Override
    public String step() {
        return "ASK_CONFIRMATION";
    }

    @Override
    @Transactional
    @Retryable(
            retryFor = { ObjectOptimisticLockingFailureException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100))
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("confirm_ok".equals(body)) {
            if (capacityGuard.offerWaitingListWhenFull(session)) {
                return;
            }

            int totalAsientos = session.getPassengerCount() != null ? session.getPassengerCount() : 1;

            Passenger passenger = passengerRepository.findByPhone(phoneNumber)
                    .map(existingPassenger -> passengerRepository.findById(existingPassenger.getId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "El pasajero ya no existe: " + existingPassenger.getId())))
                    .orElseGet(() -> createPassenger(session, phoneNumber));

            boolean airportTrip = AirportTripDetector.isAirportTrip(
                    session.getPickupLocality(), session.getDestination());
            BigDecimal price = airportTrip
                    ? BigDecimal.ZERO
                    : pricingAndScheduleService.calculateTripPrice(
                            BotRoute.fromCordoba(session.getPickupLocality())
                        ? session.getDestination() : session.getPickupLocality(), session.getRoundTrip(), totalAsientos);

            BigDecimal discountAmount = BigDecimal.ZERO;
            boolean freePromotion = false;
            Promotion appliedPromotion = null;
            if (!airportTrip && session.getPromotionCode() != null) {
                Promotion promotion;
                try {
                    promotion = promotionService.requireAvailable(session.getPromotionCode(), phoneNumber);
                } catch (IllegalArgumentException exception) {
                    session.setCurrentStep("ASK_PROMOTION_CODE");
                    conversationSessionRepository.saveAndFlush(session);
                    messaging.sendText(phoneNumber,
                            "❌ " + exception.getMessage() + ". Ingresá otro código o escribí *SIN PROMO*.");
                    return;
                }
                discountAmount = promotionService.calculateDiscount(price, promotion.getDiscountPercentage());
                price = price.subtract(discountAmount).max(BigDecimal.ZERO);
                freePromotion = promotion.getDiscountPercentage() == 100;
                appliedPromotion = promotion;
            }

            BigDecimal availableBalance = passenger.getCurrentBalance() == null
                    ? BigDecimal.ZERO
                    : passenger.getCurrentBalance().max(BigDecimal.ZERO);
            BigDecimal balanceUsed = availableBalance.min(price);
            BigDecimal transferAmount = price.subtract(balanceUsed).max(BigDecimal.ZERO);

            // 🕒 REPARACIÓN FASE 3: Tomamos el bloque dinámico real elegido por el cliente
            String baseHour = session.getScheduleBlock() != null ? session.getScheduleBlock() : "03:00 AM";
            
            String notes = baseHour;
            if (session.getReturnDate() == null && Boolean.TRUE.equals(session.getRoundTrip())) {
                notes += " (Abierta)";
            }

            String pickupLocality = canonicalizeCordoba(session.getPickupLocality());
            String destination = canonicalizeCordoba(session.getDestination());
            session.setPickupLocality(pickupLocality);
            session.setDestination(destination);

            Reservation nuevaReserva = Reservation.builder()
                    .passenger(passenger)
                    .travelDate(session.getTravelDate())
                    .returnDate(session.getReturnDate())
                    .pickupLocality(pickupLocality)
                    .routeDirection(BotRoute.fromCordoba(pickupLocality) ? "VUELTA" : null)
                    .pickupAddress(session.getPickupAddress())
                    .destination(destination)
                    .roundTrip(session.getRoundTrip())
                    .paymentVerified(freePromotion)
                    .requiresInvoice(!freePromotion)
                    .amount(price)
                    .discountAmount(discountAmount)
                    .promotionCode(session.getPromotionCode())
                    .promotionId(appliedPromotion != null ? appliedPromotion.getId() : null)
                    .promotionDiscountPercentage(
                            appliedPromotion != null ? appliedPromotion.getDiscountPercentage() : null)
                    .notes(notes)
                    .departureSchedule(baseHour)
                    .status(airportTrip ? "PENDING" : freePromotion ? "CONFIRMED" : "PENDING_PAYMENT")
                    .source(ReservationSource.WHATSAPP)
                    .passengerCount(totalAsientos)
                    .companionNames(session.getCompanionNames())
                    .build();

            List<Reservation> savedReservations = reservationService.saveReservationFlow(nuevaReserva);
            if (!savedReservations.isEmpty()) {
                Reservation saved = savedReservations.getFirst();
                message.telemetry().bookingCreated(step(), saved.getBookingGroupCode(), saved.getId());
            }
            boolean paymentConfirmed = savedReservations.stream()
                    .allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()));
            if (session.getPromotionCode() != null && paymentConfirmed) {
                promotionService.consume(session.getPromotionCode(), phoneNumber);
            }
            conversationSessionRepository.delete(session);

            if (airportTrip) {
                messaging.sendText(phoneNumber, """
                        ✈️ *Recibimos tu solicitud de viaje especial al aeropuerto.*

                        💵 *Precio: A cotizar / A convenir con operador.*

                        La solicitud quedó en espera de revisión. Un operador se contactará con vos para coordinar el horario y enviarte la cotización.
                        """);
                return;
            }

            if (Boolean.TRUE.equals(session.getRoundTrip())
                    && !BotRoute.fromCordoba(session.getPickupLocality())) {
                messaging.sendText(phoneNumber, "📌 Información importante sobre tu regreso: "
                        + "Las salidas de regreso desde Córdoba se realizan de 14:00 a 15:00 hs "
                        + "o de 17:30 a 18:00 hs. Podés responder a este mensaje indicándonos "
                        + "tu preferencia de horario.");
            }

            if (freePromotion) {
                messaging.sendText(phoneNumber, """
                        ✅ *¡Reserva confirmada con promoción 100% bonificada!*

                        🎟️ Tu código fue aplicado y el pasaje quedó emitido. No necesitás realizar ningún pago ni se emitirá factura fiscal por un importe de $0.
                        ¡Buen viaje con Lunaris! 🚐
                        """);
                return;
            }

            if (balanceUsed.signum() > 0 && transferAmount.signum() == 0 && paymentConfirmed) {
                BigDecimal remainingBalance = passenger.getCurrentBalance() == null
                        ? BigDecimal.ZERO
                        : passenger.getCurrentBalance();
                messaging.sendText(phoneNumber, """
                        ✅ *¡Excelente! Cubrimos el total del viaje con tu saldo a favor.*

                        💵 Saldo utilizado: $%s
                        💰 Saldo restante en tu cuenta: $%s

                        🚗 Tu reserva está confirmada. Un operador coordinará los detalles de tu retiro.
                        """.formatted(money(balanceUsed), money(remainingBalance)));
                return;
            }

            messaging.sendImage(phoneNumber, CBU_BANNER_IMAGE_URL, "Datos para la transferencia");
            String balanceMessage = balanceUsed.signum() > 0
                    ? "\n💰 *Aplicamos $%s de tu saldo a favor.*\n"
                            .formatted(money(balanceUsed))
                    : "";
            messaging.sendText(phoneNumber, """
                    ✅ *¡Tu traslado ha sido registrado con éxito!*
                    %s
                    💵 *Importe restante a transferir: $%s*

                    💳 *Datos bancarios para congelar la tarifa (Transferencia):*
                    • *Titular:* Martín Fernando Manuel Cuestaz
                    • *Alias:* cuestazm.bna
                    • *CBU:* 01103739330037363119529

                    📌 *Nota:* Una vez realizado el envío, *subí la captura o foto del comprobante por acá* para registrar tu pago de forma inmediata. ¡Buen viaje con Lunaris! 🚐
                    """.formatted(balanceMessage, money(transferAmount)));
            return;
        }

        if ("confirm_cancel".equals(body)) {
            message.telemetry().emit(BOOKING_DECLINED, step(), USER_DECLINED);
            session.setCurrentStep("FOLLOW_UP_RETENTION");
            conversationSessionRepository.saveAndFlush(session);

            messaging.sendText(phoneNumber, """
                    ❌ *Entendido, pausamos el trámite por acá.*

                    Tranqui, si tuviste un cambio de planes con los turnos médicos o el viaje:
                    ¿Querés que pasemos la ida para el día de mañana en el mismo horario o preferís dejar la consulta en espera?

                    _Escribinos si cambiás de idea y lo acomodamos al toque._
                    """);
            return;
        }
    }

    private String canonicalizeCordoba(String locality) {
        if (locality == null) {
            return null;
        }
        String normalized = java.text.Normalizer.normalize(locality.trim(),
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return "cordoba".equalsIgnoreCase(normalized) ? "Córdoba" : locality.trim();
    }

    private Passenger createPassenger(ConversationSession session, String phoneNumber) {
        String[] names = session.getPassengerName().trim().split("\\s+", 2);
        return passengerRepository.saveAndFlush(Passenger.builder()
                .firstName(names[0])
                .lastName(names.length > 1 ? names[1] : "")
                .phone(phoneNumber)
                .address(session.getPickupAddress())
                .locality(session.getPickupLocality())
                .cuil(session.getCuil())
                .build());
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/InterurbanConfirmationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class InterurbanConfirmationHandler implements ConversationStepHandler {
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;

    @Override
    public String step() { return "ASK_INTERURBAN_CONFIRMATION"; }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String input = message.body().trim();
        if ("interurban_reserve".equals(input)) {
            session.setBotPaused(true);
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "Enviá fecha, cantidad de pasajeros y direcciones de retiro y llegada. Un operador coordinará tu reserva interurbana.");
        } else if ("interurban_cancel".equals(input) || "0".equals(input)) {
            session.setCurrentStep("MAIN_MENU");
            sessions.saveAndFlush(session);
            messaging.sendText(session.getPhoneNumber(), "1) Reservar un viaje\n2) Ver precios\n3) Operador");
        } else {
            messaging.sendText(session.getPhoneNumber(), "Elegí Coordinar reserva o Volver al menú.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/MainMenuHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationPresenter;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MainMenuHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ConversationPresenter presenter;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService;
    private final CancelReservationHandler cancelReservationHandler; // Inyectamos el handler para invocarlo directo

    @Override
    public String step() {
        return "MAIN_MENU";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if ("1".equals(body)) {
            message.telemetry().emit(BOOKING_STARTED, step());
            session.setCurrentStep("ASK_LOCALITY");
            conversationSessionRepository.saveAndFlush(session);
            presenter.sendAllLocalitiesList(phoneNumber, "📍 *Excelente elección.* ");
            return;
        } else if ("2".equals(body)) {
            message.telemetry().emit(PRICE_REQUESTED, step());
            session.setCurrentStep("ASK_LOCALITY");
            conversationSessionRepository.saveAndFlush(session);
            String ganchoMarketing = "💰 *¡Viajá al mejor precio con Lunaris Ansenusa!*\\nContamos con las tarifas más competitivas del sector, descuentos especiales por tramos de ida y vuelta coordinados, y unidades premium climatizadas con total puntualidad.\\n\\n";
            presenter.sendAllLocalitiesList(phoneNumber, ganchoMarketing);
            return;
        } else if ("3".equals(body)) {
            if (!operationControlService.isHumanActionEnabled()) {
                messaging.sendText(phoneNumber, "🌙 *Atención Telefónica Finalizada.*\\n\\nNuestro equipo humano se encuentra descansando en este momento para iniciar las rutas temprano. 🚐💨\\n\\nTe sugerimos usar las opciones *1* o *2* para registrar tu viaje de forma **100% automática** en menos de un minuto. ¡El bot te guiará solo!");
                return;
            }
            message.telemetry().emit(HUMAN_HANDOFF, step(), OPERATOR);
            session.setBotPaused(true);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "Todos nuestros operadores están ocupados, en breve serás atendido.");
            return;
        } else if ("4".equals(body)) {
            session.setCurrentStep(WaitingForInquiryMessageHandler.STEP);
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, WaitingForInquiryMessageHandler.PROMPT);
            return;
        } else if ("6".equals(body) || body.contains("consultar")) {
            List<Reservation> viajesActivos = reservationRepository.findByPassengerPhone(phoneNumber).stream()
                    .filter(r -> !"CANCELLED".equals(r.getStatus()))
                    .toList();

            if (viajesActivos.isEmpty()) {
                messaging.sendText(phoneNumber, "No encontré ningún viaje activo o pendiente agendado con tu número de teléfono. 🤷‍♂️");
                session.setCurrentStep("START");
                conversationSessionRepository.saveAndFlush(session);
                return;
            }

            StringBuilder listado = new StringBuilder(
                    "📋 *TUS RESERVAS EN LUNARIS*\n"
                            + "_Ordenadas por fecha y horario_\n\n");
            LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

            for (int i = 0; i < viajesActivos.size(); i++) {
                Reservation r = viajesActivos.get(i);
                String fechaStr = fechaCentinela.equals(r.getTravelDate())
                        ? "🛑 VUELTA ABIERTA (pendiente de confirmación)"
                        : r.getTravelDate() != null
                                ? r.getTravelDate().format(dateFormatter)
                                : "A confirmar";
                String horario = r.getDepartureSchedule() == null
                                || r.getDepartureSchedule().isBlank()
                        ? "A confirmar" : r.getDepartureSchedule();
                listado.append(String.format("*%d. %s*%n", i + 1, fechaStr));
                listado.append(String.format("🕐 Horario: %s%n", horario));
                listado.append(String.format("📍 %s ➡️ %s%n",
                        r.getPickupLocality(), r.getDestination()));
                listado.append(String.format("🆔 Código: *%s*%n", r.getReservationCode()));
                listado.append(String.format("Estado: %s%n%n",
                        "CONFIRMED".equals(r.getStatus())
                                ? "✅ Confirmado" : "⏳ Pago pendiente"));
            }
            listado.append("Escribí *Menú* para volver a la pantalla de opciones.");
            messaging.sendText(phoneNumber, listado.toString());
            session.setCurrentStep("START");
            conversationSessionRepository.saveAndFlush(session);
            return;
        } else if ("5".equals(body) || body.contains("cancelar")) {
            // 💡 FLUJO OPTIMIZADO: Cambiamos de paso y llamamos en caliente al handler de cancelaciones de inmediato
            session.setCurrentStep("WAITING_CANCEL_CODE");
            conversationSessionRepository.saveAndFlush(session);
            cancelReservationHandler.handle(session, message);
            return;
        } else {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            messaging.sendText(phoneNumber, "⚠️ Opción inválida. Por favor, seleccioná una opción del menú (1 al 6) o escribí *Menú*.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/MarketingConfirmationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import java.util.List;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import lombok.RequiredArgsConstructor;

/** ASK_MARKETING_CONFIRMATION: el pasajero confirma (o no) iniciar la reserva tras la cotización. */
@Component
@RequiredArgsConstructor
public class MarketingConfirmationHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final ScheduleService scheduleService;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "ASK_MARKETING_CONFIRMATION";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if ("yes_reserve".equals(body)) {
            message.telemetry().emit(BOOKING_STARTED, step());
            List<String> schedules = scheduleService.getSchedulesForBot(
                    session.getPickupLocality(), session.getDestination(), session.getTravelDate(),
                    session.getPassengerCount() == null ? 1 : session.getPassengerCount());
            if (schedules.isEmpty()) {
                message.telemetry().emit(FLOW_BLOCKED, step(), NO_CAPACITY);
                message.telemetry().emit(HUMAN_HANDOFF, step(), NO_CAPACITY);
                session.setCurrentStep("WAITING_FOR_INQUIRY_MESSAGE");
                conversationSessionRepository.saveAndFlush(session);
                messaging.sendText(phoneNumber,
                        "No hay horarios con disponibilidad para esa cantidad de pasajeros y fecha. "
                        + "Contanos qué viaje necesitás y un operador revisará las alternativas.");
                return;
            }
            session.setCurrentStep("SELECT_SCHEDULE");
            conversationSessionRepository.saveAndFlush(session);

            String scheduleDetails = schedules.stream()
                    .map(schedule -> "• Pasa aprox *" + pricingAndScheduleService
                            .calculateEstimatedPickupTime(
                                    session.getPickupLocality(), schedule.substring(0, 5)) + "*")
                    .collect(java.util.stream.Collectors.joining("\n"));
            String infoTexto = "⏱️ *Horarios de retiro por tu domicilio:*\n"
                    + scheduleDetails + "\n\nSeleccioná el horario en el que preferís viajar:";

            messaging.sendButtons(phoneNumber, "Selección de Horario", infoTexto,
                    schedules.stream()
                            .map(schedule -> new Button(
                                    buttonPayload(schedule),
                                    BotRoute.fromCordoba(session.getPickupLocality())
                                            ? schedule : "03:00 AM".equals(schedule)
                                                    ? "Horario 1 🌙" : "Horario 2 ☀️"))
                            .toList());
            return;
        }
        if ("no_cancel".equals(body)) {
            message.telemetry().emit(BOOKING_DECLINED, step(), USER_DECLINED);
            conversationSessionRepository.delete(session);
            messaging.sendText(phoneNumber,
                    "Entendido. Si cambiás de opinión, escribinos 'Hola' cuando quieras.");
            return;
        }
    }

    private String buttonPayload(String schedule) {
        return "schedule_" + schedule.substring(0, 5).replace(':', '_');
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/ReturnWindowSelectionHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReturnWindowSelectionHandler implements ConversationStepHandler {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "RETURN_WINDOW_SELECTION";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String selection = message.body() == null ? "" : message.body().trim();
        String schedule = switch (selection) {
            case "1" -> "14:00";
            case "2" -> "17:30";
            default -> null;
        };
        if (schedule == null) {
            messaging.sendText(session.getPhoneNumber(),
                    "Respondé 1 para Turno Tarde o 2 para Turno Vespertino.");
            return;
        }
        Reservation reservation = reservationRepository
                .findByReservationCodeForUpdate(session.getReservationCode())
                .orElseThrow(() -> new IllegalStateException("No se encontró la reserva de regreso."));

        boolean openReturn = reservation.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN;
        if (reservation.getTravelDate() == null || openReturn) {
            reservation.setTravelStatus(
                    openReturn
                            ? Reservation.TravelStatus.OPEN_RETURN
                            : Reservation.TravelStatus.PENDING);
            reservationRepository.saveAndFlush(reservation);
            messaging.sendText(session.getPhoneNumber(),
                    "📅 La vuelta todavía está abierta. Primero debemos coordinar y confirmar la fecha; "
                            + "el horario se asignará cuando el viaje quede programado.");
            return;
        }

        reservation.setDepartureSchedule(schedule);
        reservation.setTravelStatus(Reservation.TravelStatus.CONFIRMED);
        reservationRepository.saveAndFlush(reservation);
        session.setCurrentStep("START");
        conversationSessionRepository.saveAndFlush(session);
        messaging.sendText(session.getPhoneNumber(),
                "✅ Preferencia registrada: " + ("14:00".equals(schedule)
                        ? "Turno Tarde (14:00 a 15:00 hs)."
                        : "Turno Vespertino (17:30 a 18:00 hs)."));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/SelectScheduleHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import java.util.Optional;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/** SELECT_SCHEDULE: el pasajero elige el horario; bifurca según ya exista o no como pasajero. */
@Component
@RequiredArgsConstructor
public class SelectScheduleHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final MessagingPort messaging;
    private final ScheduleService scheduleService;

    @Override
    public String step() {
        return "SELECT_SCHEDULE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if (BotRoute.fromCordoba(session.getPickupLocality())) {
            String selected = scheduleService.getSchedulesForBot(
                    session.getPickupLocality(), session.getDestination(), session.getTravelDate()).stream()
                    .filter(schedule -> ("schedule_" + schedule.substring(0, 5).replace(':', '_')).equals(body))
                    .findFirst().orElse(null);
            if (selected == null) {
                message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
                return;
            }
            session.setScheduleBlock(selected);
        } else if ("schedule_03_00".equals(body) || "time_0300".equals(body)) {
            session.setScheduleBlock("03:00 AM");
        } else if ("schedule_08_00".equals(body) || "time_0800".equals(body)) {
            session.setScheduleBlock("08:00 AM");
        } else {
            message.telemetry().emit(INPUT_REJECTED, step(), INVALID_INPUT);
            return;
        }

        if (session.getTravelDate() != null && !scheduleService.getSchedulesForBot(
                session.getPickupLocality(), session.getDestination(), session.getTravelDate(),
                session.getPassengerCount() == null ? 1 : session.getPassengerCount())
                .contains(session.getScheduleBlock())) {
            message.telemetry().emit(FLOW_BLOCKED, step(), NO_CAPACITY);
            message.telemetry().emit(HUMAN_HANDOFF, step(), NO_CAPACITY);
            session.setScheduleBlock(null);
            session.setCurrentStep("WAITING_FOR_INQUIRY_MESSAGE");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber, "Ese turno ya no está disponible. Contanos qué viaje necesitás "
                    + "y un operador revisará las alternativas.");
            return;
        }

        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);
        if (existingPassenger.isPresent()) {
            session.setPassengerName(existingPassenger.get().getFirstName() + " "
                    + existingPassenger.get().getLastName());
            session.setCurrentStep("ASK_COMPANIONS_COUNT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "🔢 *Escribí cuántas personas viajan con vos, o 0 si estás solo (0)*");
        } else {
            session.setCurrentStep("ASK_NAME");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido del pasajero titular.*\n\n_Ejemplo: Juan Pérez_");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/StartHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.service.OperationControlService; // 👈 NUEVO IMPORT
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/**
 * START / saludo: muestra el menú principal, notifica saldo corriente y transiciona a MAIN_MENU.
 */
@Component
@RequiredArgsConstructor
public class StartHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final MessagingPort messaging;
    private final OperationControlService operationControlService; // 👈 NUEVO SERVICIO INYECTADO

    @Override
    public String step() {
        return "START";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        session.setCurrentStep("MAIN_MENU");
        session.setLastInteraction(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        conversationSessionRepository.saveAndFlush(session);

        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);

        StringBuilder saludoBuilder = new StringBuilder();
        if (existingPassenger.isPresent()) {
            Passenger passenger = existingPassenger.get();
            saludoBuilder.append("¡Hola de nuevo, *").append(passenger.getFirstName())
                    .append("*! 👋\n");

            // 💰 CUENTA CORRIENTE: Si el pasajero tiene saldo a favor, se lo recordamos al inicio
            if (passenger.getCurrentBalance() != null
                    && passenger.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                saludoBuilder.append("\n💵 *Tenés un saldo a favor de $")
                        .append(String.format("%,.2f", passenger.getCurrentBalance()))
                        .append("* en tu cuenta. Se aplicará automáticamente como descuento en tu próxima reserva.\n");
            }
        } else {
            saludoBuilder.append("¡Bienvenido a Lunaris Ansenuza! 🚐\n");
        }

        saludoBuilder.append("\n¿En qué te podemos ayudar hoy? Por favor, elegí una opción enviando el número:\n");
        saludoBuilder.append("1️⃣ 🚐 *Reservar un viaje* (Flujo rápido)\n");
        saludoBuilder.append("2️⃣ 💸 *Ver precios y cotizar*\n");

        // 🕒 MUTACIÓN DINÁMICA: Solo muestra la opción 3 si la jornada humana está habilitada
        if (operationControlService.isHumanActionEnabled()) {
            saludoBuilder.append("3️⃣ 👨‍💼 *Hablar con un operador* (Soporte humano)\n");
        }

        saludoBuilder.append("4️⃣ 💬 *Deja tu consulta / Viaje Especial*\n");
        saludoBuilder.append("5️⃣ ❌ *Cancelar un viaje*\n");
        saludoBuilder.append("6️⃣ 📋 *Consultar mis reservas*\n");

        messaging.sendText(phoneNumber, saludoBuilder.toString());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/WaitingForInquiryMessageHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.*;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitingForInquiryMessageHandler implements ConversationStepHandler {
    public static final String STEP = "WAITING_FOR_INQUIRY_MESSAGE";
    public static final String PROMPT = "Por favor, escribe tu consulta o detalle del viaje especial (fecha, origen, destino, cantidad de pasajeros). Un operador la revisará a la brevedad.";
    public static final String CONFIRMATION = "¡Gracias! Tu consulta ha sido registrada. Nos pondremos en contacto contigo a la brevedad.";
    private final InquiryService inquiries;
    private final ConversationSessionRepository sessions;
    private final MessagingPort messaging;

    @Override
    public String step() { return STEP; }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        if (message.type() != IncomingMessage.MessageType.TEXT
                || message.body() == null || message.body().isBlank()) {
            messaging.sendText(session.getPhoneNumber(), PROMPT);
            return;
        }
        inquiries.register(session.getPhoneNumber(), session.getPassengerName(), message.body());
        session.setCurrentStep("START");
        sessions.saveAndFlush(session);
        messaging.sendText(session.getPhoneNumber(), CONFIRMATION);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/conversation/steps/WaitingListConfirmationHandler.java`

```java
package com.lunaris.ansenuza.application.conversation.steps;

import static com.lunaris.ansenuza.application.telemetry.ChatbotEventType.*;
import static com.lunaris.ansenuza.application.telemetry.ChatbotReason.*;

import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WaitingListConfirmationHandler implements ConversationStepHandler {

    private final WaitingListService waitingListService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Override
    public String step() {
        return "WAITING_LIST_CONFIRMATION";
    }

    @Override
    @Transactional
    public void handle(ConversationSession session, IncomingMessage message) {
        String response = message.body().trim().toLowerCase();
        if ("waiting_list_yes".equals(response)) {
            waitingListService.join(session);
            message.telemetry().emit(WAITLISTED, step(), NO_CAPACITY);
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(), """
                    ✅ *Te sumamos a la LISTA DE ESPERA.*

                    Te contactaremos por este número si se libera un lugar para el viaje solicitado.
                    """);
            return;
        }
        if ("waiting_list_no".equals(response)) {
            message.telemetry().emit(BOOKING_DECLINED, step(), USER_DECLINED);
            conversationSessionRepository.delete(session);
            messaging.sendText(session.getPhoneNumber(),
                    "Entendido. No te agregamos a la lista de espera. Escribí *Hola* cuando quieras consultar otra fecha.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/BankEmailProcessingResult.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public sealed interface BankEmailProcessingResult {
    record Detected(UUID reservationId, boolean autoConfirmed) implements BankEmailProcessingResult {}
    record Duplicate(String transactionId) implements BankEmailProcessingResult {}
    record ReservationNotFound(String reservationCode) implements BankEmailProcessingResult {}
    record AmountMismatch(BigDecimal expected, BigDecimal received) implements BankEmailProcessingResult {}
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/BankPaymentReservationPort.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.util.Optional;

public interface BankPaymentReservationPort {
    Optional<ReservationPaymentCandidate> findByReservationCode(String reservationCode);

    void confirm(String reservationCode);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/BankTransferNotification.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record BankTransferNotification(
        String source,
        String externalNotificationId,
        String transactionId,
        String reservationCode,
        BigDecimal amount,
        String payerName,
        Instant receivedAt) {

    public BankTransferNotification {
        source = requireText(source, "source");
        externalNotificationId = requireText(externalNotificationId, "externalNotificationId");
        transactionId = requireText(transactionId, "transactionId");
        reservationCode = requireText(reservationCode, "reservationCode").toUpperCase();
        amount = Objects.requireNonNull(amount, "amount");
        payerName = requireText(payerName, "payerName");
        receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/PaymentAuditOutboxPort.java`

```java
package com.lunaris.ansenuza.application.payment;

public interface PaymentAuditOutboxPort {
    void appendAudit(PaymentDetectedAuditRecord record);

    void appendConfirmed(PaymentConfirmedEvent event);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/PaymentConfirmedEvent.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        String transactionId,
        UUID reservationId,
        BigDecimal amount,
        Instant occurredAt) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/PaymentDetectedAuditRecord.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentDetectedAuditRecord(
        String eventType,
        String transactionId,
        String reservationCode,
        UUID reservationId,
        BigDecimal receivedAmount,
        BigDecimal expectedAmount,
        String payerName,
        Instant occurredAt) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/ProcessBankEmailService.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessBankEmailService implements ProcessBankEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessBankEmailService.class);

    private final ProcessedTransactionLedgerPort ledger;
    private final BankPaymentReservationPort reservations;
    private final PaymentAuditOutboxPort outbox;
    private final boolean autoConfirmEnabled;

    public ProcessBankEmailService(
            ProcessedTransactionLedgerPort ledger,
            BankPaymentReservationPort reservations,
            PaymentAuditOutboxPort outbox,
            @Value("${app.payment.auto-confirm-enabled:false}") boolean autoConfirmEnabled) {
        this.ledger = ledger;
        this.reservations = reservations;
        this.outbox = outbox;
        this.autoConfirmEnabled = autoConfirmEnabled;
    }

    @Override
    @Transactional
    public BankEmailProcessingResult process(BankTransferNotification notification) {
        if (!ledger.claim(notification)) {
            log.info("Ignoring duplicate payment transaction {}", notification.transactionId());
            return new BankEmailProcessingResult.Duplicate(notification.transactionId());
        }

        var candidate = reservations.findByReservationCode(notification.reservationCode());
        if (candidate.isEmpty()) {
            recordAudit(notification, null, null, "RESERVATION_NOT_FOUND");
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "RESERVATION_NOT_FOUND", null, null, "Reservation code was not found");
            return new BankEmailProcessingResult.ReservationNotFound(notification.reservationCode());
        }

        var paymentCandidate = candidate.get();
        if (paymentCandidate.expectedTotal().compareTo(notification.amount()) != 0) {
            recordAudit(notification, paymentCandidate, paymentCandidate.expectedTotal(), "AMOUNT_MISMATCH");
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "AMOUNT_MISMATCH", paymentCandidate.reservationId(),
                    paymentCandidate.expectedTotal(), "Transferred amount differs from expected total");
            return new BankEmailProcessingResult.AmountMismatch(
                    paymentCandidate.expectedTotal(), notification.amount());
        }

        recordAudit(notification, paymentCandidate, paymentCandidate.expectedTotal(), "PAYMENT_DETECTED");
        if (!autoConfirmEnabled) {
            ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                    "AUDIT_MATCHED", paymentCandidate.reservationId(),
                    paymentCandidate.expectedTotal(), "Audit mode: reservation was not modified");
            log.info("AUDIT payment match transaction={} reservation={} amount={}",
                    notification.transactionId(), notification.reservationCode(), notification.amount());
            return new BankEmailProcessingResult.Detected(paymentCandidate.reservationId(), false);
        }

        reservations.confirm(notification.reservationCode());
        Instant occurredAt = Instant.now();
        outbox.appendConfirmed(new PaymentConfirmedEvent(
                notification.transactionId(), paymentCandidate.reservationId(),
                notification.amount(), occurredAt));
        ledger.recordOutcome(notification.source(), notification.externalNotificationId(),
                "AUTO_CONFIRMED", paymentCandidate.reservationId(),
                paymentCandidate.expectedTotal(), "Reservation automatically confirmed");
        return new BankEmailProcessingResult.Detected(paymentCandidate.reservationId(), true);
    }

    private void recordAudit(
            BankTransferNotification notification,
            ReservationPaymentCandidate candidate,
            java.math.BigDecimal expectedAmount,
            String eventType) {
        outbox.appendAudit(new PaymentDetectedAuditRecord(
                eventType,
                notification.transactionId(),
                notification.reservationCode(),
                candidate == null ? null : candidate.reservationId(),
                notification.amount(),
                expectedAmount,
                notification.payerName(),
                notification.receivedAt()));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/ProcessBankEmailUseCase.java`

```java
package com.lunaris.ansenuza.application.payment;

public interface ProcessBankEmailUseCase {
    BankEmailProcessingResult process(BankTransferNotification notification);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/ProcessedTransactionLedgerPort.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProcessedTransactionLedgerPort {
    boolean claim(BankTransferNotification notification);

    void recordOutcome(
            String source,
            String externalNotificationId,
            String status,
            UUID reservationId,
            BigDecimal expectedAmount,
            String detail);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/payment/ReservationPaymentCandidate.java`

```java
package com.lunaris.ansenuza.application.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationPaymentCandidate(UUID reservationId, BigDecimal expectedTotal) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/WhatsAppWebhookInboxService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.infrastructure.persistence.repository.WhatsAppWebhookInboxRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WhatsAppWebhookInboxService {

    private final WhatsAppWebhookInboxRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(String messageId) {
        return messageId != null && !messageId.isBlank()
                && repository.claim(messageId, Instant.now()) == 1;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/WhatsAppConversationWindowService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhatsAppConversationWindowService {

    private static final long WINDOW_HOURS = 24;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> expirationFor(String phoneNumber) {
        return chatMessageRepository
                .findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc(phoneNumber)
                .map(message -> message.getTimestamp().plusHours(WINDOW_HOURS));
    }

    @Transactional(readOnly = true)
    public boolean isActive(String phoneNumber) {
        return expirationFor(phoneNumber)
                .map(expiration -> com.lunaris.ansenuza.shared.ArgentinaTime.now()
                        .isBefore(expiration))
                .orElse(false);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/mail/MercadoPagoImapAdapterConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.adapter.mail;

import com.lunaris.ansenuza.application.payment.ProcessBankEmailUseCase;
import com.lunaris.ansenuza.infrastructure.adapter.parser.MercadoPagoEmailParser;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;

@Configuration
@EnableConfigurationProperties(MercadoPagoImapProperties.class)
@ConditionalOnProperty(prefix = "app.payment.imap", name = "enabled", havingValue = "true")
public class MercadoPagoImapAdapterConfig {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoImapAdapterConfig.class);
    private static final Set<String> TRUSTED_DOMAINS = Set.of(
            "mercadopago.com", "mercadolibre.com");
    private static final Set<String> TRUSTED_TEST_SENDERS = Set.of(
            "ignarios1@gmail.com");

    @Bean
    IntegrationFlow mercadoPagoImapFlow(
            MercadoPagoImapProperties properties,
            MercadoPagoEmailParser parser,
            ProcessBankEmailUseCase useCase) {
        validate(properties);
        ImapMailReceiver receiver = new ImapMailReceiver(imapUrl(properties));
        receiver.setShouldDeleteMessages(false);
        receiver.setShouldMarkMessagesAsRead(true);
        // Spring Integration 6.x names this setting autoCloseFolder. Keeping the
        // folder open is required because MimeMessage body parts are loaded lazily.
        receiver.setAutoCloseFolder(false);
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.imaps.ssl.enable", "true");
        mailProperties.setProperty("mail.imaps.connectiontimeout", "10000");
        mailProperties.setProperty("mail.imaps.timeout", "10000");
        receiver.setJavaMailProperties(mailProperties);

        return IntegrationFlow
                .from(Mail.imapInboundAdapter(receiver), endpoint -> endpoint.poller(
                        Pollers.fixedDelay(Duration.ofMillis(properties.pollDelay()))
                                .maxMessagesPerPoll(10)))
                .handle(MimeMessage.class, (message, headers) -> {
                    ingest(message, properties, parser, useCase);
                    return null;
                })
                .get();
    }

    private void ingest(
            MimeMessage message,
            MercadoPagoImapProperties properties,
            MercadoPagoEmailParser parser,
            ProcessBankEmailUseCase useCase) {
        try {
            String sender = sender(message);
            if (!isAllowedSender(sender, properties.testSenders())) {
                log.debug("Ignoring payment email from untrusted sender {}", sender);
                return;
            }
            String messageId = message.getMessageID();
            if (messageId == null || messageId.isBlank()) {
                log.warn("Ignoring payment email without Message-ID from {}", sender);
                return;
            }
            Instant receivedAt = message.getReceivedDate() == null
                    ? Instant.now() : message.getReceivedDate().toInstant();
            parser.parse(messageId, message.getSubject(), content(message), receivedAt)
                    .ifPresentOrElse(useCase::process,
                            () -> log.warn("Could not parse payment email {}", messageId));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not ingest Mercado Pago IMAP message", exception);
        }
    }

    boolean isAllowedSender(String sender, String configuredTestSenders) {
        String normalized = sender.toLowerCase(Locale.ROOT);
        int separator = normalized.lastIndexOf('@');
        String senderDomain = separator < 0 ? "" : normalized.substring(separator + 1);
        boolean trustedDomain = TRUSTED_DOMAINS.stream()
                .anyMatch(domain -> senderDomain.equals(domain)
                        || senderDomain.endsWith("." + domain));
        if (trustedDomain) {
            return true;
        }
        return TRUSTED_TEST_SENDERS.contains(normalized)
                || testSenders(configuredTestSenders).contains(normalized);
    }

    private Set<String> testSenders(String configured) {
        if (configured == null || configured.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configured.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String sender(Message message) throws Exception {
        Address[] from = message.getFrom();
        if (from == null || from.length == 0) {
            return "";
        }
        if (from[0] instanceof InternetAddress address) {
            return address.getAddress();
        }
        return from[0].toString();
    }

    String content(Part part) throws Exception {
        try {
            if (part.isMimeType("text/plain")) {
                return String.valueOf(part.getContent());
            }
            if (part.isMimeType("text/html")) {
                return String.valueOf(part.getContent()).replaceAll("<[^>]+>", " ");
            }
            Object rawContent = part.getContent();
            if (rawContent instanceof Multipart multipart) {
                StringBuilder text = new StringBuilder();
                for (int index = 0; index < multipart.getCount(); index++) {
                    BodyPart bodyPart = multipart.getBodyPart(index);
                    if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        text.append(content(bodyPart)).append('\n');
                    }
                }
                return text.toString();
            }
            return "";
        } catch (FolderClosedException exception) {
            log.warn("IMAP folder closed while eagerly reading message content; "
                    + "the unavailable body is treated as empty");
            return "";
        }
    }

    private String imapUrl(MercadoPagoImapProperties properties) {
        try {
            return new URI("imaps", properties.username() + ":" + properties.password(),
                    properties.host(), properties.port(), "/INBOX", null, null).toString();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid payment IMAP configuration", exception);
        }
    }

    private void validate(MercadoPagoImapProperties properties) {
        if (properties.username() == null || properties.username().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException(
                    "PAYMENT_IMAP_USERNAME and PAYMENT_IMAP_PASSWORD are required when IMAP is enabled");
        }
        if (properties.pollDelay() < 1000) {
            throw new IllegalArgumentException("IMAP poll delay must be at least 1000 ms");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/mail/MercadoPagoImapProperties.java`

```java
package com.lunaris.ansenuza.infrastructure.adapter.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.imap")
public record MercadoPagoImapProperties(
        boolean enabled,
        String host,
        int port,
        long pollDelay,
        String username,
        String password,
        String testSenders) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/adapter/parser/MercadoPagoEmailParser.java`

```java
package com.lunaris.ansenuza.infrastructure.adapter.parser;

import com.lunaris.ansenuza.application.payment.BankTransferNotification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoEmailParser {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern TRANSACTION_ID = Pattern.compile(
            "(?:n[uú]mero\\s+de\\s+operaci[oó]n|id\\s+de\\s+transacci[oó]n|"
                    + "operaci[oó]n|transaction\\s+id)\\s*[:#]?\\s*([A-Z0-9-]{5,120})", FLAGS);
    private static final Pattern AMOUNT = Pattern.compile(
            "(?:monto|importe|amount|recibiste)\\s*[:$]?\\s*(?:ARS\\s*)?\\$?\\s*"
                    + "([0-9][0-9.,]*[0-9]|[0-9])", FLAGS);
    private static final Pattern PAYER = Pattern.compile(
            "(?:pagador|payer|enviado\\s+por|recibiste\\s+(?:un\\s+)?pago\\s+de)"
                    + "\\s*:?\\s*([\\p{L}][\\p{L} .'’-]{1,178})", FLAGS);
    private static final Pattern RESERVATION_CODE = Pattern.compile(
            "(?:reserva|c[oó]digo\\s+de\\s+reserva|referencia)\\s*[:#]?\\s*"
                    + "([A-Z]{2,5}-[A-Z]{2,5}-[0-9]{3}(?:-IDA|-VUELTA)?)", FLAGS);

    public Optional<BankTransferNotification> parse(
            String externalMessageId,
            String subject,
            String body,
            Instant receivedAt) {
        String content = String.join("\n", nullSafe(subject), nullSafe(body));
        Optional<String> transactionId = capture(TRANSACTION_ID, content);
        Optional<String> rawAmount = capture(AMOUNT, content);
        Optional<String> payerName = capture(PAYER, content).map(this::firstLine);
        Optional<String> reservationCode = capture(RESERVATION_CODE, content);

        if (transactionId.isEmpty() || rawAmount.isEmpty()
                || payerName.isEmpty() || reservationCode.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new BankTransferNotification(
                    "MERCADO_PAGO_EMAIL",
                    externalMessageId,
                    transactionId.get().toUpperCase(Locale.ROOT),
                    reservationCode.get().toUpperCase(Locale.ROOT),
                    parseArgentineAmount(rawAmount.get()),
                    payerName.get(),
                    receivedAt));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> capture(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
    }

    private BigDecimal parseArgentineAmount(String value) {
        String normalized = value.replace(" ", "");
        int lastComma = normalized.lastIndexOf(',');
        int lastDot = normalized.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                normalized = normalized.replace(".", "").replace(',', '.');
            } else {
                normalized = normalized.replace(",", "");
            }
        } else if (lastComma >= 0) {
            int decimals = normalized.length() - lastComma - 1;
            normalized = decimals == 3
                    ? normalized.replace(",", "")
                    : normalized.replace(',', '.');
        } else if (lastDot >= 0) {
            int decimals = normalized.length() - lastDot - 1;
            if (decimals == 3) {
                normalized = normalized.replace(".", "");
            }
        }
        return new BigDecimal(normalized);
    }

    private String firstLine(String value) {
        return value.lines().findFirst().orElse(value).trim();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/chat/WebSocketLiveChatAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.chat;

import java.time.LocalDateTime;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.application.conversation.BotRoute;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Locality;

/**
 * Adaptador de salida que implementa {@link LiveChatPort}: persiste el mensaje entrante
 * del cliente y lo emite por WebSocket al tópico de la sala de chat del operador.
 */
@Component
public class WebSocketLiveChatAdapter implements LiveChatPort {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationSessionRepository sessionRepository;
    private final LocalityRepository localityRepository;
    private final org.springframework.context.ApplicationEventPublisher events;

    public WebSocketLiveChatAdapter(ChatMessageRepository chatMessageRepository,
            SimpMessagingTemplate messagingTemplate, ConversationSessionRepository sessionRepository,
            LocalityRepository localityRepository) {
        this(chatMessageRepository, messagingTemplate, sessionRepository, localityRepository, event -> {});
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WebSocketLiveChatAdapter(ChatMessageRepository chatMessageRepository,
            SimpMessagingTemplate messagingTemplate,
            ConversationSessionRepository sessionRepository,
            LocalityRepository localityRepository, org.springframework.context.ApplicationEventPublisher events) {
        this.events = events;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.sessionRepository = sessionRepository;
        this.localityRepository = localityRepository;
    }

    @Override
    public void conversationChanged() {
        messagingTemplate.convertAndSend("/topic/bot-monitor", java.util.Map.of("action", "REFRESH"));
    }

    @Override
    public void recordIncomingMessage(String phoneNumber, String text) {
        String readableText = readableText(phoneNumber, text);
        ChatMessage msgCliente = chatMessageRepository.saveAndFlush(ChatMessage.builder()
                .phoneNumber(phoneNumber)
                .messageText(readableText)
                .fromOperator(false)
                .timestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now())
                .build());

        messagingTemplate.convertAndSend("/topic/messages/" + phoneNumber, msgCliente);
        conversationChanged();
        events.publishEvent(new com.lunaris.ansenuza.domain.model.PassengerMessageReceived(phoneNumber));
    }

    String readableText(String phoneNumber, String text) {
        if (text == null) return null;
        return switch (text.trim().toLowerCase()) {
            case "no_cancel" -> "Mantener reserva (No cancelar)";
            case "confirm_cancel" -> "Confirmar cancelación";
            default -> mapLocalitySelection(phoneNumber, text);
        };
    }

    private String mapLocalitySelection(String phoneNumber, String text) {
        ConversationSession session = sessionRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if (session == null || !text.trim().matches("\\d+")
                || !("ASK_LOCALITY".equals(session.getCurrentStep())
                        || "ASK_TOWN_DESTINATION".equals(session.getCurrentStep()))) {
            return text;
        }
        int option;
        try { option = Integer.parseInt(text.trim()); } catch (NumberFormatException ex) { return text; }
        var options = "ASK_LOCALITY".equals(session.getCurrentStep())
                ? localityRepository.findAllWithActiveFare().stream()
                        .filter(locality -> !BotRoute.fromCordoba(locality.getName())).toList()
                : BotRoute.destinations(localityRepository);
        if ("ASK_LOCALITY".equals(session.getCurrentStep()) && option == options.size() + 1) {
            return option + " - Córdoba";
        }
        if (option < 1 || option > options.size()) return text;
        return option + " - " + options.get(option - 1).getName();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/CloudinaryConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    Cloudinary cloudinary(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName == null ? "" : cloudName.trim(),
                "api_key", apiKey == null ? "" : apiKey.trim(),
                "api_secret", apiSecret == null ? "" : apiSecret.trim(),
                "secure", true));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/WebSocketConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Quitamos el setAllowedOrigins y permitimos que la config global 
        // de Spring maneje los orígenes para evitar el error 500.
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryDriverDocumentStorageAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.Map;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
public class CloudinaryDriverDocumentStorageAdapter implements DriverDocumentStoragePort {

    private final Cloudinary cloudinary;
    private final LocalDriverDocumentStorageAdapter localStorage;
    private final Environment environment;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryDriverDocumentStorageAdapter(
            Cloudinary cloudinary,
            ObjectProvider<LocalDriverDocumentStorageAdapter> localStorage,
            Environment environment,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage.getIfAvailable();
        this.environment = environment;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @PostConstruct
    void validateProductionConfiguration() {
        if (isProduction() && !isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary debe estar configurado para documentos de choferes en producción.");
        }
    }

    @Override
    public String store(String documentType, MultipartFile file) {
        validate(documentType, file);
        if (isConfigured()) {
            try {
                Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "resource_type", "raw",
                        "access_mode", "public",
                        "folder", "driver-applications",
                        "public_id", documentType + "_" + UUID.randomUUID(),
                        "overwrite", false));
                Object secureUrl = result.get("secure_url");
                if (secureUrl == null || secureUrl.toString().isBlank()) {
                    throw new IllegalStateException("Cloudinary no devolvió una URL persistente.");
                }
                return secureUrl.toString();
            } catch (Exception exception) {
                throw new DomainValidationException(
                        "No se pudo persistir el documento " + documentType + " en Cloudinary.");
            }
        }
        if (isProduction() || localStorage == null) {
            throw new DomainValidationException(
                    "Cloudinary es obligatorio para almacenar documentos de choferes.");
        }
        return localStorage.store(documentType, file);
    }

    private void validate(String documentType, MultipartFile file) {
        if (documentType == null || documentType.isBlank() || file == null || file.isEmpty()) {
            throw new DomainValidationException("El documento solicitado es obligatorio.");
        }
    }

    private boolean isConfigured() {
        return hasValue(cloudName) && hasValue(apiKey) && hasValue(apiSecret);
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryInvoiceStorageService.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

/** Almacenamiento principal de facturas, con compatibilidad local si Cloudinary no está configurado. */
@Service
@Primary
@Slf4j
public class CloudinaryInvoiceStorageService implements InvoiceStoragePort {

    private static final String PDF_DOWNLOAD_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int PDF_CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int PDF_READ_TIMEOUT_MILLIS = 10_000;

    private final Cloudinary cloudinary;
    private final LocalInvoiceStorageService localStorage;
    private final Environment environment;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    @Autowired
    public CloudinaryInvoiceStorageService(
            Cloudinary cloudinary,
            @Qualifier("localInvoiceStorageService") ObjectProvider<LocalInvoiceStorageService> localStorage,
            Environment environment,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage.getIfAvailable();
        this.environment = environment;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    CloudinaryInvoiceStorageService(Cloudinary cloudinary, LocalInvoiceStorageService localStorage,
            String cloudName, String apiKey, String apiSecret) {
        this(cloudinary, localStorage, new org.springframework.core.env.StandardEnvironment(),
                cloudName, apiKey, apiSecret);
    }

    CloudinaryInvoiceStorageService(Cloudinary cloudinary, LocalInvoiceStorageService localStorage,
            Environment environment, String cloudName, String apiKey, String apiSecret) {
        this.cloudinary = cloudinary;
        this.localStorage = localStorage;
        this.environment = environment;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @PostConstruct
    void validateProductionConfiguration() {
        if (isProduction() && !isConfigured()) {
            throw new IllegalStateException(
                    "Cloudinary debe estar configurado para almacenar facturas en producción.");
        }
    }

    @Override
    public StoredInvoice store(byte[] content, String desiredFileName) {
        if (isConfigured()) {
            try {
                String publicId = ensurePdfExtension(desiredFileName);
                Map<?, ?> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                        "resource_type", "raw",
                        "access_mode", "public",
                        "type", "upload",
                        "folder", "facturas",
                        "public_id", publicId,
                        "overwrite", true));
                Object secureUrl = result.get("secure_url");
                if (secureUrl != null && !secureUrl.toString().isBlank()) {
                    String url = normalizePdfUrl(secureUrl.toString());
                    log.info("Factura {} guardada en Cloudinary.", desiredFileName);
                    return new StoredInvoice(url, url);
                }
                throw new IllegalStateException("Cloudinary no devolvió una URL persistente para la factura.");
            } catch (Exception exception) {
                throw new IllegalStateException(
                        "No se pudo persistir la factura en Cloudinary.", exception);
            }
        }
        if (isProduction() || localStorage == null) {
            throw new IllegalStateException(
                    "Cloudinary es obligatorio para almacenar facturas en este entorno.");
        }
        return localStorage.store(content, desiredFileName);
    }

    @Override
    public String resolveAbsolutePath(String pdfUrl) {
        if (pdfUrl != null && pdfUrl.startsWith("https://")) {
            return pdfUrl;
        }
        return localStorage.resolveAbsolutePath(pdfUrl);
    }

    @Override
    public byte[] load(String pdfUrl) {
        if (pdfUrl != null && pdfUrl.startsWith("https://")) {
            try {
                try {
                    return download(pdfUrl);
                } catch (CloudinaryDownloadException exception) {
                    if (!exception.isAuthenticationFailure() || !isConfigured()) {
                        throw exception;
                    }
                    log.info("Cloudinary rechazó la URL pública de la factura; se reintenta con descarga firmada.");
                    return download(createSignedDownloadUrl(pdfUrl));
                }
            } catch (IOException | IllegalArgumentException exception) {
                throw new IllegalStateException("No se pudo descargar el PDF desde Cloudinary.", exception);
            }
        }
        return localStorage.load(pdfUrl);
    }

    private byte[] download(String url) throws IOException {
        HttpURLConnection connection = openConnection(url);
        configurePdfConnection(connection);
        try {
            int status = connection.getResponseCode();
            if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new CloudinaryDownloadException(status);
            }
            try (java.io.InputStream input = connection.getInputStream()) {
                return input.readAllBytes();
            }
        } finally {
            connection.disconnect();
        }
    }

    HttpURLConnection openConnection(String url) throws IOException {
        return (HttpURLConnection) URI.create(url).toURL().openConnection();
    }

    static void configurePdfConnection(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", PDF_DOWNLOAD_USER_AGENT);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(PDF_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(PDF_READ_TIMEOUT_MILLIS);
    }

    private String createSignedDownloadUrl(String pdfUrl) {
        String[] segments = URI.create(pdfUrl).getPath().split("/");
        int rawIndex = Arrays.asList(segments).indexOf("raw");
        if (rawIndex < 0 || rawIndex + 2 >= segments.length) {
            throw new IllegalArgumentException("La URL no corresponde a un recurso raw de Cloudinary.");
        }
        String deliveryType = segments[rawIndex + 1];
        int publicIdStart = rawIndex + 2;
        if (segments[publicIdStart].startsWith("s--")) {
            publicIdStart++;
        }
        if (publicIdStart < segments.length && segments[publicIdStart].matches("v\\d+")) {
            publicIdStart++;
        }
        if (publicIdStart >= segments.length) {
            throw new IllegalArgumentException("La URL de Cloudinary no contiene un public_id.");
        }
        String publicId = String.join("/", Arrays.copyOfRange(segments, publicIdStart, segments.length));
        try {
            return cloudinary.privateDownload(publicId, null, Map.<String, Object>of(
                    "resource_type", "raw",
                    "type", deliveryType));
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo firmar la descarga de la factura.", exception);
        }
    }

    private static final class CloudinaryDownloadException extends IOException {
        private final int status;

        private CloudinaryDownloadException(int status) {
            super("Falló la descarga de Cloudinary, HTTP code: " + status);
            this.status = status;
        }

        private boolean isAuthenticationFailure() {
            return status == HttpURLConnection.HTTP_UNAUTHORIZED
                    || status == HttpURLConnection.HTTP_FORBIDDEN;
        }
    }

    private boolean isConfigured() {
        return hasValue(cloudName) && hasValue(apiKey) && hasValue(cloudSecret());
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private String cloudSecret() {
        return apiSecret;
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private static String ensurePdfExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "factura.pdf";
        }
        return fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? fileName
                : fileName + ".pdf";
    }

    private static String normalizePdfUrl(String url) {
        String normalized = url.replace("/image/upload/", "/raw/upload/");
        int queryStart = normalized.indexOf('?');
        String path = queryStart >= 0 ? normalized.substring(0, queryStart) : normalized;
        String query = queryStart >= 0 ? normalized.substring(queryStart) : "";
        return path.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                ? normalized
                : path + ".pdf" + query;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryNewsBannerStorageAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryNewsBannerStorageAdapter implements NewsBannerStoragePort {

    private final Cloudinary cloudinary;

    public CloudinaryNewsBannerStorageAdapter(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public String upload(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new DomainValidationException("La imagen del flyer es obligatoria.");
        }
        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new DomainValidationException("El flyer debe ser un archivo de imagen.");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap(
                    "folder", "novedades",
                    "public_id", "flyer_" + UUID.randomUUID(),
                    "resource_type", "image"));
            return (String) result.get("secure_url");
        } catch (Exception exception) {
            throw new DomainValidationException("No se pudo subir el flyer a Cloudinary.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/CloudinaryReceiptStorageAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;

@Service
public class CloudinaryReceiptStorageAdapter implements ReceiptStoragePort {

    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    // 🔐 Constructor limpio mapeando properties
    public CloudinaryReceiptStorageAdapter(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        
        this.restTemplate = new RestTemplate();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    // 🔥 IMPLEMENTACIÓN NUEVA: Sube el archivo físico del navegador web a Cloudinary
    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "null";
        }
        try {
            String uniqueFileName = "comprobante_manual_" + UUID.randomUUID();
            Map uploadParams = ObjectUtils.asMap(
                "folder", "comprobantes",
                "public_id", uniqueFileName,
                "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return (String) uploadResult.get("secure_url");
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("❌ Falló el almacenamiento en Cloudinary desde el formulario web", e);
            return "null";
        }
    }

    // 📥 MÉTODO ORIGINAL ADAPTADO: Descarga multimedia desde la API de Meta
    @Override
    public String downloadAndSaveReceipt(String mediaId) {
        try {
            String urlMetadata = "https://graph.facebook.com/v17.0/" + mediaId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    urlMetadata, HttpMethod.GET, entity, JsonNode.class);

            if (mediaResponse.getBody() == null || !mediaResponse.getBody().has("url")) {
                return null;
            }
            
            String whatsappDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    whatsappDownloadUrl, HttpMethod.GET, entity, byte[].class);

            byte[] imageBytes = imageResponse.getBody();
            if (imageBytes == null) {
                return null;
            }

            String uniqueFileName = "comprobante_" + UUID.randomUUID();
            Map uploadParams = ObjectUtils.asMap(
                "folder", "comprobantes",
                "public_id", uniqueFileName,
                "resource_type", "image"
            );

            Map uploadResult = cloudinary.uploader().upload(imageBytes, uploadParams);
            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("❌ Falló el proceso de almacenamiento en Cloudinary para el mediaId: " + mediaId, e);
            return null;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalDriverDocumentStorageAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!prod & !production")
public class LocalDriverDocumentStorageAdapter implements DriverDocumentStoragePort {

    private final Path storageDirectory;

    public LocalDriverDocumentStorageAdapter(
            @Value("${storage.driver-applications-dir:./data/driver-applications/}")
                    String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(String documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainValidationException(
                    "El archivo " + documentType + " es obligatorio.");
        }
        String extension = extension(file.getOriginalFilename());
        String fileName = documentType + "_" + UUID.randomUUID() + extension;
        Path destination = storageDirectory.resolve(fileName).normalize();
        if (!destination.startsWith(storageDirectory)) {
            throw new DomainValidationException("Nombre de archivo inválido.");
        }
        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(destination);
            return destination.toString();
        } catch (IOException exception) {
            throw new DomainValidationException(
                    "No se pudo almacenar el archivo " + documentType + ".");
        }
    }

    private String extension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        String name = Path.of(originalFilename).getFileName().toString();
        int separator = name.lastIndexOf('.');
        if (separator < 0 || separator == name.length() - 1) {
            return "";
        }
        String extension = name.substring(separator).toLowerCase();
        return extension.length() <= 10 ? extension : "";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalInvoiceStorageService.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador local que guarda los PDF de facturas en disco y los expone bajo /facturas/**.
 */
@Slf4j
@org.springframework.stereotype.Service("localInvoiceStorageService")
@Profile("!prod & !production")
public class LocalInvoiceStorageService implements InvoiceStoragePort {

    @Value("${storage.invoices-dir}")
    private String invoicesDir;

    @Override
    public StoredInvoice store(byte[] content, String desiredFileName) {
        try {
            File directory = new File(invoicesDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            Path destination = Paths.get(invoicesDir, desiredFileName);
            Files.write(destination, content);
            log.info("Factura guardada localmente en: {}", destination.toAbsolutePath());
            return new StoredInvoice("/facturas/" + desiredFileName, destination.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("Error al guardar el PDF de la factura: {}", desiredFileName, e);
            throw new RuntimeException("No se pudo guardar el PDF de la factura", e);
        }
    }

    @Override
    public String resolveAbsolutePath(String pdfUrl) {
        String fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);
        return Paths.get(invoicesDir, fileName).toAbsolutePath().toString();
    }

    @Override
    public byte[] load(String pdfUrl) {
        try {
            return java.nio.file.Files.readAllBytes(
                    java.nio.file.Path.of(resolveAbsolutePath(pdfUrl)));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("No se pudo leer el PDF de la factura.", exception);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/storage/LocalReceiptStorageService.java`

```java
package com.lunaris.ansenuza.infrastructure.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import lombok.extern.slf4j.Slf4j;

// 🛠️ DESACTIVADO: Comentamos @Service para que Spring Boot use únicamente el adaptador de Cloudinary
// @Service
@Slf4j
public class LocalReceiptStorageService implements ReceiptStoragePort {

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${storage.local-dir}")
    private String localDir;

    @Override
    public String downloadAndSaveReceipt(String mediaId) {
        try {
            // Asegurar que el directorio exista físicamente
            File directory = new File(localDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Paso A: Consultar metadatos a Meta para obtener la URL de descarga efímera
            String metaUrl = "https://graph.facebook.com/v20.0/" + mediaId;
            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    metaUrl, HttpMethod.GET, entity, JsonNode.class);
            
            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            // Paso B: Descargar los bytes reales del archivo de los servidores de Meta
            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            byte[] imageBytes = imageResponse.getBody();

            // Paso C: Guardar el archivo en el disco local
            String fileName = "comprobante_" + mediaId + ".jpg";
            // Ajustado para combinar las rutas de forma segura sin importar los separadores / o \
            Path destinationPath = Paths.get(localDir).resolve(fileName);
            Files.write(destinationPath, imageBytes);

            log.info("Comprobante guardado localmente en: {}", destinationPath.toAbsolutePath());

            // Devolvemos la ruta web relativa con la que Martín va a acceder desde el navegador
            return "/comprobantes/" + fileName;

        } catch (Exception e) {
            log.error("Error al descargar e impactar el archivo local de WhatsApp con ID: " + mediaId, e);
            return null;
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "null";
        }
        try {
            // Reemplazo del operador Elvis (?:) no soportado en Java por un operador ternario estándar
            String baseDir = (this.localDir != null) ? this.localDir : "/tmp/comprobantes/";
            
            String uniqueFileName = "comprobante_manual_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(baseDir).resolve(uniqueFileName);
            
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());
            
            return targetPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("❌ Falló el almacenamiento local desde el formulario web", e);
            return "null";
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ChatWebSocketController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDateTime;
import java.util.Map; // 👈 Agregado para leer el ID
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload; // 👈 Agregado para interceptar el
                                                                 // JSON
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 👈 Agregado para enviar la
                                                                 // alerta global
import org.springframework.stereotype.Controller;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository; // 👈 Agregado para el
                                                                             // monitor
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@AllArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageRepository chatMessageRepository;
    private final WhatsAppService whatsAppService;
    private final ConversationSessionRepository conversationSessionRepository; // 👈 Inyectado para
                                                                               // la Torre de
                                                                               // Control
    private final SimpMessagingTemplate messagingTemplate; // 👈 Inyectado para refrescar el monitor
    private final WhatsAppConversationWindowService conversationWindowService;
                                                           // en vivo

    @MessageMapping("/chat.send/{phoneNumber}")
    @SendTo("/topic/messages/{phoneNumber}")
    public ChatMessage sendMessage(@DestinationVariable String phoneNumber, ChatMessage message) {

        if (!conversationWindowService.isActive(phoneNumber)) {
            throw new IllegalStateException(
                    "La ventana de WhatsApp venció. Reenviá la plantilla contacto_pasajero.");
        }

        message.setPhoneNumber(phoneNumber);
        message.setTimestamp(com.lunaris.ansenuza.shared.ArgentinaTime.now());
        message.setFromOperator(true);

        ChatMessage savedMessage = chatMessageRepository.save(message);

        try {
            whatsAppService.sendMessage(phoneNumber, message.getMessageText());
            log.info("[WEB CHAT] Mensaje despachado a WhatsApp con éxito hacia el número: {}",
                    phoneNumber);
        } catch (Exception e) {
            log.error(
                    "[CRÍTICO] Error al intentar enviar el mensaje de operador a la API de WhatsApp para: {}",
                    phoneNumber, e);
        }

        return savedMessage;
    }

    // 🎯 EL CONTROLADOR CORREGIDO PASANDO EL ID A LONG:
    @MessageMapping("/bot.toggle")
    public void handleBotToggle(@Payload Map<String, String> payload) {
        String rawId = payload.get("id");

        try {
            // 🪙 Convertimos el String de JavaScript al Long que exige tu repositorio
            Long sessionId = Long.parseLong(rawId);

            conversationSessionRepository.findById(sessionId).ifPresent(session -> {
                session.setBotPaused(!session.isBotPaused());
                session.setManuallyPaused(session.isBotPaused());
                conversationSessionRepository.saveAndFlush(session);

                // 📢 Alerta global a la pantalla del monitor
                if (this.messagingTemplate != null) {
                    this.messagingTemplate.convertAndSend("/topic/system-alerts", "REFRESH");
                    log.info("[WebSocket] Alerta enviada a la pantalla para el ID sesión: {}",
                            sessionId);
                }
            });
        } catch (NumberFormatException e) {
            log.error("[Torre de Control] El ID recibido ('{}') no se pudo convertir a Long: ",
                    rawId, e);
        } catch (Exception e) {
            log.error("[Torre de Control] Error inesperado en el toggle del bot: ", e);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @GetMapping("/whatsapp/test")
    public String test() {

        whatsAppService.sendMessage(
                "543562553866",
                "Hola desde Lunaris 🚐");

        return "Mensaje enviado";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppSimulatorDevController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppServiceDevMock.SimulatorMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("dev")
@RequestMapping("/api/v1/dev/whatsapp-simulator")
public class WhatsAppSimulatorDevController {

    private final WhatsAppServiceDevMock whatsApp;
    private final ConversationOrchestrator orchestrator;
    private final ConversationSessionRepository sessions;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;

    public WhatsAppSimulatorDevController(WhatsAppServiceDevMock whatsApp,
            ConversationOrchestrator orchestrator, ConversationSessionRepository sessions,
            ProcessPaymentReceiptUseCase processPaymentReceiptUseCase) {
        this.whatsApp = whatsApp;
        this.orchestrator = orchestrator;
        this.sessions = sessions;
        this.processPaymentReceiptUseCase = processPaymentReceiptUseCase;
    }

    @GetMapping("/messages")
    public List<SimulatorMessage> messages(@RequestParam @NotBlank String phone) {
        return whatsApp.messagesFor(phone);
    }

    @PostMapping("/send-user-reply")
    public ResponseEntity<List<SimulatorMessage>> reply(@Valid @RequestBody UserReply request) {
        String phone = whatsApp.normalize(request.phone());
        IncomingMessage.MessageType type = request.resolvedType();
        String value = type == IncomingMessage.MessageType.INTERACTIVE
                ? request.payload() : request.text();
        String resourceUrl = request.mediaUrl();
        whatsApp.recordUserMessage(phone, type, value, request.payload(), resourceUrl);

        IncomingMessage incoming = new IncomingMessage(
                phone, type, value, request.isMedia() ? resourceUrl : null).asAnalyticsTest();
        orchestrator.process(incoming);
        if (incoming.isMediaWithResource()) {
            processPaymentReceiptUseCase.executeStoredReceipt(phone, resourceUrl);
        }
        return ResponseEntity.ok(whatsApp.messagesFor(phone));
    }

    @DeleteMapping("/reset-session")
    @Transactional
    public ResponseEntity<Void> reset(@RequestParam @NotBlank String phone) {
        String normalized = whatsApp.normalize(phone);
        sessions.findByPhoneNumber(normalized).ifPresent(sessions::delete);
        whatsApp.reset(normalized);
        return ResponseEntity.noContent().build();
    }

    public record UserReply(@NotBlank String phone, String text, String payload,
            String mediaUrl, String messageType) {

        public UserReply(String phone, String text, String payload) {
            this(phone, text, payload, null, null);
        }

        public boolean isButton() { return payload != null && !payload.isBlank(); }

        public boolean isMedia() {
            IncomingMessage.MessageType type = resolvedType();
            return type == IncomingMessage.MessageType.IMAGE
                    || type == IncomingMessage.MessageType.DOCUMENT;
        }

        public IncomingMessage.MessageType resolvedType() {
            if (messageType == null || messageType.isBlank()) {
                if (isButton()) return IncomingMessage.MessageType.INTERACTIVE;
                if (mediaUrl != null && !mediaUrl.isBlank()) return IncomingMessage.MessageType.IMAGE;
                return IncomingMessage.MessageType.TEXT;
            }
            try {
                return IncomingMessage.MessageType.valueOf(
                        messageType.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return IncomingMessage.MessageType.OTHER;
            }
        }

        @AssertTrue(message = "El contenido no coincide con el tipo de mensaje.")
        public boolean isValidContent() {
            boolean hasText = text != null && !text.isBlank();
            boolean hasPayload = payload != null && !payload.isBlank();
            boolean hasMedia = mediaUrl != null && !mediaUrl.isBlank();
            return switch (resolvedType()) {
                case TEXT -> hasText && !hasPayload && !hasMedia;
                case INTERACTIVE -> hasPayload && !hasText && !hasMedia;
                case IMAGE, DOCUMENT -> hasMedia && !hasPayload;
                default -> false;
            };
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppSimulatorDevViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("dev")
public class WhatsAppSimulatorDevViewController {

    @GetMapping("/admin/bot-simulator")
    public String simulator() {
        return "admin/bot-simulator";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WhatsAppWebhookController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lunaris.ansenuza.application.conversation.ConversationOrchestrator;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.application.usecase.WhatsAppWebhookInboxService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppWebhookParser;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppMessageDispatcher;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador de entrada HTTP del webhook de WhatsApp Cloud API.
 *
 * <p>Responsabilidad única: verificar el handshake, parsear el payload crudo a un
 * {@link IncomingMessage} y delegar de forma asíncrona en la capa de aplicación
 * (orquestación conversacional o procesamiento de comprobantes). Toda la lógica de
 * negocio vive fuera de esta clase, respetando la arquitectura hexagonal.
 */
@RestController
@RequestMapping("/whatsapp")
@Slf4j
public class WhatsAppWebhookController {

    private final WhatsAppWebhookParser webhookParser;
    private final ConversationOrchestrator conversationOrchestrator;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;
    private final WhatsAppMessageDispatcher messageDispatcher;
    private final WhatsAppWebhookInboxService inboxService;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final String verifyToken;
    private final String appSecret;
    private final com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry;
    private final org.springframework.context.ApplicationEventPublisher events;

    @org.springframework.beans.factory.annotation.Autowired
    public WhatsAppWebhookController(
            WhatsAppWebhookParser webhookParser,
            ConversationOrchestrator conversationOrchestrator,
            ProcessPaymentReceiptUseCase processPaymentReceiptUseCase,
            WhatsAppMessageDispatcher messageDispatcher,
            WhatsAppWebhookInboxService inboxService,
            ObjectMapper objectMapper,
            Environment environment,
            @Value("${whatsapp.verify-token:}") String verifyToken,
            @Value("${whatsapp.app-secret:}") String appSecret,
            com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry,
            org.springframework.context.ApplicationEventPublisher events) {
        this.events = events;
        this.webhookParser = webhookParser;
        this.conversationOrchestrator = conversationOrchestrator;
        this.processPaymentReceiptUseCase = processPaymentReceiptUseCase;
        this.messageDispatcher = messageDispatcher;
        this.inboxService = inboxService;
        this.objectMapper = objectMapper;
        this.environment = environment;
        this.verifyToken = verifyToken;
        this.appSecret = appSecret;
        this.telemetry = telemetry;
    }

    public WhatsAppWebhookController(WhatsAppWebhookParser parser, ConversationOrchestrator orchestrator,
            ProcessPaymentReceiptUseCase receipts, WhatsAppMessageDispatcher dispatcher,
            WhatsAppWebhookInboxService inbox, ObjectMapper mapper, Environment environment,
            String verifyToken, String appSecret) {
        this(parser, orchestrator, receipts, dispatcher, inbox, mapper, environment, verifyToken, appSecret,
                com.lunaris.ansenuza.application.port.ChatbotTelemetryPort.NOOP, event -> {});
    }

    public WhatsAppWebhookController(WhatsAppWebhookParser parser, ConversationOrchestrator orchestrator,
            ProcessPaymentReceiptUseCase receipts, WhatsAppMessageDispatcher dispatcher,
            WhatsAppWebhookInboxService inbox, ObjectMapper mapper, Environment environment,
            String verifyToken, String appSecret, com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry) {
        this(parser, orchestrator, receipts, dispatcher, inbox, mapper, environment, verifyToken, appSecret,
                telemetry, event -> {});
    }

    @jakarta.annotation.PostConstruct
    void validateProductionConfiguration() {
        if (environment.acceptsProfiles(Profiles.of("prod", "production"))
                && (appSecret == null || appSecret.isBlank())) {
            log.warn("WHATSAPP_APP_SECRET no está configurada en producción; "
                    + "los webhooks entrantes serán rechazados hasta configurarla.");
        }
    }

    @GetMapping("/webhook")
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        if (!this.verifyToken.isBlank() && MessageDigest.isEqual(
                this.verifyToken.getBytes(StandardCharsets.UTF_8),
                verifyToken.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody byte[] rawPayload) {
        if (!isValidSignature(rawPayload, signature)) {
            log.warn("Webhook de WhatsApp rechazado por firma ausente o inválida.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Map<String, Object> payload = objectMapper.readValue(
                    rawPayload, new TypeReference<>() {});
            IncomingMessage message = webhookParser.parse(payload);
            if (message == null) {
                return ResponseEntity.ok().build();
            }
            if (message.messageId() == null || message.messageId().isBlank()) {
                log.warn("Webhook de WhatsApp descartado porque no contiene messageId.");
                return ResponseEntity.ok().build();
            }
            if (!inboxService.claim(message.messageId())) {
                log.debug("Webhook de WhatsApp duplicado ignorado: {}", message.messageId());
                return ResponseEntity.ok().build();
            }

            messageDispatcher.dispatch(message.from(), () -> {
                events.publishEvent(new com.lunaris.ansenuza.domain.model.PassengerMessageReceived(message.from()));
                IncomingMessage tracked = message.withTelemetry(
                        telemetry.begin(message.from(), message.messageId(), false));
                if (message.isImageWithMedia()) {
                    processPaymentReceiptUseCase.execute(message.from(), message.mediaId());
                } else if (message.body() != null) {
                    conversationOrchestrator.process(tracked);
                } else {
                    tracked.telemetry().emit(
                            com.lunaris.ansenuza.application.telemetry.ChatbotEventType.INPUT_REJECTED,
                            null, com.lunaris.ansenuza.application.telemetry.ChatbotReason.UNSUPPORTED_MESSAGE);
                }
            });

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error crítico general: ", e);
            return ResponseEntity.ok().build();
        }
    }

    private boolean isValidSignature(byte[] payload, String signature) {
        if (appSecret == null || appSecret.isBlank()) {
            return false;
        }
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            byte[] supplied = HexFormat.of().parseHex(signature.substring("sha256=".length()));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return MessageDigest.isEqual(mac.doFinal(payload), supplied);
        } catch (Exception exception) {
            return false;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessageDispatcher.java`

```java
package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WhatsAppMessageDispatcher {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentHashMap<String, CompletableFuture<Void>> pendingByPhone =
            new ConcurrentHashMap<>();

    @Async("taskExecutor")
    public void dispatch(String phoneNumber, Runnable processing) {
        String key = phoneNumber == null ? "" : phoneNumber;
        pendingByPhone.compute(key, (ignored, previous) -> {
            CompletableFuture<Void> start = previous == null
                    ? CompletableFuture.completedFuture(null)
                    : previous.handle((result, error) -> null);
            CompletableFuture<Void> next = start.thenRunAsync(() -> {
                try {
                    processing.run();
                } catch (Exception exception) {
                    log.error("Error asincrónico procesando webhook para {}.", key, exception);
                }
            }, executor);
            next.whenCompleteAsync(
                    (result, error) -> pendingByPhone.remove(key, next), executor);
            return next;
        });
    }

    @PreDestroy
    void close() {
        executor.close();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppMessagingAdapter.java`

```java
package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.List;
import java.util.Map;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link MessagingPort} sobre la WhatsApp Cloud API.
 * Traduce el modelo agnóstico de la aplicación (textos y {@link Button}) al formato que
 * espera {@link WhatsAppService}.
 */
@RequiredArgsConstructor
public class WhatsAppMessagingAdapter implements MessagingPort {

    private final WhatsAppService whatsAppService;

    @Override
    public void sendText(String to, String text, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendText(to, text, outcome);
    }

    @Override
    public void sendTemplate(String to, String template, List<String> parameters, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendTemplate(to, template, parameters, outcome);
    }

    @Override
    public void sendDocumentUrl(String to, String url, String name, String caption, java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendDocumentUrl(to, url, name, caption, outcome);
    }

    @Override
    public void sendText(String to, String message) {
        whatsAppService.sendMessage(to, message);
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons) {
        List<Map<String, String>> mappedButtons = buttons.stream()
                .map(b -> Map.of("id", b.id(), "title", b.title()))
                .toList();
        whatsAppService.sendInteractiveButtons(to, header, body, mappedButtons);
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons,
            java.util.function.Consumer<Boolean> outcome) {
        whatsAppService.sendButtons(to, header, body, buttons, outcome);
    }

    @Override
    public void requestLocation(String to, String message) {
        whatsAppService.sendLocationRequest(to, message);
    }

    @Override
    public void sendImage(String to, String imageUrl, String caption) {
        whatsAppService.sendImageMessage(to, imageUrl, caption);
    }

    @Override
    public void sendTemplate(String to, String templateName, List<String> parameters) {
        whatsAppService.sendTemplate(to, templateName, parameters);
    }

    @Override
    public void sendOtp(String to, String passengerName, String code) {
        whatsAppService.sendOtpMessage(to, passengerName, code);
    }

    @Override
    public void sendDocument(String to, String absoluteFilePath, String fileName, String caption) {
        whatsAppService.sendDocument(to, absoluteFilePath, fileName, caption);
    }

    @Override
    public void sendDocumentUrl(String to, String documentUrl, String fileName, String caption) {
        whatsAppService.sendDocumentUrl(to, documentUrl, fileName, caption);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppService.java`

```java
package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile("!dev")
@Slf4j


public class WhatsAppService implements MessagingPort {

    private static final String ARGENTINA_COUNTRY_CODE = "54";
    private static final String ARGENTINA_MOBILE_PREFIX = "549";
    private static final int ARGENTINA_NATIONAL_NUMBER_LENGTH = 10;
    static final String ACCOUNT_CREATION_TEMPLATE = "account_creation_confirmation_3";
    private static final long MIN_RECIPIENT_GAP_MILLIS = 300L;
    private static final long PAIR_RATE_LIMIT_BACKOFF_MILLIS = 1_000L;
    private static final Pattern PAIR_RATE_LIMIT_CODE = Pattern.compile(
            "\\\"code\\\"\\s*:\\s*131056");

    private static final Map<String, String> TEMPLATE_LANGUAGES = Map.of(
            "despierta_chofer", "en",
            "proximo_en_camino", "en",
            "chofer_asignado", "es",
            "contacto_pasajero", "es");

    @Value("${whatsapp.access-token:dev-mock-token}")
    private String whatsappToken;

    @Value("${whatsapp.phone-number-id:123456789}")
    private String whatsappPhoneNumberId;

    @Value("${whatsapp.phone-number-id:123456789}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token:dev-mock-token}")
    private String accessToken;

    @Value("${lunaris.support-phone:}")
    private String supportPhone;

    private final RestTemplate restTemplate;
    private final LongSupplier nanoTime;
    private final Sleeper sleeper;
    private final Map<String, Long> lastSendNanosByRecipient = new ConcurrentHashMap<>();
    private final Map<String, Object> recipientLocks = new ConcurrentHashMap<>();

    public WhatsAppService() {
        this(new RestTemplate(), System::nanoTime, Thread::sleep);
    }

    WhatsAppService(RestTemplate restTemplate, LongSupplier nanoTime, Sleeper sleeper) {
        this.restTemplate = restTemplate;
        this.nanoTime = nanoTime;
        this.sleeper = sleeper;
    }

    // MENSAJE TEXTO TRADICIONAL
    public void sendMessage(String phoneNumber, String message) {
        trySendMessage(phoneNumber, message);
    }

    @Override
    public void sendText(String to, String message) {
        sendMessage(to, message);
    }

    @Override
    public void sendOtp(String phoneNumber, String passengerName, String code) {
        sendOtpMessage(phoneNumber, passengerName, code);
    }

    public void sendOtpMessage(
            String phoneNumber, String passengerName, String code) {
        String phone = formatMetaPhoneNumber(phoneNumber);
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phone,
                "type", "template",
                "template", Map.of(
                        "name", ACCOUNT_CREATION_TEMPLATE,
                        "language", Map.of("code", "es"),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text",
                                                        safeTemplateValue(passengerName, "Pasajero")),
                                                Map.of("type", "text", "text", code))),
                                Map.of(
                                        "type", "button",
                                        "sub_type", "url",
                                        "index", "0",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", code))))));

        boolean sent = executePostCall(
                "https://graph.facebook.com/v18.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "TEMPLATE " + ACCOUNT_CREATION_TEMPLATE);
        if (sent) {
            log.info("Éxito Meta [TEMPLATE {}]: Envío OTP hacia {}",
                    ACCOUNT_CREATION_TEMPLATE, phone);
        }
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons) {
        sendInteractiveButtons(to, header, body, buttons.stream()
                .map(button -> Map.of("id", button.id(), "title", button.title()))
                .toList());
    }

    @Override
    public void sendButtons(String to, String header, String body, List<Button> buttons,
            java.util.function.Consumer<Boolean> outcome) {
        sendInteractiveButtons(to, header, body, buttons.stream()
                .map(button -> Map.of("id", button.id(), "title", button.title())).toList(), outcome);
    }

    @Override
    public void requestLocation(String to, String message) {
        sendLocationRequest(to, message);
    }

    @Override
    public void sendImage(String to, String imageUrl, String caption) {
        sendImageMessage(to, imageUrl, caption);
    }

    @Override
    public void sendText(String phone, String message, java.util.function.Consumer<Boolean> outcome) {
        Map<String, Object> body = Map.of("messaging_product", "whatsapp", "to", phone,
                "type", "text", "text", Map.of("body", message));
        executePostCall("https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "TEXTO", outcome);
    }

    boolean trySendMessage(String phoneNumber, String message) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phoneNumber,
                "type", "text",
                "text", Map.of("body", message)
        );
        return executePostCall(url, headers, body, "TEXTO");
    }

    // SOBRECARGA 1: BOTONES INTERACTIVOS COMUNES (3 ARGUMENTOS)
    public boolean sendInteractiveButtons(String phoneNumber, String bodyText, List<Map<String, String>> buttons) {
        return sendInteractiveButtons(phoneNumber, "Lunaris Ansenuza", bodyText, buttons);
    }

    // SOBRECARGA 2: BOTONES INTERACTIVOS PREMIUM CON TÍTULO DESTACADO (4 ARGUMENTOS)
    public boolean sendInteractiveButtons(String phoneNumber, String headerText, String bodyText, List<Map<String, String>> buttons) {
        return sendInteractiveButtons(phoneNumber, headerText, bodyText, buttons, ignored -> {});
    }

    public boolean sendInteractiveButtons(String phoneNumber, String headerText, String bodyText,
            List<Map<String, String>> buttons, java.util.function.Consumer<Boolean> outcome) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();

        try {
            List<Map<String, Object>> buttonObjects = new ArrayList<>();
            for (Map<String, String> btn : buttons) {
                buttonObjects.add(Map.of(
                    "type", "reply",
                    "reply", Map.of(
                            "id", btn.get("id"),
                            "title", metaReplyButtonTitle(btn.get("title")))
                ));
            }

            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                    "type", "button",
                    "header", Map.of("type", "text", "text", headerText),
                    "body", Map.of("text", bodyText),
                    "action", Map.of("buttons", buttonObjects)
                )
            );

            return executePostCall(url, headers, body, "BOTONES INTERACTIVOS", outcome);
        } catch (Exception e) {
            log.error("Error en botones interactivos: ", e);
            reportOutcome(outcome, false);
            return false;
        }
    }

    public void sendDriverBoardingConfirmation(String phoneNumber, String successMessage) {
        boolean interactiveSent = sendInteractiveButtons(
                phoneNumber,
                "Abordaje confirmado",
                successMessage,
                List.of(Map.of("id", "VIEW_ROUTE", "title", "🗺️ Ver Ruta")));
        if (!interactiveSent) {
            sendMessage(phoneNumber, successMessage + "\n\nEscribí *VER RUTA* para continuar.");
        }
    }

    public void sendLocationRequest(String phoneNumber, String message) {
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "location_request_message",
                        "body", Map.of("text", message),
                        "action", Map.of("name", "send_location")));
        executePostCall("https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "SOLICITUD DE UBICACIÓN");
    }

    public void sendImageMessage(String toPhone, String imageUrl, String caption) {
        Map<String, Object> image = new HashMap<>();
        image.put("link", imageUrl);
        if (caption != null && !caption.isBlank()) {
            image.put("caption", caption);
        }
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", toPhone,
                "type", "image",
                "image", image);
        executePostCall(
                "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "IMAGEN");
    }

    // MENÚ DESPLEGABLE PREMIUM MULTI-SECCIÓN
    public boolean sendInteractiveList(String phoneNumber, String headerText, String bodyText, String buttonLabel, List<Map<String, Object>> sections) {
        List<Map<String, Object>> safeSections = constrainInteractiveSections(sections);
        boolean sent = trySendInteractiveList(
                phoneNumber, headerText, bodyText, buttonLabel, safeSections);
        if (!sent) {
            trySendMessage(phoneNumber, buildInteractiveListFallback(bodyText, safeSections));
        }
        return sent;
    }

    boolean trySendInteractiveList(String phoneNumber, String headerText, String bodyText,
            String buttonLabel, List<Map<String, Object>> sections) {
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();

        try {
            Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", Map.of(
                    "type", "list",
                    "header", Map.of("type", "text", "text", headerText),
                    "body", Map.of("text", bodyText),
                    "action", Map.of(
                        "button", buttonLabel,
                        "sections", sections
                    )
                )
            );

            return executePostCall(url, headers, body, "LISTA GEOGRÁFICA");
        } catch (Exception e) {
            log.error("Error en lista desplegable: ", e);
            return false;
        }
    }

    private static List<Map<String, Object>> constrainInteractiveSections(
            List<Map<String, Object>> sections) {
        if (sections == null) {
            return List.of();
        }
        return sections.stream().map(section -> {
            Object rawRows = section.get("rows");
            List<?> rows = rawRows instanceof List<?> list ? list : List.of();
            List<Map<String, Object>> safeRows = rows.stream()
                    .filter(rawRow -> rawRow instanceof Map<?, ?>)
                    .map(rawRow -> {
                        Map<?, ?> row = (Map<?, ?>) rawRow;
                        Object id = row.get("id");
                        Object title = row.get("title");
                        Object description = row.get("description");
                        return Map.<String, Object>of(
                                "id", id == null ? "" : id.toString(),
                                "title", truncateMetaText(
                                        title == null ? "Opción" : title.toString(), 24),
                                "description", truncateMetaText(
                                        description == null ? "" : description.toString(), 72));
                    })
                    .toList();
            return Map.<String, Object>of(
                    "title", truncateMetaText(
                            String.valueOf(section.getOrDefault("title", "Opciones")), 24),
                    "rows", safeRows);
        }).toList();
    }

    private static String buildInteractiveListFallback(
            String bodyText, List<Map<String, Object>> sections) {
        StringBuilder fallback = new StringBuilder(textOrDefault(bodyText, "Opciones disponibles"));
        for (Map<String, Object> section : sections) {
            fallback.append("\n\n*").append(section.get("title")).append("*");
            Object rawRows = section.get("rows");
            if (rawRows instanceof List<?> rows) {
                for (Object rawRow : rows) {
                    if (rawRow instanceof Map<?, ?> row) {
                        fallback.append("\n• ").append(row.get("title"));
                        Object description = row.get("description");
                        if (description != null && !description.toString().isBlank()) {
                            fallback.append(" — ").append(description);
                        }
                    }
                }
            }
        }
        return fallback.toString();
    }

    // 🧾 ENVÍO DE DOCUMENTO (PDF) — sube el archivo local a Meta y luego lo manda por su media id
    @Override
    public void sendDocument(String phoneNumber, String absoluteFilePath, String fileName, String caption) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    java.util.concurrent.CompletableFuture.runAsync(
                            () -> sendDocument(phoneNumber, absoluteFilePath, fileName, caption));
                }
            });
            log.debug("Envío de documento a Meta diferido hasta confirmar la transacción.");
            return;
        }
        try {
            // Paso 1: Subir el PDF a la Media API (multipart) para obtener un media id
            String uploadUrl = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/media";
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setBearerAuth(accessToken);
            uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("messaging_product", "whatsapp");
            parts.add("type", "application/pdf");
            parts.add("file", new FileSystemResource(absoluteFilePath));

            HttpEntity<MultiValueMap<String, Object>> uploadRequest = new HttpEntity<>(parts, uploadHeaders);
            ResponseEntity<JsonNode> uploadResponse =
                    restTemplate.postForEntity(uploadUrl, uploadRequest, JsonNode.class);
            String mediaId = uploadResponse.getBody().get("id").asText();

            // Paso 2: Enviar el documento usando el media id
            String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
            Map<String, Object> documentNode = new HashMap<>();
            documentNode.put("id", mediaId);
            documentNode.put("filename", fileName);
            if (caption != null && !caption.isBlank()) {
                documentNode.put("caption", caption);
            }

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", phoneNumber,
                    "type", "document",
                    "document", documentNode
            );

            executePostCall(url, createHeaders(), body, "DOCUMENTO");
        } catch (Exception e) {
            log.error("Error al enviar documento por WhatsApp a {}: ", phoneNumber, e);
            throw new RuntimeException("No se pudo enviar el documento por WhatsApp", e);
        }
    }

    @Override
    public void sendDocumentUrl(String phoneNumber, String documentUrl, String fileName, String caption) {
        sendDocumentUrl(phoneNumber, documentUrl, fileName, caption, ignored -> {});
    }

    @Override
    public void sendDocumentUrl(String phoneNumber, String documentUrl, String fileName, String caption,
            java.util.function.Consumer<Boolean> outcome) {
        try {
            String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
            Map<String, Object> documentNode = new HashMap<>();
            documentNode.put("link", documentUrl);
            documentNode.put("filename", fileName);
            if (caption != null && !caption.isBlank()) {
                documentNode.put("caption", caption);
            }
            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", phoneNumber,
                    "type", "document",
                    "document", documentNode);
            executePostCall(url, createHeaders(), body, "DOCUMENTO_URL", outcome);
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo enviar el documento por URL.", exception);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private boolean executePostCall(String url, HttpHeaders headers, Map<String, Object> body, String tipoMensaje) {
        return executePostCall(url, headers, body, tipoMensaje, ignored -> {});
    }

    private boolean executePostCall(String url, HttpHeaders headers, Map<String, Object> body,
            String tipoMensaje, java.util.function.Consumer<Boolean> outcome) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            Map<String, Object> deferredBody = new HashMap<>(body);
            HttpHeaders deferredHeaders = new HttpHeaders();
            deferredHeaders.putAll(headers);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    java.util.concurrent.CompletableFuture.runAsync(
                            () -> executePostCall(url, deferredHeaders, deferredBody, tipoMensaje, outcome));
                }
            });
            log.debug("Envío Meta [{}] diferido hasta confirmar la transacción.", tipoMensaje);
            return true;
        }
        boolean sent = executeImmediatePostCall(url, headers, body, tipoMensaje);
        reportOutcome(outcome, sent);
        return sent;
    }

    protected static void reportOutcome(java.util.function.Consumer<Boolean> outcome, boolean sent) {
        try {
            outcome.accept(sent);
        } catch (RuntimeException exception) {
            log.warn("No se pudo registrar el resultado analítico del envío; tipo={}",
                    exception.getClass().getSimpleName());
        }
    }

    private boolean executeImmediatePostCall(String url, HttpHeaders headers, Map<String, Object> body,
            String tipoMensaje) {
        Map<String, Object> sanitizedBody = new HashMap<>(body);
        if (body.get("to") instanceof String destinationPhone) {
            sanitizedBody.put("to", formatMetaPhoneNumber(destinationPhone));
        }
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(sanitizedBody, headers);
        String destination = (String) sanitizedBody.get("to");
        Object recipientLock = recipientLocks.computeIfAbsent(destination, ignored -> new Object());
        synchronized (recipientLock) {
            if (!awaitRecipientThrottle(destination)) {
                return false;
            }
            try {
                return postToMeta(url, request, tipoMensaje, destination);
            } catch (HttpClientErrorException exception) {
                if (isPairRateLimit(exception)) {
                    log.warn("WhatsApp Rate Limit hit for recipient {}. Applying backoff retry...",
                            destination);
                    if (!sleep(PAIR_RATE_LIMIT_BACKOFF_MILLIS)) {
                        return false;
                    }
                    try {
                        return postToMeta(url, request, tipoMensaje, destination);
                    } catch (HttpClientErrorException retryException) {
                        log.warn("WhatsApp Rate Limit retry failed for recipient {}. "
                                + "Conversation state is preserved. Response: {}",
                                destination, retryException.getResponseBodyAsString());
                        return false;
                    }
                }
                return handleMetaClientError(tipoMensaje, destination, exception);
            } catch (RestClientResponseException exception) {
                log.error("Error de Meta al enviar [{}] hacia {}. Status: {}. Body: {}",
                        tipoMensaje, destination, exception.getStatusCode(),
                        exception.getResponseBodyAsString());
                return false;
            } catch (Exception exception) {
                log.error("Falla de red en HTTP call Meta: ", exception);
                return false;
            } finally {
                lastSendNanosByRecipient.put(destination, nanoTime.getAsLong());
            }
        }
    }

    private boolean postToMeta(String url, HttpEntity<Map<String, Object>> request,
            String messageType, String destination) {
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        log.info("Éxito Meta [{}]: Envío hacia {}. Status: {}. Body: {}",
                messageType, destination, response.getStatusCode(), response.getBody());
        return response.getStatusCode().is2xxSuccessful();
    }

    private boolean handleMetaClientError(
            String messageType, String destination, HttpClientErrorException exception) {
        if (isTemplateUnavailable(messageType, exception.getStatusCode().value(),
                exception.getResponseBodyAsString())) {
            log.warn("Plantilla de Meta no disponible o en revisión [{}] para {}. "
                            + "La operación principal continúa. Respuesta: {}",
                    messageType, destination, exception.getResponseBodyAsString());
            return false;
        }
        log.error("Error de Meta HTTP [{}]: {}", exception.getStatusCode(),
                exception.getResponseBodyAsString());
        return false;
    }

    private boolean awaitRecipientThrottle(String destination) {
        Long previousSend = lastSendNanosByRecipient.get(destination);
        if (previousSend == null) {
            return true;
        }
        long elapsedMillis = Math.max(0L,
                (nanoTime.getAsLong() - previousSend) / 1_000_000L);
        long remainingMillis = MIN_RECIPIENT_GAP_MILLIS - elapsedMillis;
        return remainingMillis <= 0 || sleep(remainingMillis);
    }

    private boolean sleep(long millis) {
        try {
            sleeper.sleep(millis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Envío a Meta interrumpido durante el backoff.");
            return false;
        }
    }

    static boolean isPairRateLimit(HttpClientErrorException exception) {
        return exception.getStatusCode().value() == 400
                && PAIR_RATE_LIMIT_CODE.matcher(exception.getResponseBodyAsString()).find();
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
// 📦 Agregá este método al final de tu archivo WhatsAppService.java
public void sendMediaMessage(String to, String type, String mediaUrl, String caption) {
    if (mediaUrl == null || "null".equals(mediaUrl)) {
        log.warn("[WhatsApp API] Intento de enviar mensaje multimedia sin URL válida.");
        return;
    }

    try {
        // 🌐 URL usando tu variable exacta: phoneNumberId
        String url = "https://graph.facebook.com/v20.0/" + this.phoneNumberId + "/messages";

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", formatMetaPhoneNumber(to));
        body.put("type", "image");

        java.util.Map<String, String> imageNode = new java.util.HashMap<>();
        imageNode.put("link", mediaUrl);
        imageNode.put("caption", caption);
        body.put("image", imageNode);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(this.accessToken); // 👈 Corregido con tu variable: accessToken

        boolean sent = executePostCall(url, headers, body, "COMPROBANTE MANUAL");
        if (sent) {
            log.info("[WhatsApp API] Comprobante manual enviado con éxito al número: {}", to);
        }

    } catch (Exception e) {
        log.error("[CRÍTICO] Error al enviar the comprobante por WhatsApp API al número {}: ", to, e);
    }
}

public void sendDespiertaChoferTemplate(
        String to, String nombreChofer, java.util.UUID driverId, java.time.LocalDate travelDate) {
    trySendDriverRouteTemplate(to, nombreChofer, driverId, travelDate);
}

boolean trySendDriverRouteTemplate(
        String to, String nombreChofer, java.util.UUID driverId, java.time.LocalDate travelDate) {
    try {
        String metaPhoneNumber = formatMetaPhoneNumber(to);
        String url = "https://graph.facebook.com/v25.0/" + this.phoneNumberId + "/messages";
        org.springframework.http.HttpHeaders headers = createHeaders();

        // Validamos que si llega nulo o vacío, use un valor por defecto para que Meta no rebote
        String nombreValido = (nombreChofer != null && !nombreChofer.isBlank()) ? nombreChofer : "Chofer";

        java.util.Map<String, Object> bodyParam = java.util.Map.of(
            "type", "text",
            "parameter_name", "nombre_chofer", // <--- ¡CLAVE OBLIGATORIA DE META PARA NAMED VARIABLES!
            "text", nombreValido
        );

        java.util.Map<String, Object> bodyComponent = java.util.Map.of(
            "type", "body",
            "parameters", java.util.List.of(bodyParam)
        );

        java.util.Map<String, Object> templateMap = java.util.Map.of(
            "name", "despierta_chofer",
            "language", java.util.Map.of("code", templateLanguageFor("despierta_chofer")),
            "components", despiertaChoferComponents(bodyComponent, driverId, travelDate)
        );

        java.util.Map<String, Object> body = java.util.Map.of(
            "messaging_product", "whatsapp",
            "recipient_type", "individual",
            "to", metaPhoneNumber,
            "type", "template",
            "template", templateMap
        );

        return executePostCall(url, headers, body, "TEMPLATE DESPIERTA CHOFER");
    } catch (Exception e) {
        log.error("Error al enviar la plantilla despierta_chofer a {}: ", to, e);
        return false;
    }
}

public DriverRouteDispatchResult sendDriverRouteDispatch(
        String to,
        String driverName,
        String navigationUrl,
        List<Reservation> reservations) {
    List<Reservation> orderedReservations = reservations == null
            ? List.of()
            : reservations.stream()
                    .filter(java.util.Objects::nonNull)
                    .filter(Reservation::isScheduledConfirmedTrip)
                    .sorted(java.util.Comparator.comparing(
                            Reservation::getRouteSequence,
                            java.util.Comparator.nullsLast(Integer::compareTo)))
                    .toList();

    String normalizedDriverPhone = formatMetaPhoneNumber(to);
    String routeSummary = buildDriverPassengerSummary(
            driverName, navigationUrl, orderedReservations);
    Reservation routeReference = orderedReservations.stream().findFirst().orElse(null);
    boolean templateSent = routeReference != null
            && routeReference.getDriver() != null
            && routeReference.getDriver().getId() != null
            && trySendDriverRouteTemplate(
                    normalizedDriverPhone,
                    driverName,
                    routeReference.getDriver().getId(),
                    routeReference.getTravelDate());
    boolean interactiveSentForAllBatches = true;
    boolean fallbackTextSent = false;

    for (int start = 0; start < orderedReservations.size(); start += 10) {
        int end = Math.min(start + 10, orderedReservations.size());
        List<Map<String, Object>> rows = orderedReservations.subList(start, end).stream()
                .map(WhatsAppService::onboardRow)
                .toList();
        Map<String, Object> section = Map.of(
                "title", "Pasajeros " + (start + 1) + "–" + end,
                "rows", rows);
        boolean interactiveSent = trySendInteractiveList(
                normalizedDriverPhone,
                "Confirmar abordajes",
                "Seleccioná al pasajero que acaba de subir.",
                "A bordo",
                List.of(section));
        if (!interactiveSent) {
            interactiveSentForAllBatches = false;
            log.warn("Meta rechazó la lista interactiva de ruta para {}. Se envía fallback de texto.",
                    normalizedDriverPhone);
            fallbackTextSent = trySendMessage(normalizedDriverPhone,
                    "⚠️ No pudimos habilitar los botones de abordaje. "
                            + "Usá esta hoja de ruta en texto:\n\n" + routeSummary)
                    || fallbackTextSent;
            sendInteractiveButtons(
                    normalizedDriverPhone,
                    "Confirmar abordaje",
                    "Seleccioná uno de los próximos pasajeros:",
                    orderedReservations.subList(start, Math.min(start + 3, end)).stream()
                            .map(WhatsAppService::onboardReplyButton)
                            .toList());
        }
    }
    if (templateSent && interactiveSentForAllBatches) {
        return new DriverRouteDispatchResult(true, "Hoja de ruta enviada por WhatsApp.");
    }
    if (!interactiveSentForAllBatches && fallbackTextSent) {
        return new DriverRouteDispatchResult(false,
                "Meta no habilitó la lista interactiva; la hoja de ruta se envió en texto.");
    }
    return new DriverRouteDispatchResult(false,
            "La asignación quedó guardada, pero Meta no confirmó todos los mensajes; "
                    + "se intentó el envío alternativo en texto.");
}

public record DriverRouteDispatchResult(boolean success, String message) {
}

static String buildDriverPassengerSummary(
        String driverName, String navigationUrl, List<Reservation> reservations) {
    StringBuilder summary = new StringBuilder()
            .append("🚐 *Hoja de ruta Lunaris*\n")
            .append("Chofer: ").append(textOrDefault(driverName, "Chofer")).append("\n\n")
            .append("📍 *Navegación GPS:*\n")
            .append(textOrDefault(navigationUrl, "No disponible")).append("\n\n")
            .append("👥 *Pasajeros:*\n");

    if (reservations == null || reservations.isEmpty()) {
        return summary.append("Sin pasajeros asignados.").toString();
    }

    int index = 1;
    for (Reservation reservation : reservations) {
        var passenger = reservation.getPassenger();
        String passengerName = passenger == null
                ? "Pasajero"
                : (textOrDefault(passenger.getFirstName(), "") + " "
                        + textOrDefault(passenger.getLastName(), "")).trim();
        String phone = passenger == null ? "" : passenger.getPhone();
        summary.append(index).append(". *")
                .append(textOrDefault(passengerName, "Pasajero")).append("*\n")
                .append("   🕒 ").append(estimatedRoutePickupTime(reservation, index - 1)).append("\n")
                .append("   📍 ").append(resolvePickupAddress(reservation)).append("\n")
                .append("   📞 ").append(textOrDefault(phone, "Sin teléfono")).append("\n")
                .append("   💺 ").append(reservation.getTotalSeats()).append(" asiento(s)");
        if (reservation.getCompanionNames() != null
                && !reservation.getCompanionNames().isBlank()) {
            summary.append(" — Acompañantes: ").append(reservation.getCompanionNames().trim());
        }
        summary.append("\n\n");
        index++;
    }
    return summary.toString().trim();
}

private static String estimatedRoutePickupTime(Reservation reservation, int routeIndex) {
    String rawSchedule = textOrDefault(reservation.getDepartureSchedule(), "03:00 AM")
            .toUpperCase(java.util.Locale.ROOT);
    for (java.time.format.DateTimeFormatter formatter : List.of(
            java.time.format.DateTimeFormatter.ofPattern("H:mm", java.util.Locale.ROOT),
            java.time.format.DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH))) {
        try {
            java.time.LocalTime base = java.time.LocalTime.parse(rawSchedule, formatter);
            return base.plusMinutes(15L * routeIndex)
                    .format(java.time.format.DateTimeFormatter.ofPattern(
                            "hh:mm a", java.util.Locale.ENGLISH));
        } catch (java.time.format.DateTimeParseException ignored) {
            // Se prueba el siguiente formato admitido.
        }
    }
    return rawSchedule;
}

private static Map<String, Object> onboardRow(Reservation reservation) {
    var passenger = reservation.getPassenger();
    String passengerName = passenger == null
            ? "Pasajero"
            : (textOrDefault(passenger.getFirstName(), "") + " "
                    + textOrDefault(passenger.getLastName(), "")).trim();
    int routeIndex = reservation.getRouteSequence() == null
            ? 0 : Math.max(0, reservation.getRouteSequence() - 1);
    return Map.of(
            "id", "ONBOARD_" + reservation.getId(),
            "title", truncateMetaText(
                    "A bordo - " + textOrDefault(passengerName, "Pasajero"), 24),
            "description", truncateMetaText(
                    estimatedRoutePickupTime(reservation, routeIndex)
                            + " · " + resolvePickupAddress(reservation), 72));
}

private static Map<String, String> onboardReplyButton(Reservation reservation) {
    var passenger = reservation.getPassenger();
    String firstName = passenger == null
            ? "Pasajero"
            : textOrDefault(passenger.getFirstName(), "Pasajero");
    return Map.of(
            "id", "ONBOARD_" + reservation.getId(),
            "title", truncateMetaText("A bordo " + firstName, 20));
}

private static String resolvePickupAddress(Reservation reservation) {
    if (reservation.getPickupAddress() != null && !reservation.getPickupAddress().isBlank()) {
        return reservation.getPickupAddress().trim();
    }
    if (reservation.getPassenger() != null
            && reservation.getPassenger().getAddress() != null
            && !reservation.getPassenger().getAddress().isBlank()) {
        return reservation.getPassenger().getAddress().trim();
    }
    return "Sin dirección registrada";
}

private static String textOrDefault(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
}

private static String truncateMetaText(String value, int maxLength) {
    if (value.length() <= maxLength) {
        return value;
    }
    int endIndex = maxLength - 1;
    if (endIndex > 0 && Character.isHighSurrogate(value.charAt(endIndex - 1))) {
        endIndex--;
    }
    return value.substring(0, endIndex) + "…";
}

static String metaReplyButtonTitle(String title) {
    if (title == null || title.isBlank()) {
        throw new IllegalArgumentException("El título del botón de WhatsApp es obligatorio.");
    }
    return truncateMetaText(title.trim(), 20);
}

static java.util.List<java.util.Map<String, Object>> despiertaChoferComponents(
        java.util.Map<String, Object> bodyComponent,
        java.util.UUID driverId,
        java.time.LocalDate travelDate) {
    java.util.Objects.requireNonNull(driverId, "El ID del chofer es obligatorio.");
    java.util.Objects.requireNonNull(travelDate, "La fecha de viaje es obligatoria.");
    java.util.Map<String, Object> quickReplyComponent = java.util.Map.of(
        "type", "button",
        "sub_type", "quick_reply",
        "index", "0",
        "parameters", java.util.List.of(java.util.Map.of(
            "type", "payload",
            "payload", "VIEW_ROUTE"))
    );
    return java.util.List.of(bodyComponent, quickReplyComponent);
}

static String buildDriverRouteSheetUrl(
        java.util.UUID driverId, java.time.LocalDate travelDate) {
    java.util.Objects.requireNonNull(driverId, "El ID del chofer es obligatorio.");
    java.util.Objects.requireNonNull(travelDate, "La fecha de viaje es obligatoria.");
    return "https://lunaris-backend-nn6s.onrender.com/hoja-ruta?driverId="
            + driverId + "&date=" + travelDate;
}

    public void sendContactoPasajeroTemplate(String to, String passengerName) {
        sendTemplate(to, com.lunaris.ansenuza.application.port.PassengerContactTemplate.NAME,
                com.lunaris.ansenuza.application.port.PassengerContactTemplate.parameters(passengerName));
    }

    public void sendChoferAsignadoTemplate(
            String to, String passengerName, String driverName, String driverPhone) {
        sendTemplate(to, "chofer_asignado", List.of(
                safeTemplateValue(passengerName, "Pasajero"),
                safeTemplateValue(driverName, "Chofer")));

        String contactPhone = driverPhone;
        if (contactPhone == null || contactPhone.isBlank()) {
            log.warn(
                    "[CHOFER_ASIGNADO] El chofer {} no tiene teléfono; se informa el contacto de soporte.",
                    safeTemplateValue(driverName, "sin identificar"));
            contactPhone = supportPhone;
        }
        sendMessage(to, buildDriverAssignmentContactMessage(
                driverName, contactPhone, supportPhone));
    }

    static String buildDriverAssignmentContactMessage(
            String driverName, String driverPhone, String supportPhone) {
        String resolvedPhone = driverPhone;
        if (resolvedPhone == null || resolvedPhone.isBlank()) {
            resolvedPhone = supportPhone;
        }
        String formattedContact = resolvedPhone == null || resolvedPhone.isBlank()
                ? "WhatsApp de Lunaris (este chat)"
                : "+" + formatMetaPhoneNumber(resolvedPhone);
        return "🚗 *Auto Lunaris asignado*\n\n"
                + "Chofer: " + safeTemplateValue(driverName, "A confirmar") + "\n"
                + "Contacto: " + formattedContact;
    }

    public void sendProximoEnCaminoTemplate(
            String to, String passengerName, String driverName) {
        sendTemplate(to, "proximo_en_camino", proximoEnCaminoParameters(
                passengerName, driverName));
    }

    static List<String> proximoEnCaminoParameters(
            String passengerName, String driverName) {
        return List.of(
                safeTemplateValue(passengerName, "Pasajero"),
                safeTemplateValue(driverName, "Chofer"));
    }

    @Override
    public void sendTemplate(String to, String templateName, List<String> values) {
        sendTemplate(to, templateName, values, ignored -> {});
    }

    @Override
    public void sendTemplate(String to, String templateName, List<String> values,
            java.util.function.Consumer<Boolean> outcome) {
        String metaPhoneNumber = formatMetaPhoneNumber(to);
        List<Map<String, Object>> parameters = values.stream()
                .map(value -> Map.<String, Object>of("type", "text", "text", value))
                .toList();
        Map<String, Object> template = Map.of(
                "name", templateName,
                "language", Map.of("code", templateLanguageFor(templateName)),
                "components", List.of(Map.of("type", "body", "parameters", parameters)));
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", metaPhoneNumber,
                "type", "template",
                "template", template);
        executePostCall("https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages",
                createHeaders(), body, "TEMPLATE " + templateName.toUpperCase(), outcome);
    }

    private static String safeTemplateValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    static String formatMetaPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }

        String digits = phoneNumber.replaceAll("\\D", "");
        try {
            String canonical = com.lunaris.ansenuza.shared.PhoneUtils
                    .normalizeArgentinePhone(phoneNumber);
            return ARGENTINA_MOBILE_PREFIX
                    + canonical.substring(ARGENTINA_COUNTRY_CODE.length());
        } catch (com.lunaris.ansenuza.domain.exception.DomainValidationException exception) {
            // Un número internacional no argentino se conserva sólo con dígitos.
        }
        if (digits.startsWith(ARGENTINA_MOBILE_PREFIX)) {
            return digits;
        }
        if (digits.length() == ARGENTINA_NATIONAL_NUMBER_LENGTH
                || digits.startsWith("351")) {
            return ARGENTINA_MOBILE_PREFIX + digits;
        }
        if (digits.startsWith(ARGENTINA_COUNTRY_CODE)
                && digits.length() == ARGENTINA_NATIONAL_NUMBER_LENGTH + 2) {
            return ARGENTINA_MOBILE_PREFIX + digits.substring(
                    ARGENTINA_COUNTRY_CODE.length());
        }
        return digits;
    }

    static String templateLanguageFor(String templateName) {
        return TEMPLATE_LANGUAGES.getOrDefault(templateName, "es");
    }

    static boolean isTemplateUnavailable(String messageType, int httpStatus, String responseBody) {
        if (messageType == null || !messageType.startsWith("TEMPLATE")) {
            return false;
        }
        return httpStatus == 404 || responseBody != null && responseBody.contains("132001");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppServiceDevMock.java`

```java
package com.lunaris.ansenuza.infrastructure.whatsapp;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.domain.model.Reservation;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Transporte de WhatsApp para desarrollo. Nunca realiza llamadas a Meta. */
@Service
@Profile("dev")
public class WhatsAppServiceDevMock extends WhatsAppService {

    private final Map<String, CopyOnWriteArrayList<SimulatorMessage>> messages =
            new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<SimulatorMessage> messagesFor(String phone) {
        return messages.getOrDefault(normalize(phone), new CopyOnWriteArrayList<>()).stream()
                .sorted(Comparator.comparingLong(SimulatorMessage::sequence))
                .toList();
    }

    public void recordUserMessage(String phone, String body, String payload) {
        add(phone, Direction.USER, payload == null ? MessageType.TEXT : MessageType.INTERACTIVE,
                body, null, null, payload);
    }

    public void recordUserMessage(String phone, IncomingMessage.MessageType incomingType,
            String body, String payload, String resourceUrl) {
        MessageType type = switch (incomingType) {
            case IMAGE -> MessageType.IMAGE;
            case DOCUMENT -> MessageType.DOCUMENT;
            case INTERACTIVE -> MessageType.INTERACTIVE;
            default -> MessageType.TEXT;
        };
        add(phone, Direction.USER, type, body, null, null,
                type == MessageType.INTERACTIVE ? payload : resourceUrl);
    }

    public void reset(String phone) {
        messages.remove(normalize(phone));
    }

    public String normalize(String phone) {
        return formatMetaPhoneNumber(phone);
    }

    @Override
    public void sendText(String to, String text, java.util.function.Consumer<Boolean> outcome) {
        sendText(to, text);
        outcome.accept(true);
    }

    @Override
    public void sendTemplate(String to, String template, List<String> parameters, java.util.function.Consumer<Boolean> outcome) {
        sendTemplate(to, template, parameters);
        outcome.accept(true);
    }

    @Override
    public void sendDocumentUrl(String to, String url, String name, String caption, java.util.function.Consumer<Boolean> outcome) {
        sendDocumentUrl(to, url, name, caption);
        outcome.accept(true);
    }

    @Override
    public void sendMessage(String phone, String message) {
        add(phone, Direction.BOT, MessageType.TEXT, message, null, null, null);
    }

    @Override
    public void sendText(String phone, String message) {
        sendMessage(phone, message);
    }

    @Override
    public void sendOtpMessage(String phone, String passengerName, String code) {
        add(phone, Direction.BOT, MessageType.TEMPLATE,
                "Código de verificación para " + safe(passengerName, "Pasajero") + ": " + code,
                null, null, null);
    }

    @Override
    public void sendOtp(String phone, String passengerName, String code) {
        sendOtpMessage(phone, passengerName, code);
    }

    @Override
    public void sendButtons(String phone, String header, String body, List<Button> buttons,
            java.util.function.Consumer<Boolean> outcome) {
        sendButtons(phone, header, body, buttons);
        reportOutcome(outcome, true);
    }

    @Override
    public void sendButtons(String phone, String header, String body, List<Button> buttons) {
        add(phone, Direction.BOT, MessageType.INTERACTIVE, body, header,
                buttons.stream().map(button -> new SimulatorButton(button.id(), button.title())).toList(), null);
    }

    @Override
    public boolean sendInteractiveButtons(String phone, String body, List<Map<String, String>> buttons) {
        return sendInteractiveButtons(phone, "Lunaris Ansenuza", body, buttons);
    }

    @Override
    public boolean sendInteractiveButtons(
            String phone, String header, String body, List<Map<String, String>> buttons) {
        add(phone, Direction.BOT, MessageType.INTERACTIVE, body, header,
                buttons.stream().map(button -> new SimulatorButton(
                        button.get("id"), button.get("title"))).toList(), null);
        return true;
    }

    @Override
    public boolean sendInteractiveList(String phone, String header, String body,
            String buttonLabel, List<Map<String, Object>> sections) {
        List<SimulatorButton> options = new ArrayList<>();
        if (sections != null) {
            sections.forEach(section -> {
                Object rows = section.get("rows");
                if (rows instanceof List<?> list) {
                    list.stream().filter(Map.class::isInstance).map(Map.class::cast)
                            .forEach(row -> options.add(new SimulatorButton(
                                    String.valueOf(row.get("id")), String.valueOf(row.get("title")))));
                }
            });
        }
        add(phone, Direction.BOT, MessageType.INTERACTIVE, body, header, options, null);
        return true;
    }

    @Override
    public void requestLocation(String phone, String message) {
        sendLocationRequest(phone, message);
    }

    @Override
    public void sendLocationRequest(String phone, String message) {
        add(phone, Direction.BOT, MessageType.LOCATION_REQUEST, message, null, null, null);
    }

    @Override
    public void sendImage(String phone, String imageUrl, String caption) {
        sendImageMessage(phone, imageUrl, caption);
    }

    @Override
    public void sendImageMessage(String phone, String imageUrl, String caption) {
        add(phone, Direction.BOT, MessageType.IMAGE, caption, null, null, imageUrl);
    }

    @Override
    public void sendMediaMessage(String phone, String type, String mediaUrl, String caption) {
        sendImageMessage(phone, mediaUrl, caption);
    }

    @Override
    public void sendDocument(String phone, String path, String fileName, String caption) {
        add(phone, Direction.BOT, MessageType.DOCUMENT, caption, fileName, null, path);
    }

    @Override
    public void sendDocumentUrl(String phone, String url, String fileName, String caption) {
        add(phone, Direction.BOT, MessageType.DOCUMENT, caption, fileName, null, url);
    }

    @Override
    public void sendTemplate(String phone, String templateName, List<String> values) {
        add(phone, Direction.BOT, MessageType.TEMPLATE,
                String.join("\n", values), templateName, null, null);
    }

    @Override
    public void sendDespiertaChoferTemplate(String phone, String driverName,
            UUID driverId, LocalDate travelDate) {
        add(phone, Direction.BOT, MessageType.INTERACTIVE,
                "Hola " + safe(driverName, "Chofer") + ", tu hoja de ruta está disponible.",
                "Hoja de ruta", List.of(new SimulatorButton("VIEW_ROUTE", "Ver ruta")), null);
    }

    @Override
    public DriverRouteDispatchResult sendDriverRouteDispatch(String phone, String driverName,
            String navigationUrl, List<Reservation> reservations) {
        sendMessage(phone, "Hoja de ruta para " + safe(driverName, "Chofer") + "\n" + navigationUrl);
        return new DriverRouteDispatchResult(true, "Hoja de ruta guardada en el simulador.");
    }

    private void add(String phone, Direction direction, MessageType type, String body,
            String header, List<SimulatorButton> buttons, String resourceUrl) {
        String normalized = normalize(phone);
        SimulatorMessage message = new SimulatorMessage(sequence.incrementAndGet(), normalized,
                direction, type, body, header, buttons == null ? List.of() : List.copyOf(buttons),
                resourceUrl, Instant.now());
        messages.computeIfAbsent(normalized, ignored -> new CopyOnWriteArrayList<>()).add(message);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public enum Direction { USER, BOT }
    public enum MessageType { TEXT, INTERACTIVE, IMAGE, DOCUMENT, TEMPLATE, LOCATION_REQUEST }
    public record SimulatorButton(String payload, String title) { }
    public record SimulatorMessage(long sequence, String phone, Direction direction, MessageType type,
            String body, String header, List<SimulatorButton> buttons, String resourceUrl,
            Instant timestamp) { }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/whatsapp/WhatsAppWebhookParser.java`

```java
 package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.Map;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.conversation.IncomingMessage.MessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce el payload crudo del webhook de WhatsApp Cloud API a un {@link IncomingMessage}
 * agnóstico. Aísla el formato propietario de Meta dentro de la capa de infraestructura.
 */
@Component
@Slf4j
public class WhatsAppWebhookParser {

    /**
     * @return el mensaje entrante, o {@code null} si el payload no contiene un mensaje procesable
     *         (eventos de status, payloads vacíos, etc.).
     */
    public IncomingMessage parse(Map<String, Object> payload) {
        Map<?, ?> entry = firstMap(payload == null ? null : payload.get("entry"));
        Map<?, ?> change = firstMap(entry == null ? null : entry.get("changes"));
        Map<?, ?> value = mapValue(change == null ? null : change.get("value"));
        Map<?, ?> message = firstMap(value == null ? null : value.get("messages"));
        if (message == null) {
            return null;
        }

        String from = normalizeWhatsAppNumber(stringValue(message.get("from")));
        String type = stringValue(message.get("type"));
        String messageId = stringValue(message.get("id"));

        if ("image".equals(type)) {
            Map<?, ?> imageData = mapValue(message.get("image"));
            String mediaId = imageData != null ? stringValue(imageData.get("id")) : null;
            return new IncomingMessage(messageId, from, MessageType.IMAGE, null, mediaId, null, null);
        }

        if ("text".equals(type)) {
            Map<?, ?> text = mapValue(message.get("text"));
            String body = text != null ? stringValue(text.get("body")) : null;
            return new IncomingMessage(messageId, from, MessageType.TEXT, body, null, null, null);
        }

        if ("location".equals(type)) {
            Map<?, ?> location = mapValue(message.get("location"));
            Double latitude = numberValue(location, "latitude");
            Double longitude = numberValue(location, "longitude");
            if (latitude == null || longitude == null) {
                return new IncomingMessage(messageId, from, MessageType.OTHER, null, null, null, null);
            }
            String mapsUrl = "https://maps.google.com/?q=" + latitude + "," + longitude;
            return new IncomingMessage(
                    messageId, from, MessageType.LOCATION, mapsUrl, null, latitude, longitude);
        }

        if ("button".equals(type)) {
            Map<?, ?> buttonData = mapValue(message.get("button"));
            String body = buttonData != null ? stringValue(buttonData.get("payload")) : null;
            if (body == null && buttonData != null) {
                body = stringValue(buttonData.get("text"));
            }
            if (body == null || body.isBlank()) return null;
            return new IncomingMessage(messageId, from, MessageType.INTERACTIVE, body, null, null, null);
        }

        if ("interactive".equals(type)) {
            String body = null;
            Map<?, ?> interactive = mapValue(message.get("interactive"));
            if (interactive != null) {
                if ("button_reply".equals(interactive.get("type"))) {
                    body = interactiveReplyId(interactive, "button_reply");
                } else if ("list_reply".equals(interactive.get("type"))) {
                    body = interactiveReplyId(interactive, "list_reply");
                }
            }
            log.info(
                    "[WhatsApp Webhook] Interactive response parsed. from={}, type={}, payload={}",
                    from, interactive != null ? interactive.get("type") : null, body);
            if (body == null || body.isBlank()) {
                log.warn("[WhatsApp Webhook] Respuesta interactiva descartada: falta un ID válido.");
                return null;
            }
            return new IncomingMessage(messageId, from, MessageType.INTERACTIVE, body, null, null, null);
        }

        return new IncomingMessage(messageId, from, MessageType.OTHER, null, null, null, null);
    }

    private String normalizeWhatsAppNumber(String phone) {
        return (phone != null && phone.startsWith("549")) ? "54" + phone.substring(3) : phone;
    }

    private Double numberValue(Map<?, ?> values, String key) {
        if (values == null || !(values.get(key) instanceof Number number)) {
            return null;
        }
        return number.doubleValue();
    }

    private String interactiveReplyId(Map<?, ?> interactive, String replyKey) {
        Object reply = interactive.get(replyKey);
        if (!(reply instanceof Map<?, ?> replyData)) {
            return null;
        }
        Object id = replyData.get("id");
        return id instanceof String value ? value : null;
    }

    private Map<?, ?> firstMap(Object value) {
        if (!(value instanceof java.util.List<?> values) || values.isEmpty()) return null;
        return mapValue(values.getFirst());
    }

    private Map<?, ?> mapValue(Object value) {
        return value instanceof Map<?, ?> map ? map : null;
    }

    private String stringValue(Object value) {
        return value instanceof String string ? string : null;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/out/payment/ExistingBankPaymentGatewayAdapter.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.out.payment;

import com.lunaris.ansenuza.application.payment.BankPaymentReservationPort;
import com.lunaris.ansenuza.reservation.application.port.out.PaymentGatewayPort;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import org.springframework.stereotype.Component;

/** Puente temporal hacia la integración bancaria que continúa atendiendo WhatsApp. */
@Component
public class ExistingBankPaymentGatewayAdapter implements PaymentGatewayPort {
    private final BankPaymentReservationPort delegate;
    public ExistingBankPaymentGatewayAdapter(BankPaymentReservationPort delegate) { this.delegate = delegate; }
    @Override public void confirm(Reservation reservation) {
        if (reservation.reservationCode() != null) delegate.confirm(reservation.reservationCode());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoPaymentClient.java`

```java
package com.lunaris.ansenuza.service.interurban;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import org.springframework.web.client.RestClient;

public class MercadoPagoPaymentClient implements PaymentQueryPort {
    private final RestClient client;

    public MercadoPagoPaymentClient(RestClient client) { this.client = client; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(String id, String status,
            @JsonProperty("external_reference") String reference,
            @JsonProperty("transaction_amount") BigDecimal amount,
            @JsonProperty("currency_id") String currency,
            @JsonProperty("collector_id") String collector,
            @JsonProperty("live_mode") Boolean liveMode,
            @JsonProperty("transaction_amount_refunded") BigDecimal refunded) {}

    @Override public Payment getPayment(String paymentId) {
        if (paymentId == null || !paymentId.matches("[0-9]{1,64}")) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_NOTIFICATION);
        }
        try {
            Response response = client.get().uri("/v1/payments/{id}", paymentId).retrieve().body(Response.class);
            if (response == null || response.id() == null || response.status() == null || response.amount() == null
                    || response.currency() == null || response.collector() == null || response.liveMode() == null
                    || response.refunded() == null) {
                throw new InterurbanPaymentException(InterurbanPaymentException.Code.PROVIDER_UNAVAILABLE);
            }
            return new Payment(response.id(), response.status(), response.reference(), response.amount(),
                    response.currency(), response.collector(), response.liveMode(), response.refunded());
        } catch (RuntimeException e) {
            // No propagar response body ni cabeceras de autorización hacia logs/HTTP.
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.PROVIDER_UNAVAILABLE);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoSignatureValidator.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MercadoPagoSignatureValidator {
    private final byte[] secret;
    private final Clock clock;
    private final Duration maxAge;

    public MercadoPagoSignatureValidator(String secret, Clock clock, Duration maxAge) {
        if (secret == null || secret.isBlank() || maxAge.isNegative() || maxAge.isZero()) {
            throw new IllegalArgumentException("Secret y ventana HMAC obligatorios.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
        this.maxAge = maxAge;
    }

    public void validate(String paymentId, String requestId, String signature) {
        try {
            if (paymentId == null || !paymentId.matches("[0-9]{1,64}")
                    || requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,128}")
                    || signature == null || signature.length() > 256) throw invalid();
            var parts = new HashMap<String, String>();
            for (String part : signature.split(",")) {
                String[] pair = part.trim().split("=", -1);
                if (pair.length != 2 || parts.putIfAbsent(pair[0].trim(), pair[1].trim()) != null) throw invalid();
            }
            String ts = parts.get("ts");
            String hash = parts.get("v1");
            if (ts == null || !ts.matches("[0-9]{10}|[0-9]{13}")
                    || hash == null || !hash.matches("[a-fA-F0-9]{64}")) throw invalid();
            // MP presenta ejemplos en segundos y milisegundos. Firmar SIEMPRE el ts original.
            long timestamp = Long.parseLong(ts);
            Instant sentAt = ts.length() == 13 ? Instant.ofEpochMilli(timestamp) : Instant.ofEpochSecond(timestamp);
            Instant now = clock.instant();
            if (sentAt.isBefore(now.minus(maxAge)) || sentAt.isAfter(now.plusSeconds(60))) throw invalid();
            String manifest = "id:" + paymentId.toLowerCase(Locale.ROOT) + ";request-id:" + requestId + ";ts:" + ts + ";";
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            if (!MessageDigest.isEqual(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)),
                    HexFormat.of().parseHex(hash))) throw invalid();
        } catch (InterurbanPaymentException e) {
            throw e;
        } catch (Exception e) {
            throw invalid();
        }
    }

    private InterurbanPaymentException invalid() {
        return new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_SIGNATURE);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoWebhookController.java`

```java
package com.lunaris.ansenuza.service.interurban;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class MercadoPagoWebhookController {
    private final MercadoPagoWebhookService service;

    public MercadoPagoWebhookController(MercadoPagoWebhookService service) { this.service = service; }

    @PostMapping("/webhook/interurban/mercadopago")
    public ResponseEntity<Void> receive(@RequestParam("data.id") String paymentId,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestBody MercadoPagoWebhookService.Notification body) {
        service.receive(paymentId, requestId, signature, body);
        return ResponseEntity.ok().build();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/MercadoPagoWebhookService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class MercadoPagoWebhookService {
    public record Notification(String type, String action, Data data) {
        public record Data(String id) {}
    }

    private final MercadoPagoSignatureValidator signatures;
    private final PaymentQueryPort payments;
    private final InterurbanPaymentRepository repository;
    private final QrGeneratorService qr;
    private final TransactionTemplate transaction;
    private final String collectorId;
    private final boolean liveMode;
    private final Clock clock;

    public MercadoPagoWebhookService(MercadoPagoSignatureValidator signatures, PaymentQueryPort payments,
            InterurbanPaymentRepository repository, QrGeneratorService qr, PlatformTransactionManager txManager,
            String collectorId, boolean liveMode, Clock clock) {
        this.signatures = signatures;
        this.payments = payments;
        this.repository = repository;
        this.qr = qr;
        this.collectorId = collectorId;
        this.liveMode = liveMode;
        this.clock = clock;
        this.transaction = new TransactionTemplate(txManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Confirma recepción sólo después del commit del inbox. El cuerpo no autoriza pagos. */
    public void receive(String queryPaymentId, String requestId, String signature, Notification notification) {
        signatures.validate(queryPaymentId, requestId, signature);
        if (notification == null || !"payment".equals(notification.type()) || notification.data() == null
                || !queryPaymentId.equals(notification.data().id())
                || !("payment.updated".equals(notification.action()) || "payment.created".equals(notification.action()))) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_NOTIFICATION);
        }
        // El event-id del body no está firmado. Deduplicar mediante datos autenticados.
        String eventKey = fingerprint(queryPaymentId + ":" + requestId + ":" + signature);
        transaction.executeWithoutResult(status -> repository.enqueue(eventKey, queryPaymentId));
    }

    @Scheduled(fixedDelayString = "${lunaris.interurban.payments.poll-ms:10000}")
    public void processPending() {
        for (var inbox : repository.pending()) {
            try {
                process(inbox);
            } catch (RuntimeException e) {
                // Se conserva pendiente. No loguear respuestas API, firmas ni tokens.
                transaction.executeWithoutResult(status -> repository.retry(inbox.id()));
            }
        }
    }

    public void process(InterurbanPaymentRepository.Inbox inbox) {
        // HTTP antes de adquirir locks. Cada intento consulta nuevamente al proveedor.
        var payment = payments.getPayment(inbox.paymentId());
        transaction.executeWithoutResult(status -> reconcile(inbox, payment));
    }

    private void reconcile(InterurbanPaymentRepository.Inbox inbox, PaymentQueryPort.Payment payment) {
        if (!repository.lockPending(inbox.id())) return;
        if (payment == null || !inbox.paymentId().equals(payment.id()) || !collectorId.equals(payment.collectorId())
                || payment.liveMode() != liveMode || !"ARS".equals(payment.currency())
                || payment.amount() == null || payment.amount().signum() < 0 || payment.refundedAmount() == null) {
            review(inbox, "PAYMENT_IDENTITY_MISMATCH");
            return;
        }
        UUID bookingId = bookingId(payment.externalReference());
        if (bookingId == null) {
            review(inbox, "INVALID_REFERENCE");
            return;
        }
        repository.lockPayment(payment.id());
        if (repository.paymentBooking(payment.id()).filter(id -> !id.equals(bookingId)).isPresent()) {
            review(inbox, "PAYMENT_BOOKING_MISMATCH");
            return;
        }
        var reservations = repository.lockBooking(bookingId);
        if (reservations.isEmpty()) {
            review(inbox, "BOOKING_NOT_FOUND");
            return;
        }
        var previous = repository.paymentStatus(payment.id()).orElse("");
        if ("REVOKED".equals(previous)) {
            repository.complete(inbox.id(), "ALREADY_REVOKED");
            return;
        }
        if ("refunded".equals(payment.status()) || "charged_back".equals(payment.status())
                || payment.refundedAmount().signum() > 0) {
            if ("APPROVED".equals(previous)) {
                repository.revokeBooking(bookingId, clock.instant());
            }
            repository.recordPayment(payment, bookingId, "REVOKED");
            review(inbox, "REFUND_REQUIRES_REVIEW");
            return;
        }
        if (!"approved".equals(payment.status())) {
            repository.complete(inbox.id(), "NOT_APPROVED");
            return;
        }
        BigDecimal expected = reservations.stream().map(InterurbanPaymentRepository.Reservation::fare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (expected.compareTo(payment.amount()) != 0) {
            review(inbox, "AMOUNT_MISMATCH");
            return;
        }
        if ("APPROVED".equals(previous)) {
            repository.complete(inbox.id(), "ALREADY_APPROVED");
            return;
        }
        if (reservations.stream().anyMatch(r -> !"HELD".equals(r.status())
                || !r.holdExpiresAt().isAfter(clock.instant()) || !r.qrExpiresAt().isAfter(clock.instant())
                || !r.completeSeats() || "CANCELLED".equals(r.tripStatus()) || "COMPLETED".equals(r.tripStatus()))) {
            review(inbox, "BOOKING_NOT_PAYABLE");
            return;
        }
        repository.recordPayment(payment, bookingId, "APPROVED");
        for (var reservation : reservations) {
            repository.markPaid(reservation.id());
            qr.generate(reservation.id(), reservation.qrExpiresAt());
        }
        repository.complete(inbox.id(), "APPROVED");
    }

    private void review(InterurbanPaymentRepository.Inbox inbox, String reason) {
        repository.review(inbox.paymentId(), reason);
        repository.complete(inbox.id(), reason);
    }

    private UUID bookingId(String reference) {
        if (reference == null || !reference.startsWith("INTERURBAN:")) return null;
        String id = reference.substring("INTERURBAN:".length());
        try {
            UUID uuid = UUID.fromString(id);
            return uuid.toString().equalsIgnoreCase(id) ? uuid : null;
        } catch (IllegalArgumentException e) { return null; }
    }

    private String fingerprint(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_SIGNATURE); }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/SimulatedDriverPayoutAdapter.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Revisión interna exclusivamente: no realiza transferencias ni confirma pagos. */
public class SimulatedDriverPayoutAdapter implements DriverPayoutPort {
    private static final Logger log = LoggerFactory.getLogger(SimulatedDriverPayoutAdapter.class);
    @Override public List<Result> submitBatch(List<Order> orders) {
        return orders.stream().map(order -> {
            log.info("Liquidación {} lista para aprobación interna; clave={}", order.id(), order.idempotencyKey());
            return new Result(order.idempotencyKey(), Status.READY_FOR_APPROVAL, null);
        }).toList();
    }
    @Override public Result reconcile(String idempotencyKey) {
        return new Result(idempotencyKey, Status.READY_FOR_APPROVAL, null);
    }
}
```
