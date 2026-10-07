# 2. CAPA DE NEGOCIO / SERVICIOS

158 archivos. Rutas relativas a la raíz del repositorio. Código original, sin reformatear.

La agrupación es funcional: no modifica paquetes. Cada archivo aparece una sola vez; los archivos con responsabilidades mixtas se asignan a su función principal.

## Índice

- `src/main/java/com/lunaris/ansenuza/LunarisAnsenuzaApplication.java`
- `src/main/java/com/lunaris/ansenuza/application/port/Button.java`
- `src/main/java/com/lunaris/ansenuza/application/port/ChatbotTelemetryPort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/DriverDocumentStoragePort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/InvoiceStoragePort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/LiveChatPort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/MessagingPort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/NewsBannerStoragePort.java`
- `src/main/java/com/lunaris/ansenuza/application/port/PassengerContactTemplate.java`
- `src/main/java/com/lunaris/ansenuza/application/port/ReceiptStoragePort.java`
- `src/main/java/com/lunaris/ansenuza/application/scheduler/DailyReturnScheduler.java`
- `src/main/java/com/lunaris/ansenuza/application/scheduler/OpenReturnCutoffScheduler.java`
- `src/main/java/com/lunaris/ansenuza/application/scheduler/ReservationPaymentExpirationScheduler.java`
- `src/main/java/com/lunaris/ansenuza/application/scheduler/ReturnScheduleAuditScheduler.java`
- `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotEventType.java`
- `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotInteraction.java`
- `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotReason.java`
- `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotSignal.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/BookingVerificationData.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/BotMonitorService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CompleteTripUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ConfirmPaymentUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CreateFareLocalityService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CreateManualReservationUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CreatePassengerUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CreateReservationUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/CreateSpecialTripService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/DailyPassengerManifestService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/DeleteFareLocalityService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/DriverApplicationManagementService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/DriverAuthorizationService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/DriverManagementService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ExpireReservationPaymentUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/FareLocalityValidation.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetBillingPanelUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetDailyOperationSummaryUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetFaresService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetHojaDeRutaUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetPassengerProfileUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetPublicReservationStatusUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/GetSpecialTripsService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/InquiryService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/InvoicePersistenceService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/IssueInvoiceUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/LocalityService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ManualReservationNotificationService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/NewsBannerService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/OnboardPassengerUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/OperatorNotificationService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/OperatorPhoneService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/PassengerOtpService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/PersistPaymentReceiptUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ProcessPaymentReceiptUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ProcessPromotionCommandUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ReservationDriverAssignmentService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ResolveEffectiveTripOriginService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ScheduleService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/SubmitDriverApplicationUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/TakeOverConversationUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/ToggleSpecialTripStatusService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateFareService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateLocalityFareService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/UpdatePassengerAddressUseCase.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateSpecialTripService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListConversionService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListOtpService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListReengagementService.java`
- `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListService.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/DomainValidationException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/DriverApplicationNotFoundException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/FareLocalityInUseException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/InquiryNotFoundException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/MassivePromotionAlreadyUsedException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/PromotionExpiredException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/ReservationAlreadyCompletedException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/SameDayBookingClosedException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/SeatCapacityExceededException.java`
- `src/main/java/com/lunaris/ansenuza/domain/exception/SpecialTripNotFoundException.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ConversationState.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ConversationStep.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/InquiryStatus.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ManualReservationCreated.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/OperatorNotification.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/PassengerMessageReceived.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/ReservationSource.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/Role.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/SpecialTrip.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/TripType.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/AirportTripDetector.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/BookingInvoiceAmount.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/CuilCalculator.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/DriverRouteService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/FleetCapacityService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/OperationControlService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/PricingAndScheduleService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/PromotionService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/ReservationCancellationService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/ReservationService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/ReturnCapacityPolicy.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/SameDayBookingPolicy.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/SystemConfigurationService.java`
- `src/main/java/com/lunaris/ansenuza/domain/model/service/TripRouteCalculatorService.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/CreateFareLocalityUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/CreateSpecialTripUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/DeleteFareLocalityUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/FareLocalityView.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/GetFaresQuery.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/GetSpecialTripsQuery.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/ResolveEffectiveTripOriginUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/RouteOriginResolution.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/SpecialTripCommand.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/ToggleSpecialTripStatusUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateFareUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateLocalityFareUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateSpecialTripUseCase.java`
- `src/main/java/com/lunaris/ansenuza/domain/port/out/SpecialTripRepositoryPort.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/AdminUserInitializer.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/AsyncConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/ChatbotAnalyticsProperties.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/DataInitializer.java`
- `src/main/java/com/lunaris/ansenuza/reservation/application/port/in/ConfirmPaymentUseCase.java`
- `src/main/java/com/lunaris/ansenuza/reservation/application/port/in/CreateReservationUseCase.java`
- `src/main/java/com/lunaris/ansenuza/reservation/application/port/out/PaymentGatewayPort.java`
- `src/main/java/com/lunaris/ansenuza/reservation/application/port/out/ReservationRepositoryPort.java`
- `src/main/java/com/lunaris/ansenuza/reservation/application/service/ReservationApplicationService.java`
- `src/main/java/com/lunaris/ansenuza/reservation/domain/exception/ReservationNotFoundException.java`
- `src/main/java/com/lunaris/ansenuza/reservation/domain/model/Reservation.java`
- `src/main/java/com/lunaris/ansenuza/reservation/domain/model/ReservationStatus.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/BookingClosedException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityExceededException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/CheckInService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DailyDriverPayoutJob.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverIdentityPort.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverPayoutPort.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverPayoutReviewService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverRouteSheetQuery.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/DriverSettlementService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/ExistingDriverIdentityAdapter.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanCatalog.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanSettlementConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidHoldException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidQrException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidRouteException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidTripException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/PassengerHold.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/PaymentQueryPort.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/QrAlreadyConsumedException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/QrArtifactCipher.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/QrExpiredException.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/QrGeneratorService.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/Route.java`
- `src/main/java/com/lunaris/ansenuza/shared/ArgentinaTime.java`
- `src/main/java/com/lunaris/ansenuza/shared/PhoneUtils.java`

## Archivo: `src/main/java/com/lunaris/ansenuza/LunarisAnsenuzaApplication.java`

```java
package com.lunaris.ansenuza;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableRetry
public class LunarisAnsenuzaApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Cordoba"));
		SpringApplication.run(LunarisAnsenuzaApplication.class, args);
	}

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/Button.java`

```java
package com.lunaris.ansenuza.application.port;

/**
 * Botón interactivo de respuesta rápida, agnóstico del canal de mensajería.
 * El adaptador de salida (ej: WhatsApp) lo traduce al formato del proveedor.
 */
public record Button(String id, String title) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/ChatbotTelemetryPort.java`

```java
package com.lunaris.ansenuza.application.port;

import com.lunaris.ansenuza.application.telemetry.ChatbotInteraction;
import com.lunaris.ansenuza.application.telemetry.ChatbotSignal;

public interface ChatbotTelemetryPort {
    ChatbotInteraction begin(String phone, String messageId, boolean test);
    void record(ChatbotSignal signal);
    default void handoff(String phone) {}

    ChatbotTelemetryPort NOOP = new ChatbotTelemetryPort() {
        public ChatbotInteraction begin(String phone, String messageId, boolean test) {
            return ChatbotInteraction.NONE;
        }
        public void record(ChatbotSignal signal) {}
    };
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/DriverDocumentStoragePort.java`

```java
package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface DriverDocumentStoragePort {

    String store(String documentType, MultipartFile file);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/InvoiceStoragePort.java`

```java
package com.lunaris.ansenuza.application.port;

/**
 * Puerto de salida para persistir el PDF de la factura que sube la operadora.
 * La capa de aplicación no conoce el sistema de archivos concreto.
 */
public interface InvoiceStoragePort {

    /** Guarda el contenido del PDF y devuelve su ubicación web y absoluta. */
    StoredInvoice store(byte[] content, String desiredFileName);

    /** Resuelve la ruta absoluta en disco a partir de la URL web guardada (para reenviar). */
    String resolveAbsolutePath(String pdfUrl);

    /** Recupera el PDF persistido para servirlo con headers HTTP controlados. */
    byte[] load(String pdfUrl);

    record StoredInvoice(String webUrl, String absolutePath) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/LiveChatPort.java`

```java
package com.lunaris.ansenuza.application.port;

/**
 * Puerto de salida para reflejar los mensajes entrantes del cliente en la sala
 * de chat en vivo del operador (persistencia + broadcast por WebSocket).
 */
public interface LiveChatPort {

    default void conversationChanged() {}

    void recordIncomingMessage(String phoneNumber, String text);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/MessagingPort.java`

```java
package com.lunaris.ansenuza.application.port;

import java.util.List;

/**
 * Puerto de salida para enviar mensajes salientes al pasajero.
 * La capa de aplicación depende de esta abstracción, nunca del proveedor concreto
 * (WhatsApp Cloud API), respetando la inversión de dependencias hexagonal.
 */
public interface MessagingPort {

    void sendText(String to, String message);

    default void sendText(String to, String message, java.util.function.Consumer<Boolean> outcome) {
        sendText(to, message);
    }

    default void sendTemplate(String to, String template, List<String> parameters,
            java.util.function.Consumer<Boolean> outcome) {
        sendTemplate(to, template, parameters);
    }

    default void sendDocumentUrl(String to, String url, String fileName, String caption,
            java.util.function.Consumer<Boolean> outcome) {
        sendDocumentUrl(to, url, fileName, caption);
    }

    void sendButtons(String to, String header, String body, List<Button> buttons);

    /** Result callback is invoked only when an adapter knows the actual send outcome. */
    default void sendButtons(String to, String header, String body, List<Button> buttons,
            java.util.function.Consumer<Boolean> outcome) {
        sendButtons(to, header, body, buttons);
    }

    void requestLocation(String to, String message);

    void sendImage(String to, String imageUrl, String caption);

    void sendTemplate(String to, String templateName, List<String> parameters);

    void sendOtp(String to, String passengerName, String code);

    /**
     * Envía un documento (PDF) ubicado en {@code absoluteFilePath} al destinatario.
     * Usado para mandar la factura por WhatsApp.
     */
    void sendDocument(String to, String absoluteFilePath, String fileName, String caption);

    /** Envía un documento directamente desde una URL HTTPS (Cloudinary, por ejemplo). */
    default void sendDocumentUrl(String to, String documentUrl, String fileName, String caption) {
        sendDocument(to, documentUrl, fileName, caption);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/NewsBannerStoragePort.java`

```java
package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface NewsBannerStoragePort {

    String upload(MultipartFile image);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/PassengerContactTemplate.java`

```java
package com.lunaris.ansenuza.application.port;

import java.util.List;

/** Contrato compartido con la plantilla de reactivación del Chat en Vivo. */
public final class PassengerContactTemplate {
    public static final String NAME = "contacto_pasajero";

    private PassengerContactTemplate() {}

    public static List<String> parameters(String passengerName) {
        return List.of(passengerName == null || passengerName.isBlank()
                ? "Pasajero" : passengerName.replaceAll("\\s+", " ").trim());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/port/ReceiptStoragePort.java`

```java
package com.lunaris.ansenuza.application.port;

import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de salida para descargar y persistir el comprobante de pago enviado
 * por el pasajero. Devuelve la URL web local o de Cloudinary del comprobante, 
 * o {@code null} si la descarga falló.
 */
public interface ReceiptStoragePort {

    // Tu método original del Bot (Mantenido intacto)
    String downloadAndSaveReceipt(String mediaId);

    // 🔥 NUEVO: Método para que Martín suba archivos desde el formulario web
    String uploadFile(MultipartFile file);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/scheduler/DailyReturnScheduler.java`

```java
package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyReturnScheduler {

    private static final String RETURN_SCHEDULER_TIME_KEY = "return.scheduler.time";
    private static final String RETURN_MESSAGE_HEADER_KEY = "return.message.header";
    private static final String RETURN_MESSAGE_BODY_KEY = "return.message.body";
    private static final String RETURN_BUTTON_YES_TITLE_KEY = "return.button.yes.title";
    private static final String RETURN_BUTTON_LATER_TITLE_KEY = "return.button.later.title";
    private static final String RETURN_BUTTON_NO_TITLE_KEY = "return.button.no.title";

    private static final String DEFAULT_RETURN_SCHEDULER_TIME = "15:00";
    private static final String DEFAULT_RETURN_MESSAGE_HEADER = "Confirmación de vuelta";
    private static final String DEFAULT_RETURN_MESSAGE_BODY = """
            Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?
            Elegí una opción para que podamos organizar las butacas.
            """;

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final SystemConfigurationService configurationService;

    private LocalDate lastExecutionDate;

    @Scheduled(fixedDelayString = "60000")
    public void askPassengersAboutTodayReturn() {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        LocalTime configuredTime = resolveConfiguredReturnTime();
        LocalTime now = com.lunaris.ansenuza.shared.ArgentinaTime.currentTime()
                .withSecond(0).withNano(0);

        if (!now.equals(configuredTime)) {
            return;
        }

        if (today.equals(lastExecutionDate)) {
            return;
        }

        lastExecutionDate = today;

        List<Reservation> returnReservations = new ArrayList<>(
                reservationRepository.findScheduledReturnsWithRealizedOutbound(today, TravelStatus.REALIZED));

        returnReservations.addAll(
                reservationRepository.findRealizedOutboundReservationsWithReturnDate(today, TravelStatus.REALIZED));

        Set<String> notifiedPhones = new LinkedHashSet<>();
        for (Reservation reservation : returnReservations) {
            if (reservation.getPassenger() == null || reservation.getPassenger().getPhone() == null
                    || reservation.getPassenger().getPhone().isBlank()) {
                log.warn("[DailyReturnScheduler] Reserva {} sin teléfono de pasajero; se omite.",
                        reservation.getId());
                continue;
            }

            String phone = reservation.getPassenger().getPhone().trim();
            if (!notifiedPhones.add(phone)) {
                continue;
            }

            String header = configurationService.getValue(RETURN_MESSAGE_HEADER_KEY, DEFAULT_RETURN_MESSAGE_HEADER);
            String body = configurationService.getValue(RETURN_MESSAGE_BODY_KEY, DEFAULT_RETURN_MESSAGE_BODY);

            whatsAppService.sendInteractiveButtons(phone, header, body, buildReturnDecisionButtons());
            log.info("[DailyReturnScheduler] Botones de vuelta enviados a {} para la fecha {}.", phone, today);
        }
    }

    private LocalTime resolveConfiguredReturnTime() {
        String configuredTime = configurationService.getValue(
                RETURN_SCHEDULER_TIME_KEY,
                DEFAULT_RETURN_SCHEDULER_TIME);
        try {
            return LocalTime.parse(configuredTime.trim()).withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            log.warn("[DailyReturnScheduler] Hora configurada inválida '{}'. Usando {}.",
                    configuredTime, DEFAULT_RETURN_SCHEDULER_TIME);
            return LocalTime.parse(DEFAULT_RETURN_SCHEDULER_TIME);
        }
    }

    private List<Map<String, String>> buildReturnDecisionButtons() {
        return List.of(
                Map.of(
                        "id", "return_yes_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_YES_TITLE_KEY, "SÍ, VOLVER ✅")),
                Map.of(
                        "id", "return_later_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_LATER_TITLE_KEY, "OTRO DÍA 📅")),
                Map.of(
                        "id", "return_no_ID",
                        "title", configurationService.getValue(RETURN_BUTTON_NO_TITLE_KEY, "NO, CANCELAR ❌")));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/scheduler/OpenReturnCutoffScheduler.java`

```java
package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Component
@RequiredArgsConstructor
public class OpenReturnCutoffScheduler {
    private final ReservationRepository reservations;
    private final ReservationEventRepository events;
    private final CapacityLockRepository locks;
    private final SimpMessagingTemplate messaging;

    @Scheduled(cron = "0 0 11 * * *", zone = "America/Argentina/Cordoba")
    @Transactional
    public void alertPendingReturns() {
        var today = ArgentinaTime.today();
        String key = today + "|DAY|RETURN";
        locks.ensureExists(key);
        if (locks.findForUpdate(key) == null) throw new IllegalStateException("No se pudo bloquear el regreso.");
        var passengers = new ArrayList<String>();
        for (Reservation reservation : reservations.findReturnCapacityCandidates(today)) {
            if (reservation.getTravelStatus() != Reservation.TravelStatus.OPEN_RETURN
                    || events.existsByReservationIdAndEventTypeAndCreatedAtGreaterThanEqual(
                            reservation.getId(), "OPEN_RETURN_CUTOFF", today.atStartOfDay())) continue;
            String label = reservation.getReservationCode() + " - "
                    + (reservation.getPassenger() == null ? "Sin pasajero" : reservation.getPassenger().getFirstName() + " " + reservation.getPassenger().getLastName());
            events.save(ReservationEvent.builder().id(UUID.randomUUID())
                    .reservationId(reservation.getId()).eventType("OPEN_RETURN_CUTOFF")
                    .description("11:00: vuelta sin confirmar; cupo preventivo liberado.")
                    .triggeredBy("SYSTEM").build());
            passengers.add(label);
        }
        if (!passengers.isEmpty()) {
            messaging.convertAndSend("/topic/system-alerts", Map.of(
                    "action", "OPEN_RETURN_CUTOFF", "message",
                    "Vueltas sin confirmar: cupos liberados a las 11:00. " + String.join(", ", passengers)));
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/scheduler/ReservationPaymentExpirationScheduler.java`

```java
package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.application.usecase.ExpireReservationPaymentUseCase;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentExpirationScheduler {

    private final ReservationRepository reservationRepository;
    private final ExpireReservationPaymentUseCase expirationUseCase;

    @Scheduled(fixedDelayString = "${lunaris.reservations.expiration-scan-ms:60000}")
    public void expirePendingPayments() {
        var now = ArgentinaTime.now();
        int expired = reservationRepository
                .findExpiredPaymentCandidateIds(now, PageRequest.of(0, 100))
                .stream()
                .mapToInt(id -> expirationUseCase.execute(id, now))
                .sum();
        if (expired > 0) log.info("Reservas expiradas por TTL de pago: {}", expired);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/scheduler/ReturnScheduleAuditScheduler.java`

```java
package com.lunaris.ansenuza.application.scheduler;

import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnScheduleAuditScheduler {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final WhatsAppService whatsAppService;

    @Scheduled(cron = "0 0 9 * * *", zone = "America/Argentina/Cordoba")
    public void auditReturnSchedules() {
        LocalDate today = ArgentinaTime.today();
        Map<String, Reservation> candidateByPhone = new LinkedHashMap<>();
        reservationRepository.findReturnScheduleAuditCandidates(today.atStartOfDay())
                .stream()
                .filter(reservation -> reservation.getPassenger() != null)
                .filter(reservation -> reservation.getPassenger().getPhone() != null
                        && !reservation.getPassenger().getPhone().isBlank())
                .forEach(reservation -> candidateByPhone.merge(
                        reservation.getPassenger().getPhone().trim(), reservation,
                        ReturnScheduleAuditScheduler::preferReturnLeg));

        candidateByPhone.forEach((phone, candidate) -> {
            try {
                Reservation reservation = candidate;
                if (reservation == null || alreadyAuditedToday(reservation, today)) {
                    return;
                }
                ConversationSession session = conversationSessionRepository.findByPhoneNumber(phone)
                        .orElseGet(() -> ConversationSession.builder().phoneNumber(phone).build());
                if (session.isBotPaused()) {
                    log.info("[ReturnScheduleAudit] Se omite {}: conversación bajo atención humana.",
                            phone);
                    return;
                }
                if (session.getCurrentStep() != null
                        && !"RETURN_WINDOW_SELECTION".equals(session.getCurrentStep())) {
                    log.info("[ReturnScheduleAudit] Se omite {}: conversación activa en {}.",
                            phone, session.getCurrentStep());
                    return;
                }
                if (reservation.getId() != null
                        && reservationRepository.claimReturnAudit(reservation.getId(),
                                ArgentinaTime.now(), today.atStartOfDay()) != 1) {
                    return;
                }
                session.setCurrentStep("START");
                session.setReservationCode(reservation.getReservationCode());
                conversationSessionRepository.saveAndFlush(session);
                // La marca se reclama atómicamente antes de la llamada externa para que
                // dos instancias nunca dupliquen el prompt.
                whatsAppService.sendInteractiveButtons(
                        phone,
                        "Confirmación de regreso",
                        "¿Volvés hoy?",
                        List.of(
                                Map.of("id", "return_yes_ID", "title", "Sí, vuelvo hoy"),
                                Map.of("id", "return_postpone", "title", "No vuelvo hoy")));
                log.info("[ReturnScheduleAudit] Regreso consultado para reserva {}.",
                        reservation.getReservationCode());
            } catch (Exception exception) {
                log.error("[ReturnScheduleAudit] Error procesando aviso para {}", phone, exception);
            }
        });
    }

    private boolean alreadyAuditedToday(Reservation reservation, LocalDate today) {
        return reservation.getReturnAuditSentAt() != null
                && reservation.getReturnAuditSentAt().toLocalDate().equals(today);
    }

    private static Reservation preferReturnLeg(Reservation first, Reservation second) {
        return returnPriority(second) > returnPriority(first) ? second : first;
    }

    private static int returnPriority(Reservation reservation) {
        if (reservation.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN) return 3;
        String code = reservation.getReservationCode();
        if (code != null && (code.endsWith("-VUELTA") || code.startsWith("VTA-"))) return 2;
        return com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService
                .isCordoba(reservation.getPickupLocality()) ? 1 : 0;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotEventType.java`

```java
package com.lunaris.ansenuza.application.telemetry;

public enum ChatbotEventType {
    SESSION_STARTED, MESSAGE_RECEIVED, STEP_CHANGED, PASSENGER_IDENTIFIED, DRIVER_IDENTIFIED,
    BOOKING_STARTED, PRICE_REQUESTED, PRICE_SENT, ROUTE_SELECTED, DATE_SELECTED,
    PASSENGER_DATA_COMPLETED, SUMMARY_SENT, BOOKING_CREATED, BOOKING_DECLINED,
    SESSION_EXPIRED, HUMAN_HANDOFF, WAITLISTED, FLOW_BLOCKED, INPUT_REJECTED, SEND_FAILED
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotInteraction.java`

```java
package com.lunaris.ansenuza.application.telemetry;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.ChatbotTelemetryPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import java.util.List;
import java.util.UUID;

/** Immutable context passed explicitly across business transactions and async sends. */
public record ChatbotInteraction(ChatbotTelemetryPort port, UUID sessionId, String interactionKey) {
    public static final ChatbotInteraction NONE = new ChatbotInteraction(null, null, "");
    public boolean active() { return port != null && sessionId != null; }

    public void emit(ChatbotEventType type, String step) { emit(type, step, null); }
    public void emit(ChatbotEventType type, String step, ChatbotReason reason) {
        if (active()) port.record(new ChatbotSignal(sessionId, interactionKey, type, step, reason, null, null));
    }
    public void bookingCreated(String step, String group, UUID reservationId) {
        if (active()) port.record(new ChatbotSignal(sessionId, interactionKey,
                ChatbotEventType.BOOKING_CREATED, step, null, group, reservationId));
    }
    public void sendButtons(MessagingPort messaging, String to, String header, String body,
            List<Button> buttons, ChatbotEventType success, String step) {
        if (!active()) {
            messaging.sendButtons(to, header, body, buttons);
            return;
        }
        messaging.sendButtons(to, header, body, buttons, sent -> emit(
                sent ? success : ChatbotEventType.SEND_FAILED, step,
                sent ? null : ChatbotReason.SEND_FAILED));
    }
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotReason.java`

```java
package com.lunaris.ansenuza.application.telemetry;

public enum ChatbotReason {
    NO_FARE, NO_CAPACITY, DATE_CUTOFF, INVALID_INPUT, USER_DECLINED,
    INACTIVITY, OPERATOR, UNSUPPORTED_MESSAGE, PROCESSING_FAILED, SEND_FAILED
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/telemetry/ChatbotSignal.java`

```java
package com.lunaris.ansenuza.application.telemetry;

import java.util.UUID;

/** Only pseudonymous correlation and controlled codes; never message content. */
public record ChatbotSignal(UUID sessionId, String interactionKey, ChatbotEventType type,
        String step, ChatbotReason reason, String bookingGroupCode, UUID reservationId) {}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/BookingVerificationData.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.TripType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingVerificationData(
        LocalDate travelDate,
        String scheduleBlock,
        String pickupLocality,
        String destination,
        Integer passengerCount,
        TripType tripType,
        BigDecimal totalAmount) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/BotMonitorService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class BotMonitorService {
    private final ConversationSessionRepository sessions;
    private final SimpMessagingTemplate messaging;

    @Transactional(readOnly = true)
    public List<ConversationSessionRepository.MonitorRow> rows() {
        return sessions.findMonitorRows();
    }

    @Transactional
    public void setPaused(long id, boolean paused) {
        var session = sessions.findById(id)
                .orElseThrow(() -> new DomainValidationException("Sesión no encontrada."));
        session.setBotPaused(paused);
        session.setManuallyPaused(paused);
        sessions.saveAndFlush(session);
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() {
                        messaging.convertAndSend("/topic/bot-monitor", Map.of("action", "REFRESH"));
                    }
                });
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CompleteTripUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

/** Cierra atómicamente una hoja de ruta despachada por un chofer. */
@Service
@RequiredArgsConstructor
public class CompleteTripUseCase {
    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public int execute(UUID driverId, LocalDate effectiveDate, String direction) {
        if (driverId == null || effectiveDate == null) {
            throw new IllegalArgumentException("Chofer y fecha son obligatorios.");
        }
        String normalizedDirection = direction == null ? null : direction.trim().toUpperCase();
        List<Reservation> route = reservationRepository.findAllAssignedByDriverId(driverId).stream()
                .filter(r -> effectiveDate.equals(effectiveDate(r)))
                .filter(r -> normalizedDirection == null || normalizedDirection.equalsIgnoreCase(r.getRouteDirection()))
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getTravelStatus() != Reservation.TravelStatus.CANCELED
                        && r.getTravelStatus() != Reservation.TravelStatus.NO_SHOW
                        && r.getTravelStatus() != Reservation.TravelStatus.COMPLETED
                        && r.getTravelStatus() != Reservation.TravelStatus.REALIZED)
                .filter(this::isBoarded)
                .toList();
        for (Reservation reservation : route) {
            reservation.setTravelStatus(Reservation.TravelStatus.COMPLETED);
            reservation.setStatus("COMPLETED");
            reservationRepository.save(reservation);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId())
                    .eventType("TRIP_COMPLETED_BY_DRIVER")
                    .description("Viaje finalizado por el chofer")
                    .triggeredBy("DRIVER_WHATSAPP")
                    .build());
        }
        return route.size();
    }

    private boolean isBoarded(Reservation reservation) {
        return reservation.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || reservation.getTravelStatus() == Reservation.TravelStatus.ONBOARDED;
    }

    private LocalDate effectiveDate(Reservation reservation) {
        if ("VUELTA".equalsIgnoreCase(reservation.getRouteDirection())
                && reservation.getReturnDate() != null) {
            return reservation.getReturnDate();
        }
        return reservation.getTravelDate() != null ? reservation.getTravelDate() : reservation.getReturnDate();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ConfirmPaymentUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConfirmPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final PromotionService promotionService;
    private final WaitingListRepository waitingListRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final ReservationEventRepository eventRepository;
    private final MessagingPort messaging;

    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions) {
        this(reservations, promotions, waitingLists, sessions, null, null);
    }

    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions,
            ReservationEventRepository events) {
        this(reservations, promotions, waitingLists, sessions, events, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ConfirmPaymentUseCase(ReservationRepository reservations, PromotionService promotions,
            WaitingListRepository waitingLists, ConversationSessionRepository sessions,
            ReservationEventRepository events, MessagingPort messaging) {
        this.reservationRepository = reservations;
        this.promotionService = promotions;
        this.waitingListRepository = waitingLists;
        this.conversationSessionRepository = sessions;
        this.eventRepository = events;
        this.messaging = messaging;
    }

    @Transactional
    public Reservation execute(UUID reservationId) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        String groupCode = groupCode(initial.getReservationCode());
        List<Reservation> group;
        Reservation selected;
        if (groupCode == null) {
            selected = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Reserva no encontrada: " + reservationId));
            group = List.of(selected);
        } else {
            group = reservationRepository.findReservationGroupForUpdate(groupCode);
            selected = group.stream()
                    .filter(reservation -> reservationId.equals(reservation.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "El grupo de reserva cambió durante la confirmación."));
        }
        String promotionCode = group.stream()
                .map(Reservation::getPromotionCode)
                .filter(code -> code != null && !code.isBlank())
                .findFirst()
                .orElse(null);
        String phoneNumber = selected.getPassenger() != null ? selected.getPassenger().getPhone() : null;

        if (group.stream().allMatch(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))) {
            // Repara reservas confirmadas por flujos anteriores que no consumieron la promoción.
            promotionService.consumeIfAvailable(promotionCode, phoneNumber);
            completeWaitingListEntries(group, phoneNumber);
            return selected;
        }

        // La confirmación puede reintentarse desde el panel. El consumo idempotente evita que
        // una promoción ya aplicada marque la transacción como rollback-only.
        promotionService.consumeIfAvailable(promotionCode, phoneNumber);

        LocalDateTime confirmedAt = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        group.forEach(reservation -> {
            reservation.setPaymentVerified(true);
            reservation.setStatus("CONFIRMED");
            reservation.setPaymentConfirmedAt(confirmedAt);
            reservation.setPaymentExpiresAt(null);
        });
        reservationRepository.saveAll(group);
        if (eventRepository != null) group.forEach(reservation -> eventRepository.save(
                ReservationEvent.builder().reservationId(reservation.getId())
                        .eventType("PAYMENT_VERIFIED")
                        .description("Pago verificado y reserva confirmada.")
                        .triggeredBy("OPERATOR").build()));
        completeWaitingListEntries(group, phoneNumber);
        notifyPaymentConfirmation(selected);
        return selected;
    }

    private void notifyPaymentConfirmation(Reservation reservation) {
        if (messaging == null || reservation.getPassenger() == null
                || reservation.getPassenger().getPhone() == null
                || reservation.getPassenger().getPhone().isBlank()) {
            return;
        }
        String passengerName = reservation.getPassenger().getFirstName();
        String destination = reservation.getDestination();
        try {
            messaging.sendText(reservation.getPassenger().getPhone(), """
                    ✅ *¡Pago Verificado con Éxito!*

                    Hola %s, te confirmamos que recibimos correctamente tu transferencia. Tu reserva para el traslado hacia *%s* ya se encuentra asentada de forma definitiva.

                    🚐 Próximamente nos comunicaremos para coordinar el horario exacto en el que el chofer pasará por tu domicilio. ¡Muchas gracias por viajar con Lunaris!
                    """.formatted(
                            passengerName == null || passengerName.isBlank() ? "Pasajero" : passengerName,
                            destination == null || destination.isBlank() ? "tu destino" : destination));
        } catch (RuntimeException exception) {
            log.warn("No se pudo emitir la notificación de confirmación para la reserva {}.",
                    reservation.getId());
        }
    }

    private void completeWaitingListEntries(List<Reservation> reservations, String phoneNumber) {
        reservations.stream()
                .map(Reservation::getWaitingListEntryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(id -> waitingListRepository.findByIdForUpdate(id).ifPresent(entry -> {
                    entry.setStatus(WaitingListEntry.CONVERTED);
                    waitingListRepository.saveAndFlush(entry);
                }));
        if (phoneNumber != null) {
            conversationSessionRepository.findByPhoneNumber(phoneNumber)
                    .filter(session -> session.getWaitingListEntryId() != null)
                    .ifPresent(conversationSessionRepository::delete);
        }
    }

    private String groupCode(String reservationCode) {
        if (reservationCode == null || reservationCode.isBlank()
                || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) {
            return null;
        }
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CreateFareLocalityService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@RequiredArgsConstructor
public class CreateFareLocalityService implements CreateFareLocalityUseCase {
    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FareLocalityView create(String localityName, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount) {
        String normalizedName = FareLocalityValidation.localityName(localityName);
        FareLocalityValidation.amount(amount);
        FareLocalityValidation.nonNegative(kmsToCordoba, "Los kilómetros");
        int effectiveKmsToCordoba = kmsToCordoba == null ? 0 : kmsToCordoba;
        int effectiveMinutesFromOrigin = minutesFromOrigin == null ? 0 : minutesFromOrigin;
        if (localityRepository.findFirstByNameIgnoreCase(normalizedName).isPresent()) {
            throw new DomainValidationException("Ya existe una localidad con ese nombre.");
        }
        if (fareRepository.findFirstByLocalityNameIgnoreCase(normalizedName).isPresent()) {
            throw new DomainValidationException("Ya existe una tarifa para esa localidad.");
        }

        Locality locality = Locality.builder().id(UUID.randomUUID()).name(normalizedName)
                .kmsToCordoba(effectiveKmsToCordoba).minutesFromOrigin(effectiveMinutesFromOrigin).build();
        Fare fare = Fare.builder().id(UUID.randomUUID()).localityName(normalizedName).amount(amount).build();
        locality = localityRepository.saveAndFlush(locality);
        fare = fareRepository.saveAndFlush(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CreateManualReservationUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateManualReservationUseCase {
    private final PassengerRepository passengers;
    private final ReservationService reservations;

    @Transactional
    public List<Reservation> execute(Reservation reservation, String returnSchedule) {
        Passenger input = reservation.getPassenger();
        if (input == null || input.getFirstName() == null || input.getFirstName().isBlank()
                || input.getLastName() == null || input.getLastName().isBlank()) {
            throw new DomainValidationException("El nombre y apellido del pasajero son obligatorios.");
        }
        String phone = PhoneUtils.normalizeArgentinePhone(input.getPhone());
        reservations.validateManualReservation(reservation);
        Passenger passenger = passengers.findFirstByPhone(phone).orElseGet(Passenger::new);
        passenger.setPhone(phone);
        passenger.setFirstName(input.getFirstName().trim());
        passenger.setLastName(input.getLastName().trim());
        if (input.getCuil() != null && !input.getCuil().isBlank()) passenger.setCuil(input.getCuil().trim());
        reservation.setPassenger(passengers.saveAndFlush(passenger));
        return reservations.saveManualReservationFlow(reservation, returnSchedule);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CreatePassengerUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.CreatePassengerRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePassengerUseCase {

    private final PassengerRepository repository;

    public Passenger execute(CreatePassengerRequest request) {

        Passenger passenger = Passenger.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .cuil(request.cuil())
                .phone(request.phone())
                .address(request.address())
                .locality(request.locality())
                .build();

        return repository.save(passenger);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CreateReservationUseCase.java`

