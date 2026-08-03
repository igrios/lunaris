package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;

class WhatsAppTemplateConfigurationTest {

    @Test
    void resolvesLanguagePerApprovedTemplate() {
        assertEquals("en", WhatsAppService.templateLanguageFor("despierta_chofer"));
        assertEquals("en", WhatsAppService.templateLanguageFor("proximo_en_camino"));
        assertEquals("es", WhatsAppService.templateLanguageFor("chofer_asignado"));
        assertEquals("es", WhatsAppService.templateLanguageFor("contacto_pasajero"));
    }

    @Test
    void treatsPendingOrMissingTemplatesAsNonBlocking() {
        assertTrue(WhatsAppService.isTemplateUnavailable(
                "TEMPLATE PROXIMO_EN_CAMINO", 400, """
                        {"error":{"code":132001,"message":"Template does not exist"}}
                        """));
        assertTrue(WhatsAppService.isTemplateUnavailable(
                "TEMPLATE DESPIERTA CHOFER", 404, "Not found"));
        assertFalse(WhatsAppService.isTemplateUnavailable("TEXTO", 404, "Not found"));
    }

    @Test
    void nextPassengerTemplateUsesExactlyTwoApprovedParameters() {
        var parameters = WhatsAppService.proximoEnCaminoParameters("Ana", "Juan");

        assertEquals(2, parameters.size());
        assertEquals("Ana", parameters.get(0));
        assertEquals("Juan", parameters.get(1));
    }

