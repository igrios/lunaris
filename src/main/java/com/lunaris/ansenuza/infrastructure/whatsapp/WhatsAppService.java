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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.application.port.Button;
import com.lunaris.ansenuza.application.port.MessagingPort;
import lombok.extern.slf4j.Slf4j;

@Service
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

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${whatsapp.phone-number-id}")
    private String whatsappPhoneNumberId;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
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
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", List.of(
                                        Map.of("type", "text", "text",
                                                safeTemplateValue(passengerName, "Pasajero")),
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
    public void requestLocation(String to, String message) {
        sendLocationRequest(to, message);
    }

    @Override
    public void sendImage(String to, String imageUrl, String caption) {
        sendImageMessage(to, imageUrl, caption);
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
        String url = "https://graph.facebook.com/v25.0/" + phoneNumberId + "/messages";
        HttpHeaders headers = createHeaders();

        try {
            List<Map<String, Object>> buttonObjects = new ArrayList<>();
            for (Map<String, String> btn : buttons) {
                buttonObjects.add(Map.of(
                    "type", "reply",
                    "reply", Map.of("id", btn.get("id"), "title", btn.get("title"))
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

            return executePostCall(url, headers, body, "BOTONES INTERACTIVOS");
        } catch (Exception e) {
            log.error("Error en botones interactivos: ", e);
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
            executePostCall(url, createHeaders(), body, "DOCUMENTO_URL");
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
        log.info("Éxito Meta [{}]: Envío hacia {}. Status: {}",
                messageType, destination, response.getStatusCode());
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
    return value.length() <= maxLength ? value : value.substring(0, maxLength - 1) + "…";
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
        sendTemplate(to, "contacto_pasajero", List.of(safeTemplateValue(passengerName, "Pasajero")));
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
                createHeaders(), body, "TEMPLATE " + templateName.toUpperCase());
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