```java
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

    @Value("${lunaris.trips.capacity:19}")
    private int tripCapacity = 19;

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

    public Reservation executePublic(CreateReservationRequest request) {
        return executePublic(request, null);
    }

    public Reservation executePublic(CreateReservationRequest request, String paymentReceiptUrl) {
        if (request != null && request.passengerId() != null) {
            throw new DomainValidationException(
                    "La creación pública no admite un identificador de pasajero.");
        }
        return execute(request, paymentReceiptUrl);
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/CreateSpecialTripService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSpecialTripService implements CreateSpecialTripUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip create(SpecialTripCommand command) {
        SpecialTrip trip = SpecialTrip.create(command.title(), command.description(), command.origin(),
                command.destination(), command.startDate(), command.endDate(), command.price(),
                command.maxPassengers(), command.imageUrl(), command.active(), ArgentinaTime.now());
        return repository.save(trip);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/DailyPassengerManifestService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Manifiesto PDF operativo consolidado, generado sin alterar el esquema de datos. */
@Service
public class DailyPassengerManifestService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(LocalDate date, List<Reservation> reservations) {
        StringBuilder stream = new StringBuilder("BT /F1 9 Tf 30 550 Td ");
        line(stream, "Lunaris Ansenuza - Manifiesto Diario de Pasajeros");
        line(stream, "Fecha: " + DATE.format(date) + " | Pasajeros únicos: "
                + uniquePassengers(reservations) + " | Butacas reservadas: " + reservedSeats(reservations));
        section(stream, "TRAMOS DE IDA (Pueblos -> Cordoba)", reservations, false);
        section(stream, "TRAMOS DE VUELTA (Cordoba -> Pueblos)", reservations, true);
        stream.append("ET");
        return buildPdf(stream.toString());
    }

    /** Personas físicas, deduplicadas entre los tramos de ida y vuelta del manifiesto. */
    public long uniquePassengers(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Objects::nonNull)
                .map(Reservation::getPassenger)
                .filter(Objects::nonNull)
                .map(Passenger::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    /** Suma de butacas de todos los tramos, sin deduplicar pasajeros. */
    public int reservedSeats(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Objects::nonNull)
                .map(Reservation::getPassengerCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void section(StringBuilder out, String title, List<Reservation> all, boolean returns) {
        line(out, "");
        line(out, title);
        line(out, "Horario | Pasajero | Telefono | Origen/Punto de ascenso | Destino | Asientos | Pago");
        all.stream().filter(r -> isReturn(r) == returns)
                .sorted(Comparator.comparing(this::schedule).thenComparing(this::name))
                .forEach(r -> line(out, schedule(r) + " | " + name(r) + " | " + phone(r) + " | "
                        + text(r.getPickupLocality()) + " - " + text(r.getPickupAddress()) + " | "
                        + text(r.getDestination()) + " | " + r.getTotalSeats() + " | "
                        + (Boolean.TRUE.equals(r.getPaymentVerified()) ? "VERIFICADO" : "PENDIENTE")));
    }

    private byte[] buildPdf(String stream) {
        byte[] content = stream.getBytes(StandardCharsets.ISO_8859_1);
        String objects = "1 0 obj<< /Type /Catalog /Pages 2 0 R>>endobj\n"
                + "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1>>endobj\n"
                + "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources<< /Font<< /F1 4 0 R>>>> /Contents 5 0 R>>endobj\n"
                + "4 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica>>endobj\n"
                + "5 0 obj<< /Length " + content.length + ">>stream\n" + stream + "\nendstream endobj\n";
        String header = "%PDF-1.4\n";
        int start = header.length();
        String xref = "xref\n0 6\n0000000000 65535 f \n"
                + String.format("%010d 00000 n \n", start)
                + String.format("%010d 00000 n \n", start + objects.indexOf("2 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("3 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("4 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("5 0 obj"));
        String trailer = "trailer<< /Size 6 /Root 1 0 R>>\nstartxref\n" + (start + objects.length()) + "\n%%EOF";
        return (header + objects + xref + trailer).getBytes(StandardCharsets.ISO_8859_1);
    }

    private void line(StringBuilder out, String value) {
        out.append('(').append(value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)"))
                .append(") Tj 0 -13 Td ");
    }
    private boolean isReturn(Reservation r) { return "VUELTA".equalsIgnoreCase(r.getRouteDirection()) || TripRouteCalculatorService.isCordoba(r.getPickupLocality()); }
    private String schedule(Reservation r) { return text(r.getDepartureSchedule()).isBlank() ? "03:00" : text(r.getDepartureSchedule()); }
    private String name(Reservation r) { return r.getPassenger() == null ? "Sin pasajero" : (text(r.getPassenger().getFirstName()) + " " + text(r.getPassenger().getLastName())).trim(); }
    private String phone(Reservation r) { return r.getPassenger() == null ? "" : text(r.getPassenger().getPhone()); }
    private String text(String value) { return value == null ? "" : value.trim(); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/DeleteFareLocalityService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.FareLocalityInUseException;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteFareLocalityService implements DeleteFareLocalityUseCase {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void delete(UUID fareId) {
        var fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new DomainValidationException("La tarifa indicada no existe."));
        String localityName = fare.getLocalityName();
        var locality = localityRepository.findFirstByNameIgnoreCase(localityName)
                .orElseThrow(() -> new DomainValidationException("La tarifa no tiene una localidad válida asociada."));
        if (reservationRepository.existsActiveByLocality(localityName)) {
            throw new FareLocalityInUseException(localityName);
        }

        fareRepository.delete(fare);
        localityRepository.delete(locality);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/DriverApplicationManagementService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DriverApplicationNotFoundException;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverApplicationManagementService {

    private final DriverApplicationRepository applicationRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public List<DriverApplication> findPending() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(
                DriverApplication.Status.PENDING);
    }

    @Transactional
    public DriverApplication approve(UUID applicationId) {
        DriverApplication application = findApplication(applicationId);
        application.approve();

        Driver driver = driverRepository.findFirstByPhone(application.getPhone())
                .orElseGet(Driver::new);
        driver.setFullName(application.getFullName());
        driver.setPhone(application.getPhone());
        driver.setActive(true);
        driverRepository.save(driver);

        return applicationRepository.save(application);
    }

    @Transactional
    public DriverApplication reject(UUID applicationId) {
        DriverApplication application = findApplication(applicationId);
        application.reject();
        return applicationRepository.save(application);
    }

    private DriverApplication findApplication(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DriverApplicationNotFoundException(applicationId));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/DriverAuthorizationService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverAuthorizationService {

    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public void assertCanAccessDriver(Authentication authentication, UUID requestedDriverId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Se requiere autenticación.");
        }
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (!hasRole(authentication, "ROLE_CHOFER") || requestedDriverId == null) {
            throw new AccessDeniedException("No tiene acceso a esta hoja de ruta.");
        }
        UUID authenticatedDriverId = resolveActiveDriverId(authentication.getName());
        if (!requestedDriverId.equals(authenticatedDriverId)) {
            throw new AccessDeniedException("La hoja de ruta pertenece a otro chofer.");
        }
    }

    private UUID resolveActiveDriverId(String username) {
        String normalizedUsername = normalize(username);
        return driverRepository.findFirstByPhone(username)
                .filter(Driver::isActive)
                .or(() -> driverRepository.findByActiveTrue().stream()
                        .filter(driver -> normalize(driver.getPhone()).equals(normalizedUsername))
                        .findFirst())
                .map(Driver::getId)
                .orElseThrow(() -> new AccessDeniedException(
                        "La cuenta autenticada no corresponde a un chofer activo."));
    }

    private boolean hasRole(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private String normalize(String phone) {
        try {
            return PhoneUtils.normalizeArgentinePhone(phone);
        } catch (RuntimeException exception) {
            return "";
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/DriverManagementService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverManagementService {

    private final DriverRepository repository;

    @Transactional(readOnly = true)
    public List<Driver> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Driver create(String fullName, String phone, Integer ranking, Boolean active) {
        Driver driver = new Driver();
        mapFields(driver, fullName, phone, ranking, active);
        return repository.saveAndFlush(driver);
    }

    private void mapFields(
            Driver driver, String fullName, String phone, Integer ranking, Boolean active) {
        driver.setFullName(requiredText(fullName, "El nombre del chofer es obligatorio."));
        driver.setPhone(PhoneUtils.normalizeArgentinePhone(phone));
        if (ranking != null && (ranking < 1 || ranking > 5)) {
            throw new DomainValidationException("El ranking debe estar entre 1 y 5.");
        }
        driver.setRanking(ranking);
        driver.setActive(active == null || active);
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
        return value.trim();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ExpireReservationPaymentUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpireReservationPaymentUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository eventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int execute(UUID candidateId, LocalDateTime now) {
        Reservation candidate = reservationRepository.findByIdForUpdate(candidateId).orElse(null);
        if (!isExpired(candidate, now)) return 0;

        String groupCode = candidate.getBookingGroupCode();
        List<Reservation> reservations = groupCode == null || groupCode.isBlank()
                ? List.of(candidate)
                : reservationRepository.findByBookingGroupCodeForUpdate(groupCode);
        int expired = 0;
        for (Reservation reservation : reservations) {
            if (!isExpired(reservation, now)) continue;
            reservation.setStatus("EXPIRED");
            reservation.setTravelStatus(Reservation.TravelStatus.CANCELED);
            reservationRepository.save(reservation);
            eventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId())
                    .eventType("PAYMENT_EXPIRED")
                    .description("Reserva expirada automáticamente luego de 20 minutos sin pago verificado.")
                    .triggeredBy("SYSTEM_TTL")
                    .build());
            expired++;
        }
        return expired;
    }

    private boolean isExpired(Reservation reservation, LocalDateTime now) {
        return reservation != null
                && !Boolean.TRUE.equals(reservation.getPaymentVerified())
                && reservation.getPaymentExpiresAt() != null
                && !reservation.getPaymentExpiresAt().isAfter(now)
                && ("PENDING_PAYMENT".equalsIgnoreCase(reservation.getStatus())
                    || "PENDING_VERIFICATION".equalsIgnoreCase(reservation.getStatus()));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/FareLocalityValidation.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;

final class FareLocalityValidation {
    private FareLocalityValidation() {
    }

    static String localityName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("El nombre de la localidad es obligatorio.");
        }
        String normalized = name.trim();
        if (normalized.length() > 100) {
            throw new DomainValidationException("El nombre de la localidad no puede superar los 100 caracteres.");
        }
        return normalized;
    }

    static void amount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new DomainValidationException("La tarifa debe ser mayor a cero.");
        }
    }

    static void nonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new DomainValidationException(field + " no pueden ser negativos.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetBillingPanelUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.service.CuilCalculator;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.billing.BillingPanelView;
import com.lunaris.ansenuza.infrastructure.web.dto.billing.IssuedInvoiceRow;
import com.lunaris.ansenuza.infrastructure.web.dto.billing.PendingInvoiceRow;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBillingPanelUseCase {

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public BillingPanelView execute() {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();

        BigDecimal ingresoHoy = reservationRepository.sumConfirmedIncomeBetween(startOfDay, startOfTomorrow);
        long countHoy = reservationRepository.countConfirmedIncomeBetween(startOfDay, startOfTomorrow);
        BigDecimal ingresoMes = reservationRepository.sumConfirmedIncomeBetween(startOfMonth, startOfNextMonth);
        long countMes = reservationRepository.countConfirmedIncomeBetween(startOfMonth, startOfNextMonth);

        List<PendingInvoiceRow> pendientes = consolidatePendingInvoices(
                reservationRepository.findPendingInvoiceReservations());

        return new BillingPanelView(
                ingresoHoy != null ? ingresoHoy : BigDecimal.ZERO,
                countHoy,
                ingresoMes != null ? ingresoMes : BigDecimal.ZERO,
                countMes,
                pendientes,
                invoiceRepository.findAllIssuedWithReservation().stream()
                        .map(this::toIssuedRow)
                        .toList()
        );
    }

    private IssuedInvoiceRow toIssuedRow(Invoice invoice) {
        Reservation reservation = invoice.getReservation();
        String reservationStatus = reservation == null ? null : reservation.getStatus();
        boolean refundedToWallet = reservation != null
                && "CANCELLED".equalsIgnoreCase(reservationStatus)
                && Boolean.TRUE.equals(reservation.getPaymentVerified())
                && invoice.getAmount() != null
                && invoice.getAmount().signum() > 0;
        return new IssuedInvoiceRow(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPassengerName(),
                invoice.getPassengerCuil(),
                invoice.getAmount(),
                invoice.getPdfUrl(),
                invoice.getSentViaWhatsapp(),
                invoice.getCreatedAt(),
                reservationStatus,
                refundedToWallet);
    }

    private List<PendingInvoiceRow> consolidatePendingInvoices(List<Reservation> reservations) {
        var groups = new LinkedHashMap<String, List<Reservation>>();
        for (Reservation reservation : reservations) {
            groups.compute(baseCode(reservation), (code, existing) -> {
                var legs = existing == null
                        ? new java.util.ArrayList<Reservation>()
                        : new java.util.ArrayList<>(existing);
                legs.add(reservation);
                return legs;
            });
        }
        return groups.values().stream()
                .filter(legs -> combinedAmount(legs).signum() > 0)
                .map(this::toRow)
                .toList();
    }

    private PendingInvoiceRow toRow(List<Reservation> legs) {
        Reservation primary = legs.stream()
                .filter(item -> item.getReservationCode() != null
                        && item.getReservationCode().endsWith("-IDA"))
                .findFirst()
                .orElse(legs.getFirst());
        Passenger passenger = legs.stream()
                .map(Reservation::getPassenger)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        String nombre = passenger == null
                ? "Pasajero sin vincular"
                : fullName(passenger);
        String rawDoc = passenger == null ? null : passenger.getCuil();
        String code = baseCode(primary);
        String route = primary.getPickupLocality() + " → " + primary.getDestination();
        if (legs.size() > 1) {
            route += " (Ida y Vuelta)";
        }
        return new PendingInvoiceRow(
                primary.getId(),
                code,
                nombre,
                passenger == null ? null : passenger.getPhone(),
                rawDoc,
                CuilCalculator.suggestCuil(rawDoc),
                combinedAmount(legs),
                primary.getTravelDate(),
                route
        );
    }

    private String baseCode(Reservation reservation) {
        return com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
    }

    private BigDecimal combinedAmount(List<Reservation> reservations) {
        return com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.total(reservations);
    }

    private String fullName(Passenger passenger) {
        String firstName = passenger.getFirstName() == null ? "" : passenger.getFirstName().trim();
        String lastName = passenger.getLastName() == null ? "" : passenger.getLastName().trim();
        String name = (firstName + " " + lastName).trim();
        return name.isBlank() ? "Pasajero sin nombre" : name;
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetDailyOperationSummaryUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetDailyOperationSummaryUseCase {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public DailyOperationSummaryResponse execute(LocalDate travelDate) {
        long totalReservations = reservationRepository.findByTravelDate(travelDate).stream()
                .filter(r -> r != null).count();
        long totalPassengers = reservationRepository.countDistinctPassengersByTravelDate(travelDate);
        long paidReservations = reservationRepository.countDistinctPaidPassengersByTravelDate(travelDate);
        long pendingPayments = reservationRepository.countDistinctPendingPassengersByTravelDate(travelDate);

        // 🚗 División limpia por 4.0 para agrupar en autos de a cuatro
        long estimatedVehicles = totalPassengers == 0 ? 0 : (long) Math.ceil(totalPassengers / 4.0);

        return new DailyOperationSummaryResponse(
                travelDate,
                totalReservations,
                totalPassengers, // 👈 Pasamos el nuevo conteo de asientos físicos
                paidReservations,
                pendingPayments,
                estimatedVehicles
        );
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetFaresService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFaresService implements GetFaresQuery {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;

    @Override
    public List<FareLocalityView> getAll() {
        return fareRepository.findAllByOrderByLocalityNameAsc().stream().map(fare -> {
            var locality = localityRepository.findFirstByNameIgnoreCase(fare.getLocalityName()).orElse(null);
            return new FareLocalityView(fare.getId(), locality == null ? null : locality.getId(),
                    fare.getLocalityName(), fare.getAmount(), locality == null ? null : locality.getKmsToCordoba(),
                    locality == null ? null : locality.getMinutesFromOrigin());
        }).toList();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetHojaDeRutaUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.hojaruta.HojaRutaViewModel;
import lombok.RequiredArgsConstructor;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;

@Service
@RequiredArgsConstructor
public class GetHojaDeRutaUseCase {

    private final ReservationRepository reservationRepository;
    private final ConversationSessionRepository sessionRepository;

    public HojaRutaViewModel execute(LocalDate travelDate) {
        return execute(travelDate, "03:00", RouteDirection.OUTBOUND);
    }

    public HojaRutaViewModel execute(
            LocalDate travelDate, String schedule, RouteDirection direction) {
        List<Reservation> reservations = reservationRepository.findActiveManifest(
                travelDate, schedule, direction == RouteDirection.RETURN);
        List<ConversationSession> sesiones = sessionRepository.findAll();

        // 🏙️ Si el origen es "Córdoba", asumimos que es "VUELTA" hacia los pueblos.
        // Si NO es Córdoba (Morteros, La Puerta, etc.), es "IDA" hacia Córdoba.
        long totalYendo = reservations.stream()
                .filter(r -> !TripRouteCalculatorService.isCordoba(r.getPickupLocality()))
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        long totalVolviendo = reservations.stream()
                .filter(r -> TripRouteCalculatorService.isCordoba(r.getPickupLocality()))
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        // 🚨 Filtramos el turno de las 08:00 AM buscando en las notas que genera tu bot
        long pasajeros0800Count = reservations.stream()
                .filter(r -> r.getNotes() != null && r.getNotes().contains("08:00")) 
                .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 1)
                .sum();

        // El hub de La Puerta se activa si pasan los 15 pasajeros en ese turno
        boolean hubActivado = pasajeros0800Count > 15; 

        return new HojaRutaViewModel(
                travelDate,
                totalYendo,
                totalVolviendo,
                hubActivado,
                pasajeros0800Count,
                reservations,
                sesiones
        );
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetPassengerProfileUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPassengerProfileUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public PassengerProfile execute(String phone) {
        var passenger = passengerRepository.findByPhone(phone)
                .orElseThrow(() -> new DomainValidationException("El pasajero autenticado no existe."));
        List<ReservationHistory> history =
                reservationRepository
                        .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(
                                passenger)
                        .stream()
                        .map(reservation -> new ReservationHistory(
                                reservation.getId(),
                                reservation.getReservationCode(),
                                reservation.getTravelDate(),
                                reservation.getPickupLocality(),
                                reservation.getDestination(),
                                reservation.getDepartureSchedule(),
                                reservation.getStatus(),
                                reservation.getAmount()))
                        .toList();
        return new PassengerProfile(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getPhone(),
                passenger.getAddress(),
                passenger.getLocality(),
                passenger.getCurrentBalance() != null
                        ? passenger.getCurrentBalance() : BigDecimal.ZERO,
                history);
    }

    public record PassengerProfile(
            UUID id,
            String firstName,
            String lastName,
            String phone,
            String address,
            String locality,
            @JsonProperty("current_balance") BigDecimal currentBalance,
            List<ReservationHistory> reservations) {
    }

    public record ReservationHistory(
            UUID id,
            String reservationCode,
            LocalDate travelDate,
            String pickupLocality,
            String destination,
            String departureSchedule,
            String status,
            BigDecimal amount) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetPublicReservationStatusUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPublicReservationStatusUseCase {

    private final ReservationRepository repository;

    @Transactional(readOnly = true)
    public PublicReservationStatus execute(String rawCode) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        return repository.findByReservationCode(code)
                .map(reservation -> new PublicReservationStatus(
                        reservation.getReservationCode(),
                        reservation.getStatus(),
                        reservation.getTravelStatus() == null
                                ? null : reservation.getTravelStatus().name(),
                        reservation.getTravelDate()))
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
    }

    public record PublicReservationStatus(
            String reservationCode, String status, String travelStatus, LocalDate travelDate) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/GetSpecialTripsService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSpecialTripsService implements GetSpecialTripsQuery {
    private final SpecialTripRepositoryPort repository;

    @Override
    public List<SpecialTrip> getAll() {
        return repository.findAll();
    }

    @Override
    public List<SpecialTrip> getActive() {
        return repository.findActive();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/InquiryService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.*;
import com.lunaris.ansenuza.domain.repository.*;
import com.lunaris.ansenuza.domain.exception.*;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InquiryService {
    private final InquiryRepository inquiries;
    private final PassengerRepository passengers;

    private final org.springframework.context.ApplicationEventPublisher events;

    public InquiryService(InquiryRepository inquiries, PassengerRepository passengers) {
        this(inquiries, passengers, event -> {});
    }

    @org.springframework.beans.factory.annotation.Autowired
    public InquiryService(InquiryRepository inquiries, PassengerRepository passengers,
            org.springframework.context.ApplicationEventPublisher events) {
        this.inquiries = inquiries;
        this.passengers = passengers;
        this.events = events;
    }

    public Inquiry register(String phone, String passengerName, String message) {
        if (phone == null || phone.isBlank() || message == null || message.isBlank()) {
            throw new DomainValidationException("El teléfono y el mensaje son obligatorios.");
        }
        Passenger passenger = passengers.findFirstByPhone(phone).orElse(null);
        String name = passenger == null ? passengerName
                : (passenger.getFirstName() + " " + passenger.getLastName()).trim();
        var now = ArgentinaTime.now();
        // El ID generado debe quedar nulo para que JPA persista una entidad nueva.
        Inquiry inquiry = Inquiry.builder().passenger(passenger)
                .phone(phone).passengerName(name).message(message.trim())
                .status(InquiryStatus.PENDING).createdAt(now).updatedAt(now).build();
        Inquiry saved = inquiries.save(inquiry);
        events.publishEvent(OperatorNotification.inquiry(saved));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Inquiry> list() {
        return inquiries.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long pendingCount() {
        return inquiries.countByStatus(InquiryStatus.PENDING);
    }

    public void updateStatus(UUID id, InquiryStatus status) {
        if (status == null || status == InquiryStatus.PENDING) {
            throw new DomainValidationException("Seleccioná un estado válido para gestionar la consulta.");
        }
        Inquiry inquiry = inquiries.findById(id).orElseThrow(InquiryNotFoundException::new);
        inquiry.setStatus(status);
        inquiry.setUpdatedAt(ArgentinaTime.now());
        inquiries.save(inquiry);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/InvoicePersistenceService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoicePersistenceService {

    private final InvoiceRepository invoiceRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<String> findInvoiceNumber(UUID reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .map(Invoice::getInvoiceNumber);
    }

    @Transactional
    public Invoice persistUploadedInvoice(InvoiceData data) {
        Invoice invoice = invoiceRepository.findByReservationIdForUpdate(data.reservationId())
                .orElseGet(Invoice::new);
        boolean newInvoice = invoice.getId() == null;
        if (newInvoice) {
            invoice.setReservationId(data.reservationId());
        }
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(data.invoiceNumber());
        }
        invoice.setPassengerName(data.passengerName());
        invoice.setPassengerCuil(data.passengerCuil());
        invoice.setAmount(data.amount());
        invoice.setPdfUrl(data.pdfUrl());
        invoice.setSentViaWhatsapp(false);
        invoice.setSentAt(null);
        if (newInvoice) {
            entityManager.persist(invoice);
            entityManager.flush();
        }
        return invoice;
    }

    @Transactional
    public Invoice updateDeliveryStatus(UUID invoiceId, boolean sent, LocalDateTime sentAt) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new IllegalStateException(
                        "La factura desapareció durante la emisión: " + invoiceId));
        invoice.setSentViaWhatsapp(sent);
        invoice.setSentAt(sent ? sentAt : null);
        return invoice;
    }

    public record InvoiceData(
            UUID reservationId,
            String invoiceNumber,
            String passengerName,
            String passengerCuil,
            BigDecimal amount,
            String pdfUrl) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/IssueInvoiceUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort;
import com.lunaris.ansenuza.application.port.InvoiceStoragePort.StoredInvoice;
import com.lunaris.ansenuza.domain.model.Invoice;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.InvoiceRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.service.CuilCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sube el PDF de la factura (armada aparte por la operadora), lo registra y lo envía
 * por WhatsApp al pasajero. Permite además reenviar una factura ya emitida.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IssueInvoiceUseCase {

    private static final String DEFAULT_PUBLIC_BASE_URL =
            "https://lunaris-backend-nn6s.onrender.com";

    public record InvoiceDocument(String invoiceNumber, byte[] content) {
        public InvoiceDocument {
            content = content.clone();
        }
    }

    private final ReservationRepository reservationRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceStoragePort invoiceStorage;
    private final InvoicePersistenceService invoicePersistenceService;
    private final ManualReservationNotificationService notifications;

    @Value("${lunaris.public-base-url:" + DEFAULT_PUBLIC_BASE_URL + "}")
    private String publicBaseUrl = DEFAULT_PUBLIC_BASE_URL;

    /** Emite (o re-sube) la factura de una reserva y la envía por WhatsApp. */
    public Invoice issue(UUID reservationId, byte[] pdfBytes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        List<Reservation> group = invoiceGroup(reservation);
        if (group.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getPaymentVerified())
                || !"CONFIRMED".equals(item.getStatus()))) {
            throw new IllegalStateException("La factura solo puede emitirse después de confirmar el pago.");
        }
        BigDecimal invoiceAmount = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.total(group);
        if (invoiceAmount.signum() <= 0) {
            throw new IllegalStateException("No se emiten facturas fiscales para reservas bonificadas al 100%.");
        }

        Reservation primary = primaryReservation(group, reservation);
        String invoiceNumber = invoicePersistenceService.findInvoiceNumber(primary.getId())
                .orElseGet(this::nextInvoiceNumber);

        String fileName = "factura_" + invoiceNumber.replace("-", "_") + ".pdf";
        StoredInvoice stored = invoiceStorage.store(pdfBytes, fileName);

        InvoicePersistenceService.InvoiceData invoiceData = new InvoicePersistenceService.InvoiceData(
                primary.getId(), invoiceNumber,
                reservation.getPassenger().getFirstName() + " "
                        + reservation.getPassenger().getLastName(),
                CuilCalculator.suggestCuil(reservation.getPassenger().getCuil()),
                invoiceAmount, stored.webUrl());
        Invoice invoice = persistWithConcurrentRetry(invoiceData);

        boolean sent = sendByWhatsApp(reservation, publicInvoiceUrl(invoice));
        return invoicePersistenceService.updateDeliveryStatus(
                invoice.getId(), sent,
                sent ? com.lunaris.ansenuza.shared.ArgentinaTime.now() : null);
    }

    private Invoice persistWithConcurrentRetry(InvoicePersistenceService.InvoiceData invoiceData) {
        try {
            return invoicePersistenceService.persistUploadedInvoice(invoiceData);
        } catch (DataIntegrityViolationException concurrentInsert) {
            log.info("Otra transacción creó la factura de la reserva {}. Se actualiza la fila existente.",
                    invoiceData.reservationId());
            return invoicePersistenceService.persistUploadedInvoice(invoiceData);
        }
    }

    /** Reenvía por WhatsApp una factura ya registrada. */
    public Invoice resend(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        Reservation reservation = reservationRepository.findById(invoice.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada para la factura " + invoiceId));

        boolean sent = sendByWhatsApp(reservation, publicInvoiceUrl(invoice));
        boolean delivered = sent || Boolean.TRUE.equals(invoice.getSentViaWhatsapp());
        LocalDateTime sentAt = sent
                ? com.lunaris.ansenuza.shared.ArgentinaTime.now()
                : invoice.getSentAt();
        return invoicePersistenceService.updateDeliveryStatus(
                invoice.getId(), delivered, sentAt);
    }

    @Transactional(readOnly = true)
    public InvoiceDocument download(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + invoiceId));
        return new InvoiceDocument(
                invoice.getInvoiceNumber(), invoiceStorage.load(invoice.getPdfUrl()));
    }

    private boolean sendByWhatsApp(
            Reservation reservation, String publicDocumentUrl) {
        try {
            return notifications.invoiceReady(reservation.getId(), publicDocumentUrl);
        } catch (RuntimeException exception) {
            log.error("La factura de la reserva {} quedó guardada pero no se pudo programar el envío", reservation.getId(), exception);
            return false;
        }
    }

    private String publicInvoiceUrl(Invoice invoice) {
        String baseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? DEFAULT_PUBLIC_BASE_URL
                : publicBaseUrl.strip();
        baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return baseUrl + "/public/invoices/" + invoice.getId() + ".pdf";
    }

    private String nextInvoiceNumber() {
        long sequence = invoiceRepository.count() + 1;
        return String.format("F-%d-%05d", Year.now().getValue(), sequence);
    }

    private List<Reservation> invoiceGroup(Reservation reservation) {
        String groupCode = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
        if (groupCode.startsWith("UUID:")) return List.of(reservation);
        List<Reservation> group = reservationRepository.findReservationGroup(groupCode);
        return group.isEmpty() ? List.of(reservation) : group;
    }

    private Reservation primaryReservation(List<Reservation> group, Reservation fallback) {
        return group.stream()
                .filter(item -> item.getReservationCode() != null
                        && item.getReservationCode().endsWith("-IDA"))
                .findFirst()
                .orElse(fallback);
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/LocalityService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalityService {

    private final LocalityRepository localityRepository;

    @Transactional(readOnly = true)
    public List<Locality> findAllWithActiveFare() {
        return localityRepository.findAllWithActiveFare();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ManualReservationNotificationService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.PassengerContactTemplate;
import com.lunaris.ansenuza.domain.model.ManualReservationCreated;
import com.lunaris.ansenuza.domain.model.PassengerMessageReceived;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class ManualReservationNotificationService {
    public static final String PROMO = "💡 Tip Lunaris: ¡La próxima vez podés pedir tu viaje directamente por acá en 1 minuto! Nuestro Bot automático está disponible las 24 hs para cotizar, reservar y confirmarte al instante sin esperas. ¡Probalo en tu próximo viaje!";
    private final ReservationRepository reservations;
    private final WhatsAppConversationWindowService window;
    private final MessagingPort messaging;
    private final com.lunaris.ansenuza.domain.repository.InvoiceRepository invoices;
    private final TransactionTemplate transaction;
    private final TransactionTemplate withoutTransaction;

    public ManualReservationNotificationService(ReservationRepository reservations,
            WhatsAppConversationWindowService window, MessagingPort messaging,
            PlatformTransactionManager manager,
            com.lunaris.ansenuza.domain.repository.InvoiceRepository invoices) {
        this.reservations = reservations;
        this.window = window;
        this.messaging = messaging;
        this.invoices = invoices;
        this.transaction = new TransactionTemplate(manager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        withoutTransaction = new TransactionTemplate(manager);
        withoutTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    }

    @TransactionalEventListener
    public void created(ManualReservationCreated event) {
        deliver(event.reservationId(), false);
    }

    @EventListener
    public void incoming(PassengerMessageReceived event) {
        String phone;
        try {
            phone = PhoneUtils.normalizeArgentinePhone(event.phone());
        } catch (com.lunaris.ansenuza.domain.exception.DomainValidationException invalidPhone) {
            return;
        }
        try {
            for (Reservation reservation : reservations.findByPassengerPhoneAndManualNotificationWaitingReplyTrue(phone)) {
                deliver(reservation.getId(), true);
            }
        } catch (RuntimeException exception) {
            log.error("No se pudo recuperar el detalle pendiente del pasajero", exception);
        }
    }

    @Scheduled(fixedDelayString = "${lunaris.manual-notification.retry-ms:60000}")
    public void retryPending() {
        var now = com.lunaris.ansenuza.shared.ArgentinaTime.now();
        reservations.findRetryableManualNotifications(now.minusMinutes(5), now.minusHours(24),
                org.springframework.data.domain.PageRequest.of(0, 50))
                .forEach(reservation -> deliver(reservation.getId(), reservation.isManualNotificationWaitingReply()));
    }

    /** Asociar la factura nunca reemplaza la evidencia de pago del pasajero. */
    public boolean invoiceReady(UUID id, String url) {
        transaction.executeWithoutResult(status -> {
            Reservation reservation = reservations.findById(id).orElseThrow();
            String groupCode = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
            var group = groupCode.startsWith("UUID:") ? List.of(reservation) : reservations.findReservationGroup(groupCode);
            if (group.isEmpty()) group = List.of(reservation);
            for (Reservation leg : group) leg.setInvoiceUrl(url);
            reservation.setInvoiceUrl(url);
            reservation.setManualNotificationPending(true);
            reservation.setManualNotificationWaitingReply(false);
            reservation.setManualNotificationAttemptAt(null);
        });
        deliver(id, false);
        return Boolean.TRUE.equals(transaction.execute(status -> reservations.findById(id)
                .map(r -> !r.isManualNotificationPending()).orElse(false)));
    }

    public void deliver(UUID id, boolean replying) {
        try {
            Reservation reservation = transaction.execute(status -> {
                var locked = reservations.findAllByIdForUpdate(List.of(id));
                if (locked.isEmpty()) return null;
                Reservation r = locked.getFirst();
                if (!r.isManualNotificationPending()) return null;
                if (r.isManualNotificationWaitingReply() && !replying) return null;
                var now = com.lunaris.ansenuza.shared.ArgentinaTime.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
                if (r.getManualNotificationAttemptAt() != null
                        && r.getManualNotificationAttemptAt().isAfter(now.minusMinutes(5))) return null;
                if (java.util.Set.of("CANCELLED", "CANCELED", "EXPIRED", "REJECTED").contains(value(r.getStatus()))) {
                    r.setManualNotificationPending(false);
                    r.setManualNotificationWaitingReply(false);
                    return null;
                }
                r.setManualNotificationAttemptAt(now);
                if (replying) r.setManualNotificationWaitingReply(false);
                return r;
            });
            if (reservation == null) return;
            withoutTransaction.executeWithoutResult(status -> dispatch(reservation, replying));
        } catch (RuntimeException exception) {
            log.error("La notificación de la reserva {} continúa pendiente", id, exception);
        }
    }

    private void dispatch(Reservation reservation, boolean replying) {
        String phone = reservation.getPassenger().getPhone();
        boolean active = replying || window.isActive(phone);
        List<String> parameters = parameters(reservation);
        if (!active) {
            messaging.sendTemplate(phone, PassengerContactTemplate.NAME,
                    PassengerContactTemplate.parameters(reservation.getPassenger().getFirstName()),
                    sent -> complete(reservation, sent, true));
            return;
        }
        messaging.sendText(phone, confirmation(parameters) + extendedDetails(reservation), sent -> {
            if (!sent || reservation.getInvoiceUrl() == null) {
                complete(reservation, sent, false);
            } else {
                messaging.sendDocumentUrl(phone, reservation.getInvoiceUrl(), "Factura.pdf",
                        "Factura de tu reserva " + reservation.getReservationCode(),
                        documentSent -> complete(reservation, documentSent, false));
            }
        });
    }

    private void complete(Reservation attempt, boolean sent, boolean hsm) {
        transaction.executeWithoutResult(status -> {
            var locked = reservations.findAllByIdForUpdate(List.of(attempt.getId()));
            if (locked.isEmpty()) return;
            Reservation reservation = locked.getFirst();
            if (!java.util.Objects.equals(reservation.getManualNotificationAttemptAt(), attempt.getManualNotificationAttemptAt())) return;
            reservation.setManualNotificationAttemptAt(null);
            reservation.setManualNotificationPending(!sent || hsm);
            reservation.setManualNotificationWaitingReply(sent && hsm);
            if (sent && !hsm && reservation.getInvoiceUrl() != null) {
                String code = com.lunaris.ansenuza.domain.model.service.BookingInvoiceAmount.groupCode(reservation);
                var group = code.startsWith("UUID:") ? List.of(reservation) : reservations.findReservationGroup(code);
                var ids = group.isEmpty() ? List.of(reservation.getId()) : group.stream().map(Reservation::getId).toList();
                invoices.findFirstByReservationIdIn(ids).ifPresent(invoice -> {
                    invoice.setSentViaWhatsapp(true);
                    invoice.setSentAt(com.lunaris.ansenuza.shared.ArgentinaTime.now());
                });
            }
        });
    }

    public static List<String> parameters(Reservation reservation) {
        String document = reservation.getInvoiceUrl();
        if (document == null || document.isBlank()) document = reservation.getPaymentReceiptUrl();
        if (document == null || document.isBlank()) document = Boolean.TRUE.equals(reservation.getRequiresInvoice()) ? "Factura pendiente de emisión y envío por administración" : "No solicitado";
        return List.of(reservation.getPassenger().getFirstName(), reservation.getPickupLocality(),
                reservation.getDestination(), String.valueOf(reservation.getTravelDate()) + " " + value(reservation.getDepartureSchedule()),
                reservation.getReservationCode(), document).stream()
                .map(parameter -> parameter.replaceAll("\\s+", " ").trim()).toList();
    }

    public static String confirmation(List<String> p) {
        return "¡Hola %s! Tu reserva fue registrada con éxito 🚌✨\n\n📍 Trayecto: %s -> %s\n📅 Fecha y hora: %s\n🎟️ Código de reserva: %s\n📄 Factura/Comprobante: %s\n\n%s"
                .formatted(p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), p.get(5), PROMO);
    }

    private static String extendedDetails(Reservation r) {
        return "\n\nDomicilio: " + value(r.getPickupAddress()) + "\nPasajeros: " + r.getPassengerCount()
                + "\nAcompañantes: " + value(r.getCompanionNames()) + "\nTipo de viaje: " + tripLabel(r);
    }

    private static String tripLabel(Reservation reservation) {
        if (reservation.getTripType() == null) return "A confirmar";
        return switch (reservation.getTripType()) {
            case ONE_WAY -> "Solo ida";
            case ROUND_TRIP -> "Ida y vuelta";
            case OPEN_RETURN -> "Ida y vuelta con regreso abierto";
        };
    }

    private static String value(String value) { return value == null || value.isBlank() ? "A confirmar" : value; }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/NewsBannerService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.NewsBannerStoragePort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import com.lunaris.ansenuza.domain.repository.NewsBannerRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class NewsBannerService {

    private final NewsBannerRepository repository;
    private final NewsBannerStoragePort storage;

    @Transactional(readOnly = true)
    public List<NewsBanner> findActive() {
        List<NewsBanner> banners = repository.findActiveOn(ArgentinaTime.today());
        return banners == null ? List.of()
                : banners.stream().filter(java.util.Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public List<NewsBanner> findAll() {
        List<NewsBanner> banners = repository.findAllByOrderByCreatedAtDesc();
        return banners == null ? List.of()
                : banners.stream().filter(java.util.Objects::nonNull).toList();
    }

    @Transactional
    public NewsBanner create(
            String title, boolean active, LocalDate validUntil, MultipartFile image) {
        return create(title, null, null, false, active, validUntil, null, image);
    }

    @Transactional
    public NewsBanner create(String title, String description, String eventType,
            boolean hasWaitingList, boolean active, LocalDate validUntil,
            String externalImageUrl, MultipartFile image) {
        return save(null, title, description, eventType, hasWaitingList, active,
                validUntil, externalImageUrl, image);
    }

    @Transactional
    public NewsBanner save(UUID id, String title, String description, String eventType,
            boolean hasWaitingList, boolean active, LocalDate validUntil,
            String externalImageUrl, MultipartFile image) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("El título es obligatorio.");
        }
        if (title.trim().length() > 150) {
            throw new DomainValidationException("El título no puede superar los 150 caracteres.");
        }
        NewsBanner banner = id == null
                ? new NewsBanner()
                : repository.findById(id).orElseThrow(() ->
                        new DomainValidationException("La novedad indicada no existe."));
        banner.setTitle(title.trim());
        banner.setDescription(normalizeOptional(description));
        banner.setEventType(normalizeEventType(eventType, title));
        banner.setHasWaitingList(hasWaitingList);
        banner.setImageUrl(resolveImageUrl(
                externalImageUrl, image, banner.getImageUrl()));
        banner.setActive(active);
        banner.setValidUntil(validUntil);
        return repository.save(banner);
    }

    @Transactional(readOnly = true)
    public Map<String, String> findEventLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        findAll().stream()
                .filter(banner -> banner.getEventType() != null
                        && !banner.getEventType().isBlank())
                .forEach(banner -> labels.putIfAbsent(
                        banner.getEventType(), banner.getTitle()));
        return labels;
    }

    private String resolveImageUrl(
            String externalImageUrl, MultipartFile image, String currentImageUrl) {
        if (externalImageUrl != null && !externalImageUrl.isBlank()) {
            String url = externalImageUrl.trim();
            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                throw new DomainValidationException("La URL externa del flyer no es válida.");
            }
            return url;
        }
        if (image != null && !image.isEmpty()) {
            return storage.upload(image);
        }
        if (currentImageUrl == null || currentImageUrl.isBlank()) {
            throw new DomainValidationException("Debés subir un flyer o indicar una URL externa.");
        }
        return currentImageUrl;
    }

    private String normalizeEventType(String eventType, String title) {
        String source = eventType == null || eventType.isBlank() ? title : eventType;
        String normalized = Normalizer.normalize(source.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (normalized.isBlank()) {
            throw new DomainValidationException("No se pudo generar el código del evento.");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new DomainValidationException("La novedad indicada no existe.");
        }
        repository.deleteById(id);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/OnboardPassengerUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OnboardPassengerUseCase {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final LocalityRepository localityRepository;
    private final MessagingPort messaging;
    private final ReservationEventRepository eventRepository;

    public OnboardPassengerUseCase(ReservationRepository reservations, DriverRepository drivers,
            LocalityRepository localities, MessagingPort messaging) {
        this(reservations, drivers, localities, messaging, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public OnboardPassengerUseCase(ReservationRepository reservations, DriverRepository drivers,
            LocalityRepository localities, MessagingPort messaging,
            ReservationEventRepository eventRepository) {
        this.reservationRepository = reservations;
        this.driverRepository = drivers;
        this.localityRepository = localities;
        this.messaging = messaging;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Reservation execute(UUID reservationId) {
        return updateTravelStatus(reservationId, Reservation.TravelStatus.ONBOARDED);
    }

    @Transactional
    public Reservation execute(UUID reservationId, String driverPhone) {
        DriverActor actor = resolveActiveDriver(driverPhone);
        return boardPassenger(reservationId, actor.driverId());
    }

    @Transactional
    public Reservation updateTravelStatus(
            UUID reservationId, Reservation.TravelStatus newStatus) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        if ("COMPLETED".equalsIgnoreCase(initial.getStatus())
                || initial.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException();
        }
        if (newStatus != Reservation.TravelStatus.ONBOARD
                && newStatus != Reservation.TravelStatus.BOARDED
                && newStatus != Reservation.TravelStatus.ONBOARDED) {
            assertValidTransition(initial.getTravelStatus(), newStatus);
            initial.setTravelStatus(newStatus);
            Reservation saved = reservationRepository.saveAndFlush(initial);
            audit(saved, "TRAVEL_STATUS_CHANGED", "Estado actualizado a " + newStatus,
                    "OPERATOR_API");
            return saved;
        }
        return boardPassenger(reservationId, null);
    }

    @Transactional
    public Reservation recordReturnedPassengers(UUID reservationId, int returnedPassengerCount) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reserva no encontrada: " + reservationId));
        if ("COMPLETED".equalsIgnoreCase(reservation.getStatus())
                || reservation.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException();
        }
        if (!isReturnLeg(reservation)) {
            throw new IllegalArgumentException("El conteo de regreso solo aplica al tramo de vuelta.");
        }
        int total = reservation.getTotalSeats();
        if (returnedPassengerCount < 0 || returnedPassengerCount > total) {
            throw new IllegalArgumentException("La cantidad de pasajeros regresados no es válida.");
        }
        reservation.setReturnedPassengerCount(returnedPassengerCount);
        if (returnedPassengerCount == total) {
            reservation.setTravelStatus(Reservation.TravelStatus.COMPLETED);
            reservation.setStatus("COMPLETED");
        } else if (returnedPassengerCount > 0) {
            reservation.setTravelStatus(Reservation.TravelStatus.PARTIALLY_COMPLETED);
            reservation.setStatus("PARTIALLY_COMPLETED");
        }
        return reservationRepository.saveAndFlush(reservation);
    }

    private Reservation boardPassenger(UUID reservationId, UUID actorDriverId) {
        Reservation initial = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        LocalDate effectiveDate = effectiveLegDate(initial);
        if (initial.getDriver() == null || effectiveDate == null) {
            throw new IllegalStateException("La reserva no pertenece a una ruta asignada.");
        }
        UUID driverId = initial.getDriver().getId();
        if (actorDriverId != null && !actorDriverId.equals(driverId)) {
            throw new IllegalStateException(
                    "La reserva no pertenece al viaje del chofer autenticado.");
        }
        if (driverId == null || driverRepository.findAllByIdForUpdate(java.util.Set.of(driverId)).isEmpty()) {
            throw new IllegalStateException("No se pudo bloquear el chofer de la ruta.");
        }
        Reservation onboard = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + reservationId));
        LocalDate lockedEffectiveDate = effectiveLegDate(onboard);
        if (onboard.getDriver() == null || !driverId.equals(onboard.getDriver().getId())
                || lockedEffectiveDate == null || !effectiveDate.equals(lockedEffectiveDate)) {
            throw new IllegalStateException(
                    "La ruta cambió durante el abordaje. Reintentá la operación.");
        }
        if (onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARD
                || onboard.getTravelStatus() == Reservation.TravelStatus.BOARDED
                || onboard.getTravelStatus() == Reservation.TravelStatus.ONBOARDED) {
            log.warn(
                    "[ONBOARD] Duplicate boarding ignored. reservationId={}, travelStatus={}",
                    onboard.getId(), onboard.getTravelStatus());
            return onboard;
        }
        if (!isBoardable(onboard)) {
            log.warn(
                    "[ONBOARD] Boarding rejected by reservation state. reservationId={}, "
                            + "status={}, travelStatus={}",
                    onboard.getId(), onboard.getStatus(), onboard.getTravelStatus());
            throw new IllegalStateException(
                    "Esta reserva ya se encuentra abordada, finalizada o en un estado inválido.");
        }

        onboard.setTravelStatus(Reservation.TravelStatus.BOARDED);
        reservationRepository.saveAndFlush(onboard);
        audit(onboard, "PASSENGER_BOARDED", "Pasajero marcado como presente y abordado.",
                actorDriverId == null ? "OPERATOR_API" : "DRIVER_WHATSAPP");

        Optional<Reservation> nextPassenger =
                findNextPassengerInRoute(onboard, lockedEffectiveDate);
        afterCommit(() -> {
        notifyDriver(onboard, nextPassenger);
        nextPassenger.ifPresentOrElse(
                next -> {
                    String phone = next.getPassenger() != null
                            ? next.getPassenger().getPhone()
                            : null;
                    log.info(
                            "[ONBOARD] Target N+1 passenger found. sequence={}, passengerId={}, phone={}",
                            next.getRouteSequence(), next.getId(), phone);
                    notifyNext(onboard, next);
                },
                () -> log.info(
                        "[ONBOARD] No N+1 passenger found with sequence {}",
                        expectedNextSequence(onboard)));
        });
        return onboard;
    }

    private void assertValidTransition(Reservation.TravelStatus current,
            Reservation.TravelStatus next) {
        boolean reserved = current == null || current == Reservation.TravelStatus.SCHEDULED
                || current == Reservation.TravelStatus.PENDING
                || current == Reservation.TravelStatus.CONFIRMED;
        boolean boarded = current == Reservation.TravelStatus.BOARDED
                || current == Reservation.TravelStatus.ONBOARD
                || current == Reservation.TravelStatus.ONBOARDED;
        boolean valid = (reserved && (next == Reservation.TravelStatus.NO_SHOW
                        || next == Reservation.TravelStatus.CANCELED))
                || (boarded && next == Reservation.TravelStatus.COMPLETED);
        if (!valid) throw new IllegalStateException(
                "Transición de viaje inválida: " + current + " -> " + next);
    }

    private void audit(Reservation reservation, String type, String description, String actor) {
        if (eventRepository != null) eventRepository.save(ReservationEvent.builder()
                .reservationId(reservation.getId()).eventType(type).description(description)
                .triggeredBy(actor).build());
    }

    private void afterCommit(Runnable action) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override public void afterCommit() { action.run(); }
                    });
        } else action.run();
    }

    private void notifyDriver(Reservation onboard, Optional<Reservation> nextPassenger) {
        if (onboard.getDriver() == null || onboard.getDriver().getPhone() == null
                || onboard.getDriver().getPhone().isBlank()) {
            return;
        }
        String passengerName = onboard.getPassenger() == null
                ? "El pasajero"
                : (onboard.getPassenger().getFirstName() + " "
                        + onboard.getPassenger().getLastName()).trim();
        if (nextPassenger.isEmpty()) {
            String direction = onboard.getRouteDirection() == null ? "IDA" : onboard.getRouteDirection();
            String payload = "COMPLETE_TRIP_" + onboard.getDriver().getId() + "_"
                    + effectiveLegDate(onboard) + "_" + direction;
            messaging.sendButtons(
                    onboard.getDriver().getPhone(),
                    "Último pasajero abordado",
                    "🏁 ¡Llegaste al último pasajero de la hoja de ruta! Cuando arribes al destino final, presioná el botón para cerrar el viaje.",
                    List.of(new Button(payload, "🏁 Finalizar Viaje")));
            return;
        }
        String nextNotice = nextPassenger
                .filter(next -> next.getPassenger() != null)
                .map(next -> " Ya avisamos a " + next.getPassenger().getFirstName()
                        + " que es el próximo pasajero.")
                .orElse(" No quedan pasajeros pendientes en esta ruta.");
        messaging.sendButtons(
                onboard.getDriver().getPhone(),
                "Abordaje confirmado",
                "✅ " + passengerName + " fue confirmado a bordo." + nextNotice,
                List.of(new Button("VIEW_ROUTE", "🗺️ Ver Ruta")));
    }

    private DriverActor resolveActiveDriver(String phone) {
        String normalized = normalizePhone(phone);
        return driverRepository.findFirstByPhone(normalized)
                .filter(com.lunaris.ansenuza.domain.model.Driver::isActive)
                .or(() -> driverRepository.findByActiveTrue().stream()
                        .filter(driver -> normalizePhone(driver.getPhone()).equals(normalized))
                        .findFirst())
                .map(driver -> new DriverActor(driver.getId()))
                .orElseThrow(() ->
                        new IllegalStateException("El callback no pertenece a un chofer activo."));
    }

    private String normalizePhone(String phone) {
        return com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phone);
    }

    private boolean isBoardable(Reservation reservation) {
        String status = reservation.getStatus();
        boolean validReservationStatus = "CONFIRMED".equalsIgnoreCase(status)
                || "PENDING".equalsIgnoreCase(status);
        return validReservationStatus
                && reservation.getTravelStatus() != Reservation.TravelStatus.CANCELED
                && reservation.getTravelStatus() != Reservation.TravelStatus.NO_SHOW;
    }

    private Optional<Reservation> findNextPassengerInRoute(
            Reservation onboard, LocalDate effectiveDate) {
        if (onboard.getRouteSequence() != null && onboard.getRouteDirection() != null) {
            Optional<Reservation> persistedNext = reservationRepository.findNextRoutePassenger(
                            onboard.getDriver().getId(), effectiveDate,
                            onboard.getRouteDirection(), onboard.getRouteSequence() + 1)
                    .stream()
                    .filter(candidate -> belongsToSameLegAndDate(onboard, candidate, effectiveDate))
                    .filter(this::isPendingCandidate)
                    .findFirst();
            if (persistedNext.isPresent()) {
                log.info("[ONBOARD] N+1 encontrado por secuencia persistida: {}",
                        persistedNext.get().getRouteSequence());
                return persistedNext;
            }
        }
        List<Reservation> route = reservationRepository.findRouteByEffectiveDate(
                onboard.getDriver().getId(), effectiveDate);
        log.info(
                "[ONBOARD] Current passenger sequence={}, passengers found in route={}",
                onboard.getRouteSequence(), route.size());
        Comparator<Reservation> fallbackOrder = Comparator
                .comparing(Reservation::getDepartureSchedule,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getId,
                        Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<Reservation> routeOrder = onboard.getRouteSequence() == null
                ? fallbackOrder
                : Comparator.comparing(
                        Reservation::getRouteSequence,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(fallbackOrder);
        List<Reservation> orderedRoute = route.stream()
                .filter(candidate -> belongsToSameLegAndDate(onboard, candidate, effectiveDate))
                .sorted(routeOrder)
                .toList();
        if (onboard.getRouteSequence() != null) {
            return orderedRoute.stream()
                    .filter(candidate -> candidate.getRouteSequence() != null)
                    .filter(candidate -> candidate.getRouteSequence()
                            > onboard.getRouteSequence())
                    .filter(this::isPendingCandidate)
                    .findFirst();
        }
        int currentIndex = orderedRoute.stream()
                .map(Reservation::getId)
                .toList()
                .indexOf(onboard.getId());
        return currentIndex < 0 ? Optional.empty() : orderedRoute.stream()
                .skip(currentIndex + 1L)
                .filter(this::isPendingCandidate)
                .findFirst();
    }

    private boolean isPendingCandidate(Reservation reservation) {
        return reservation.getTravelStatus() == null
                || reservation.getTravelStatus() == Reservation.TravelStatus.PENDING
                || reservation.getTravelStatus() == Reservation.TravelStatus.SCHEDULED;
    }

    private String expectedNextSequence(Reservation onboard) {
        return onboard.getRouteSequence() == null
                ? "UNKNOWN"
                : String.valueOf(onboard.getRouteSequence() + 1);
    }

    private boolean belongsToSameLegAndDate(
            Reservation onboard, Reservation candidate, LocalDate effectiveDate) {
        return isReturnLeg(onboard) == isReturnLeg(candidate)
                && effectiveDate.equals(effectiveLegDate(candidate));
    }

    private LocalDate effectiveLegDate(Reservation reservation) {
        if (isReturnLeg(reservation) && reservation.getReturnDate() != null) {
            return reservation.getReturnDate();
        }
        return reservation.getTravelDate() != null
                ? reservation.getTravelDate()
                : reservation.getReturnDate();
    }

    private boolean isReturnLeg(Reservation reservation) {
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().endsWith("-VUELTA");
    }

    private void notifyNext(Reservation onboard, Reservation next) {
        if (next.getPassenger() == null || next.getPassenger().getPhone() == null
                || next.getPassenger().getPhone().isBlank()) {
            return;
        }
        messaging.sendTemplate(
                next.getPassenger().getPhone(),
                "proximo_en_camino",
                List.of(
                        textOrDefault(next.getPassenger().getFirstName(), "Pasajero"),
                        textOrDefault(onboard.getDriver().getFullName(), "Chofer")));
        messaging.sendText(
                next.getPassenger().getPhone(),
                "¡Hola " + next.getPassenger().getFirstName()
                        + "! El auto de Lunaris ya recogió al pasajero anterior y sos el próximo en la lista. "
                        + "Por favor estate atento/a en la puerta.");
        String locationUrl = onboard.getDriver().getCurrentLocationUrl();
        if (locationUrl != null && !locationUrl.isBlank()) {
            messaging.sendText(
                    next.getPassenger().getPhone(),
                    "📍 Ubicación actual del chofer: " + locationUrl);
        }
    }

    private int calculateEtaMinutes(String currentLocality, String nextLocality) {
        int currentMinutes = localityRepository.findFirstByNameIgnoreCase(currentLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        int nextMinutes = localityRepository.findFirstByNameIgnoreCase(nextLocality)
                .map(Locality::getMinutesFromOrigin)
                .orElse(0);
        return Math.abs(nextMinutes - currentMinutes);
    }

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private record DriverActor(UUID driverId) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/OperatorNotificationService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotification;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Executor;

@Service @Slf4j
public class OperatorNotificationService {
    private final OperatorNotificationPhoneRepository phones;
    private final WhatsAppService whatsApp;
    private final Executor executor;

    public OperatorNotificationService(OperatorNotificationPhoneRepository phones, WhatsAppService whatsApp,
            @Qualifier("taskExecutor") Executor executor) {
        this.phones = phones;
        this.whatsApp = whatsApp;
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyAfterCommit(OperatorNotification notification) {
        try {
            executor.execute(() -> send(notification));
        } catch (RuntimeException exception) {
            log.error("No se pudo programar la alerta de operadores", exception);
        }
    }

    private void send(OperatorNotification notification) {
        try {
            for (var operator : phones.findByActiveTrueOrderByCreatedAtAsc()) {
                try {
                    whatsApp.sendMessage(operator.getPhone(), notification.message());
                } catch (RuntimeException exception) {
                    log.error("No se pudo alertar al operador {}", operator.getId(), exception);
                }
            }
        } catch (RuntimeException exception) {
            log.error("No se pudieron consultar los operadores activos", exception);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/OperatorPhoneService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotificationPhone;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor @Transactional
public class OperatorPhoneService {
    private final OperatorNotificationPhoneRepository phones;

    @Transactional(readOnly = true)
    public List<OperatorNotificationPhone> list() { return phones.findAllByOrderByCreatedAtAsc(); }

    public void add(String name, String phone) {
        String normalized = phone == null ? "" : phone.replaceAll("[ +()\\-]", "");
        if (name == null || name.isBlank() || name.trim().length() > 255
                || !normalized.matches("[1-9][0-9]{7,14}")) {
            throw new DomainValidationException("Ingresá un nombre y un teléfono internacional válido (ej. 5493512282251).");
        }
        if (phones.existsByPhone(normalized)) {
            throw new DomainValidationException("El teléfono ya está registrado.");
        }
        var operator = new OperatorNotificationPhone();
        // Igual que Inquiry: Hibernate genera el UUID al persistir una entidad nueva.
        operator.setName(name.trim());
        operator.setPhone(normalized);
        operator.setCreatedAt(ArgentinaTime.now());
        phones.save(operator);
    }

    public void setActive(UUID id, boolean active) {
        var phone = find(id);
        phone.setActive(active);
        phones.save(phone);
    }

    public void delete(UUID id) { phones.delete(find(id)); }

    private OperatorNotificationPhone find(UUID id) {
        return phones.findById(id).orElseThrow(() -> new DomainValidationException("No se encontró el operador."));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/PassengerOtpService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassengerOtpService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int ARGENTINA_COUNTRY_CODE_LENGTH = 2;

    private final PassengerRepository passengerRepository;
    private final MessagingPort messagingPort;
    private final Duration otpTtl;
    private final Duration tokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();

    public PassengerOtpService(
            PassengerRepository passengerRepository,
            MessagingPort messagingPort,
            @Value("${lunaris.auth.otp-ttl:PT10M}") Duration otpTtl,
            @Value("${lunaris.auth.token-ttl:PT12H}") Duration tokenTtl) {
        this.passengerRepository = passengerRepository;
        this.messagingPort = messagingPort;
        this.otpTtl = otpTtl;
        this.tokenTtl = tokenTtl;
    }

    @Transactional
    public void sendOtp(String rawPhone) {
        sendOtp(rawPhone, null);
    }

    @Transactional
    public void sendOtp(String rawPhone, String fullName) {
        String phone = PhoneUtils.normalizeArgentinePhone(rawPhone);
        String otpKey = normalizeOtpKey(phone);
        Passenger passenger = passengerRepository.findByPhone(phone)
                .or(() -> findByOriginalPhone(rawPhone, phone))
                .orElseGet(() -> createPassenger(fullName, phone));
        String storedPhone = PhoneUtils.normalizeArgentinePhone(passenger.getPhone());
        String code = String.format("%04d", secureRandom.nextInt(10_000));
        challenges.put(otpKey, new OtpChallenge(code, Instant.now().plus(otpTtl), 0, storedPhone));
        messagingPort.sendOtp(storedPhone, passengerName(passenger), code);
    }

    private String passengerName(Passenger passenger) {
        return (passenger.getFirstName() + " " + passenger.getLastName()).trim();
    }

    private Passenger createPassenger(String fullName, String phone) {
        String normalizedName = fullName == null || fullName.isBlank()
                ? "Pasajero Sin apellido"
                : fullName.trim().replaceAll("\\s+", " ");
        int separator = normalizedName.indexOf(' ');
        String firstName = separator > 0 ? normalizedName.substring(0, separator) : normalizedName;
        String lastName = separator > 0 ? normalizedName.substring(separator + 1) : "Sin apellido";
        Passenger newPassenger = Passenger.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .build();
        try {
            // El ID debe permanecer nulo: con @GeneratedValue Hibernate hará persist en vez de merge.
            return passengerRepository.save(newPassenger);
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException exception) {
            // Otra petición puede haber creado al pasajero entre el find y el save.
            return passengerRepository.findByPhone(phone).orElseThrow(() -> exception);
        }
    }

    private Optional<Passenger> findByOriginalPhone(String rawPhone, String normalizedPhone) {
        String originalPhone = rawPhone.trim();
        return originalPhone.equals(normalizedPhone)
                ? Optional.empty()
                : passengerRepository.findByPhone(originalPhone);
    }

    public TokenResult verifyOtp(String rawPhone, String code) {
        String otpKey = normalizeOtpKey(rawPhone);
        OtpChallenge challenge = challenges.get(otpKey);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            challenges.remove(otpKey);
            throw new DomainValidationException("El código venció o no fue solicitado.");
        }
        if (challenge.attempts() >= MAX_ATTEMPTS || !challenge.code().equals(code)) {
            int attempts = challenge.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                challenges.remove(otpKey);
            } else {
                challenges.put(otpKey, new OtpChallenge(
                        challenge.code(), challenge.expiresAt(), attempts, challenge.storedPhone()));
            }
            throw new DomainValidationException("El código ingresado no es válido.");
        }

        challenges.remove(otpKey);
        Instant expiresAt = Instant.now().plus(tokenTtl);
        String token = generateToken();
        tokens.put(token, new AccessToken(challenge.storedPhone(), expiresAt));
        return new TokenResult(token, expiresAt);
    }

    private String normalizeOtpKey(String rawPhone) {
        String internationalPhone = PhoneUtils.normalizeArgentinePhone(rawPhone);
        return internationalPhone.substring(ARGENTINA_COUNTRY_CODE_LENGTH);
    }

    public Optional<String> resolvePhone(String token) {
        AccessToken accessToken = tokens.get(token);
        if (accessToken == null) {
            return Optional.empty();
        }
        if (accessToken.expiresAt().isBefore(Instant.now())) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(accessToken.phone());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record OtpChallenge(String code, Instant expiresAt, int attempts, String storedPhone) {
    }

    private record AccessToken(String phone, Instant expiresAt) {
    }

    public record TokenResult(String accessToken, Instant expiresAt) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/PersistPaymentReceiptUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ReservationEventRepository;
import com.lunaris.ansenuza.domain.model.ReservationEvent;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lunaris.ansenuza.shared.PhoneUtils;

/** Persiste el comprobante en una transacción corta, sin llamadas de red externas. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PersistPaymentReceiptUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationEventRepository reservationEventRepository;

    @Transactional
    public void execute(String phoneNumber, String receiptUrl) {
        Optional<Passenger> passenger = passengerRepository.findByPhone(phoneNumber);
        if (passenger.isEmpty()) {
            log.warn("[Bot Webhook] No existe ningún pasajero registrado con el teléfono: {}",
                    phoneNumber);
            return;
        }

        Optional<Reservation> pending = reservationRepository
                .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger.get())
                .stream()
                .filter(reservation -> "PENDING_PAYMENT".equals(reservation.getStatus()))
                .findFirst();
        if (pending.isEmpty()) {
            log.warn("[Bot Webhook] No se encontró ninguna reserva en PENDING_PAYMENT para el teléfono: {}",
                    phoneNumber);
            return;
        }

        Reservation selected = pending.get();
        String groupCode = selected.getBookingGroupCode() != null
                && !selected.getBookingGroupCode().isBlank()
                        ? selected.getBookingGroupCode()
                        : paymentGroupCode(selected.getReservationCode());
        List<Reservation> group = groupCode == null
                ? List.of(selected)
                : reservationRepository.findReservationGroupForUpdate(groupCode);
        if (group.isEmpty()) group = List.of(selected);
        String receiptGroup = groupCode == null ? selected.getReservationCode() : groupCode;
        if (reservationRepository.existsActiveReceiptInAnotherGroup(receiptUrl, receiptGroup)) {
            throw new DomainValidationException(
                    "El comprobante ya está vinculado a otra reserva activa.");
        }
        group.forEach(reservation -> {
            reservation.setPaymentReceiptUrl(receiptUrl);
            reservation.setPaymentVerified(false);
            reservation.setStatus("PAYMENT_RECEIVED");
            reservation.setPaymentExpiresAt(null);
        });
        reservationRepository.saveAllAndFlush(group);
        group.forEach(reservation -> reservationEventRepository.save(ReservationEvent.builder()
                .reservationId(reservation.getId())
                .eventType("PAYMENT_RECEIPT_LINKED")
                .description("Comprobante recibido; pago pendiente de verificación.")
                .triggeredBy("PASSENGER_WHATSAPP")
                .build()));
        log.info("[Bot Webhook] Comprobante enlazado con éxito para código: {}",
                selected.getReservationCode());
    }

    @Transactional
    public void executeByReservationCode(String reservationCode, String receiptUrl,
            String triggeredBy) {
        Reservation selected = reservationRepository.findByReservationCodeForUpdate(reservationCode)
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
        String groupCode = selected.getBookingGroupCode() != null
                && !selected.getBookingGroupCode().isBlank()
                        ? selected.getBookingGroupCode() : paymentGroupCode(reservationCode);
        List<Reservation> group = groupCode == null ? List.of(selected)
                : reservationRepository.findByBookingGroupCodeForUpdate(groupCode);
        if (group.isEmpty()) group = List.of(selected);
        String receiptGroup = groupCode == null ? reservationCode : groupCode;
        if (reservationRepository.existsActiveReceiptInAnotherGroup(receiptUrl, receiptGroup)) {
            throw new DomainValidationException(
                    "El comprobante ya está vinculado a otra reserva activa.");
        }
        for (Reservation reservation : group) {
            reservation.setPaymentReceiptUrl(receiptUrl);
            reservation.setPaymentVerified(false);
            reservation.setStatus("PAYMENT_RECEIVED");
            reservation.setPaymentExpiresAt(null);
            reservationRepository.save(reservation);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservationId(reservation.getId()).eventType("PAYMENT_RECEIPT_LINKED")
                    .description("Comprobante recibido; pago pendiente de verificación.")
                    .triggeredBy(triggeredBy).build());
        }
    }

    @Transactional
    public void executeByReservationCodeOwnedBy(String reservationCode, String receiptUrl,
            String triggeredBy, String authenticatedPhone) {
        Reservation selected = reservationRepository.findByReservationCodeForUpdate(reservationCode)
                .orElseThrow(() -> new DomainValidationException("La reserva indicada no existe."));
        String ownerPhone = selected.getPassenger() == null
                ? null : selected.getPassenger().getPhone();
        if (ownerPhone == null || !PhoneUtils.normalizeArgentinePhone(ownerPhone).equals(
                PhoneUtils.normalizeArgentinePhone(authenticatedPhone))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "La reserva no pertenece al pasajero autenticado.");
        }
        executeByReservationCode(reservationCode, receiptUrl, triggeredBy);
    }

    private String paymentGroupCode(String reservationCode) {
        if (reservationCode == null || !(reservationCode.endsWith("-IDA")
                || reservationCode.endsWith("-VUELTA"))) return null;
        return reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ProcessPaymentReceiptUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.lunaris.ansenuza.application.port.LiveChatPort;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.service.SameDayBookingPolicy;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Procesa el comprobante de pago (imagen) que envía el pasajero por WhatsApp:
 * lo enlaza con la primera reserva cronológica en estado PENDING_PAYMENT y la
 * promueve a PAYMENT_RECEIVED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessPaymentReceiptUseCase {

    private final PassengerRepository passengerRepository;
    private final ReservationRepository reservationRepository;
    private final ReceiptStoragePort receiptStoragePort;
    private final MessagingPort messaging;
    private final LiveChatPort liveChat;
    private final SameDayBookingPolicy sameDayBookingPolicy;
    private final ReservationService reservationService;
    private final PersistPaymentReceiptUseCase persistPaymentReceiptUseCase;
    private final PricingAndScheduleService pricingAndScheduleService;

    public void execute(String phoneNumber, String mediaId) {
        // 1. Descargamos y persistimos el comprobante una única vez. Devuelve la URL
        //    pública (Cloudinary secure_url) con la que se renderiza la imagen.
        String receiptUrl = receiptStoragePort.downloadAndSaveReceipt(mediaId);

        if (receiptUrl != null) {
            // 2. Reflejamos la imagen en la sala de chat en vivo del operador (persistencia +
            //    broadcast por WebSocket). El frontend detecta la URL y la renderiza como <img>.
            liveChat.recordIncomingMessage(phoneNumber, receiptUrl);
        } else {
            log.warn("[Bot Webhook] El almacenamiento devolvió NULL al descargar el mediaId: {}",
                    mediaId);
        }

        // 3. Recién ahora se abre una transacción corta para datos financieros.
        if (receiptUrl != null) persistPaymentReceiptUseCase.execute(phoneNumber, receiptUrl);

        messaging.sendText(phoneNumber,
                "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
    }

    /** Procesa un recurso ya disponible localmente, usado por el simulador de desarrollo. */
    public void executeStoredReceipt(String phoneNumber, String receiptUrl) {
        if (receiptUrl == null || receiptUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del comprobante es obligatoria.");
        }
        liveChat.recordIncomingMessage(phoneNumber, receiptUrl);
        persistPaymentReceiptUseCase.execute(phoneNumber, receiptUrl);
        messaging.sendText(phoneNumber,
                "✅ *Comprobante recibido.*\n\nNuestro equipo verificará la transferencia y confirmará tu viaje a la brevedad.");
    }

    public Reservation confirmOrCreateWebBooking(
            String phoneNumber,
            MultipartFile receiptFile,
            BookingVerificationData bookingData) {
        String normalizedPhone = com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(phoneNumber);
        Passenger passenger = passengerRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                        "No existe un pasajero para confirmar la reserva."));
        Optional<Reservation> pendingReservation = reservationRepository
                .findByPassengerOrderByTravelDateAscDepartureScheduleAscCreatedAtDesc(passenger)
                .stream()
                .filter(candidate -> "PENDING_PAYMENT".equals(candidate.getStatus())
                        || "PAYMENT_RECEIVED".equals(candidate.getStatus())
                        || "PENDING_VERIFICATION".equals(candidate.getStatus()))
                .findFirst();

        if (pendingReservation.isEmpty()) {
            validateBookingData(bookingData);
            sameDayBookingPolicy.validate(
                    bookingData.travelDate(), bookingData.scheduleBlock());
        }

        // La llamada a Cloudinary/almacenamiento ocurre antes de abrir cualquier transacción.
        String receiptUrl = uploadReceipt(receiptFile, normalizedPhone);
        Reservation reservation = pendingReservation.orElseGet(() -> newWebReservation(passenger, bookingData));
        if (pendingReservation.isPresent()) {
            if (receiptUrl != null) {
                persistPaymentReceiptUseCase.executeByReservationCode(
                        reservation.getReservationCode(), receiptUrl, "PASSENGER_WEB");
                return reservationRepository.findById(reservation.getId()).orElse(reservation);
            }
            return reservation;
        }
        if (receiptUrl != null) reservation.setPaymentReceiptUrl(receiptUrl);
        return reservationService.saveReservationFlow(reservation).getFirst();
    }

    private String uploadReceipt(MultipartFile receiptFile, String phoneNumber) {
        if (receiptFile == null || receiptFile.isEmpty()) {
            return null;
        }
        String receiptUrl = receiptStoragePort.uploadFile(receiptFile);
        if (receiptUrl == null || receiptUrl.isBlank()) {
            log.warn("No se pudo almacenar el comprobante subido para el teléfono {}.", phoneNumber);
            return null;
        }
        return receiptUrl;
    }

    private Reservation newWebReservation(Passenger passenger, BookingVerificationData data) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(passenger)
                .travelDate(data.travelDate())
                .departureSchedule(data.scheduleBlock().trim())
                .pickupLocality(data.pickupLocality().trim())
                .destination(data.destination().trim())
                .passengerCount(data.passengerCount())
                .tripType(data.tripType())
                .roundTrip(data.tripType() != com.lunaris.ansenuza.domain.model.TripType.ONE_WAY)
                // El total enviado por el navegador se ignora deliberadamente.
                .amount(pricingAndScheduleService.calculateReservationAmount(
                        data.pickupLocality(), data.destination(), data.tripType(), data.passengerCount()))
                .paymentVerified(false)
                .requiresInvoice(true)
                .status("PENDING_VERIFICATION")
                .source(ReservationSource.WEB)
                .build();
    }

    private void validateBookingData(BookingVerificationData data) {
        if (data == null || data.travelDate() == null
                || isBlank(data.scheduleBlock()) || isBlank(data.pickupLocality())
                || isBlank(data.destination()) || data.passengerCount() == null
                || data.passengerCount() < 1 || data.tripType() == null) {
            throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                    "Los datos completos del viaje son obligatorios para crear la reserva.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ProcessPromotionCommandUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.service.PromotionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessPromotionCommandUseCase {

    private static final Pattern INDIVIDUAL_COMMAND = Pattern.compile(
            "^PROMO\\s+(GRATIS|\\d{1,3})$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MASSIVE_COMMAND = Pattern.compile(
            "^PROMO\\s+MASIVA\\s+(GRATIS|\\d{1,3})(?:\\s+(\\d{1,6})D)?$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter EXPIRATION_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PromotionService promotionService;
    private final MessagingPort messaging;

    @Value("${promotions.authorized-operator-phone:}")
    private String martinPhoneNumber;

    public boolean isPromotionCommand(String body) {
        return body != null && body.trim().toUpperCase(Locale.ROOT).startsWith("PROMO");
    }

    public void execute(String phoneNumber, String body) {
        if (!normalize(phoneNumber).equals(normalize(martinPhoneNumber))) {
            messaging.sendText(phoneNumber, "⛔ No estás autorizado para generar promociones.");
            return;
        }

        Matcher massiveMatcher = MASSIVE_COMMAND.matcher(body.trim());
        Matcher individualMatcher = INDIVIDUAL_COMMAND.matcher(body.trim());
        boolean massive = massiveMatcher.matches();
        if (!massive && !individualMatcher.matches()) {
            messaging.sendText(phoneNumber,
                    "Formato inválido. Usá *PROMO 10*, *PROMO GRATIS* o *PROMO MASIVA 10 7D*.");
            return;
        }

        Matcher matcher = massive ? massiveMatcher : individualMatcher;
        int percentage = "GRATIS".equalsIgnoreCase(matcher.group(1)) ? 100 : Integer.parseInt(matcher.group(1));
        try {
            Promotion promotion = massive
                    ? promotionService.createMassive(percentage, parseDays(matcher.group(2)))
                    : promotionService.create(percentage);
            String response = massive
                    ? "✅ Promoción masiva creada: código *%s* con *%d%%* de descuento. Vence el *%s hs*."
                            .formatted(promotion.getCode(), promotion.getDiscountPercentage(),
                                    promotion.getExpiresAt().format(EXPIRATION_FORMAT))
                    : "✅ Promoción individual creada: código *%s* con *%d%%* de descuento."
                            .formatted(promotion.getCode(), promotion.getDiscountPercentage());
            messaging.sendText(phoneNumber, response);
        } catch (IllegalArgumentException exception) {
            messaging.sendText(phoneNumber, "❌ " + exception.getMessage());
        }
    }

    private Duration parseDays(String amount) {
        return Duration.ofDays(amount == null ? 7 : Long.parseLong(amount));
    }

    private String normalize(String number) {
        return number == null ? "" : number.replaceAll("[^0-9]", "");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ReservationDriverAssignmentService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationDriverAssignmentService {

    private static final int VEHICLE_CAPACITY = 4;
    private static final String CAPACITY_MESSAGE =
            "No se pueden asignar más de 4 pasajeros a un solo vehículo/chofer.";

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final TripRouteCalculatorService routeCalculator = new TripRouteCalculatorService();

    @Transactional
    public Optional<Reservation> assign(UUID reservationId, UUID driverId) {
        Optional<Reservation> reservation = reservationRepository.findByIdForUpdate(reservationId);
        if (reservation.isEmpty()) {
            return Optional.empty();
        }
        return driverRepository.findById(driverId)
                .map(driver -> {
                    if (reservation.get().getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                        throw new DomainValidationException(
                                "No se puede modificar una reserva porque la ruta ya fue enviada al chofer.");
                    }
                    String direction = reservation.get().getRouteDirection();
                    if (direction == null) {
                        direction = TripRouteCalculatorService.isCordoba(reservation.get().getPickupLocality())
                                ? "VUELTA" : "IDA";
                    }
                    String schedule = reservation.get().getDepartureSchedule();
                    java.util.stream.Stream<Reservation> assignedReservations = (schedule == null || schedule.isBlank())
                            ? reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                                    driverId, reservation.get().getTravelDate()).stream()
                            : reservationRepository.findByDriverAndRouteScope(
                                    driverId, reservation.get().getTravelDate(), schedule, direction).stream();
                    int assignedSeats = assignedReservations
                            .filter(assigned -> !assigned.getId().equals(reservationId))
                            .filter(assigned -> routeCalculator.sameManifest(reservation.get(), assigned))
                            .mapToInt(Reservation::getTotalSeats)
                            .sum();
                    if (assignedSeats + reservation.get().getTotalSeats() > VEHICLE_CAPACITY) {
                        throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                                CAPACITY_MESSAGE);
                    }
                    reservation.get().setDriver(driver);
                    return reservationRepository.saveAndFlush(reservation.get());
                });
    }

    @Transactional
    public Optional<Reservation> unassign(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    if (reservation.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT) {
                        throw new DomainValidationException(
                                "No se puede modificar una reserva porque la ruta ya fue enviada al chofer.");
                    }
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                    reservation.setTravelStatus(Reservation.TravelStatus.PENDING);
                    return reservationRepository.saveAndFlush(reservation);
                });
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ResolveEffectiveTripOriginService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.BookingDemand;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolveEffectiveTripOriginService implements ResolveEffectiveTripOriginUseCase {
    private final ReservationRepository reservationRepository;
    private final LocalityRepository localityRepository;
    private final TripRouteCalculatorService calculator = new TripRouteCalculatorService();

    @Override
    @Transactional(readOnly = true)
    public RouteOriginResolution resolve(LocalDate travelDate, String scheduleBlock) {
        if (travelDate == null || scheduleBlock == null || scheduleBlock.isBlank()) {
            throw new IllegalArgumentException("La fecha y el bloque horario son obligatorios.");
        }
        String normalizedSchedule = normalizeSchedule(scheduleBlock);
        List<Reservation> reservations = reservationRepository.findConfirmedActiveByTravelDate(travelDate).stream()
                .filter(reservation -> calculator.matchesManifest(
                        reservation, RouteDirection.OUTBOUND, normalizedSchedule))
                .filter(reservation -> reservation.getTotalSeats() > 0)
                .toList();
        var calculation = calculator.calculate(reservations.stream()
                .map(reservation -> new BookingDemand(reservation.getPickupLocality(), reservation.getTotalSeats()))
                .toList());
        var offsets = calculateOffsets(calculation.effectiveOrigin());
        log.info("{} Fecha={}, turno={}", calculation.message(), travelDate, normalizedSchedule);
        return new RouteOriginResolution(travelDate, normalizedSchedule, calculation.effectiveOrigin(),
                calculation.skippedLocalities(), offsets, calculation.message());
    }

    private java.util.Map<String, Integer> calculateOffsets(String effectiveOrigin) {
        if (effectiveOrigin == null) return java.util.Map.of();
        int originMinutes = localityRepository.findFirstByNameIgnoreCase(effectiveOrigin)
                .map(locality -> locality.getMinutesFromOrigin() == null ? 0 : locality.getMinutesFromOrigin())
                .orElse(0);
        var offsets = new LinkedHashMap<String, Integer>();
        TripRouteCalculatorService.NORTH_TERMINAL_CORRIDOR.forEach(locality -> localityRepository
                .findFirstByNameIgnoreCase(locality)
                .ifPresent(found -> offsets.put(locality,
                        Math.abs((found.getMinutesFromOrigin() == null ? 0 : found.getMinutesFromOrigin())
                                - originMinutes))));
        offsets.put(effectiveOrigin, 0);
        return java.util.Map.copyOf(offsets);
    }

    static String normalizeSchedule(String schedule) {
        if (schedule == null) return "";
        String value = schedule.trim().toUpperCase(Locale.ROOT);
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("H:mm", Locale.ROOT),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ROOT))) {
            try {
                return LocalTime.parse(value, formatter).format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato soportado.
            }
        }
        return value;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ScheduleService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private static final List<String> RETURN_SCHEDULES = List.of("14:00", "17:30");

    private final PricingAndScheduleService pricingAndScheduleService;
    private final LocalityService localityService;

    public List<ScheduleDto> getSchedulesForWeb(String pickupLocality, LocalDate travelDate) {
        if (!isActivePickupLocality(pickupLocality)) {
            return List.of();
        }
        return pricingAndScheduleService.departureSchedules().stream()
                .map(schedule -> {
                    int availableSeats = pricingAndScheduleService
                            .availableSeats(travelDate, schedule);
                    String baseTime = schedule.substring(0, 5);
                    String calculatedTime = pricingAndScheduleService
                            .calculateEstimatedPickupTime(
                                    pickupLocality, baseTime, false, travelDate);
                    String departureTime = calculatedTime.substring(0, 5);
                    return new ScheduleDto(
                            baseTime, departureTime, calculatedTime,
                            availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<ScheduleDto> getReturnSchedulesForWeb(LocalDate travelDate) {
        return RETURN_SCHEDULES.stream()
                .map(schedule -> {
                    int availableSeats = pricingAndScheduleService.availableReturnSeats(travelDate, schedule);
                    String label = pricingAndScheduleService.calculateEstimatedPickupTime(
                            null, schedule, true, travelDate);
                    return new ScheduleDto(
                            schedule, label.substring(0, 5), label,
                            availableSeats, availableSeats > 0);
                })
                .toList();
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate) {
        return getSchedulesForBot(pickupLocality, destination, travelDate, 1);
    }

    public List<String> getSchedulesForBot(
            String pickupLocality, String destination, LocalDate travelDate, int requestedSeats) {
        if (requestedSeats < 1) return List.of();
        if (BotRoute.fromCordoba(pickupLocality)) {
            return travelDate == null ? RETURN_SCHEDULES : RETURN_SCHEDULES.stream()
                    .filter(schedule -> pricingAndScheduleService.isWithinPlanningWindow(travelDate, schedule))
                    .filter(schedule -> pricingAndScheduleService.availableReturnSeats(travelDate, schedule) >= requestedSeats)
                    .toList();
        }
        if (!isActivePickupLocality(pickupLocality)) return List.of();
        // Se revalida disponibilidad al conocer fecha y cantidad definitiva.
        if (travelDate == null) return pricingAndScheduleService.departureSchedules();
        return pricingAndScheduleService.availableDepartureSchedules(
                pickupLocality, destination, travelDate, requestedSeats);
    }

    private boolean isActivePickupLocality(String pickupLocality) {
        if (pickupLocality == null || pickupLocality.isBlank()) {
            return true;
        }
        return localityService.findAllWithActiveFare().stream()
                .anyMatch(locality -> locality.getName().equalsIgnoreCase(pickupLocality.trim()));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/SubmitDriverApplicationUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.DriverDocumentStoragePort;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.shared.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SubmitDriverApplicationUseCase {

    private static final String COMPANY_VEHICLE = "Unidad de Empresa";

    private final DriverApplicationRepository repository;
    private final DriverDocumentStoragePort documentStorage;

    @Transactional
    public DriverApplication execute(DriverApplicationRequest request) {
        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(request.phone());
        DriverApplication application = repository.findFirstByPhone(normalizedPhone)
                .orElseGet(DriverApplication::new);
        application.updateSubmission(
                request.fullName().trim(),
                normalizedPhone,
                normalizeVehicleModel(request.vehicleModel()),
                request.vehicleYear(),
                normalizeLicensePlate(request.licensePlate()),
                false);
        application.setLocality(normalizeLocality(request.locality()));
        application.updateDocuments(
                normalizeOptional(request.insuranceFileUrl()),
                normalizeOptional(request.greenCardFileUrl()),
                null);
        return repository.save(application);
    }

    @Transactional
    public DriverApplication execute(
            MultipartSubmission submission,
            MultipartFile insuranceFile,
            MultipartFile greenCardFile,
            MultipartFile criminalRecordFile) {
        String insuranceFileUrl = storeIfPresent("insurance", insuranceFile);
        String greenCardFileUrl = storeIfPresent("green-card", greenCardFile);
        String criminalRecordFileUrl = storeIfPresent("criminal-record", criminalRecordFile);

        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(submission.phone());
        DriverApplication application = repository.findFirstByPhone(normalizedPhone)
                .orElseGet(DriverApplication::new);
        application.updateSubmission(
                submission.fullName().trim(),
                normalizedPhone,
                normalizeVehicleModel(submission.vehicleModel()),
                submission.vehicleYear(),
                normalizeLicensePlate(submission.plateNumber()),
                submission.wantsDirectContact());
        application.setLocality(normalizeLocality(submission.locality()));
        application.updateDocuments(
                insuranceFileUrl, greenCardFileUrl, criminalRecordFileUrl);
        return repository.save(application);
    }

    private String storeIfPresent(String documentType, MultipartFile file) {
        return file == null || file.isEmpty() ? null : documentStorage.store(documentType, file);
    }

    private String normalizeLocality(String locality) {
        return locality == null || locality.isBlank() ? "Sin especificar" : locality.trim();
    }

    private String normalizeVehicleModel(String vehicleModel) {
        return vehicleModel == null || vehicleModel.isBlank()
                ? COMPANY_VEHICLE
                : vehicleModel.trim();
    }

    private String normalizeLicensePlate(String licensePlate) {
        return licensePlate == null || licensePlate.isBlank()
                ? null
                : licensePlate.trim().toUpperCase();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record MultipartSubmission(
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String plateNumber,
            boolean wantsDirectContact) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/TakeOverConversationUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TakeOverConversationUseCase {
    private final ConversationSessionRepository sessionRepository;
    private final com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry;

    @org.springframework.beans.factory.annotation.Autowired
    public TakeOverConversationUseCase(ConversationSessionRepository sessionRepository,
            com.lunaris.ansenuza.application.port.ChatbotTelemetryPort telemetry) {
        this.sessionRepository = sessionRepository;
        this.telemetry = telemetry;
    }

    public TakeOverConversationUseCase(ConversationSessionRepository sessionRepository) {
        this(sessionRepository, com.lunaris.ansenuza.application.port.ChatbotTelemetryPort.NOOP);
    }

    @Transactional
    public String execute(String phoneNumber) {
        // Keep every country/area digit; never interpret a session ID as a phone.
        String phone = phoneNumber == null ? "" : phoneNumber.replaceAll("[\\s()+-]", "");
        if (!phone.matches("[1-9][0-9]{7,14}")) {
            throw new DomainValidationException("El teléfono debe incluir país y área completos.");
        }
        var session = sessionRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new DomainValidationException("No existe una conversación para ese teléfono."));
        if (!session.isBotPaused()) {
            session.setBotPaused(true);
            sessionRepository.saveAndFlush(session);
        }
        telemetry.handoff(session.getPhoneNumber());
        return session.getPhoneNumber();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/ToggleSpecialTripStatusService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.ToggleSpecialTripStatusUseCase;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleSpecialTripStatusService implements ToggleSpecialTripStatusUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip setActive(Long id, boolean active) {
        SpecialTrip current = repository.findById(id).orElseThrow(() -> new SpecialTripNotFoundException(id));
        return repository.save(current.update(current.title(), current.description(), current.origin(),
                current.destination(), current.startDate(), current.endDate(), current.price(),
                current.maxPassengers(), current.imageUrl(), active));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateFareService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.UpdateFareUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFareService implements UpdateFareUseCase {
    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;

    @Override
    @Transactional
    public FareLocalityView updateFare(UUID fareId, BigDecimal amount) {
        FareLocalityValidation.amount(amount);
        var fare = fareRepository.findById(fareId)
                .orElseThrow(() -> new DomainValidationException("La tarifa indicada no existe."));
        var locality = localityRepository.findFirstByNameIgnoreCase(fare.getLocalityName())
                .orElseThrow(() -> new DomainValidationException("La tarifa no tiene una localidad válida asociada."));
        fare.setAmount(amount);
        fareRepository.save(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateLocalityFareService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateLocalityFareService implements UpdateLocalityFareUseCase {
    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;

    @Override
    @Transactional
    public FareLocalityView updateLocalityAndFare(UUID localityId, String name, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount) {
        String normalizedName = FareLocalityValidation.localityName(name);
        FareLocalityValidation.amount(amount);
        FareLocalityValidation.nonNegative(kmsToCordoba, "Los kilómetros");

        var locality = localityRepository.findById(localityId)
                .orElseThrow(() -> new DomainValidationException("La localidad indicada no existe."));
        localityRepository.findFirstByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(localityId))
                .ifPresent(existing -> { throw new DomainValidationException("Ya existe una localidad con ese nombre."); });

        String previousName = locality.getName();
        Fare fare = fareRepository.findFirstByLocalityNameIgnoreCase(previousName).orElseGet(() -> Fare.builder()
                .id(UUID.randomUUID()).localityName(normalizedName).amount(amount).build());
        fareRepository.findFirstByLocalityNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(fare.getId()))
                .ifPresent(existing -> { throw new DomainValidationException("Ya existe una tarifa para esa localidad."); });

        locality.setName(normalizedName);
        locality.setKmsToCordoba(kmsToCordoba);
        locality.setMinutesFromOrigin(minutesFromOrigin);
        fare.setLocalityName(normalizedName);
        fare.setAmount(amount);
        localityRepository.save(locality);
        fareRepository.save(fare);
        return new FareLocalityView(fare.getId(), locality.getId(), locality.getName(), fare.getAmount(),
                locality.getKmsToCordoba(), locality.getMinutesFromOrigin());
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/UpdatePassengerAddressUseCase.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePassengerAddressUseCase {

    private final PassengerRepository passengerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void update(String phone, String address, String locality) {
        passengerRepository.findByPhoneForUpdate(phone).ifPresent(passenger -> {
            passenger.setAddress(address);
            passenger.setLocality(locality);
        });
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/UpdateSpecialTripService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.out.SpecialTripRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSpecialTripService implements UpdateSpecialTripUseCase {
    private final SpecialTripRepositoryPort repository;

    @Override
    @Transactional
    public SpecialTrip update(Long id, SpecialTripCommand command) {
        SpecialTrip current = repository.findById(id).orElseThrow(() -> new SpecialTripNotFoundException(id));
        return repository.save(current.update(command.title(), command.description(), command.origin(),
                command.destination(), command.startDate(), command.endDate(), command.price(),
                command.maxPassengers(), command.imageUrl(), command.active()));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListConversionService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListConversionService {

    private final WaitingListRepository waitingListRepository;
    private final PassengerRepository passengerRepository;
    private final ReservationService reservationService;
    private final PricingAndScheduleService pricingAndScheduleService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation convert(Long id) {
        WaitingListEntry entry = requireWaitingEntry(id);
        Reservation reservation = createReservation(entry, "CONFIRMED");
        entry.setStatus(WaitingListEntry.CONFIRMED);
        waitingListRepository.saveAndFlush(entry);
        return reservation;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Reservation beginPayment(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.NOTIFIED.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no está disponible para confirmar.");
        }
        Reservation reservation = createReservation(entry, "PENDING_PAYMENT");
        entry.setStatus(WaitingListEntry.AWAITING_PAYMENT);
        waitingListRepository.saveAndFlush(entry);
        return reservation;
    }

    private Reservation createReservation(WaitingListEntry entry, String status) {
        int requestedSeats = Math.max(1, entry.getPassengerCount());

        // La promoción fue autorizada explícitamente por Operaciones, normalmente luego de
        // asignar un coche de refuerzo. Por eso este flujo no reaplica el cupo nominal global.
        Passenger passenger = resolvePassenger(entry);
        BigDecimal amount = pricingAndScheduleService.calculateReservationAmount(
                entry.getPickupLocality(), entry.getDestination(),
                TripType.ONE_WAY, requestedSeats);
        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(entry.getTravelDate())
                .pickupLocality(entry.getPickupLocality())
                .destination(entry.getDestination())
                .roundTrip(false)
                .tripType(TripType.ONE_WAY)
                .passengerCount(requestedSeats)
                .paymentVerified(false)
                .requiresInvoice(true)
                .status(status)
                .source(ReservationSource.MANUAL)
                .amount(amount)
                .waitingListEntryId(entry.getId())
                .notes("Promovida desde lista de espera")
                .build();
        List<Reservation> saved = reservationService.saveReservationFlow(reservation);
        return saved.getFirst();
    }

    @Transactional
    public WaitingListEntry cancel(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.PENDING.equals(entry.getStatus())
                && !WaitingListEntry.WAITING.equals(entry.getStatus())
                && !WaitingListEntry.NOTIFIED.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no se puede cancelar desde este flujo.");
        }
        entry.setStatus(WaitingListEntry.CANCELLED);
        return waitingListRepository.saveAndFlush(entry);
    }

    private WaitingListEntry requireWaitingEntry(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.WAITING.equals(entry.getStatus())) {
            throw new DomainValidationException(
                    "La entrada ya no se encuentra en estado WAITING.");
        }
        return entry;
    }

    private Passenger resolvePassenger(WaitingListEntry entry) {
        String normalizedPhone = PhoneUtils.normalizeArgentinePhone(entry.getPhoneNumber());
        return passengerRepository.findByPhone(normalizedPhone).orElseGet(() -> {
            String normalizedName = entry.getPassengerName().trim().replaceAll("\\s+", " ");
            int separator = normalizedName.indexOf(' ');
            String firstName = separator > 0 ? normalizedName.substring(0, separator) : normalizedName;
            String lastName = separator > 0 ? normalizedName.substring(separator + 1) : "Sin apellido";
            return passengerRepository.saveAndFlush(Passenger.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .phone(normalizedPhone)
                    .build());
        });
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListOtpService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Gestiona el desafío OTP específico para altas públicas de lista de espera. */
@Service
public class WaitingListOtpService {

    private static final Logger logger = LoggerFactory.getLogger(WaitingListOtpService.class);

    private static final String INVALID_CODE = "Código OTP inválido o vencido";

    private final WhatsAppService whatsAppService;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    public WaitingListOtpService(
            WhatsAppService whatsAppService,
            @Value("${lunaris.waiting-list.otp-ttl:PT5M}") Duration ttl) {
        this.whatsAppService = whatsAppService;
        this.ttl = ttl;
    }

    public String request(String rawPhone, String passengerName) {
        String phone = normalize(rawPhone);
        String code = String.format("%04d", random.nextInt(10_000));
        challenges.put(phone, new Challenge(code, Instant.now().plus(ttl)));
        try {
            String effectiveName = passengerName == null || passengerName.isBlank()
                    ? "Pasajero"
                    : passengerName.trim();
            whatsAppService.sendOtpMessage(
                    phone, effectiveName, code);
            logger.info("WhatsApp OTP sent for special event waiting list to {}", phone);
        } catch (RuntimeException exception) {
            logger.warn("No se pudo enviar OTP por WhatsApp para {}. Se conserva el desafío.", phone,
                    exception);
        }
        return code;
    }

    public void verify(String rawPhone, String code) {
        String phone;
        try {
            phone = normalize(rawPhone);
        } catch (DomainValidationException exception) {
            throw new DomainValidationException(INVALID_CODE);
        }
        Challenge challenge = challenges.get(phone);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())
                || code == null || !code.matches("\\d{4}")
                || !challenge.code().equals(code)) {
            if (challenge != null && challenge.expiresAt().isBefore(Instant.now())) {
                challenges.remove(phone, challenge);
            }
            throw new DomainValidationException(INVALID_CODE);
        }
        challenges.remove(phone, challenge);
    }

    private String normalize(String rawPhone) {
        return PhoneUtils.normalizeArgentinePhone(rawPhone);
    }

    private record Challenge(String code, Instant expiresAt) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListReengagementService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListReengagementService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final WaitingListRepository waitingListRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;

    @Transactional
    public WaitingListEntry promote(Long id) {
        WaitingListEntry entry = waitingListRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        if (!WaitingListEntry.WAITING.equals(entry.getStatus())) {
            throw new DomainValidationException("Sólo se pueden notificar entradas en estado WAITING.");
        }

        ConversationSession session = conversationSessionRepository
                .findByPhoneNumber(entry.getPhoneNumber())
                .orElseGet(() -> ConversationSession.builder()
                        .phoneNumber(entry.getPhoneNumber())
                        .build());
        session.setPassengerName(entry.getPassengerName());
        session.setTravelDate(entry.getTravelDate());
        session.setPickupLocality(entry.getPickupLocality());
        session.setDestination(entry.getDestination());
        session.setPassengerCount(entry.getPassengerCount());
        session.setWaitingListEntryId(entry.getId());
        session.setCurrentStep("CONFIRMING_WAITING_LIST_BOOKING");
        session.setBotPaused(false);
        conversationSessionRepository.saveAndFlush(session);

        entry.setStatus(WaitingListEntry.NOTIFIED);
        waitingListRepository.saveAndFlush(entry);
        messaging.sendButtons(entry.getPhoneNumber(), "Lugar disponible",
                "¡Hola " + entry.getPassengerName()
                        + "! Se liberó un lugar para tu viaje del "
                        + entry.getTravelDate().format(DATE_FORMAT) + " ("
                        + entry.getPickupLocality() + " -> " + entry.getDestination()
                        + "). ¿Deseás confirmar tu reserva ahora?",
                List.of(
                        new Button("confirm_waiting_list", "Confirmar y Pagar ✅"),
                        new Button("reject_waiting_list", "Rechazar ❌")));
        return entry;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/application/usecase/WaitingListService.java`

