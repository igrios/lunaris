package com.lunaris.ansenuza.infrastructure.whatsapp;

import com.lunaris.ansenuza.application.port.Button;
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

    public void reset(String phone) {
        messages.remove(normalize(phone));
    }

    public String normalize(String phone) {
        return formatMetaPhoneNumber(phone);
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