    @Test
    void formatsArgentinePhoneNumbersForMetaCloudApi() {
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("+54 9 351-555-1234"));
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("351 555-1234"));
        assertEquals("5493515551234",
                WhatsAppService.formatMetaPhoneNumber("+54 351-555-1234"));
    }

    @Test
    void cleansButDoesNotRewriteOtherInternationalPhoneNumbers() {
        assertEquals("59899123456",
                WhatsAppService.formatMetaPhoneNumber("+598 99 123-456"));
        assertEquals("", WhatsAppService.formatMetaPhoneNumber(null));
    }

    @Test
    void driverAssignmentContactIncludesNameAndSanitizedArgentinePhone() {
        String message = WhatsAppService.buildDriverAssignmentContactMessage(
                "Carlos Gómez", "+54 351-555-1234", "+54 351-111-1111");

        assertTrue(message.contains("Auto Lunaris asignado"));
        assertTrue(message.contains("Chofer: Carlos Gómez"));
        assertTrue(message.contains("Contacto: +5493515551234"));
        assertFalse(message.contains("3511111111"));
    }

    @Test
    void driverAssignmentContactFallsBackToLunarisSupport() {
        String configuredSupport = WhatsAppService.buildDriverAssignmentContactMessage(
                "Carlos Gómez", null, "+54 351-111-1111");
        String chatSupport = WhatsAppService.buildDriverAssignmentContactMessage(
                "Carlos Gómez", " ", " ");

        assertTrue(configuredSupport.contains("Contacto: +5493511111111"));
        assertTrue(chatSupport.contains("Contacto: WhatsApp de Lunaris (este chat)"));
    }

    @Test
    void driverRouteSheetUrlContainsDatabaseUuidAndTravelDate() {
        UUID driverId = UUID.fromString("917d74d2-d5de-4ec7-a674-d0d0fb52f99c");

        assertEquals(
                "https://lunaris-backend-nn6s.onrender.com/hoja-ruta"
                        + "?driverId=917d74d2-d5de-4ec7-a674-d0d0fb52f99c&date=2026-08-05",
                WhatsAppService.buildDriverRouteSheetUrl(
                        driverId, LocalDate.of(2026, 8, 5)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void driverTemplateUsesQuickReplyPayloadAtButtonIndexZero() {
        UUID driverId = UUID.fromString("917d74d2-d5de-4ec7-a674-d0d0fb52f99c");
        Map<String, Object> body = Map.of("type", "body", "parameters", List.of());

        var components = WhatsAppService.despiertaChoferComponents(
                body, driverId, LocalDate.of(2026, 8, 5));
        Map<String, Object> button = components.get(1);
        Map<String, Object> parameter =
                ((List<Map<String, Object>>) button.get("parameters")).getFirst();

        assertEquals("button", button.get("type"));
        assertEquals("quick_reply", button.get("sub_type"));
        assertEquals("0", button.get("index"));
        assertEquals("payload", parameter.get("type"));
        assertEquals(
                WhatsAppService.buildDriverRouteSheetUrl(
                        driverId, LocalDate.of(2026, 8, 5)),
                parameter.get("payload"));
        assertFalse(parameter.containsKey("text"));
    }

    @Test
    void driverDispatchSummaryContainsAllPassengerDetailsAndOnlyNavigationLink() {
        Reservation reservation = Reservation.builder()
                .id(UUID.fromString("5ca1ab1e-6806-4a50-94e3-3785b4bf5b68"))
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .phone("351 555-1234")
                        .address("https://maps.google.com/?q=-31.1,-62.1")
                        .build())
                .pickupAddress("San Martín 123")
                .passengerCount(2)
                .companionNames("Juan Pérez")
                .build();
        String navigationUrl = "https://www.google.com/maps/dir/?api=1"
                + "&origin=San%20Mart%C3%ADn%20123&destination=Cordoba";

        String summary = WhatsAppService.buildDriverPassengerSummary(
                "Carlos", navigationUrl, List.of(reservation));

        assertTrue(summary.contains("Ana Pérez"));
        assertTrue(summary.contains("San Martín 123"));
        assertTrue(summary.contains("351 555-1234"));
        assertTrue(summary.contains("2 asiento(s)"));
        assertTrue(summary.contains("Acompañantes: Juan Pérez"));
        assertTrue(summary.contains(navigationUrl));
        assertFalse(summary.contains("/hoja-ruta"));
        assertFalse(summary.contains("lunaris-backend"));
    }

    @Test
    void driverDispatchFallsBackToPassengerMapLocation() {
        Reservation reservation = Reservation.builder()
                .passenger(Passenger.builder()
                        .firstName("Ana")
                        .lastName("Pérez")
                        .address("https://maps.google.com/?q=-31.1,-62.1")
                        .build())
                .build();

        String summary = WhatsAppService.buildDriverPassengerSummary(
                "Carlos", "https://www.google.com/maps/dir/?api=1", List.of(reservation));

        assertTrue(summary.contains("https://maps.google.com/?q=-31.1,-62.1"));
    }

    @Test
    void driverDispatchNormalizesPhoneAndFallsBackToTextWhenMetaRejectsInteractiveList() {
        FailingInteractiveWhatsAppService service = new FailingInteractiveWhatsAppService();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .travelDate(LocalDate.of(2026, 8, 5))
                .departureSchedule("08:00")
                .status("CONFIRMED")
                .passengerCount(1)
                .reservationCode("SAN-COR-001")
                .build();

        service.sendDriverRouteDispatch("+54 351 555-1234", "Carlos",
                "https://maps.example/route", List.of(reservation));

        assertEquals("5493515551234", service.interactivePhone);
        assertEquals(1, service.textMessages.size());
        assertTrue(service.textMessages.getFirst().contains("No pudimos habilitar los botones"));
    }

    @Test
    void driverDispatchIncludesConfirmedReservationWithoutDepartureSchedule() {
        FailingInteractiveWhatsAppService service = new FailingInteractiveWhatsAppService();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .travelDate(LocalDate.of(2026, 8, 5))
                .departureSchedule(null)
                .status("CONFIRMED")
                .reservationCode("SAN-COR-002")
                .build();

        service.sendDriverRouteDispatch("3515551234", "Carlos",
                "https://maps.example/route", List.of(reservation));

        assertTrue(service.textMessages.getFirst().contains("Ana Pérez"));
        assertTrue(service.textMessages.getFirst().contains("1 asiento(s)"));
    }

    @Test
    void driverDispatchSendsApprovedTemplateBeforeInteractiveList() {
        RecordingRouteWhatsAppService service = new RecordingRouteWhatsAppService();
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .driver(driver)
                .passenger(Passenger.builder().firstName("Ana").lastName("Pérez").build())
                .travelDate(LocalDate.of(2026, 8, 5))
                .status("CONFIRMED")
                .reservationCode("SAN-COR-003")
                .build();

        var result = service.sendDriverRouteDispatch("+54 351 555-1234", "Carlos",
                "https://maps.example/route", List.of(reservation));

        assertEquals(List.of("template:5493515551234", "interactive:5493515551234"),
                service.events);
        assertTrue(result.success());
    }

    private static final class FailingInteractiveWhatsAppService extends WhatsAppService {
        private final List<String> textMessages = new ArrayList<>();
        private String interactivePhone;

        @Override
        boolean trySendMessage(String phoneNumber, String message) {
            assertEquals("5493515551234", phoneNumber);
            textMessages.add(message);
            return true;
        }

        @Override
        boolean trySendInteractiveList(String phoneNumber, String headerText, String bodyText,
                String buttonLabel, List<Map<String, Object>> sections) {
            interactivePhone = phoneNumber;
            return false;
        }
    }

    private static final class RecordingRouteWhatsAppService extends WhatsAppService {
        private final List<String> events = new ArrayList<>();

        @Override
        boolean trySendDriverRouteTemplate(String phoneNumber, String driverName,
                UUID driverId, LocalDate travelDate) {
            events.add("template:" + phoneNumber);
            return true;
        }

        @Override
        boolean trySendInteractiveList(String phoneNumber, String headerText, String bodyText,
                String buttonLabel, List<Map<String, Object>> sections) {
            events.add("interactive:" + phoneNumber);
            return true;
        }

        @Override
        boolean trySendMessage(String phoneNumber, String message) {
            events.add("text:" + phoneNumber);
            return true;
        }
    }
}