```java
package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitingListService {

    private static final String DEFAULT_DESTINATION = "Córdoba";

    private static final Set<String> VALID_STATUSES = Set.of(
            WaitingListEntry.PENDING, "PENDIENTE", "NEW",
            WaitingListEntry.WAITING, WaitingListEntry.CONTACTED,
            WaitingListEntry.CONFIRMED, WaitingListEntry.CANCELLED,
            WaitingListEntry.NOTIFIED, WaitingListEntry.AWAITING_PAYMENT,
            WaitingListEntry.CONVERTED);

    private final WaitingListRepository repository;

    @Transactional(readOnly = true)
    public List<WaitingListEntry> find(LocalDate travelDate, String status) {
        if (status != null && !status.isBlank()) {
            String normalizedStatus = normalizeStatus(status);
            return travelDate == null
                    ? repository.findByNormalizedStatusOrderByCreatedAtDesc(normalizedStatus)
                    : repository.findByTravelDateAndNormalizedStatusOrderByCreatedAtAsc(
                            travelDate, normalizedStatus);
        }
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<WaitingListEntry> findWaiting(LocalDate travelDate) {
        return travelDate == null
                ? repository.findAllActiveWaitingOrderByCreatedAtDesc()
                : repository.findActiveWaitingForDateIncludingNull(travelDate);
    }

    @Transactional(readOnly = true)
    public List<WaitingListEntry> findAllActiveWaiting() {
        return repository.findAllActiveWaitingOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<WaitingListEntry> findActiveSpecialEvents() {
        return repository.findActiveSpecialEventsOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countAllActiveWaiting() {
        return repository.countAllActiveWaiting();
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctEventTypes() {
        return repository.findDistinctEventTypes();
    }

    @Transactional
    public WaitingListEntry create(String phoneNumber, String passengerName,
            LocalDate travelDate, String pickupLocality, String destination,
            Integer passengerCount, String notes, String eventType) {
        String normalizedEventType = eventType == null || eventType.isBlank()
                ? "GENERAL" : eventType.trim().toUpperCase(Locale.ROOT);
        String effectiveDestination = destination == null || destination.isBlank()
                ? DEFAULT_DESTINATION : destination.trim();
        return repository.saveAndFlush(WaitingListEntry.builder()
                .phoneNumber(requireText(phoneNumber, "teléfono del pasajero"))
                .passengerName(requireText(passengerName, "nombre del pasajero"))
                .travelDate(travelDate)
                .pickupLocality(requireText(pickupLocality, "localidad de origen"))
                .destination(effectiveDestination)
                .passengerCount(passengerCount == null ? 1 : Math.max(1, passengerCount))
                .notes(notes)
                .eventType(normalizedEventType)
                .status(WaitingListEntry.PENDING)
                .build());
    }

    @Transactional
    public WaitingListEntry join(ConversationSession session) {
        if (session.getTravelDate() == null) {
            throw new DomainValidationException(
                    "Falta la fecha de viaje para ingresar a lista de espera.");
        }
        return repository.saveAndFlush(WaitingListEntry.builder()
                .phoneNumber(session.getPhoneNumber())
                .passengerName(requireText(session.getPassengerName(), "nombre del pasajero"))
                .travelDate(session.getTravelDate())
                .pickupLocality(requireText(session.getPickupLocality(), "localidad de origen"))
                .destination(requireText(session.getDestination(), "destino"))
                .passengerCount(session.getPassengerCount() == null ? 1 : session.getPassengerCount())
                .status(WaitingListEntry.WAITING)
                .build());
    }

    @Transactional
    public WaitingListEntry updateStatus(Long id, String status) {
        WaitingListEntry entry = repository.findById(id)
                .orElseThrow(() -> new DomainValidationException(
                        "La entrada de lista de espera indicada no existe."));
        entry.setStatus(normalizeStatus(status));
        return repository.saveAndFlush(entry);
    }

    private String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) {
            throw new DomainValidationException("Estado de lista de espera inválido.");
        }
        return normalized;
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException("Falta " + field + " para ingresar a lista de espera.");
        }
        return value.trim();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/DomainValidationException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/DriverApplicationNotFoundException.java`

```java
package com.lunaris.ansenuza.domain.exception;

import java.util.UUID;

public class DriverApplicationNotFoundException extends RuntimeException {

    public DriverApplicationNotFoundException(UUID id) {
        super("No se encontró la solicitud de chofer " + id + ".");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/FareLocalityInUseException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class FareLocalityInUseException extends RuntimeException {
    public FareLocalityInUseException(String localityName) {
        super("No se puede eliminar la tarifa de " + localityName
                + " porque existen reservas activas asociadas.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/InquiryNotFoundException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class InquiryNotFoundException extends RuntimeException {
    public InquiryNotFoundException() {
        super("No se encontró la consulta solicitada.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/MassivePromotionAlreadyUsedException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class MassivePromotionAlreadyUsedException extends IllegalArgumentException {

    public MassivePromotionAlreadyUsedException() {
        super("Esta promoción masiva ya fue utilizada por este número de teléfono");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/PromotionExpiredException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class PromotionExpiredException extends IllegalArgumentException {

    public PromotionExpiredException() {
        super("El código ingresado ha expirado");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/ReservationAlreadyCompletedException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class ReservationAlreadyCompletedException extends DomainValidationException {

    public ReservationAlreadyCompletedException() {
        super("La reserva ya fue completada y no admite cancelaciones, reintegros ni modificaciones.");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/SameDayBookingClosedException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class SameDayBookingClosedException extends DomainValidationException {

    public static final String MESSAGE = "Lo sentimos, las reservas para el día de hoy ya se "
            + "encuentran cerradas por motivos logísticos. Te invitamos a seleccionar una fecha "
            + "a partir de mañana. Por favor, indicá la fecha de tu viaje "
            + "(por ejemplo: 12/08/2026):";

    public SameDayBookingClosedException() {
        super(MESSAGE);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/SeatCapacityExceededException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class SeatCapacityExceededException extends DomainValidationException {

    public SeatCapacityExceededException(String message) {
        super(message);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/exception/SpecialTripNotFoundException.java`

```java
package com.lunaris.ansenuza.domain.exception;

public class SpecialTripNotFoundException extends RuntimeException {
    public SpecialTripNotFoundException(Long id) {
        super("No existe el viaje especial con id " + id + ".");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ConversationState.java`

```java
package com.lunaris.ansenuza.domain.model;



public enum ConversationState {

  MAIN_MENU,

  ASK_LOCALITY,

  ASK_ADDRESS,

  ASK_DESTINATION,

  ASK_DATE,

  ASK_CUIL,

  CONFIRM_RESERVATION
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ConversationStep.java`

```java
package com.lunaris.ansenuza.domain.model;

public enum ConversationStep {

    MENU,

    WAITING_FOR_INQUIRY_MESSAGE,

    ASK_PICKUP_LOCALITY,

    ASK_DESTINATION,

    ASK_TRAVEL_DATE,

    ASK_ROUND_TRIP,

    ASK_RETURN_DATE,

    PAYMENT_PENDING
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/InquiryStatus.java`

```java
package com.lunaris.ansenuza.domain.model;

public enum InquiryStatus {
    PENDING, IN_PROGRESS, RESOLVED, ARCHIVED
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ManualReservationCreated.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.util.UUID;

public record ManualReservationCreated(UUID reservationId) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/OperatorNotification.java`

```java
package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Snapshot inmutable: no transporta entidades JPA al hilo de envío. */
public record OperatorNotification(String message) {
    public static OperatorNotification inquiry(Inquiry inquiry) {
        return new OperatorNotification("""
                💬 *NUEVA CONSULTA / VIAJE ESPECIAL*
                👤 Pasajero: %s (%s)
                📝 Mensaje: "%s"
                👉 Responder en el panel: /admin/consultas""".formatted(
                text(inquiry.getPassengerName()), inquiry.getPhone(), inquiry.getMessage()));
    }

    public static OperatorNotification reservation(List<Reservation> reservations) {
        Reservation main = reservations.getFirst();
        Passenger passenger = main.getPassenger();
        BigDecimal total = reservations.stream().map(r -> amount(r.getAmount()).add(amount(r.getExtraAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OperatorNotification("""
                🚨 *NUEVA RESERVA REGISTRADA*
                👤 Pasajero: %s (%s)
                🚌 Viaje: %s ➡️ %s
                📅 Fecha: %s - %s
                💺 Asientos: %s
                💵 Monto: $%s""".formatted(
                passenger == null ? "Sin nombre" : text(passenger.getFirstName()) + " " + text(passenger.getLastName()),
                passenger == null ? "Sin teléfono" : text(passenger.getPhone()),
                text(main.getPickupLocality()), text(main.getDestination()),
                main.getTravelDate() == null ? "A confirmar" : main.getTravelDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                text(main.getDepartureSchedule()), main.getTotalSeats(), total.toPlainString()));
    }

    private static BigDecimal amount(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static String text(String value) { return value == null || value.isBlank() ? "Sin informar" : value; }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/PassengerMessageReceived.java`

```java
package com.lunaris.ansenuza.domain.model;

public record PassengerMessageReceived(String phone) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/ReservationSource.java`

```java
package com.lunaris.ansenuza.domain.model;

public enum ReservationSource {
    WEB,
    WHATSAPP,
    MANUAL
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/Role.java`

```java
package com.lunaris.ansenuza.domain.model;

/** Roles de acceso disponibles en la aplicación. */
public enum Role {
    ADMIN,
    OPERADOR,
    CHOFER,
    FACTURACION
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/SpecialTrip.java`

```java
package com.lunaris.ansenuza.domain.model;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SpecialTrip(
        Long id,
        String title,
        String description,
        String origin,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        Integer maxPassengers,
        String imageUrl,
        boolean active,
        LocalDateTime createdAt) {

    public SpecialTrip {
        title = required(title, "El título es obligatorio.", 255);
        description = optional(description);
        origin = optional(origin, 100, "El origen no puede superar los 100 caracteres.");
        destination = optional(destination, 100, "El destino no puede superar los 100 caracteres.");
        imageUrl = optional(imageUrl, 500, "La URL de imagen no puede superar los 500 caracteres.");
        if (startDate == null || endDate == null) {
            throw new DomainValidationException("Las fechas de inicio y fin son obligatorias.");
        }
        if (endDate.isBefore(startDate)) {
            throw new DomainValidationException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        if (price == null || price.signum() < 0) {
            throw new DomainValidationException("El precio debe ser mayor o igual a cero.");
        }
        if (maxPassengers == null || maxPassengers < 1) {
            throw new DomainValidationException("La capacidad máxima debe ser mayor a cero.");
        }
    }

    public static SpecialTrip create(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active, LocalDateTime createdAt) {
        return new SpecialTrip(null, title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active, createdAt);
    }

    public SpecialTrip update(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active) {
        return new SpecialTrip(id, title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active, createdAt);
    }

    private static String required(String value, String message, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(message);
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new DomainValidationException("El título no puede superar los " + maxLength + " caracteres.");
        }
        return normalized;
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String optional(String value, int maxLength, String message) {
        String normalized = optional(value);
        if (normalized != null && normalized.length() > maxLength) {
            throw new DomainValidationException(message);
        }
        return normalized;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/TripType.java`

```java
package com.lunaris.ansenuza.domain.model;

public enum TripType {
    ONE_WAY,
    ROUND_TRIP,
    OPEN_RETURN
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/AirportTripDetector.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.text.Normalizer;
import java.util.Locale;

/** Identifica solicitudes especiales hacia o desde el Aeropuerto de Córdoba. */
public final class AirportTripDetector {

    private static final String AIRPORT = "aeropuerto";
    private static final String PAJAS_BLANCAS = "pajas blancas";

    private AirportTripDetector() {
    }

    public static boolean isAirportTrip(String pickupLocality, String destination) {
        return isAirportLocation(pickupLocality) || isAirportLocation(destination);
    }

    private static boolean isAirportLocation(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String normalized = Normalizer.normalize(location, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized.contains(AIRPORT) || normalized.contains(PAJAS_BLANCAS);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/BookingInvoiceAmount.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import java.math.BigDecimal;
import java.util.List;

/** Contrato compartido por la vista de pendientes y la emisión de facturas. */
public final class BookingInvoiceAmount {
    private BookingInvoiceAmount() {}

    public static String groupCode(Reservation reservation) {
        if (reservation.getBookingGroupCode() != null && !reservation.getBookingGroupCode().isBlank()) {
            return reservation.getBookingGroupCode();
        }
        return reservation.getReservationCode() == null ? "UUID:" + reservation.getId()
                : reservation.getReservationCode().replaceFirst("-(IDA|VUELTA)$", "");
    }

    public static BigDecimal total(List<Reservation> legs) {
        var groupAmounts = legs.stream().filter(Reservation::isAmountIsGroupTotal)
                .map(r -> zero(r.getAmount()).stripTrailingZeros()).distinct().toList();
        if (groupAmounts.size() > 1 || (!groupAmounts.isEmpty()
                && legs.stream().anyMatch(r -> !r.isAmountIsGroupTotal()))) {
            throw new DomainValidationException("El grupo tiene importes incompatibles. Revisar el total acordado.");
        }
        BigDecimal base = groupAmounts.isEmpty()
                ? legs.stream().map(r -> zero(r.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add)
                : groupAmounts.getFirst();
        return base.add(legs.stream().map(r -> zero(r.getExtraAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/CuilCalculator.java`

```java
package com.lunaris.ansenuza.domain.model.service;

/**
 * 🧮 Utilidad para obtener el CUIL de una persona a partir de lo que haya cargado:
 * - Si ya es un CUIL/CUIT de 11 dígitos, lo devuelve formateado.
 * - Si es un DNI (7 u 8 dígitos), calcula el CUIL sugerido (prefijo 20 por defecto)
 *   con su dígito verificador. La operadora debe verificar el sexo (20/27) al facturar.
 */
public final class CuilCalculator {

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private CuilCalculator() {
    }

    /** Devuelve el CUIL formateado (XX-XXXXXXXX-X) o null si no se puede calcular. */
    public static String suggestCuil(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() == 11) {
            return format(digits);
        }
        if (digits.length() == 7 || digits.length() == 8) {
            String dni = String.format("%08d", Long.parseLong(digits));
            return format(buildCuil(dni, 20));
        }
        return digits; // No reconocido: devolvemos lo cargado tal cual
    }

    private static String buildCuil(String dni8, int prefix) {
        String base = String.format("%02d", prefix) + dni8; // 10 dígitos
        int verifier = verifier(base);
        if (verifier == 10) {
            // Caso especial: el tipo pasa a 23 y el verificador se recalcula
            base = "23" + dni8;
            verifier = verifier(base);
        }
        return base + verifier;
    }

    private static int verifier(String tenDigits) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (tenDigits.charAt(i) - '0') * WEIGHTS[i];
        }
        int mod = sum % 11;
        int verifier = 11 - mod;
        if (verifier == 11) {
            return 0;
        }
        return verifier; // puede ser 10 (se maneja arriba)
    }

    private static String format(String eleven) {
        if (eleven.length() != 11) {
            return eleven;
        }
        return eleven.substring(0, 2) + "-" + eleven.substring(2, 10) + "-" + eleven.substring(10);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/DriverRouteService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DriverRouteService {

    private static final int VEHICLE_CAPACITY = 4;
    private static final String CAPACITY_MESSAGE =
            "No se pueden asignar más de 4 pasajeros a un solo vehículo/chofer.";

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final TripRouteCalculatorService routeCalculator = new TripRouteCalculatorService();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Reservation> replaceRoute(
            Driver driver, LocalDate travelDate, List<UUID> orderedReservationIds) {
        return replaceRoute(driver, travelDate, orderedReservationIds, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Reservation> replaceRoute(
            Driver driver, LocalDate travelDate, List<UUID> orderedReservationIds,
            Comparator<Reservation> finalOrderComparator) {
        if (driver == null || driver.getId() == null || travelDate == null
                || orderedReservationIds == null) {
            throw new IllegalArgumentException("Chofer, fecha y orden de reservas son obligatorios.");
        }
        Set<UUID> uniqueIds = new HashSet<>(orderedReservationIds);
        if (uniqueIds.size() != orderedReservationIds.size()) {
            throw new IllegalArgumentException("La ruta no puede contener reservas duplicadas.");
        }

        List<Reservation> selected = reservationRepository.findAllByIdInForUpdate(orderedReservationIds);
        rejectDispatched(selected);
        if (selected.size() != orderedReservationIds.size()
                || selected.stream().anyMatch(reservation ->
                        !travelDate.equals(reservation.getTravelDate())
                                || !reservation.isScheduledConfirmedTrip())
                || !sameManifest(selected)) {
            throw new IllegalArgumentException(
                    "Solo se pueden asignar reservas confirmadas del mismo sentido, fecha y turno.");
        }
        assertVehicleCapacity(selected);

        Set<UUID> affectedDriverIds = new HashSet<>();
        affectedDriverIds.add(driver.getId());
        selected.stream()
                .filter(reservation -> reservation.getDriver() != null)
                .map(reservation -> reservation.getDriver().getId())
                .forEach(affectedDriverIds::add);
        List<Driver> lockedDrivers = driverRepository.findAllByIdForUpdate(affectedDriverIds);
        if (lockedDrivers.size() != affectedDriverIds.size()) {
            throw new IllegalArgumentException("No se pudo bloquear la totalidad de los choferes afectados.");
        }
        Driver targetDriver = lockedDrivers.stream()
                .filter(locked -> locked.getId().equals(driver.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado: " + driver.getId()));

        Set<Reservation> changed = new LinkedHashSet<>();
        Reservation manifestReference = selected.getFirst();
        List<Reservation> targetRoute = scopedRoute(targetDriver.getId(), travelDate, manifestReference);
        targetRoute.stream()
                .filter(reservation -> !uniqueIds.contains(reservation.getId()))
                .forEach(reservation -> {
                    reservation.setDriver(null);
                    reservation.setRouteSequence(null);
                    changed.add(reservation);
                });

        var selectedById = selected.stream()
                .collect(java.util.stream.Collectors.toMap(Reservation::getId, reservation -> reservation));
        Comparator<Reservation> effectiveComparator = finalOrderComparator != null
                ? finalOrderComparator : routeComparator(selected.getFirst());
        List<Reservation> geographicallyOrdered = orderedReservationIds.stream()
                .map(selectedById::get)
                .sorted(effectiveComparator)
                .toList();
        List<Reservation> ordered = java.util.stream.IntStream.range(0, geographicallyOrdered.size())
                .mapToObj(index -> {
                    Reservation reservation = geographicallyOrdered.get(index);
                    reservation.setDriver(targetDriver);
                    reservation.setRouteSequence(index + 1);
                    changed.add(reservation);
                    return reservation;
                })
                .toList();

        lockedDrivers.stream()
                .filter(locked -> !locked.getId().equals(targetDriver.getId()))
                .forEach(previousDriver -> {
                    List<Reservation> remaining = scopedRoute(previousDriver.getId(), travelDate, manifestReference).stream()
                                    .filter(reservation -> !uniqueIds.contains(reservation.getId()))
                                    .toList();
                    java.util.stream.IntStream.range(0, remaining.size()).forEach(index -> {
                        remaining.get(index).setRouteSequence(index + 1);
                        changed.add(remaining.get(index));
                    });
                });

        reservationRepository.saveAllAndFlush(changed);
        return ordered;
    }

    /** Marca de despacho idempotente y protegida por bloqueo de fila. */
    @Transactional
    public void markRouteSent(List<UUID> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) return;
        List<Reservation> locked = reservationRepository.findAllByIdForUpdate(reservationIds);
        if (locked.size() != new HashSet<>(reservationIds).size()) {
            throw new DomainValidationException("No se encontraron todas las reservas del despacho.");
        }
        locked.stream()
                .filter(reservation -> reservation.getTravelStatus() != Reservation.TravelStatus.ROUTE_SENT)
                .forEach(reservation -> reservation.setTravelStatus(Reservation.TravelStatus.ROUTE_SENT));
        reservationRepository.saveAllAndFlush(locked);
    }

    private boolean sameManifest(List<Reservation> reservations) {
        if (reservations.isEmpty()) return true;
        Reservation first = reservations.getFirst();
        return reservations.stream().allMatch(candidate -> routeCalculator.sameManifest(first, candidate));
    }

    private void rejectDispatched(List<Reservation> reservations) {
        if (reservations.stream().anyMatch(r -> r.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT)) {
            throw new DomainValidationException(
                    "No se puede reasignar una reserva porque la ruta ya fue enviada al chofer.");
        }
    }

    /** Compatibilidad con el repositorio legado: el filtrado de alcance se hace antes de mutar. */
    private List<Reservation> scopedRoute(UUID driverId, LocalDate date, Reservation reference) {
        return reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(driverId, date)
                .stream().filter(candidate -> routeCalculator.sameManifest(reference, candidate)).toList();
    }

    private void assertVehicleCapacity(List<Reservation> reservations) {
        int occupiedSeats = reservations.stream().mapToInt(Reservation::getTotalSeats).sum();
        if (occupiedSeats > VEHICLE_CAPACITY) {
            throw new IllegalArgumentException(CAPACITY_MESSAGE);
        }
    }

    private java.util.Comparator<Reservation> routeComparator(Reservation reference) {
        boolean returnDirection = TripRouteCalculatorService.isCordoba(reference.getPickupLocality());
        java.util.Comparator<Reservation> localityOrder = java.util.Comparator.comparingInt(reservation -> {
            String locality = returnDirection
                    ? reservation.getDestination() : reservation.getPickupLocality();
            int index = routeCalculator.corridorIndex(locality);
            if (index < 0) return Integer.MAX_VALUE;
            return returnDirection ? -index : index;
        });
        return localityOrder
                .thenComparing(reservation -> reservation.getPickupAddress() == null
                        ? "" : reservation.getPickupAddress(), String.CASE_INSENSITIVE_ORDER);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/FleetCapacityService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FleetCapacityService {

    public static final int OWN_VEHICLES = 2;
    public static final int SEATS_PER_VEHICLE = 4;
    public static final int OWN_FLEET_CAPACITY = OWN_VEHICLES * SEATS_PER_VEHICLE;

    private final BigDecimal externalDriverCost;

    public FleetCapacityService(
            @Value("${lunaris.fleet.external-driver-cost:0}")
            BigDecimal externalDriverCost) {
        this.externalDriverCost = externalDriverCost == null
                ? BigDecimal.ZERO
                : externalDriverCost.max(BigDecimal.ZERO);
    }

    public FleetSummary calculate(int totalPassengers) {
        int passengers = Math.max(totalPassengers, 0);
        int internalPassengers = Math.min(passengers, OWN_FLEET_CAPACITY);
        int externalPassengers = Math.max(passengers - OWN_FLEET_CAPACITY, 0);
        int externalVehicles = externalPassengers == 0
                ? 0
                : (int) Math.ceil((double) externalPassengers / SEATS_PER_VEHICLE);
        return new FleetSummary(
                passengers,
                internalPassengers,
                externalPassengers,
                externalVehicles,
                externalDriverCost.multiply(BigDecimal.valueOf(externalVehicles)));
    }

    public record FleetSummary(
            int totalPassengers,
            int internalPassengers,
            int externalPassengers,
            int externalVehicles,
            BigDecimal externalDriverExpense) {

        public boolean requiresExternalReinforcement() {
            return externalPassengers > 0;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/OperationControlService.java`

```java
package com.lunaris.ansenuza.domain.model.service;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;

@Service
public class OperationControlService {

    // 🕒 Estado Global del Interruptor de Jornada (true = abierto/acción humana, false = piloto automático 24/7)
    private final AtomicBoolean humanActionEnabled = new AtomicBoolean(true);

    // ⚖️ Lista estática de operadores actuales para el Load Balancer
    private final List<String> availableOperators = Arrays.asList("martin", "operador2");

    // 📊 Contador en memoria de chats asignados por operador para el cálculo rápido de carga mínima
    private final ConcurrentHashMap<String, Integer> operatorLoadMap = new ConcurrentHashMap<>();

    public OperationControlService() {
        // Inicializamos los operadores con carga cero al levantar el sistema
        for (String op : availableOperators) {
            operatorLoadMap.put(op, 0);
        }
    }

    // 🔄 MÉTODOS PARA EL INTERRUPTOR DE JORNADA LABORAL
    public boolean isHumanActionEnabled() {
        return humanActionEnabled.get();
    }

    public void setHumanActionEnabled(boolean enabled) {
        this.humanActionEnabled.set(enabled);
    }

    // ⚖️ LOGICA DEL LOAD BALANCER: Devuelve el operador con menos carga actual
    public String getOperatorWithLeastLoad() {
        String leastLoadedOperator = "martin"; // fallback por defecto
        int minLoad = Integer.MAX_VALUE;

        for (String operator : availableOperators) {
            int currentLoad = operatorLoadMap.getOrDefault(operator, 0);
            if (currentLoad < minLoad) {
                minLoad = currentLoad;
                leastLoadedOperator = operator;
            }
        }
        
        // Simulamos un incremento de carga al asignarlo
        operatorLoadMap.put(leastLoadedOperator, minLoad + 1);
        return leastLoadedOperator;
    }

    // Libera carga cuando un chat se cierra o se archiva
    public void releaseOperatorLoad(String operator) {
        if (operator != null && operatorLoadMap.containsKey(operator)) {
            operatorLoadMap.computeIfPresent(operator, (k, v) -> Math.max(0, v - 1));
        }
    }

    // ⏱️ VALIDACIÓN DE HORA DE CORTE (Deadline de las 19:00 Hs)
    public boolean isPastCutoffTime() {
        LocalTime ahora = com.lunaris.ansenuza.shared.ArgentinaTime.currentTime();
        LocalTime horaCorte = LocalTime.of(19, 0); // 19:00 Hs
        return ahora.isAfter(horaCorte);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/PricingAndScheduleService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.model.TripType;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PricingAndScheduleService {

    private static final List<String> DEPARTURE_BLOCKS = List.of("03:00 AM", "08:00 AM");
    private static final String ONE_WAY_EXTRA_AMOUNT = "ONE_WAY_EXTRA_AMOUNT";
    private static final String PRICE_PER_KM = "PRICE_PER_KM";
    private static final String DEFAULT_FARE = "DEFAULT_FARE";
    private static final java.math.BigDecimal DEFAULT_ONE_WAY_EXTRA = new java.math.BigDecimal("8000");
    private static final java.math.BigDecimal DEFAULT_PRICE_PER_KM = new java.math.BigDecimal("1000");
    private static final java.math.BigDecimal FALLBACK_FARE = new java.math.BigDecimal("100000");

    private final FareRepository fareRepository;
    private final LocalityRepository localityRepository;
    private final BusinessParameterRepository businessParameterRepository;
    private final ReservationRepository reservationRepository; 

    private final java.time.Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public PricingAndScheduleService(FareRepository fares, LocalityRepository localities,
            BusinessParameterRepository parameters, ReservationRepository reservations) {
        this(fares, localities, parameters, reservations,
                java.time.Clock.system(com.lunaris.ansenuza.shared.ArgentinaTime.ZONE_ID));
    }

    public PricingAndScheduleService(FareRepository fares, LocalityRepository localities,
            BusinessParameterRepository parameters, ReservationRepository reservations, java.time.Clock clock) {
        this.fareRepository = fares;
        this.localityRepository = localities;
        this.businessParameterRepository = parameters;
        this.reservationRepository = reservations;
        this.clock = clock;
    }

    @Value("${lunaris.trips.capacity:19}")
    private int tripCapacity = 19;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, Integer> MINUTES_VUELTA_FROM_HUB = new HashMap<>();
    private static final Map<String, LocalTime> SECOND_MORNING_SCHEDULE = Map.ofEntries(
            Map.entry("san guillermo", LocalTime.of(7, 20)),
            Map.entry("suardi", LocalTime.of(7, 40)),
            Map.entry("morteros", LocalTime.of(8, 0)),
            Map.entry("brinkmann", LocalTime.of(8, 20)),
            Map.entry("portena", LocalTime.of(8, 40)),
            Map.entry("freyre", LocalTime.of(9, 0)),
            Map.entry("la paquita", LocalTime.of(8, 30)),
            Map.entry("altos de chipion", LocalTime.of(8, 40)),
            Map.entry("balnearia", LocalTime.of(9, 0)),
            Map.entry("miramar", LocalTime.of(9, 10)));

    static {
        MINUTES_VUELTA_FROM_HUB.put("la puerta", 0);
        MINUTES_VUELTA_FROM_HUB.put("marull", 20);
        MINUTES_VUELTA_FROM_HUB.put("balnearia", 35);
        MINUTES_VUELTA_FROM_HUB.put("miramar", 50);
        MINUTES_VUELTA_FROM_HUB.put("freyre", 65);
        MINUTES_VUELTA_FROM_HUB.put("porteña", 85);
        MINUTES_VUELTA_FROM_HUB.put("brinkmann", 100);
        MINUTES_VUELTA_FROM_HUB.put("morteros", 115);
        MINUTES_VUELTA_FROM_HUB.put("suardi", 140);
        MINUTES_VUELTA_FROM_HUB.put("san guillermo", 160);
    }

    /**
     * ⏱️ Calcula dinámicamente el horario basándose en la ocupación física o el tipo de tramo.
     */
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr, boolean isReturn, LocalDate travelDate) {
        if (localityName == null) return baseTimeStr + " hs";
        
        LocalTime baseTime = LocalTime.parse(baseTimeStr.trim(), TIME_FORMATTER);

        // Controlamos el bloque de regresos o tramo de las 08:00 AM desde Córdoba
        if (isReturn || "08:00".equals(baseTimeStr.trim())) {
            String key = normalizeLocality(localityName);
            LocalTime scheduledTime = "08:00".equals(baseTimeStr.trim())
                    ? SECOND_MORNING_SCHEDULE.getOrDefault(key, baseTime)
                    : baseTime.plusMinutes(MINUTES_VUELTA_FROM_HUB.getOrDefault(key, 0));

            int pasajerosRegreso = reservationRepository.countPassengersByReturnDateAndNotesContaining(
                    travelDate != null ? travelDate : com.lunaris.ansenuza.shared.ArgentinaTime.today(), "08:00 AM");
            
            if (pasajerosRegreso <= 8) {
                return scheduledTime.format(TIME_FORMATTER) + " hs";
            } else {
                int delayPorDobleViaje = 45; 
                log.warn("Capacidad excedida para el retorno. Retorno activado con demora.");
                LocalTime horarioConRetorno = scheduledTime.plusMinutes(delayPorDobleViaje);
                return horarioConRetorno.format(TIME_FORMATTER) + " hs (Demorado por Alta Demanda)";
            }
        } else {
            // Ida tradicional de la madrugada (03:00 AM)
            return localityRepository.findByName(localityName)
                    .map(locality -> {
                        LocalTime startTime = LocalTime.of(3, 0); 
                        int minutesFromOrigin = locality.getMinutesFromOrigin();
                        return startTime.plusMinutes(minutesFromOrigin).format(TIME_FORMATTER) + " hs";
                    })
                    .orElse(baseTimeStr + " hs");
        }
    }

    private static String normalizeLocality(String localityName) {
        return java.text.Normalizer.normalize(localityName.trim().toLowerCase(),
                        java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /**
     * 💰 PRIORIDAD URGENTE: Calcula el precio aplicando la regla (Tarifa / 2) + 8000 si es SOLO IDA
     */
    public java.math.BigDecimal calculateTripPrice(String localityName, Boolean isRoundTrip, int passengerCount) {
        if (localityName == null || localityName.isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }

        // 1. Buscamos la tarifa completa paramétrica de la base de datos
        java.math.BigDecimal baseFare = resolveBaseFare(localityName.trim());

        java.math.BigDecimal finalPricePerPassenger;

        // 2. Evaluamos si es "Solo Ida" (isRoundTrip == false)
        if (Boolean.FALSE.equals(isRoundTrip)) {
            java.math.BigDecimal extraOneWayFee = businessParameterRepository
                    .findByParameterKey(ONE_WAY_EXTRA_AMOUNT)
                    .map(parameter -> new java.math.BigDecimal(parameter.getParameterValue()))
                    .orElse(DEFAULT_ONE_WAY_EXTRA);
            finalPricePerPassenger = baseFare.divide(new java.math.BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP)
                                             .add(extraOneWayFee);
            log.info("[Tarifa Solo Ida Aplicada] Pueblo: {} | Base original: {} | Con regla aplicada: {}", 
                     localityName, baseFare, finalPricePerPassenger);
        } else {
            // Si es Ida y Vuelta completo, mantiene la tarifa base paramétrica normal
            finalPricePerPassenger = baseFare;
        }

        // 3. Multiplicamos por la cantidad total de asientos requeridos
        return finalPricePerPassenger.multiply(java.math.BigDecimal.valueOf(passengerCount));
    }

    /** Diferencia a reliquidar cuando una tarifa ida/vuelta termina siendo solo ida. */
    public java.math.BigDecimal calculateOneWaySurcharge(int passengerCount) {
        if (passengerCount <= 0) {
            return java.math.BigDecimal.ZERO;
        }
        return positiveBusinessParameter(ONE_WAY_EXTRA_AMOUNT, DEFAULT_ONE_WAY_EXTRA)
                .multiply(java.math.BigDecimal.valueOf(Math.min(passengerCount, 4)));
    }

    private java.math.BigDecimal resolveBaseFare(String localityName) {
        return fareRepository.findByLocalityNameIgnoreCase(localityName)
                .map(fare -> fare.getAmount())
                .filter(amount -> amount != null && amount.signum() > 0)
                .orElseGet(() -> calculateFallbackFare(localityName));
    }

    private java.math.BigDecimal calculateFallbackFare(String localityName) {
        java.math.BigDecimal pricePerKm = positiveBusinessParameter(
                PRICE_PER_KM, DEFAULT_PRICE_PER_KM);
        java.math.BigDecimal calculatedFare = localityRepository
                .findFirstByNameIgnoreCase(localityName)
                .map(com.lunaris.ansenuza.domain.model.Locality::getKmsToCordoba)
                .filter(kms -> kms > 0)
                .map(kms -> pricePerKm.multiply(java.math.BigDecimal.valueOf(kms)))
                .orElseGet(() -> positiveBusinessParameter(DEFAULT_FARE, FALLBACK_FARE));

        log.warn("No se encontró tarifa explícita para {}. Se utiliza tarifa de respaldo: {}.",
                localityName, calculatedFare);
        return calculatedFare;
    }

    private java.math.BigDecimal positiveBusinessParameter(
            String key, java.math.BigDecimal defaultValue) {
        return businessParameterRepository.findByParameterKey(key)
                .map(parameter -> parameter.getParameterValue())
                .flatMap(value -> {
                    try {
                        java.math.BigDecimal parsed = new java.math.BigDecimal(value);
                        return parsed.signum() > 0
                                ? java.util.Optional.of(parsed)
                                : java.util.Optional.empty();
                    } catch (NumberFormatException exception) {
                        log.warn("Parámetro de negocio {} inválido: {}.", key, value);
                        return java.util.Optional.empty();
                    }
                })
                .orElse(defaultValue);
    }

    public java.math.BigDecimal calculateTripPrice(String localityName, boolean isRoundTrip, int passengerCount) {
        return calculateTripPrice(localityName, Boolean.valueOf(isRoundTrip), passengerCount);
    }

    /**
     * Calcula el importe total de una reserva tomando la ruta completa.
     *
     * <p>Centraliza la regla de negocio usada por el bot, el formulario web y la API:
     * la localidad "de zona" es la que no corresponde a Córdoba.
     */
    public java.math.BigDecimal calculateReservationAmount(String pickupLocality, String destination,
            Boolean isRoundTrip, int passengerCount) {
        String zoneLocality = resolveZoneLocality(pickupLocality, destination);
        return calculateTripPrice(zoneLocality, isRoundTrip, passengerCount);
    }

    public java.math.BigDecimal calculateReservationAmount(String pickupLocality, String destination,
            TripType tripType, int passengerCount) {
        boolean fullRoundTripFare = tripType == TripType.ROUND_TRIP || tripType == TripType.OPEN_RETURN;
        return calculateReservationAmount(
                pickupLocality, destination, fullRoundTripFare, passengerCount);
    }

    private String resolveZoneLocality(String pickupLocality, String destination) {
        if (pickupLocality == null || pickupLocality.isBlank()) {
            return destination;
        }
        if (destination == null || destination.isBlank()) {
            return pickupLocality;
        }

        return normalizeLocality(pickupLocality).contains("cordoba") ? destination : pickupLocality;
    }

    public long countReservedSeats(LocalDate date, String schedule) {
        return reservationRepository.countReservedSeats(date, schedule);
    }

    public List<String> departureSchedules() {
        return DEPARTURE_BLOCKS;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int availableReturnSeats(LocalDate date, String schedule) {
        return ReturnCapacityPolicy.availableSeats(reservationRepository, date, schedule, java.time.LocalDateTime.now(clock));
    }

    public boolean hasReturnCapacity(LocalDate date, String schedule, int requestedSeats) {
        return requestedSeats > 0 && availableReturnSeats(date, schedule) >= requestedSeats;
    }

    public int availableSeats(LocalDate date, String schedule) {
        long remainingSeats = (long) tripCapacity - countReservedSeats(date, schedule);
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, remainingSeats));
    }

    /**
     * Bloques de salida compartidos por el bot y la API pública.
     * La disponibilidad corresponde únicamente al tramo de ida y a su fecha de viaje.
     */
    public List<String> availableDepartureSchedules(
            String pickupLocality, String destination, LocalDate travelDate) {
        return availableDepartureSchedules(pickupLocality, destination, travelDate, 1);
    }

    public boolean isWithinPlanningWindow(LocalDate date, String schedule) {
        if (date == null) return true;
        var now = java.time.LocalDateTime.now(clock);
        var departure = date.atTime(LocalTime.parse(ReturnCapacityPolicy.normalizeSchedule(schedule)));
        return !departure.isBefore(now.plusMinutes(60));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<String> availableDepartureSchedules(
            String pickupLocality, String destination, LocalDate travelDate, int requestedSeats) {
        if (pickupLocality == null || pickupLocality.isBlank() || travelDate == null || requestedSeats < 1) {
            return List.of();
        }
        return DEPARTURE_BLOCKS.stream()
                .filter(schedule -> isWithinPlanningWindow(travelDate, schedule))
                .filter(schedule -> availableSeats(travelDate, schedule) >= requestedSeats)
                .toList();
    }

    /**
     * ⏱️ REESCRITO COMPATIBILIDAD Y URGENCIA: Corrige el error que clavaba a las 03:00 AM el turno de las 08:00
     */
    public String calculateEstimatedPickupTime(String localityName, String baseTimeStr) {
        if (localityName != null && "cordoba".equals(normalizeLocality(localityName))) {
            return baseTimeStr + " hs";
        }
        if ("08:00".equals(baseTimeStr.trim())) {
            // Redirige dinámicamente usando la fecha de hoy como fallback seguro para calcular el desvío de las 08:00 AM
            return calculateEstimatedPickupTime(
                    localityName, baseTimeStr, false,
                    com.lunaris.ansenuza.shared.ArgentinaTime.today());
        }
        
        // Mantiene el fallback de las 03:00 AM para el resto
        return localityRepository.findByName(localityName)
                .map(locality -> LocalTime.of(3, 0).plusMinutes(locality.getMinutesFromOrigin()).format(TIME_FORMATTER) + " hs")
                .orElse(baseTimeStr + " hs");
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/PromotionService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.model.Promotion;
import com.lunaris.ansenuza.domain.model.PromotionUsage;
import com.lunaris.ansenuza.domain.exception.MassivePromotionAlreadyUsedException;
import com.lunaris.ansenuza.domain.exception.PromotionExpiredException;
import com.lunaris.ansenuza.domain.repository.PromotionRepository;
import com.lunaris.ansenuza.domain.repository.PromotionUsageRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionUsageRepository promotionUsageRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Promotion create(int discountPercentage) {
        return createPromotion(discountPercentage, false, null);
    }

    @Transactional
    public Promotion createMassive(int discountPercentage, Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("La duración de la promoción debe ser mayor a cero.");
        }
        return createPromotion(
                discountPercentage, true,
                com.lunaris.ansenuza.shared.ArgentinaTime.now().plus(duration));
    }

    private Promotion createPromotion(int discountPercentage, boolean massive, LocalDateTime expiresAt) {
        if (discountPercentage < 10 || discountPercentage > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 10% y 100%.");
        }
        String code;
        do {
            code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10_000));
        } while (promotionRepository.existsByCode(code));

        Promotion promotion = new Promotion();
        promotion.setCode(code);
        promotion.setDiscountPercentage(discountPercentage);
        promotion.setUsed(false);
        promotion.setMassive(massive);
        promotion.setExpiresAt(expiresAt);
        return promotionRepository.saveAndFlush(promotion);
    }

    @Transactional(readOnly = true)
    public Promotion requireAvailable(String code, String phoneNumber) {
        Promotion promotion = promotionRepository.findFirstByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("El código promocional no existe."));
        validateAvailable(promotion, phoneNumber, true);
        return promotion;
    }

    public BigDecimal calculateDiscount(BigDecimal total, int discountPercentage) {
        if (total == null || total.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0% y 100%.");
        }
        return total.multiply(BigDecimal.valueOf(discountPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void consume(String code, String phoneNumber) {
        if (code == null || code.isBlank()) {
            return;
        }
        Promotion promotion = promotionRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new IllegalStateException("La promoción no existe."));
        validateAvailable(promotion, phoneNumber, false);
        String normalizedPhone = normalizePhone(phoneNumber);
        if (!promotion.isMassive()) {
            promotion.setUsed(true);
            promotionRepository.saveAndFlush(promotion);
        }
        promotionUsageRepository.saveAndFlush(new PromotionUsage(promotion, normalizedPhone));
    }

    @Transactional
    public void consumeIfAvailable(String code, String phoneNumber) {
        if (code == null || code.isBlank()) {
            return;
        }
        promotionRepository.findByCodeForUpdate(code).ifPresent(promotion -> {
            String normalizedPhone = normalizePhone(phoneNumber);
            long previousUsages = promotionUsageRepository.countByPromotionAndNormalizedPhone(
                    promotion.getId(), normalizedPhone);
            if (promotion.isMassive()) {
                if (previousUsages > 0) {
                    return;
                }
                validateNotExpired(promotion);
                promotionUsageRepository.saveAndFlush(new PromotionUsage(promotion, normalizedPhone));
            } else if (!promotion.isUsed()) {
                validateNotExpired(promotion);
                promotion.setUsed(true);
                promotionRepository.saveAndFlush(promotion);
                if (previousUsages == 0) {
                    promotionUsageRepository.saveAndFlush(new PromotionUsage(promotion, normalizedPhone));
                }
            } else if (previousUsages == 0) {
                throw new IllegalArgumentException("El código promocional ya fue utilizado.");
            }
        });
    }

    private void validateAvailable(
            Promotion promotion, String phoneNumber, boolean checkActiveReservations) {
        String normalizedPhone = normalizePhone(phoneNumber);
        if (normalizedPhone.isBlank()) {
            throw new IllegalArgumentException("No se pudo identificar el teléfono para aplicar la promoción.");
        }
        validateNotExpired(promotion);
        if (!promotion.isMassive()) {
            if (promotion.isUsed()) {
                throw new IllegalArgumentException("El código promocional ya fue utilizado.");
            }
            return;
        }
        boolean consumed = promotionUsageRepository.countByPromotionAndNormalizedPhone(
                promotion.getId(), normalizedPhone) > 0;
        boolean reserved = checkActiveReservations
                && reservationRepository.existsActivePromotionUsageByPhone(
                        normalizedPhone, promotion.getId(), promotion.getCode());
        if (consumed || reserved) {
            throw new MassivePromotionAlreadyUsedException();
        }
    }

    private void validateNotExpired(Promotion promotion) {
        if (promotion.getExpiresAt() != null
                && !com.lunaris.ansenuza.shared.ArgentinaTime.now()
                        .isBefore(promotion.getExpiresAt())) {
            throw new PromotionExpiredException();
        }
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.startsWith("549")) {
            return "54" + digits.substring(3);
        }
        if (!digits.startsWith("54") && digits.length() == 10) {
            return "54" + digits;
        }
        return digits;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/ReservationCancellationService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCancellationService {

    public static final String RETURN_YES_ID = "return_yes_ID";
    public static final String RETURN_LATER_ID = "return_later_ID";
    public static final String RETURN_POSTPONE_ID = "return_postpone";
    public static final String RETURN_NO_ID = "return_no_ID";
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @Transactional
    public void processReturnDecision(String passengerPhone, String decisionId) {
        if (RETURN_YES_ID.equals(decisionId)) {
            return;
        }

        Reservation returnReservation = findTodayReturnReservation(passengerPhone);
        if ("COMPLETED".equalsIgnoreCase(returnReservation.getStatus())
                || returnReservation.getTravelStatus() == TravelStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }

        if (isPostponeDecision(decisionId)) {
            returnReservation.setTravelStatus(TravelStatus.OPEN_RETURN);
            returnReservation.setReturnAuditSentAt(
                    com.lunaris.ansenuza.shared.ArgentinaTime.now());
            returnReservation.setNotes(appendNote(returnReservation.getNotes(),
                    "Vuelta marcada como abierta por decisión del pasajero."));
            reservationRepository.saveAndFlush(returnReservation);
            return;
        }

        if (RETURN_NO_ID.equals(decisionId)) {
            reservationService.cancelReservation(
                    returnReservation.getId(), "PASSENGER_RETURN_DECISION");
            return;
        }

        throw new IllegalArgumentException("Decisión de vuelta no soportada: " + decisionId);
    }

    public boolean isReturnDecision(String decisionId) {
        return RETURN_YES_ID.equals(decisionId)
                || RETURN_LATER_ID.equals(decisionId)
                || RETURN_POSTPONE_ID.equals(decisionId)
                || RETURN_NO_ID.equals(decisionId);
    }

    private Reservation findTodayReturnReservation(String passengerPhone) {
        String normalizedPhone = passengerPhone != null ? passengerPhone.trim() : "";
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();

        List<Reservation> scheduledReturns =
                reservationRepository.findActiveReturnReservationsByPassengerPhoneAndDate(normalizedPhone, today);
        if (!scheduledReturns.isEmpty()) {
            return scheduledReturns.get(0);
        }

        List<Reservation> outboundReservations =
                reservationRepository.findRealizedOutboundReservationsByPassengerPhoneAndReturnDate(
                        normalizedPhone, today, TravelStatus.REALIZED);
        if (!outboundReservations.isEmpty()) {
            return outboundReservations.get(0);
        }

        List<Reservation> openReturns =
                reservationRepository.findOpenReturnReservationsByPassengerPhone(normalizedPhone);
        if (!openReturns.isEmpty()) {
            return openReturns.get(0);
        }

        throw new IllegalArgumentException("No hay una vuelta activa para confirmar hoy.");
    }

    private boolean isPostponeDecision(String decisionId) {
        return RETURN_POSTPONE_ID.equals(decisionId) || RETURN_LATER_ID.equals(decisionId);
    }

    private String appendNote(String currentNotes, String newNote) {
        if (currentNotes == null || currentNotes.isBlank()) {
            return newNote;
        }
        return currentNotes + " | " + newNote;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/ReservationService.java`

```java
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
    private final PricingAndScheduleService pricingAndScheduleService;

    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository,
            PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase) {
        this(reservationRepository, reservationEventRepository, passengerRepository,
                onboardPassengerUseCase, null, null);
    }

    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository,
            PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase,
            CapacityLockRepository capacityLockRepository) {
        this(reservationRepository, reservationEventRepository, passengerRepository,
                onboardPassengerUseCase, capacityLockRepository, null);
    }

    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository,
            PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase,
            CapacityLockRepository capacityLockRepository,
            PricingAndScheduleService pricingAndScheduleService) {
        this(reservationRepository, reservationEventRepository, passengerRepository,
                onboardPassengerUseCase, capacityLockRepository, pricingAndScheduleService, event -> {});
    }

    private final org.springframework.context.ApplicationEventPublisher notificationEvents;

    @org.springframework.beans.factory.annotation.Autowired
    public ReservationService(ReservationRepository reservationRepository,
            ReservationEventRepository reservationEventRepository, PassengerRepository passengerRepository,
            OnboardPassengerUseCase onboardPassengerUseCase, CapacityLockRepository capacityLockRepository,
            PricingAndScheduleService pricingAndScheduleService,
            org.springframework.context.ApplicationEventPublisher notificationEvents) {
        this.notificationEvents = notificationEvents;
        this.reservationRepository = reservationRepository;
        this.reservationEventRepository = reservationEventRepository;
        this.passengerRepository = passengerRepository;
        this.onboardPassengerUseCase = onboardPassengerUseCase;
        this.capacityLockRepository = capacityLockRepository;
        this.pricingAndScheduleService = pricingAndScheduleService;
    }

    @Transactional
    public List<Reservation> saveManualReservationFlow(Reservation reservation, String returnSchedule) {
        if (reservation.getId() != null) {
            throw new DomainValidationException("La creación manual requiere una reserva nueva.");
        }
        validateManualReservation(reservation);
        resetManualState(reservation);
        reservation.setSource(com.lunaris.ansenuza.domain.model.ReservationSource.MANUAL);
        List<Reservation> saved = saveReservationFlow(reservation, returnSchedule, false);
        for (Reservation leg : saved) {
            if (leg.getTravelStatus() != Reservation.TravelStatus.OPEN_RETURN) {
                leg.setTravelStatus(Reservation.TravelStatus.SCHEDULED);
            }
        }
        saved.getFirst().setManualNotificationPending(true);
        reservationRepository.saveAllAndFlush(saved);
        notificationEvents.publishEvent(new com.lunaris.ansenuza.domain.model.ManualReservationCreated(saved.getFirst().getId()));
        return saved;
    }

    public void validateManualReservation(Reservation reservation) {
        if (reservation.getId() != null) throw new DomainValidationException("La creación manual requiere una reserva nueva.");
        if (reservation.getTravelDate() == null || reservation.getPickupLocality() == null
                || reservation.getPickupLocality().isBlank() || reservation.getDestination() == null
                || reservation.getDestination().isBlank()) {
            throw new DomainValidationException("Origen, destino y fecha son obligatorios.");
        }
        if (reservation.getReturnDate() != null && reservation.getReturnDate().isBefore(reservation.getTravelDate())) {
            throw new DomainValidationException("El regreso no puede ser anterior a la ida.");
        }
        if (reservation.getPassengerCount() == null || reservation.getPassengerCount() < 1) {
            throw new DomainValidationException("La cantidad de pasajeros debe ser positiva.");
        }
        if (reservation.getAmount() == null) {
            reservation.setAmount(pricingAndScheduleService.calculateReservationAmount(
                    reservation.getPickupLocality(), reservation.getDestination(),
                    Boolean.TRUE.equals(reservation.getRoundTrip()), reservation.getPassengerCount()));
        }
        if (reservation.getDiscountAmount() == null) reservation.setDiscountAmount(BigDecimal.ZERO);
        if (reservation.getExtraAmount() == null) reservation.setExtraAmount(BigDecimal.ZERO);
        for (BigDecimal value : List.of(reservation.getAmount(), reservation.getDiscountAmount(), reservation.getExtraAmount())) {
            if (value.signum() < 0 || value.stripTrailingZeros().scale() > 2) {
                throw new DomainValidationException("Los montos deben ser positivos o cero y tener hasta dos decimales.");
            }
        }
        String direction = reservation.getRouteDirection();
        if (direction != null && !direction.isBlank() && !List.of("IDA", "VUELTA").contains(direction)) {
            throw new DomainValidationException("Sentido de viaje inválido.");
        }
    }

    private void resetManualState(Reservation reservation) {
        reservation.setStatus("CONFIRMED");
        reservation.setTravelStatus(Reservation.TravelStatus.SCHEDULED);
        reservation.setPaymentExpiresAt(null);
        reservation.setReturnedPassengerCount(0);
        reservation.setReturnAuditSentAt(null);
        reservation.setRouteSequence(null);
    }

    @Transactional
    public List<Reservation> saveReservationFlow(Reservation mainReservation) {
        return saveReservationFlow(mainReservation, null);
    }

    @Transactional
    public List<Reservation> saveReservationFlow(
            Reservation mainReservation, String returnDepartureSchedule) {
        return saveReservationFlow(mainReservation, returnDepartureSchedule, true);
    }

    private List<Reservation> saveReservationFlow(Reservation mainReservation,
            String returnDepartureSchedule, boolean applyBalance) {
        List<Reservation> savedReservations = new ArrayList<>();

        lockAndValidateCapacity(mainReservation);
        if (Boolean.TRUE.equals(mainReservation.getRoundTrip())
                && mainReservation.getReturnDate() != null) {
            lockAndValidateCapacity(mainReservation.getReturnDate(), returnDepartureSchedule,
                    mainReservation.getDestination(), mainReservation.getTotalSeats());
        }

        if (Boolean.TRUE.equals(mainReservation.getRoundTrip())
                && (mainReservation.getTripType() == TripType.OPEN_RETURN || mainReservation.getReturnDate() == null)
                && mainReservation.getTravelDate() != null
                && com.lunaris.ansenuza.shared.ArgentinaTime.now().isBefore(mainReservation.getTravelDate().atTime(11, 0))) {
            for (String block : List.of("14:00", "17:30")) {
                lockAndValidateCapacity(mainReservation.getTravelDate(), block,
                        mainReservation.getDestination(), mainReservation.getTotalSeats());
            }
        }

        normalizePassengerName(mainReservation.getPassenger());
        boolean requiresInvoice = Boolean.TRUE.equals(mainReservation.getRequiresInvoice());
        mainReservation.setRequiresInvoice(requiresInvoice);

        // 1. Normalizamos la ruta y sus prefijos para que todos los canales
        // (web, panel y bot) compartan el mismo formato de código.
        String originClean = cleanLocality(mainReservation.getPickupLocality());
        String destClean = cleanLocality(mainReservation.getDestination());
        String outboundDirection = mainReservation.getRouteDirection();
        if (outboundDirection == null || outboundDirection.isBlank()) {
            outboundDirection = routeDirection(originClean, destClean);
        }
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
        BigDecimal saldoAplicado = BigDecimal.ZERO;

        if (applyBalance && saldoDisponible.compareTo(BigDecimal.ZERO) > 0
                && costoTotalFlujo.compareTo(BigDecimal.ZERO) > 0) {
            saldoAplicado = saldoDisponible.min(costoTotalFlujo);
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
        BigDecimal saldoIda = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? saldoAplicado.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                : saldoAplicado;
        BigDecimal saldoVuelta = Boolean.TRUE.equals(mainReservation.getRoundTrip())
                ? saldoAplicado.subtract(saldoIda)
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
        mainReservation.setUsedBalance(saldoIda);
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
            returnReservation.setUsedBalance(saldoVuelta);
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

        notificationEvents.publishEvent(com.lunaris.ansenuza.domain.model.OperatorNotification.reservation(savedReservations));
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
        if (TripRouteCalculatorService.isCordoba(pickupLocality)
                && (departureSchedule == null || departureSchedule.isBlank())) {
            for (String block : List.of("14:00", "17:30")) {
                lockAndValidateCapacity(travelDate, block, pickupLocality, requestedSeats);
            }
            return;
        }
        String schedule = departureSchedule == null
                || departureSchedule.isBlank()
                ? "03:00 AM" : departureSchedule.trim();
        String direction = TripRouteCalculatorService.isCordoba(pickupLocality)
                ? "RETURN" : "OUTBOUND";
        String key = travelDate + "|" + ("RETURN".equals(direction) ? "DAY" : ReturnCapacityPolicy.normalizeSchedule(schedule))
                + "|" + direction;
        capacityLockRepository.ensureExists(key);
        if (capacityLockRepository.findForUpdate(key) == null) {
            throw new DomainValidationException("No se pudo bloquear la capacidad del turno.");
        }
        long available = "RETURN".equals(direction)
                ? ReturnCapacityPolicy.availableSeats(reservationRepository, travelDate, schedule)
                : 19 - reservationRepository.countReservedSeats(travelDate, schedule);
        if (requestedSeats > available) {
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

    // 🗑️ BAJA LÓGICA ATÓMICA CON CASCADA DE TODO EL GRUPO DE RESERVA
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
                                    returnReservation, reservation.getPassenger(), triggeredBy,
                                    reservation))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CancellationResult(credited.signum() > 0, credited);
                }

                if (isReturnLeg(reservation)) {
                    List<Reservation> outbounds = associatedReservations(reservation);
                    java.util.Optional<Reservation> consumedOutbound = outbounds.stream()
                            .filter(this::isOutboundLeg)
                            .filter(this::isUsed)
                            .findFirst();
                    if (consumedOutbound.isPresent()) {
                        BigDecimal credited = cancelReturnOnly(
                                reservation, reservation.getPassenger(), triggeredBy,
                                consumedOutbound.get());
                        return new CancellationResult(credited.signum() > 0, credited);
                    }
                }
                
                Passenger passenger = lockPassenger(reservation.getPassenger());
                reservation.setPassenger(passenger);
                BigDecimal saldoActual = passenger.getCurrentBalance() != null ? passenger.getCurrentBalance() : BigDecimal.ZERO;
                
                final BigDecimal[] totalReintegro = { BigDecimal.ZERO };

                // El dinero externo exige verificación; el saldo ya debitado siempre se restituye.
                boolean pagoRealizado = Boolean.TRUE.equals(reservation.getPaymentVerified());
                if (!pagoRealizado && usedBalance(reservation).signum() == 0) {
                    log.warn("[CANCEL] Cancelación SIN reembolso para reserva {}. Motivo: payment_verified es FALSE (Estado actual: {})",
                            reservation.getReservationCode(), reservation.getStatus());
                }

                // 1. Cancelamos la reserva actual seleccionada (Ida o Vuelta Abierta)
                reservation.setStatus("CANCELLED");
                reservation.setTravelStatus(Reservation.TravelStatus.CANCELED);

                BigDecimal reintegroReserva = refundableAmount(reservation);
                totalReintegro[0] = totalReintegro[0].add(reintegroReserva);
                reservationRepository.saveAndFlush(reservation);

                // Registro del evento
                ReservationEvent cancelEvent = ReservationEvent.builder()
                        .reservationId(reservation.getId())
                        .eventType(reintegroReserva.signum() > 0 ? "CANCELLED_CREDIT_ACCRUED"
                                : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                        .description("Reserva " + reservation.getReservationCode() + " dada de baja. Pago verificado anteriormente: " + pagoRealizado)
                        .triggeredBy(triggeredBy)
                        .build();
                reservationEventRepository.save(cancelEvent);

                // 2. 🔄 CASCADA DE GRUPO: cualquier tramo cancela sus tramos gemelos.
                associatedReservations(reservation).forEach(associated -> {
                        if (!"CANCELLED".equals(associated.getStatus())) {
                            assertCancellationAllowed(associated, triggeredBy);
                            assertNotCompleted(associated);
                            boolean pagoAsociadoRealizado = Boolean.TRUE.equals(associated.getPaymentVerified());
                            
                            associated.setStatus("CANCELLED");
                            associated.setTravelStatus(Reservation.TravelStatus.CANCELED);

                            BigDecimal reintegroAsociado = refundableAmount(associated);
                            totalReintegro[0] = totalReintegro[0].add(reintegroAsociado);
                            reservationRepository.saveAndFlush(associated);

                            ReservationEvent cancelAssociatedEvent = ReservationEvent.builder()
                                    .reservationId(associated.getId())
                                    .eventType(reintegroAsociado.signum() > 0 ? "CANCELLED_CREDIT_ACCRUED"
                                            : "CANCELLED_WITHOUT_REFUND_UNVERIFIED_PAYMENT")
                                    .description("Cancelación automática del grupo "
                                            + reservation.getBookingGroupCode()
                                            + " por baja de " + reservation.getReservationCode()
                                            + ". Pago verificado: " + pagoAsociadoRealizado)
                                    .triggeredBy(triggeredBy)
                                    .build();
                            reservationEventRepository.save(cancelAssociatedEvent);
                        }
                    });

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
            Reservation returnReservation, Passenger passenger, String triggeredBy,
            Reservation consumedOutbound) {
        assertNotCompleted(returnReservation);
        assertCancellationAllowed(returnReservation, triggeredBy);
        boolean refundable = isRefundEligible(returnReservation);
        BigDecimal refund = refundable
                ? refundableAmount(returnReservation)
                : BigDecimal.ZERO;
        BigDecimal oneWayAdjustment = refundable && pricingAndScheduleService != null
                ? pricingAndScheduleService.calculateOneWaySurcharge(
                        consumedOutbound.getTotalSeats()).min(refund)
                : BigDecimal.ZERO;
        refund = refund.subtract(oneWayAdjustment).max(BigDecimal.ZERO);
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
        if (oneWayAdjustment.signum() > 0) {
            BigDecimal existingExtra = consumedOutbound.getExtraAmount() == null
                    ? BigDecimal.ZERO : consumedOutbound.getExtraAmount();
            consumedOutbound.setExtraAmount(existingExtra.add(oneWayAdjustment));
            reservationRepository.saveAndFlush(consumedOutbound);
            reservationEventRepository.save(ReservationEvent.builder()
                    .reservationId(consumedOutbound.getId())
                    .eventType("ONE_WAY_REPRICED_AFTER_RETURN_CANCELLATION")
                    .description("Reliquidación de tarifa solo ida descontada del reintegro: "
                            + oneWayAdjustment)
                    .triggeredBy(triggeredBy)
                    .build());
        }
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
        String groupCode = paymentGroupCode(parent.getReservationCode());
        if (groupCode == null) {
            return List.of();
        }
        List<Reservation> legacyGroup = reservationRepository
                .findReservationGroupForUpdate(groupCode).stream()
                .filter(candidate -> !java.util.Objects.equals(candidate.getId(), parent.getId()))
                .filter(candidate -> !"CANCELLED".equalsIgnoreCase(candidate.getStatus()))
                .toList();
        if (!legacyGroup.isEmpty()) {
            return legacyGroup;
        }
        String twinCode = isOutboundLeg(parent)
                ? groupCode + "-VUELTA" : groupCode + "-IDA";
        return reservationRepository.findByReservationCode(twinCode)
                .filter(candidate -> !"CANCELLED".equalsIgnoreCase(candidate.getStatus()))
                .stream()
                .toList();
    }

    private BigDecimal refundableAmount(Reservation reservation) {
        BigDecimal paidAmount = usedBalance(reservation);
        if (Boolean.TRUE.equals(reservation.getPaymentVerified())) {
            paidAmount = paidAmount.add(amountWithExtras(reservation));
        }
        if (paidAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        if (!isReturnLeg(reservation)) {
            return isUsed(reservation) ? BigDecimal.ZERO : paidAmount;
        }
        int totalSeats = reservation.getTotalSeats();
        int unusedSeats = Math.max(0, totalSeats - returnedSeats(reservation));
        return paidAmount
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
     * Un comprobante o estado no prueban dinero externo. El saldo previamente
     * debitado sí es valor realizado y debe poder restituirse.
     */
    public boolean isRefundEligible(Reservation reservation) {
        return reservation != null && (Boolean.TRUE.equals(reservation.getPaymentVerified())
                || usedBalance(reservation).signum() > 0);
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

    private BigDecimal usedBalance(Reservation reservation) {
        return reservation.getUsedBalance() == null ? BigDecimal.ZERO : reservation.getUsedBalance();
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
            boolean manualReactivation = "ADMIN_PANEL".equals(triggeredBy)
                    && "CONFIRMED".equals(updatedData.getStatus())
                    && ("CANCELLED".equalsIgnoreCase(reservation.getStatus())
                        || reservation.getTravelStatus() == Reservation.TravelStatus.CANCELED
                        || reservation.getTravelStatus() == Reservation.TravelStatus.NO_SHOW);
            if (manualReactivation) {
                lockAndValidateCapacity(
                        updatedData.getTravelDate() != null ? updatedData.getTravelDate() : reservation.getTravelDate(),
                        updatedData.getDepartureSchedule() != null ? updatedData.getDepartureSchedule() : reservation.getDepartureSchedule(),
                        reservation.getPickupLocality(),
                        updatedData.getPassengerCount() != null ? updatedData.getPassengerCount() : reservation.getTotalSeats());
            }
            Reservation.TravelStatus requestedTravelStatus = updatedData.getTravelStatus();
            StringBuilder auditoriaDesc = new StringBuilder("Campos modificados: ");
            LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);

            if (updatedData.getTravelDate() != null && !updatedData.getTravelDate().equals(reservation.getTravelDate())) {
                boolean schedulingOpenReturn = reservation.getTravelStatus()
                        == Reservation.TravelStatus.OPEN_RETURN
                        && !updatedData.getTravelDate().equals(fechaCentinela);
                auditoriaDesc.append(String.format("[Fecha: %s -> %s] ", reservation.getTravelDate(), updatedData.getTravelDate()));
                reservation.setTravelDate(updatedData.getTravelDate());
                if (schedulingOpenReturn && requestedTravelStatus == null) {
                    reservation.setTravelStatus(Reservation.TravelStatus.PENDING);
                }
                
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

            if (manualReactivation) {
                resetManualState(reservation);
                auditoriaDesc.append("[Reserva reactivada manualmente] ");
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/ReturnCapacityPolicy.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

/** Shared return inventory. Undated returns conservatively retain seats in each block. */
public final class ReturnCapacityPolicy {
    public static final int CAPACITY = 19;
    private ReturnCapacityPolicy() {}

    public static String normalizeSchedule(String schedule) {
        if (schedule == null || schedule.isBlank()) return "03:00";
        String value = schedule.toUpperCase(Locale.ROOT).replaceAll("\\s+", "").replace("HS", "");
        boolean pm = value.endsWith("PM");
        boolean am = value.endsWith("AM");
        value = value.replace("AM", "").replace("PM", "");
        LocalTime time = LocalTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("H:mm"));
        if (pm && time.getHour() < 12) time = time.plusHours(12);
        if (am && time.getHour() == 12) time = time.minusHours(12);
        return time.toString();
    }

    public static int availableSeats(ReservationRepository repository, LocalDate date, String schedule) {
        return availableSeats(repository, date, schedule, ArgentinaTime.now());
    }

    public static int availableSeats(ReservationRepository repository, LocalDate date,
            String schedule, LocalDateTime now) {
        boolean retain = now.isBefore(date.atTime(11, 0));
        long occupied = repository.findReturnCapacityCandidates(date).stream()
                .filter(r -> r.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                        ? retain : r.getDepartureSchedule() == null || r.getDepartureSchedule().isBlank()
                                || normalizeSchedule(r.getDepartureSchedule()).equals(normalizeSchedule(schedule)))
                .mapToLong(r -> r.getPassengerCount() == null ? 1 : Math.max(1, r.getPassengerCount()))
                .sum();
        return (int) Math.max(0, CAPACITY - occupied);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/SameDayBookingPolicy.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.exception.SameDayBookingClosedException;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SameDayBookingPolicy {

    public static final String CONFIGURATION_KEY = "SAME_DAY_CUTOFF_TIME";
    public static final String BUFFER_CONFIGURATION_KEY = "SAME_DAY_CUTOFF_BUFFER_MINUTES";
    public static final LocalTime DEFAULT_CUTOFF = LocalTime.of(8, 0);
    public static final List<LocalTime> DEFAULT_SHIFTS = List.of(
            LocalTime.of(3, 0), LocalTime.of(8, 0));

    private final SystemConfigurationService configurationService;

    public void validate(LocalDate travelDate) {
        if (isClosed(travelDate, ArgentinaTime.now())) {
            throw new SameDayBookingClosedException();
        }
    }

    public void validate(LocalDate travelDate, String selectedShift) {
        LocalDateTime now = ArgentinaTime.now();
        if (travelDate != null && travelDate.equals(now.toLocalDate())
                && isShiftClosed(selectedShift, now.toLocalTime())) {
            throw new SameDayBookingClosedException();
        }
    }

    public boolean isTodayClosed() {
        LocalDateTime now = ArgentinaTime.now();
        return isClosed(now.toLocalDate(), now);
    }

    boolean isClosed(LocalDate travelDate, LocalDateTime now) {
        return travelDate != null
                && travelDate.equals(now.toLocalDate())
                && DEFAULT_SHIFTS.stream().allMatch(shift -> isShiftClosed(shift, now.toLocalTime()));
    }

    public boolean isTodayClosed(String selectedShift) {
        return isShiftClosed(selectedShift, ArgentinaTime.currentTime());
    }

    boolean isShiftClosed(String selectedShift, LocalTime now) {
        LocalTime shift = parseShift(selectedShift);
        return shift == null
                ? DEFAULT_SHIFTS.stream().allMatch(candidate -> isShiftClosed(candidate, now))
                : isShiftClosed(shift, now);
    }

    boolean isShiftClosed(LocalTime shift, LocalTime now) {
        return java.time.Duration.between(now, shift).compareTo(
                java.time.Duration.ofMinutes(cutoffBufferMinutes())) < 0;
    }

    private LocalTime parseShift(String selectedShift) {
        if (selectedShift == null || selectedShift.isBlank()) return null;
        String value = selectedShift.trim().toUpperCase(Locale.ROOT);
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("H:mm", Locale.ROOT),
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Se prueba el siguiente formato soportado.
            }
        }
        return null;
    }

    public int cutoffBufferMinutes() {
        try {
            return Math.max(60, Integer.parseInt(configurationService.getValue(
                    BUFFER_CONFIGURATION_KEY, "0").trim()));
        } catch (RuntimeException exception) {
            return 60;
        }
    }

    public LocalTime cutoffTime() {
        String configured = configurationService.getValue(
                CONFIGURATION_KEY, DEFAULT_CUTOFF.toString());
        try {
            return LocalTime.parse(configured.trim());
        } catch (RuntimeException exception) {
            return DEFAULT_CUTOFF;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/SystemConfigurationService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private static final String SCHEDULE_MAX_CAPACITY = "schedule.max.capacity";
    private static final String PRIMARY_VEHICLE_CAPACITY = "primary.vehicle.capacity";
    private static final int DEFAULT_SCHEDULE_MAX_CAPACITY = 19;

    private final SystemConfigurationRepository repository;

    @Transactional(readOnly = true)
    public List<SystemConfiguration> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public SystemConfiguration findByKey(String key) {
        return repository.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada: " + key));
    }

    @Transactional(readOnly = true)
    @Cacheable("systemConfigurations")
    public String getValue(String key, String defaultValue) {
        if (key == null || key.isBlank()) {
            return defaultValue;
        }
        return repository.findById(key.trim())
                .map(SystemConfiguration::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public int getScheduleMaxCapacity() {
        String configuredValue = getValue(
                SCHEDULE_MAX_CAPACITY, String.valueOf(DEFAULT_SCHEDULE_MAX_CAPACITY));
        try {
            int capacity = Integer.parseInt(configuredValue.trim());
            return capacity > 0 ? capacity : DEFAULT_SCHEDULE_MAX_CAPACITY;
        } catch (NumberFormatException exception) {
            return DEFAULT_SCHEDULE_MAX_CAPACITY;
        }
    }

    @Transactional(readOnly = true)
    public int getPrimaryVehicleCapacity() {
        String configuredValue = getValue(
                PRIMARY_VEHICLE_CAPACITY, String.valueOf(DEFAULT_SCHEDULE_MAX_CAPACITY));
        try {
            int capacity = Integer.parseInt(configuredValue.trim());
            return capacity > 0 ? capacity : DEFAULT_SCHEDULE_MAX_CAPACITY;
        } catch (NumberFormatException exception) {
            return DEFAULT_SCHEDULE_MAX_CAPACITY;
        }
    }

    @Transactional
    @CacheEvict(value = "systemConfigurations", allEntries = true)
    public SystemConfiguration save(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave de configuración es obligatoria.");
        }

        SystemConfiguration configuration = repository.findById(key.trim())
                .orElseGet(() -> SystemConfiguration.builder().key(key.trim()).build());
        configuration.setValue(value);
        return repository.saveAndFlush(configuration);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/model/service/TripRouteCalculatorService.java`

```java
package com.lunaris.ansenuza.domain.model.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import com.lunaris.ansenuza.domain.model.Reservation;

public class TripRouteCalculatorService {
    public enum RouteDirection { OUTBOUND, RETURN }

    public static final List<String> NORTH_TERMINAL_CORRIDOR = List.of(
            "Arrufó", "Villa Trinidad", "San Guillermo", "Suardi", "Morteros", "Brinkmann",
            "Porteña", "Freyre", "La Paquita", "Altos de Chipión", "Balnearia", "Miramar", "Córdoba");

    private static final Map<String, Integer> CORRIDOR_INDEX = IntStream.range(0, NORTH_TERMINAL_CORRIDOR.size())
            .boxed().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    index -> normalize(NORTH_TERMINAL_CORRIDOR.get(index)), index -> index));

    public Calculation calculate(List<BookingDemand> bookings) {
        List<BookingDemand> safeBookings = bookings == null ? List.of() : bookings;
        int effectiveIndex = safeBookings.stream()
                .filter(booking -> booking != null && booking.passengers() > 0)
                .mapToInt(booking -> corridorIndex(booking.locality()))
                .filter(index -> index >= 0)
                .min()
                .orElse(-1);
        if (effectiveIndex < 0) {
            return new Calculation(null, NORTH_TERMINAL_CORRIDOR.getFirst(), List.of(),
                    "Sin pasajeros confirmados para el corredor en este turno.");
        }

        String effectiveOrigin = NORTH_TERMINAL_CORRIDOR.get(effectiveIndex);
        List<String> skipped = List.copyOf(NORTH_TERMINAL_CORRIDOR.subList(0, effectiveIndex));
        String message = skipped.isEmpty()
                ? "Cabecera del día confirmada: " + effectiveOrigin + "."
                : "Cabecera del día recalculada: " + effectiveOrigin
                        + " (Sin pasajeros en " + String.join("/", skipped) + ").";
        return new Calculation(effectiveOrigin, NORTH_TERMINAL_CORRIDOR.getFirst(), skipped, message);
    }

    public int corridorIndex(String locality) {
        if (locality == null) return -1;
        String normalized = normalize(locality).replace(" capital", "");
        return CORRIDOR_INDEX.getOrDefault(normalized, -1);
    }

    public boolean matchesManifest(
            Reservation reservation, RouteDirection direction, String scheduleBlock) {
        if (reservation == null || direction == null) return false;
        boolean fromCordoba = isCordoba(reservation.getPickupLocality());
        boolean toCordoba = isCordoba(reservation.getDestination());
        boolean routeMatches = direction == RouteDirection.RETURN
                ? fromCordoba && !toCordoba
                : !fromCordoba && toCordoba;
        return routeMatches
                && normalizeSchedule(scheduleBlock).equals(
                        normalizeSchedule(reservation.getDepartureSchedule()));
    }

    public boolean sameManifest(Reservation first, Reservation candidate) {
        if (first == null || candidate == null) return false;
        RouteDirection direction = isCordoba(first.getPickupLocality())
                ? RouteDirection.RETURN : RouteDirection.OUTBOUND;
        return matchesManifest(candidate, direction, first.getDepartureSchedule());
    }

    public static String normalizeSchedule(String schedule) {
        if (schedule == null) return "";
        String normalized = schedule.trim().toUpperCase(Locale.ROOT);
        return normalized.endsWith(" AM") || normalized.endsWith(" PM")
                ? normalized.substring(0, normalized.length() - 3)
                : normalized;
    }

    public static boolean isCordoba(String locality) {
        if (locality == null) return false;
        String normalized = normalize(locality);
        return normalized.contains("cordoba");
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public record BookingDemand(String locality, int passengers) {
    }

    public record Calculation(
            String effectiveOrigin, String theoreticalOrigin, List<String> skippedLocalities, String message) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/CreateFareLocalityUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;

public interface CreateFareLocalityUseCase {
    FareLocalityView create(String localityName, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/CreateSpecialTripUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface CreateSpecialTripUseCase {
    SpecialTrip create(SpecialTripCommand command);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/DeleteFareLocalityUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.util.UUID;

public interface DeleteFareLocalityUseCase {
    void delete(UUID fareId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/FareLocalityView.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record FareLocalityView(
        UUID fareId, UUID localityId, String localityName, BigDecimal amount,
        Integer kmsToCordoba, Integer minutesFromOrigin) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/GetFaresQuery.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.util.List;

public interface GetFaresQuery {
    List<FareLocalityView> getAll();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/GetSpecialTripsQuery.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import java.util.List;

public interface GetSpecialTripsQuery {
    List<SpecialTrip> getAll();
    List<SpecialTrip> getActive();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/ResolveEffectiveTripOriginUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.time.LocalDate;

public interface ResolveEffectiveTripOriginUseCase {
    RouteOriginResolution resolve(LocalDate travelDate, String scheduleBlock);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/RouteOriginResolution.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record RouteOriginResolution(
        LocalDate travelDate,
        String scheduleBlock,
        String effectiveOrigin,
        List<String> skippedLocalities,
        Map<String, Integer> minuteOffsets,
        String summary) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/SpecialTripCommand.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpecialTripCommand(
        String title, String description, String origin, String destination,
        LocalDate startDate, LocalDate endDate, BigDecimal price,
        Integer maxPassengers, String imageUrl, boolean active) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/ToggleSpecialTripStatusUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface ToggleSpecialTripStatusUseCase {
    SpecialTrip setActive(Long id, boolean active);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateFareUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateFareUseCase {
    FareLocalityView updateFare(UUID fareId, BigDecimal amount);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateLocalityFareUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateLocalityFareUseCase {
    FareLocalityView updateLocalityAndFare(UUID localityId, String name, Integer kmsToCordoba,
            Integer minutesFromOrigin, BigDecimal amount);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/in/UpdateSpecialTripUseCase.java`

```java
package com.lunaris.ansenuza.domain.port.in;

import com.lunaris.ansenuza.domain.model.SpecialTrip;

public interface UpdateSpecialTripUseCase {
    SpecialTrip update(Long id, SpecialTripCommand command);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/domain/port/out/SpecialTripRepositoryPort.java`

```java
package com.lunaris.ansenuza.domain.port.out;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import java.util.List;
import java.util.Optional;

public interface SpecialTripRepositoryPort {
    SpecialTrip save(SpecialTrip specialTrip);
    Optional<SpecialTrip> findById(Long id);
    List<SpecialTrip> findAll();
    List<SpecialTrip> findActive();
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/AdminUserInitializer.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class AdminUserInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.security.admin.username:admin}") String adminUsername,
            @Value("${app.security.admin.password:}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (isProduction() && !hasPassword()) {
            log.warn("ADMIN_INITIAL_PASSWORD no está configurada en producción; "
                    + "se omite la creación o actualización del usuario administrador.");
            return;
        }
        accountRepository.findByUsernameIgnoreCase(adminUsername)
                .ifPresentOrElse(this::updateManagedAdmin, this::createAdmin);
    }

    private void updateManagedAdmin(Account account) {
        if (hasPassword() && (account.getPasswordHash() == null
                || environment.acceptsProfiles(Profiles.of("dev")))) {
            account.setPasswordHash(passwordEncoder.encode(adminPassword));
        }
        account.setActive(true);
        Set<Role> roles = account.getRoles() == null
                ? new HashSet<>()
                : new HashSet<>(account.getRoles());
        roles.add(Role.ADMIN);
        account.setRoles(roles);
        // No se invoca save(): la entidad pertenece a esta transacción y Hibernate
        // persiste los cambios mediante dirty checking al confirmar el commit.
    }

    private void createAdmin() {
        if (!hasPassword()) {
            return;
        }
        Account account = newAdminAccount();
        account.setPasswordHash(passwordEncoder.encode(adminPassword));
        accountRepository.save(account);
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private boolean hasPassword() {
        return adminPassword != null && !adminPassword.isBlank();
    }

    private Account newAdminAccount() {
        return Account.builder()
                .username(adminUsername)
                .displayName("Administrador")
                .active(true)
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .build();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/AsyncConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/ChatbotAnalyticsProperties.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chatbot.analytics")
@Getter
@Setter
public class ChatbotAnalyticsProperties {
    private boolean enabled = true;
    private String hmacSecret = "";
    private short subjectKeyVersion = 1;
    private String environment = "local";
    private Duration inactivityTimeout = Duration.ofMinutes(30);
    private Duration retention = Duration.ofDays(90);
    private List<String> internalPhones = List.of();
}

```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/DataInitializer.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.BusinessParameter;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Locality;
import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.repository.BusinessParameterRepository;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.SystemConfigurationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class DataInitializer implements CommandLineRunner {

    private static final List<LocalitySeed> DEFAULT_LOCALITIES = List.of(
            new LocalitySeed("Córdoba Capital", 0, 0, "20000"),
            new LocalitySeed("Río Primero", 52, 45, "30000"),
            new LocalitySeed("Villa Santa Rosa", 86, 75, "38000"),
            new LocalitySeed("Obispo Trejo", 122, 105, "44000"),
            new LocalitySeed("La Puerta", 128, 115, "46000"),
            new LocalitySeed("La Para", 148, 135, "50000"),
            new LocalitySeed("Marull", 167, 155, "54000"),
            new LocalitySeed("Balnearia", 180, 170, "56000"),
            new LocalitySeed("Miramar", 197, 185, "62000"));

    private static final Map<String, String> DEFAULT_BUSINESS_PARAMETERS = Map.of(
            "ONE_WAY_EXTRA_AMOUNT", "8000",
            "PRICE_PER_KM", "1000");

    private static final Map<String, String> DEFAULT_SYSTEM_CONFIGURATIONS = Map.of(
            "return.scheduler.time", "15:00",
            "return.message.header", "Confirmación de vuelta",
            "return.message.body",
                    "Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?\n"
                            + "Elegí una opción para que podamos organizar las butacas.",
            "return.button.yes.title", "SÍ, VOLVER ✅",
            "return.button.later.title", "OTRO DÍA 📅",
            "return.button.no.title", "NO, CANCELAR ❌",
            "SAME_DAY_CUTOFF_TIME", "08:00",
            "SAME_DAY_CUTOFF_BUFFER_MINUTES", "0",
            "primary.vehicle.capacity", "12",
            "session.inactivity.timeout.minutes", "30");

    private final LocalityRepository localityRepository;
    private final FareRepository fareRepository;
    private final BusinessParameterRepository businessParameterRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    public DataInitializer(
            LocalityRepository localityRepository,
            FareRepository fareRepository,
            BusinessParameterRepository businessParameterRepository,
            SystemConfigurationRepository systemConfigurationRepository) {
        this.localityRepository = localityRepository;
        this.fareRepository = fareRepository;
        this.businessParameterRepository = businessParameterRepository;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (localityRepository.count() == 0 && fareRepository.count() == 0) {
            seedLocalitiesAndFares();
        }
        seedBusinessParameters();
        seedSystemConfigurations();
    }

    private void seedLocalitiesAndFares() {
        List<Locality> localities = DEFAULT_LOCALITIES.stream()
                .map(seed -> Locality.builder()
                        .name(seed.name())
                        .kmsToCordoba(seed.kmsToCordoba())
                        .minutesFromOrigin(seed.minutesFromOrigin())
                        .build())
                .toList();
        localityRepository.saveAllAndFlush(localities);

        List<Fare> fares = DEFAULT_LOCALITIES.stream()
                .map(seed -> Fare.builder()
                        .localityName(seed.name())
                        .amount(new BigDecimal(seed.amount()))
                        .build())
                .toList();
        fareRepository.saveAllAndFlush(fares);
    }

    private void seedBusinessParameters() {
        DEFAULT_BUSINESS_PARAMETERS.forEach((key, value) -> {
            if (!businessParameterRepository.existsById(key)) {
                businessParameterRepository.save(BusinessParameter.builder()
                        .parameterKey(key)
                        .parameterValue(value)
                        .build());
            }
        });
    }

    private void seedSystemConfigurations() {
        DEFAULT_SYSTEM_CONFIGURATIONS.forEach((key, value) -> {
            if (!systemConfigurationRepository.existsById(key)) {
                systemConfigurationRepository.save(SystemConfiguration.builder()
                        .key(key)
                        .value(value)
                        .build());
            }
        });
    }

    private record LocalitySeed(
            String name, int kmsToCordoba, int minutesFromOrigin, String amount) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/application/port/in/ConfirmPaymentUseCase.java`

```java
package com.lunaris.ansenuza.reservation.application.port.in;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.util.UUID;

public interface ConfirmPaymentUseCase {
    Reservation confirmPayment(UUID reservationId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/application/port/in/CreateReservationUseCase.java`

```java
package com.lunaris.ansenuza.reservation.application.port.in;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;

public interface CreateReservationUseCase {
    Reservation create(Reservation reservation);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/application/port/out/PaymentGatewayPort.java`

```java
package com.lunaris.ansenuza.reservation.application.port.out;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;

public interface PaymentGatewayPort {
    void confirm(Reservation reservation);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/application/port/out/ReservationRepositoryPort.java`

```java
package com.lunaris.ansenuza.reservation.application.port.out;

import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findByPickupLocality(String locality);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/application/service/ReservationApplicationService.java`

```java
package com.lunaris.ansenuza.reservation.application.service;

import com.lunaris.ansenuza.reservation.application.port.in.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.reservation.application.port.in.CreateReservationUseCase;
import com.lunaris.ansenuza.reservation.application.port.out.PaymentGatewayPort;
import com.lunaris.ansenuza.reservation.application.port.out.ReservationRepositoryPort;
import com.lunaris.ansenuza.reservation.domain.exception.ReservationNotFoundException;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationApplicationService implements CreateReservationUseCase, ConfirmPaymentUseCase {
    private final ReservationRepositoryPort repository;
    private final PaymentGatewayPort paymentGateway;
    private final Clock clock;

    @Autowired
    public ReservationApplicationService(ReservationRepositoryPort repository,
            PaymentGatewayPort paymentGateway) {
        this(repository, paymentGateway, Clock.systemDefaultZone());
    }

    ReservationApplicationService(ReservationRepositoryPort repository, PaymentGatewayPort paymentGateway, Clock clock) {
        this.repository = repository;
        this.paymentGateway = paymentGateway;
        this.clock = clock;
    }

    @Override @Transactional
    public Reservation create(Reservation reservation) { return repository.save(reservation); }

    @Override @Transactional
    public Reservation confirmPayment(UUID reservationId) {
        Reservation reservation = repository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        paymentGateway.confirm(reservation);
        reservation.confirmPayment(LocalDateTime.now(clock));
        return repository.save(reservation);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/domain/exception/ReservationNotFoundException.java`

```java
package com.lunaris.ansenuza.reservation.domain.exception;

import java.util.UUID;

public final class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(UUID reservationId) {
        super("Reserva no encontrada: " + reservationId);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/domain/model/Reservation.java`

```java
package com.lunaris.ansenuza.reservation.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Agregado de reserva puro. No conoce HTTP, Spring ni persistencia. */
public final class Reservation {
    private UUID id;
    private final UUID passengerId;
    private UUID driverId;
    private LocalDate travelDate;
    private final String pickupLocality;
    private String pickupAddress;
    private final String destination;
    private BigDecimal amount;
    private boolean amountIsGroupTotal;
    public boolean amountIsGroupTotal() { return amountIsGroupTotal; }
    private Boolean roundTrip;
    private String tripType;
    private LocalDate returnDate;
    private BigDecimal extraAmount;
    private String promotionCode;
    private UUID promotionId;
    private Integer promotionDiscountPercentage;
    private BigDecimal discountAmount;
    private boolean paymentVerified;
    private ReservationStatus status;
    private String source;
    private String travelStatus;
    private String notes;
    private String paymentReceiptUrl;
    private Long waitingListEntryId;
    private LocalDateTime paymentConfirmedAt;
    private String companionNames;
    private int passengerCount;
    private int returnedPassengerCount;
    private String reservationCode;
    private String bookingGroupCode;
    private String routeDirection;
    private LocalDateTime returnAuditSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String departureSchedule;
    private Integer routeSequence;
    private boolean requiresInvoice;

    private Reservation(Builder builder) {
        id = builder.id;
        passengerId = Objects.requireNonNull(builder.passengerId, "passengerId es obligatorio");
        driverId = builder.driverId;
        travelDate = builder.travelDate;
        pickupLocality = requireText(builder.pickupLocality, "pickupLocality");
        pickupAddress = builder.pickupAddress;
        destination = requireText(builder.destination, "destination");
        amount = nullSafe(builder.amount);
        amountIsGroupTotal = builder.amountIsGroupTotal;
        roundTrip = builder.roundTrip;
        tripType = builder.tripType;
        returnDate = builder.returnDate;
        extraAmount = nullSafe(builder.extraAmount);
        promotionCode = builder.promotionCode;
        promotionId = builder.promotionId;
        promotionDiscountPercentage = builder.promotionDiscountPercentage;
        discountAmount = nullSafe(builder.discountAmount);
        paymentVerified = builder.paymentVerified;
        status = builder.status == null ? ReservationStatus.PENDING : builder.status;
        source = builder.source == null ? "MANUAL" : builder.source;
        travelStatus = builder.travelStatus == null ? "PENDING" : builder.travelStatus;
        notes = builder.notes;
        paymentReceiptUrl = builder.paymentReceiptUrl;
        waitingListEntryId = builder.waitingListEntryId;
        paymentConfirmedAt = builder.paymentConfirmedAt;
        companionNames = builder.companionNames;
        passengerCount = Math.max(1, builder.passengerCount);
        returnedPassengerCount = Math.max(0, builder.returnedPassengerCount);
        reservationCode = builder.reservationCode;
        bookingGroupCode = builder.bookingGroupCode;
        routeDirection = builder.routeDirection;
        returnAuditSentAt = builder.returnAuditSentAt;
        createdAt = builder.createdAt;
        updatedAt = builder.updatedAt;
        departureSchedule = builder.departureSchedule;
        routeSequence = builder.routeSequence;
        requiresInvoice = builder.requiresInvoice;
    }

    public static Builder builder(UUID passengerId, String pickupLocality, String destination) {
        return new Builder(passengerId, pickupLocality, destination);
    }

    public void confirmPayment(LocalDateTime confirmedAt) {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("No se puede confirmar el pago de una reserva cancelada");
        }
        paymentVerified = true;
        status = ReservationStatus.CONFIRMED;
        paymentConfirmedAt = Objects.requireNonNull(confirmedAt, "confirmedAt es obligatorio");
        requiresInvoice = true;
    }

    public void cancel() {
        if (status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("No se puede cancelar una reserva completada");
        }
        status = ReservationStatus.CANCELLED;
        routeSequence = null;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " es obligatorio");
        return value.trim();
    }
    private static BigDecimal nullSafe(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    public UUID id() { return id; }
    public UUID passengerId() { return passengerId; }
    public UUID driverId() { return driverId; }
    public LocalDate travelDate() { return travelDate; }
    public String pickupLocality() { return pickupLocality; }
    public String pickupAddress() { return pickupAddress; }
    public String destination() { return destination; }
    public BigDecimal amount() { return amount; }
    public Boolean roundTrip() { return roundTrip; }
    public String tripType() { return tripType; }
    public LocalDate returnDate() { return returnDate; }
    public BigDecimal extraAmount() { return extraAmount; }
    public String promotionCode() { return promotionCode; }
    public UUID promotionId() { return promotionId; }
    public Integer promotionDiscountPercentage() { return promotionDiscountPercentage; }
    public BigDecimal discountAmount() { return discountAmount; }
    public boolean paymentVerified() { return paymentVerified; }
    public ReservationStatus status() { return status; }
    public String source() { return source; }
    public String travelStatus() { return travelStatus; }
    public String notes() { return notes; }
    public String paymentReceiptUrl() { return paymentReceiptUrl; }
    public Long waitingListEntryId() { return waitingListEntryId; }
    public LocalDateTime paymentConfirmedAt() { return paymentConfirmedAt; }
    public String companionNames() { return companionNames; }
    public int passengerCount() { return passengerCount; }
    public int returnedPassengerCount() { return returnedPassengerCount; }
    public String reservationCode() { return reservationCode; }
    public String bookingGroupCode() { return bookingGroupCode; }
    public String routeDirection() { return routeDirection; }
    public LocalDateTime returnAuditSentAt() { return returnAuditSentAt; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
    public String departureSchedule() { return departureSchedule; }
    public Integer routeSequence() { return routeSequence; }
    public boolean requiresInvoice() { return requiresInvoice; }

    public static final class Builder {
        private UUID id; private final UUID passengerId; private UUID driverId; private LocalDate travelDate;
        private final String pickupLocality; private String pickupAddress; private final String destination;
        private boolean amountIsGroupTotal;
        public Builder amountIsGroupTotal(boolean value) { amountIsGroupTotal = value; return this; }
        private BigDecimal amount; private Boolean roundTrip; private String tripType; private LocalDate returnDate;
        private BigDecimal extraAmount; private String promotionCode; private UUID promotionId;
        private Integer promotionDiscountPercentage; private BigDecimal discountAmount; private boolean paymentVerified;
        private ReservationStatus status; private String source; private String travelStatus; private String notes;
        private String paymentReceiptUrl; private Long waitingListEntryId; private LocalDateTime paymentConfirmedAt;
        private String companionNames; private int passengerCount = 1; private int returnedPassengerCount;
        private String reservationCode; private String bookingGroupCode; private String routeDirection;
        private LocalDateTime returnAuditSentAt; private LocalDateTime createdAt; private LocalDateTime updatedAt;
        private String departureSchedule; private Integer routeSequence; private boolean requiresInvoice = true;
        private Builder(UUID passengerId, String pickupLocality, String destination) { this.passengerId=passengerId; this.pickupLocality=pickupLocality; this.destination=destination; }
        public Builder id(UUID v){id=v;return this;} public Builder driverId(UUID v){driverId=v;return this;}
        public Builder travelDate(LocalDate v){travelDate=v;return this;} public Builder pickupAddress(String v){pickupAddress=v;return this;}
        public Builder amount(BigDecimal v){amount=v;return this;} public Builder roundTrip(Boolean v){roundTrip=v;return this;}
        public Builder tripType(String v){tripType=v;return this;} public Builder returnDate(LocalDate v){returnDate=v;return this;}
        public Builder extraAmount(BigDecimal v){extraAmount=v;return this;} public Builder promotionCode(String v){promotionCode=v;return this;}
        public Builder promotionId(UUID v){promotionId=v;return this;} public Builder promotionDiscountPercentage(Integer v){promotionDiscountPercentage=v;return this;}
        public Builder discountAmount(BigDecimal v){discountAmount=v;return this;} public Builder paymentVerified(boolean v){paymentVerified=v;return this;}
        public Builder status(ReservationStatus v){status=v;return this;} public Builder source(String v){source=v;return this;}
        public Builder travelStatus(String v){travelStatus=v;return this;} public Builder notes(String v){notes=v;return this;}
        public Builder paymentReceiptUrl(String v){paymentReceiptUrl=v;return this;} public Builder waitingListEntryId(Long v){waitingListEntryId=v;return this;}
        public Builder paymentConfirmedAt(LocalDateTime v){paymentConfirmedAt=v;return this;} public Builder companionNames(String v){companionNames=v;return this;}
        public Builder passengerCount(int v){passengerCount=v;return this;} public Builder returnedPassengerCount(int v){returnedPassengerCount=v;return this;}
        public Builder reservationCode(String v){reservationCode=v;return this;} public Builder bookingGroupCode(String v){bookingGroupCode=v;return this;}
        public Builder routeDirection(String v){routeDirection=v;return this;} public Builder returnAuditSentAt(LocalDateTime v){returnAuditSentAt=v;return this;}
        public Builder createdAt(LocalDateTime v){createdAt=v;return this;} public Builder updatedAt(LocalDateTime v){updatedAt=v;return this;}
        public Builder departureSchedule(String v){departureSchedule=v;return this;} public Builder routeSequence(Integer v){routeSequence=v;return this;}
        public Builder requiresInvoice(boolean v){requiresInvoice=v;return this;} public Reservation build(){return new Reservation(this);}
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/domain/model/ReservationStatus.java`

```java
package com.lunaris.ansenuza.reservation.domain.model;

/** Estados persistidos por los flujos históricos y actuales de reservas. */
public enum ReservationStatus {
    PENDING,
    PENDING_PAYMENT,
    PAYMENT_RECEIVED,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REJECTED;

    public static ReservationStatus fromPersistenceValue(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/BookingClosedException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class BookingClosedException extends RuntimeException {
    public BookingClosedException() { super("El viaje está cerrado para nuevas reservas."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityExceededException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException() { super("No hay plazas en todos los tramos solicitados."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/CapacityService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.transaction.annotation.Transactional;

/** Se registra exclusivamente mediante InterurbanConfiguration. */
public class CapacityService {
    private final CapacityRepository repository;
    private final Clock clock;

    public CapacityService(CapacityRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Lectura orientativa; hold siempre vuelve a comprobar con locks. */
    @Transactional(readOnly = true)
    public int available(UUID tripId, Route route) {
        validateRequest(tripId, route);
        var trip = repository.findTrip(tripId)
                .orElseThrow(() -> new InvalidRouteException("Viaje inexistente."));
        validateDirection(trip, route);
        var legs = repository.findLegs(tripId, route.ordinals());
        validateLegs(legs, route);
        if (!isOpen(trip)) return 0;
        return legs.stream().mapToInt(leg -> freeSeats(leg).size()).min().orElse(0);
    }

    /** Inserta reservas y plazas de todo el grupo en una única transacción. */
    @Transactional
    public List<UUID> hold(UUID tripId, Route route, List<PassengerHold> passengers, Instant expiresAt) {
        validateRequest(tripId, route);
        if (passengers == null || passengers.isEmpty() || passengers.size() > 4
                || passengers.stream().anyMatch(p -> p == null) || expiresAt == null) {
            throw new InvalidHoldException("Se requieren entre uno y cuatro pasajeros y vencimiento.");
        }
        var party = List.copyOf(passengers);
        if (party.stream().map(PassengerHold::bookingId).distinct().count() != 1) {
            throw new InvalidHoldException("Los pasajeros deben pertenecer al mismo grupo.");
        }
        var trip = repository.lockTrip(tripId)
                .orElseThrow(() -> new InvalidRouteException("Viaje inexistente."));
        validateDirection(trip, route);
        // El orden se aplica en SQL ANTES de adquirir locks, no en memoria después.
        var legs = repository.lockLegs(tripId, route.ordinals());
        validateLegs(legs, route);
        if (!isOpen(trip)) throw new BookingClosedException();
        if (!expiresAt.isAfter(clock.instant()) || expiresAt.isAfter(trip.closesAt())) {
            throw new InvalidHoldException("El hold debe vencer en el futuro y no superar el cierre.");
        }
        var freeByLeg = legs.stream().map(this::freeSeats).toList();
        if (freeByLeg.stream().anyMatch(seats -> seats.size() < party.size())) {
            throw new CapacityExceededException();
        }
        var ids = new ArrayList<UUID>();
        for (var passenger : party) {
            UUID id = UUID.randomUUID(); // JDBC: no entidades JPA ni generación implícita.
            repository.insertReservation(id, tripId, route, passenger, expiresAt);
            ids.add(id);
        }
        for (int leg = 0; leg < legs.size(); leg++) {
            for (int passenger = 0; passenger < ids.size(); passenger++) {
                repository.insertSeat(tripId, legs.get(leg).id(), freeByLeg.get(leg).get(passenger), ids.get(passenger));
            }
        }
        return List.copyOf(ids);
    }

    private List<Integer> freeSeats(CapacityRepository.Leg leg) {
        var occupied = repository.occupiedSeats(leg.id());
        return IntStream.rangeClosed(1, 4).filter(seat -> !occupied.contains(seat)).boxed().toList();
    }

    private boolean isOpen(CapacityRepository.Trip trip) {
        return "OPEN".equals(trip.status()) && clock.instant().isBefore(trip.closesAt());
    }

    private void validateRequest(UUID tripId, Route route) {
        if (tripId == null || route == null) throw new InvalidRouteException("Viaje y recorrido obligatorios.");
    }

    private void validateDirection(CapacityRepository.Trip trip, Route route) {
        if (trip.direction() != route.direction()) throw new InvalidRouteException("Sentido incompatible con el viaje.");
    }

    private void validateLegs(List<CapacityRepository.Leg> legs, Route route) {
        if (legs.size() != route.ordinals().size()
                || !new HashSet<>(legs.stream().map(CapacityRepository.Leg::ordinal).toList())
                        .equals(new HashSet<>(route.ordinals()))) {
            throw new InvalidRouteException("El viaje no contiene todos los tramos requeridos.");
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/CheckInService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public class CheckInService {
    public record Result(UUID reservationId, UUID tripId, String status, Instant checkedInAt) {}
    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Cordoba");
    private final DriverOperationsRepository repository;
    private final DriverIdentityPort identity;
    private final Clock clock;
    private final Duration earlyWindow;
    private final Duration lateWindow;

    public CheckInService(DriverOperationsRepository repository, DriverIdentityPort identity, Clock clock,
            Duration earlyWindow, Duration lateWindow) {
        if (earlyWindow.isNegative() || lateWindow.isNegative()) throw new IllegalArgumentException("Ventana de abordaje inválida.");
        this.repository = repository;
        this.identity = identity;
        this.clock = clock;
        this.earlyWindow = earlyWindow;
        this.lateWindow = lateWindow;
    }

    @Transactional
    public Result verify(String token, UUID tripId, Authentication authentication) {
        UUID driverId = identity.requireDriver(authentication);
        if (tripId == null) throw new InvalidTripException();
        String hash = tokenHash(token);
        // Mismo orden que pagos/capacidad: viaje -> reserva/QR. Nunca tomar un lock de pago después.
        var trip = repository.lockTrip(tripId).orElseThrow(InvalidTripException::new);
        if (!driverId.equals(trip.driverId())) throw new AccessDeniedException("El viaje no pertenece al chofer autenticado.");
        if (!"ASSIGNED".equals(trip.status())) throw new InvalidTripException();
        var ticket = repository.lockTicket(hash, tripId).orElseThrow(InvalidTripException::new);
        if (!tripId.equals(ticket.tripId())) throw new InvalidTripException();
        if (ticket.consumedAt() != null) throw new QrAlreadyConsumedException();
        Instant now = clock.instant(); // Releer reloj después de esperar por los locks.
        if (!now.isBefore(ticket.expiresAt())) throw new QrExpiredException();
        if (!"PAID".equals(ticket.status()) || !ticket.approvedPayment()) throw new InvalidTripException();
        Instant pickup = ticket.pickupAt() == null ? trip.departureAt() : ticket.pickupAt();
        if (!trip.date().equals(LocalDate.ofInstant(now, ZONE))
                || now.isBefore(pickup.minus(earlyWindow)) || now.isAfter(pickup.plus(lateWindow))) {
            throw new InvalidTripException();
        }
        repository.consume(ticket.reservationId(), now);
        repository.markCheckedIn(ticket.reservationId(), driverId, now);
        repository.earn(ticket.reservationId(), driverId, ticket.fare().subtract(ticket.commission()), now);
        return new Result(ticket.reservationId(), tripId, "CHECKED_IN", now);
    }

    static String tokenHash(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw new InvalidQrException();
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(token);
            if (bytes.length != 32 || !Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).equals(token)) {
                throw new InvalidQrException();
            }
            // QR etapa 2: hash del texto canónico UTF-8, no de los bytes de entropía.
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (IllegalArgumentException e) { throw new InvalidQrException(); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no disponible", e); }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DailyDriverPayoutJob.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

public class DailyDriverPayoutJob {
    private final DriverSettlementService settlement;
    private final DriverPayoutReviewService review;
    private final Clock clock;
    public DailyDriverPayoutJob(DriverSettlementService settlement, DriverPayoutReviewService review, Clock clock) {
        this.settlement = settlement; this.review = review; this.clock = clock;
    }
    @Scheduled(cron = "0 0 22 * * *", zone = "America/Argentina/Cordoba")
    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        settlement.settle(DriverSettlementService.latestCutoff(clock.instant()));
        review.dispatch(review.pending());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverIdentityPort.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.UUID;
import org.springframework.security.core.Authentication;

public interface DriverIdentityPort {
    UUID requireDriver(Authentication authentication);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverPayoutPort.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Las claves identifican órdenes inmutables y deben reutilizarse en cada reintento. */
public interface DriverPayoutPort {
    record Order(UUID id, UUID driverId, BigDecimal amount, String idempotencyKey) {}
    enum Status { READY_FOR_APPROVAL, UNKNOWN, PAID, FAILED }
    record Result(String idempotencyKey, Status status, String providerReference) {}
    List<Result> submitBatch(List<Order> orders);
    Result reconcile(String idempotencyKey);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverPayoutReviewService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/** Consume únicamente eventos de revisión interna; nunca cambia la orden a PAID. */
public class DriverPayoutReviewService {
    private final NamedParameterJdbcTemplate jdbc;
    private final DriverPayoutPort port;
    public DriverPayoutReviewService(NamedParameterJdbcTemplate jdbc, DriverPayoutPort port) {
        this.jdbc = jdbc; this.port = port;
    }
    @Transactional(readOnly = true)
    public List<DriverPayoutPort.Order> pending() {
        return jdbc.getJdbcTemplate().query("""
                SELECT p.id,p.driver_id,p.amount,p.idempotency_key FROM interurban.payout_orders p
                JOIN interurban.outbox o ON o.aggregate_id=p.id AND o.event_type='PAYOUT_READY'
                WHERE o.delivered_at IS NULL AND p.status='PENDING' ORDER BY p.created_at,p.id
                """, (rs, row) -> new DriverPayoutPort.Order(rs.getObject("id", UUID.class),
                rs.getObject("driver_id", UUID.class), rs.getBigDecimal("amount"), rs.getString("idempotency_key")));
    }
    public void dispatch(List<DriverPayoutPort.Order> orders) {
        // La liquidación ya confirmó su transacción; el adaptador se invoca sin locks contables.
        for (var result : port.submitBatch(orders)) {
            if (result.status() == DriverPayoutPort.Status.READY_FOR_APPROVAL) {
                jdbc.getJdbcTemplate().update("""
                        UPDATE interurban.outbox SET delivered_at=CURRENT_TIMESTAMP,attempts=attempts+1
                        WHERE event_key=? AND event_type='PAYOUT_READY' AND delivered_at IS NULL
                        """, result.idempotencyKey());
            }
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverRouteSheetQuery.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public class DriverRouteSheetQuery {
    public enum StopType { PICKUP, DROPOFF }
    public record Stop(UUID tripId, UUID reservationId, StopType type, Instant scheduledAt,
            String passengerName, String address, String phone, String callUrl, String whatsappUrl, String status) {}
    public record RouteSheet(UUID driverId, LocalDate date, List<Stop> stops) {}
    private final DriverOperationsRepository repository;
    private final DriverIdentityPort identity;

    public DriverRouteSheetQuery(DriverOperationsRepository repository, DriverIdentityPort identity) {
        this.repository = repository;
        this.identity = identity;
    }

    @Transactional(readOnly = true)
    public RouteSheet find(LocalDate date, Authentication authentication) {
        UUID driverId = identity.requireDriver(authentication);
        if (date == null) throw new InvalidTripException();
        var stops = new ArrayList<Stop>();
        for (var passenger : repository.routeSheet(driverId, date)) {
            String phone = internationalPhone(passenger.phone());
            String tel = phone == null ? null : "tel:" + phone;
            String wa = phone == null ? null : "https://wa.me/" + phone.substring(1);
            stops.add(new Stop(passenger.tripId(), passenger.reservationId(), StopType.PICKUP, passenger.pickupAt(),
                    passenger.name(), passenger.pickupAddress(), phone, tel, wa, passenger.status()));
            stops.add(new Stop(passenger.tripId(), passenger.reservationId(), StopType.DROPOFF, passenger.dropoffAt(),
                    passenger.name(), passenger.dropoffAddress(), phone, tel, wa, passenger.status()));
        }
        stops.sort(Comparator.comparing(Stop::scheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Stop::type).thenComparing(Stop::tripId).thenComparing(Stop::reservationId));
        return new RouteSheet(driverId, date, List.copyOf(stops));
    }

    private String internationalPhone(String raw) {
        if (raw == null) return null;
        String clean = raw.replaceAll("[\\s().-]", "");
        // Conservar E.164 explícito (incluido 549); no convertir datos arbitrarios en URLs.
        if (clean.matches("\\+[1-9][0-9]{7,14}")) return clean;
        if (clean.matches("54[1-9][0-9]{9,10}")) return "+" + clean;
        try { return "+" + com.lunaris.ansenuza.shared.PhoneUtils.normalizeArgentinePhone(raw); }
        catch (RuntimeException e) { return null; }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/DriverSettlementService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DriverSettlementService {
    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Cordoba");
    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;
    public DriverSettlementService(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }
    public static LocalDate latestCutoff(Instant now) {
        var local = now.atZone(ZONE);
        return local.toLocalTime().isBefore(LocalTime.of(22, 0)) ? local.toLocalDate().minusDays(1) : local.toLocalDate();
    }
    private record Entry(UUID id, UUID driver, BigDecimal amount, BigDecimal fare, BigDecimal fee) {}

    @Transactional
    public int settle(LocalDate date) {
        Instant cutoff = date.atTime(22, 0).atZone(ZONE).toInstant();
        if (cutoff.isAfter(clock.instant())) throw new IllegalArgumentException("El corte todavía no ocurrió");
        // Lock transaccional compartido por todas las réplicas y fechas; evita reservas dobles del saldo.
        jdbc.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(742019220)");
        var entries = jdbc.query("""
                SELECT l.id,l.driver_id,l.amount,
                       CASE WHEN l.entry_type='EARNED' THEN r.fare ELSE 0 END AS fare,
                       CASE WHEN l.entry_type='EARNED' THEN r.commission ELSE 0 END AS fee
                FROM interurban.driver_ledger l JOIN interurban.reservations r ON r.id=l.reservation_id
                WHERE l.entry_type IN ('EARNED','REVERSAL') AND l.created_at<=:cutoff
                  AND NOT EXISTS(SELECT 1 FROM interurban.payout_items i WHERE i.ledger_id=l.id)
                ORDER BY l.driver_id,l.id FOR UPDATE OF l
                """, Map.of("cutoff", Timestamp.from(cutoff)), (rs, row) -> new Entry(
                rs.getObject("id", UUID.class), rs.getObject("driver_id", UUID.class), rs.getBigDecimal("amount"),
                rs.getBigDecimal("fare"), rs.getBigDecimal("fee")));
        Map<UUID, List<Entry>> drivers = new LinkedHashMap<>();
        entries.forEach(e -> drivers.computeIfAbsent(e.driver(), key -> new ArrayList<>()).add(e));
        int created = 0;
        for (var driver : drivers.entrySet()) {
            BigDecimal net = BigDecimal.ZERO, gross = BigDecimal.ZERO, fees = BigDecimal.ZERO;
            for (var entry : driver.getValue()) {
                net = net.add(entry.amount()); gross = gross.add(entry.fare()); fees = fees.add(entry.fee());
            }
            if (net.signum() <= 0) continue;
            UUID id = UUID.randomUUID();
            String key = "interurban:payout:" + driver.getKey() + ":" + date;
            var params = new HashMap<String, Object>();
            params.put("id", id); params.put("driver", driver.getKey()); params.put("date", date);
            params.put("net", net); params.put("gross", gross); params.put("fees", fees);
            params.put("adjustments", net.subtract(gross.subtract(fees))); params.put("key", key);
            if (jdbc.update("""
                    INSERT INTO interurban.payout_orders(id,driver_id,settlement_date,amount,status,idempotency_key,
                        gross_amount,commission_amount,adjustment_amount)
                    VALUES (:id,:driver,:date,:net,'PENDING',:key,:gross,:fees,:adjustments)
                    ON CONFLICT (driver_id,settlement_date) DO NOTHING
                    """, params) == 0) continue;
            for (var entry : driver.getValue()) jdbc.update("""
                    INSERT INTO interurban.payout_items(ledger_id,payout_order_id) VALUES (:ledger,:order)
                    """, Map.of("ledger", entry.id(), "order", id));
            jdbc.update("""
                    INSERT INTO interurban.outbox(id,event_key,event_type,aggregate_id,payload)
                    VALUES (:event,:key,'PAYOUT_READY',:order,'{}'::jsonb)
                    """, Map.of("event", UUID.randomUUID(), "key", key, "order", id));
            created++;
        }
        return created;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/ExistingDriverIdentityAdapter.java`

```java
package com.lunaris.ansenuza.service.interurban;

import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.infrastructure.config.UserPrincipal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/** Resuelve el chofer activo mediante la vinculación explícita con su cuenta. */
public class ExistingDriverIdentityAdapter implements DriverIdentityPort {
    private final DriverRepository drivers;

    public ExistingDriverIdentityAdapter(DriverRepository drivers) { this.drivers = drivers; }

    @Override public UUID requireDriver(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_CHOFER".equals(a.getAuthority())
                        || "ROLE_ADMIN".equals(a.getAuthority()))) throw denied();
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) throw denied();
        return drivers.findByAccountIdAndActiveTrue(principal.getAccountId())
                .map(driver -> driver.getId())
                .orElseThrow(this::denied);
    }

    private AccessDeniedException denied() { return new AccessDeniedException("No hay un chofer activo inequívoco para esta cuenta."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanCatalog.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/** Catálogo direccional: ordinal identifica un tramo, las paradas son sus extremos. */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true")
public class InterurbanCatalog {
    private final NamedParameterJdbcTemplate jdbc;

    public record Destination(int stop, String name, BigDecimal fare) {}

    @Transactional(readOnly = true)
    public List<String> origins() {
        return jdbc.queryForList("""
                WITH stops AS (
                    SELECT ordinal - 1 AS stop, origin AS name FROM interurban.corridor_legs
                    UNION SELECT ordinal AS stop, destination AS name FROM interurban.corridor_legs
                )
                SELECT DISTINCT s.name FROM stops s
                JOIN interurban.interurban_fares f ON f.origin_stop = s.stop
                WHERE f.valid_from <= :date ORDER BY s.name
                """, Map.of("date", LocalDate.now(com.lunaris.ansenuza.shared.ArgentinaTime.ZONE_ID)), String.class);
    }

    @Transactional(readOnly = true)
    public List<Destination> destinations(String origin) {
        return jdbc.query("""
                WITH stops AS (
                    SELECT ordinal - 1 AS stop, origin AS name FROM interurban.corridor_legs
                    UNION SELECT ordinal AS stop, destination AS name FROM interurban.corridor_legs
                ), current_fares AS (
                    SELECT DISTINCT ON (origin_stop, destination_stop) *
                    FROM interurban.interurban_fares WHERE valid_from <= :date
                    ORDER BY origin_stop, destination_stop, valid_from DESC
                )
                SELECT d.stop, d.name, f.fare FROM stops o
                JOIN current_fares f ON f.origin_stop = o.stop
                JOIN stops d ON d.stop = f.destination_stop
                WHERE upper(trim(o.name)) = upper(trim(:origin)) ORDER BY d.stop
                """, Map.of("origin", origin, "date", LocalDate.now(com.lunaris.ansenuza.shared.ArgentinaTime.ZONE_ID)),
                (rs, row) -> new Destination(rs.getInt("stop"), rs.getString("name"), rs.getBigDecimal("fare")));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanConfiguration.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanConfiguration {
    @Bean
    public CapacityRepository interurbanCapacityRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcCapacityRepository(jdbc);
    }

    @Bean
    public CapacityService interurbanCapacityService(CapacityRepository repository) {
        return new CapacityService(repository, Clock.systemUTC());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverConfiguration.java`

```java
package com.lunaris.ansenuza.service.interurban;

import com.lunaris.ansenuza.domain.repository.DriverRepository;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanDriverConfiguration {
    @Bean public DriverOperationsRepository interurbanDriverRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcDriverOperationsRepository(jdbc);
    }
    @Bean public DriverIdentityPort interurbanDriverIdentity(DriverRepository drivers) {
        return new ExistingDriverIdentityAdapter(drivers);
    }
    @Bean public CheckInService interurbanCheckIn(DriverOperationsRepository repository, DriverIdentityPort identity,
            @Value("${lunaris.interurban.driver.early-window:PT1H}") String early,
            @Value("${lunaris.interurban.driver.late-window:PT2H}") String late) {
        return new CheckInService(repository, identity, Clock.systemUTC(), Duration.parse(early), Duration.parse(late));
    }
    @Bean public DriverRouteSheetQuery interurbanRouteSheet(DriverOperationsRepository repository, DriverIdentityPort identity) {
        return new DriverRouteSheetQuery(repository, identity);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentConfiguration.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanPaymentConfiguration {
    @Bean public InterurbanPaymentRepository interurbanPaymentRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcInterurbanPaymentRepository(jdbc);
    }

    @Bean public QrArtifactCipher interurbanQrCipher(
            @Value("${lunaris.interurban.payments.qr-encryption-key}") String key,
            @Value("${lunaris.interurban.payments.qr-key-id}") String keyId) {
        return new QrArtifactCipher(key, keyId);
    }

    @Bean public QrGeneratorService interurbanQrGenerator(InterurbanPaymentRepository repository, QrArtifactCipher cipher) {
        return new QrGeneratorService(repository, cipher, Clock.systemUTC());
    }

    @Bean public MercadoPagoSignatureValidator interurbanPaymentSignatures(
            @Value("${lunaris.interurban.payments.webhook-secret}") String secret,
            @Value("${lunaris.interurban.payments.signature-max-age:PT24H}") String maxAge) {
        return new MercadoPagoSignatureValidator(secret, Clock.systemUTC(), Duration.parse(maxAge));
    }

    @Bean public PaymentQueryPort interurbanPaymentQuery(
            @Value("${lunaris.interurban.payments.access-token}") String accessToken) {
        if (accessToken.isBlank()) throw new IllegalArgumentException("Access token MP obligatorio.");
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return new MercadoPagoPaymentClient(RestClient.builder().baseUrl("https://api.mercadopago.com")
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + accessToken).build());
    }

    @Bean public MercadoPagoWebhookService interurbanPaymentWebhook(MercadoPagoSignatureValidator signatures,
            PaymentQueryPort payments, InterurbanPaymentRepository repository, QrGeneratorService qr,
            PlatformTransactionManager transactions,
            @Value("${lunaris.interurban.payments.collector-id}") String collectorId,
            @Value("${lunaris.interurban.payments.live-mode:true}") boolean liveMode) {
        if (!collectorId.matches("[0-9]+")) throw new IllegalArgumentException("Collector ID MP obligatorio.");
        return new MercadoPagoWebhookService(signatures, payments, repository, qr, transactions,
                collectorId, liveMode, Clock.systemUTC());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentException.java`

```java
package com.lunaris.ansenuza.service.interurban;

/** Mensajes sanitizados: nunca incluir credenciales, tokens ni respuesta del proveedor. */
public class InterurbanPaymentException extends RuntimeException {
    public enum Code { INVALID_SIGNATURE, INVALID_NOTIFICATION, PROVIDER_UNAVAILABLE, QR_FAILURE }
    private final Code code;

    public InterurbanPaymentException(Code code) {
        super(code.name());
        this.code = code;
    }

    public Code code() { return code; }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanSettlementConfiguration.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.settlement.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanSettlementConfiguration {
    @Bean DriverSettlementService driverSettlementService(NamedParameterJdbcTemplate jdbc) {
        return new DriverSettlementService(jdbc, Clock.systemUTC());
    }
    @Bean DriverPayoutPort driverPayoutPort() { return new SimulatedDriverPayoutAdapter(); }
    @Bean DriverPayoutReviewService driverPayoutReviewService(NamedParameterJdbcTemplate jdbc, DriverPayoutPort port) {
        return new DriverPayoutReviewService(jdbc, port);
    }
    @Bean DailyDriverPayoutJob dailyDriverPayoutJob(DriverSettlementService service, DriverPayoutReviewService review) {
        return new DailyDriverPayoutJob(service, review, Clock.systemUTC());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidHoldException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class InvalidHoldException extends RuntimeException {
    public InvalidHoldException(String message) { super(message); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidQrException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class InvalidQrException extends RuntimeException {
    public InvalidQrException() { super("Formato de pase inválido."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidRouteException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class InvalidRouteException extends RuntimeException {
    public InvalidRouteException(String message) { super(message); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InvalidTripException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class InvalidTripException extends RuntimeException {
    public InvalidTripException() { super("El pase no está habilitado para este viaje y horario."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/PassengerHold.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;
import java.util.UUID;

/** Datos ya cotizados por el caso de uso de booking; un registro por pasajero. */
public record PassengerHold(UUID bookingId, UUID fareId, String name, String phone,
        String pickupAddress, String dropoffAddress, BigDecimal fare, BigDecimal commission) {
    public PassengerHold {
        if (bookingId == null || fareId == null || invalid(name, 150) || invalid(phone, 30)
                || invalid(pickupAddress, 255) || invalid(dropoffAddress, 255)
                || invalidMoney(fare) || invalidMoney(commission) || commission.compareTo(fare) > 0) {
            throw new InvalidHoldException("Datos de pasajero o cotización inválidos.");
        }
    }

    private static boolean invalid(String value, int max) {
        return value == null || value.isBlank() || value.length() > max;
    }

    private static boolean invalidMoney(BigDecimal value) {
        return value == null || value.signum() < 0 || value.stripTrailingZeros().scale() > 2
                || value.compareTo(new BigDecimal("9999999999.99")) > 0;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/PaymentQueryPort.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.math.BigDecimal;

public interface PaymentQueryPort {
    record Payment(String id, String status, String externalReference, BigDecimal amount,
            String currency, String collectorId, boolean liveMode, BigDecimal refundedAmount) {}
    Payment getPayment(String paymentId);
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/QrAlreadyConsumedException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class QrAlreadyConsumedException extends RuntimeException {
    public QrAlreadyConsumedException() { super("El pase ya fue utilizado."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/QrArtifactCipher.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Formato: nonce de 12 bytes || PNG cifrado || tag GCM. AAD = reservationId. */
public class QrArtifactCipher {
    private final SecretKeySpec key;
    private final String keyId;
    private final SecureRandom random = new SecureRandom();

    public QrArtifactCipher(String base64Key, String keyId) {
        byte[] bytes = Base64.getDecoder().decode(base64Key);
        if (bytes.length != 32 || keyId == null || !keyId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("QR requiere clave AES de 256 bits y key-id.");
        }
        this.key = new SecretKeySpec(bytes, "AES");
        this.keyId = keyId;
    }

    public String keyId() { return keyId; }

    public byte[] encrypt(UUID reservationId, byte[] png) {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            cipher.updateAAD(reservationId.toString().getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(png);
            return ByteBuffer.allocate(nonce.length + ciphertext.length).put(nonce).put(ciphertext).array();
        } catch (Exception e) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/QrExpiredException.java`

```java
package com.lunaris.ansenuza.service.interurban;

public class QrExpiredException extends RuntimeException {
    public QrExpiredException() { super("El pase está vencido o revocado."); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/QrGeneratorService.java`

```java
package com.lunaris.ansenuza.service.interurban;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class QrGeneratorService {
    private final InterurbanPaymentRepository repository;
    private final QrArtifactCipher cipher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public QrGeneratorService(InterurbanPaymentRepository repository, QrArtifactCipher cipher, Clock clock) {
        this.repository = repository;
        this.cipher = cipher;
        this.clock = clock;
    }

    /** Debe participar de la transacción de confirmación; nunca genera pases para HELD. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void generate(UUID reservationId, Instant expiresAt) {
        if (!repository.lockPaidReservation(reservationId)) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
        if (repository.hasQr(reservationId)) return;
        if (expiresAt == null || !expiresAt.isAfter(clock.instant())) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
        try {
            byte[] entropy = new byte[32];
            random.nextBytes(entropy);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
            var matrix = new QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, 320, 320);
            var png = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", png);
            repository.saveQr(reservationId, hash, expiresAt, cipher.encrypt(reservationId, png.toByteArray()), cipher.keyId());
        } catch (InterurbanPaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/Route.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.List;
import java.util.stream.IntStream;

public record Route(int origin, int destination) {
    public Route {
        if (origin < 0 || origin > 3 || destination < 0 || destination > 3 || origin == destination) {
            throw new InvalidRouteException("Origen y destino deben ser paradas distintas entre 0 y 3.");
        }
    }

    public List<Integer> ordinals() {
        return IntStream.rangeClosed(Math.min(origin, destination) + 1,
                Math.max(origin, destination)).boxed().toList();
    }

    public int direction() { return Integer.signum(destination - origin); }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/shared/ArgentinaTime.java`

```java
package com.lunaris.ansenuza.shared;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ArgentinaTime {

    public static final ZoneId ZONE_ID = ZoneId.of("America/Argentina/Cordoba");
    private static final Clock CLOCK = Clock.system(ZONE_ID);

    private ArgentinaTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(CLOCK);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(CLOCK);
    }

    public static LocalTime currentTime() {
        return LocalTime.now(CLOCK);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/shared/PhoneUtils.java`

```java
package com.lunaris.ansenuza.shared;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;

/** Normaliza teléfonos argentinos al formato internacional requerido por WhatsApp. */
public final class PhoneUtils {

    private static final String ARGENTINA_COUNTRY_CODE = "54";

    private PhoneUtils() {
    }

    public static String normalizeArgentinePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new DomainValidationException("El teléfono es obligatorio.");
        }

        String nationalNumber = rawPhone.replaceAll("\\D", "").replaceFirst("^0+", "");
        if (nationalNumber.startsWith(ARGENTINA_COUNTRY_CODE)) {
            nationalNumber = nationalNumber.substring(ARGENTINA_COUNTRY_CODE.length());
        }
        nationalNumber = nationalNumber.replaceFirst("^0+", "");
        if (nationalNumber.startsWith("9")) {
            nationalNumber = nationalNumber.substring(1);
        }
        nationalNumber = nationalNumber.replaceFirst("^0+", "");
        if (nationalNumber.startsWith("15")) {
            nationalNumber = nationalNumber.substring(2);
        } else if (nationalNumber.length() == 12) {
            nationalNumber = removeLegacyMobilePrefix(nationalNumber);
        }

        if (!nationalNumber.matches("[1-9][0-9]{9}")) {
            throw new DomainValidationException("El teléfono no es válido.");
        }
        return ARGENTINA_COUNTRY_CODE + nationalNumber;
    }

    private static String removeLegacyMobilePrefix(String number) {
        for (int areaCodeLength = 2; areaCodeLength <= 4; areaCodeLength++) {
            if (number.startsWith("15", areaCodeLength)) {
                return number.substring(0, areaCodeLength) + number.substring(areaCodeLength + 2);
            }
        }
        return number;
    }
}
```
