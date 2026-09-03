package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.DriverRouteService;
import com.lunaris.ansenuza.domain.model.service.FleetCapacityService;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.application.usecase.DriverAuthorizationService;
import com.lunaris.ansenuza.application.conversation.GoogleMapsParameterFormatter;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.model.service.AirportTripDetector;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.EnviarHojaRutaRequest;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaViewController {

    private static final List<String> OUTBOUND_SCHEDULES = List.of("03:00", "08:00");
    private static final List<String> RETURN_SCHEDULES =
            List.of("12:00", "14:00", "16:00", "17:30");

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final DriverRepository driverRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final DriverRouteService driverRouteService;
    private final FleetCapacityService fleetCapacityService;
    private final WaitingListRepository waitingListRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ResolveEffectiveTripOriginUseCase resolveEffectiveTripOriginUseCase;
    private final DriverAuthorizationService driverAuthorizationService;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${lunaris.trips.capacity:12}")
    private int vehicleCapacity = 4;

    // 📅 1. Vista resumen de los próximos 7 días (Semana Completa) BLINDADA CONTRA VUELTAS ABIERTAS Y CANCELADOS
    @GetMapping("/agenda")
    public String agenda(
            @RequestParam(defaultValue = "7") int days,
            Model model) {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);
        int displayedDays = Math.max(7, Math.min(days, 56));
        java.util.Map<LocalDate, List<Reservation>> reservationsByDate =
                reservationRepository.findAgendaBetween(
                                today, today.plusDays(displayedDays - 1L)).stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                Reservation::getTravelDate));

        List<AgendaDayView> agenda =
                java.util.stream.IntStream.range(0, displayedDays)
                        .mapToObj(today::plusDays).map(date -> {
                    List<Reservation> reservations = reservationsByDate.getOrDefault(date, List.of());

                    // 🌟 FILTRO: Excluimos registros con fecha centinela, CANCELLED o passengerCount <= 0
                    List<Reservation> activeReservations = reservations.stream()
                            .filter(r -> r != null)
                            .filter(r -> r.getTravelDate() == null || !r.getTravelDate().equals(fechaCentinela))
                            .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                            .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                            .toList();

                    int totalPassengers = countDistinctBookingSeats(activeReservations);
                    int confirmedPassengers = countDistinctBookingSeats(activeReservations.stream()
                            .filter(r -> Boolean.TRUE.equals(r.getPaymentVerified())
                                    && "CONFIRMED".equals(r.getStatus()))
                            .toList());
                    int waitingListPassengers = Math.toIntExact(
                            waitingListRepository.sumPassengerCountByTravelDateAndStatus(
                                    date, WaitingListEntry.WAITING));
                    int maxCapacity = systemConfigurationService.getScheduleMaxCapacity();

                    int pendingPayments = countDistinctBookings(activeReservations.stream()
                            .filter(r -> !Boolean.TRUE.equals(r.getPaymentVerified())).toList());

                    int safeVehicleCapacity = Math.max(vehicleCapacity, 1);
                    int estimatedVehicles = totalPassengers == 0
                            ? 0
                            : (int) Math.ceil((double) totalPassengers / safeVehicleCapacity);
                    int paidReservations = countDistinctBookings(activeReservations.stream()
                            .filter(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))
                            .toList());
                    java.math.BigDecimal totalCollected = activeReservations.stream()
                            .filter(reservation ->
                                    Boolean.TRUE.equals(reservation.getPaymentVerified()))
                            .map(reservation -> {
                                java.math.BigDecimal amount = reservation.getAmount() == null
                                        ? java.math.BigDecimal.ZERO
                                        : reservation.getAmount();
                                java.math.BigDecimal extra =
                                        reservation.getExtraAmount() == null
                                            ? java.math.BigDecimal.ZERO
                                            : reservation.getExtraAmount();
                                return amount.add(extra);
                            })
                            .reduce(
                                    java.math.BigDecimal.ZERO,
                                    java.math.BigDecimal::add);

                    UUID assignedDriverId = activeReservations.stream()
                            .map(Reservation::getDriver)
                            .filter(java.util.Objects::nonNull)
                            .map(Driver::getId)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    return new AgendaDayView(
                            date,
                            totalPassengers,
                            confirmedPassengers,
                            waitingListPassengers,
                            confirmedPassengers > maxCapacity,
                            pendingPayments,
                            estimatedVehicles,
                            safeVehicleCapacity,
                            estimatedVehicles * safeVehicleCapacity,
                            totalCollected,
                            paidReservations,
                            assignedDriverId);
                }).toList();

        model.addAttribute("agenda", agenda);
        model.addAttribute("displayedDays", displayedDays);
        return "agenda";
    }

    static int countDistinctBookingSeats(List<Reservation> reservations) {
        return reservations.stream().collect(java.util.stream.Collectors.toMap(
                        AgendaViewController::bookingGroupKey,
                        Reservation::getTotalSeats,
                        Math::max))
                .values().stream().mapToInt(Integer::intValue).sum();
    }

    static int countDistinctBookings(List<Reservation> reservations) {
        return (int) reservations.stream().map(AgendaViewController::bookingGroupKey).distinct().count();
    }

    private static String bookingGroupKey(Reservation reservation) {
        String code = reservation.getReservationCode();
        if (code != null && !code.isBlank()) {
            return code.replaceFirst("-(IDA|VUELTA)$", "");
        }
        if (Boolean.TRUE.equals(reservation.getRoundTrip()) && reservation.getPassenger() != null
                && reservation.getPassenger().getId() != null) {
            return "ROUND_TRIP_PASSENGER:" + reservation.getPassenger().getId();
        }
        return "RESERVATION:" + reservation.getId();
    }

    
    // 🚐 2. Vista detalle del día (Excluye Pasajeros Fantasma / Cancelados / Conteo <= 0)
    @GetMapping("/agenda/view-detalle")
    public String dayAgenda(
            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE,
                    fallbackPatterns = {"dd/MM/yyyy", "d/M/yy"})
            LocalDate date,
            @RequestParam(value = "schedule", defaultValue = "03:00") String schedule,
            @RequestParam(value = "direction", defaultValue = "OUTBOUND") RouteDirection direction,
            Model model) {

        List<Reservation> reservations = reservationRepository.findActiveManifest(
                date, schedule, direction == RouteDirection.RETURN);
        List<Driver> choferes = driverRepository.findByActiveTrue();

        List<Reservation> activeReservations = reservations.stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                .sorted(dispatchedLastComparator())
                .toList();

        model.addAttribute("date", date);
        model.addAttribute("reservations", activeReservations);
        model.addAttribute("choferes", choferes);
        model.addAttribute("selectedSchedule", schedule);
        model.addAttribute("selectedDirection", direction);
        model.addAttribute("outboundSchedules", OUTBOUND_SCHEDULES);
        model.addAttribute("returnSchedules", RETURN_SCHEDULES);
        model.addAttribute("specialReservationIds", activeReservations.stream()
                .filter(AgendaViewController::isSpecialTrip)
                .map(Reservation::getId)
                .collect(java.util.stream.Collectors.toSet()));
        model.addAttribute(
                "pickupAddressTexts",
                activeReservations.stream().collect(java.util.stream.Collectors.toMap(
                        Reservation::getId,
                        reservation -> resolvePickupAddress(reservation).text(),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new)));
        model.addAttribute(
                "pickupMapUrls",
                activeReservations.stream().collect(java.util.stream.Collectors.toMap(
                        Reservation::getId,
                        reservation -> resolvePickupAddress(reservation).mapUrl(),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new)));
        int occupiedSeats = activeReservations.stream()
                .mapToInt(Reservation::getTotalSeats)
                .sum();
        FleetCapacityService.FleetSummary fleetSummary =
                fleetCapacityService.calculate(occupiedSeats);
        int safeVehicleCapacity = Math.max(vehicleCapacity, 1);
        int estimatedVehicles = occupiedSeats == 0
                ? 0
                : (int) Math.ceil((double) occupiedSeats / safeVehicleCapacity);
        java.math.BigDecimal totalAmount = sumMoney(
                activeReservations, Reservation::getAmount);
        java.math.BigDecimal totalExtraAmount = sumMoney(
                activeReservations, Reservation::getExtraAmount);
        java.math.BigDecimal totalDiscount = sumMoney(
                activeReservations, Reservation::getDiscountAmount);
        model.addAttribute("occupiedSeats", occupiedSeats);
        model.addAttribute("vehicleCapacity", safeVehicleCapacity);
        model.addAttribute("estimatedVehicles", estimatedVehicles);
        model.addAttribute("plannedCapacity", estimatedVehicles * safeVehicleCapacity);
        int plannedCapacity = estimatedVehicles * safeVehicleCapacity;
        model.addAttribute(
                "occupancyPercentage",
                plannedCapacity == 0
                        ? 0
                        : (int) Math.round(occupiedSeats * 100.0 / plannedCapacity));
        model.addAttribute(
                "requiresAdditionalVehicle",
                occupiedSeats > safeVehicleCapacity);
        model.addAttribute("ownFleetCapacity", FleetCapacityService.OWN_FLEET_CAPACITY);
        model.addAttribute("internalPassengers", fleetSummary.internalPassengers());
        model.addAttribute("externalPassengers", fleetSummary.externalPassengers());
        model.addAttribute("externalVehicles", fleetSummary.externalVehicles());
        model.addAttribute(
                "requiresExternalReinforcement",
                fleetSummary.requiresExternalReinforcement());
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalExtraAmount", totalExtraAmount);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute(
                "netBalance",
                totalAmount.add(totalExtraAmount));
        java.math.BigDecimal totalRevenue = activeReservations.stream()
                .filter(reservation ->
                        Boolean.TRUE.equals(reservation.getPaymentVerified()))
                .map(reservation -> {
                    java.math.BigDecimal amount = reservation.getAmount() == null
                            ? java.math.BigDecimal.ZERO
                            : reservation.getAmount();
                    java.math.BigDecimal extra = reservation.getExtraAmount() == null
                            ? java.math.BigDecimal.ZERO
                            : reservation.getExtraAmount();
                    return amount.add(extra);
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute(
                "externalDriverExpense",
                fleetSummary.externalDriverExpense());
        model.addAttribute(
                "netRevenue",
                totalRevenue.subtract(fleetSummary.externalDriverExpense()));

        return "agenda-day";
    }

    static boolean isSpecialTrip(Reservation reservation) {
        boolean pendingWithoutPrice = (reservation.getAmount() == null
                || reservation.getAmount().signum() == 0)
                && "PENDING".equalsIgnoreCase(reservation.getStatus());
        return AirportTripDetector.isAirportTrip(
                reservation.getPickupLocality(), reservation.getDestination())
                || pendingWithoutPrice;
    }

    static PickupAddressDisplay resolvePickupAddress(Reservation reservation) {
        String reservationAddress = trimToNull(reservation.getPickupAddress());
        String passengerAddress = reservation.getPassenger() == null
                ? null
                : trimToNull(reservation.getPassenger().getAddress());
        String text = firstMatching(false, reservationAddress, passengerAddress);
        String mapUrl = firstMatching(true, reservationAddress, passengerAddress);
        return new PickupAddressDisplay(
                text == null ? "Sin dirección registrada" : text,
                mapUrl == null ? "" : mapUrl);
    }

    private static String firstMatching(
            boolean mapLocation, String... candidates) {
        return java.util.Arrays.stream(candidates)
                .filter(java.util.Objects::nonNull)
                .filter(candidate -> isMapLocation(candidate) == mapLocation)
                .findFirst()
                .orElse(null);
    }

    private static boolean isMapLocation(String value) {
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        return normalized.startsWith("https://maps.google.")
                || normalized.startsWith("https://www.google.")
                        && normalized.contains("/maps")
                || normalized.startsWith("https://maps.app.")
                || normalized.startsWith("https://waze.com/")
                || normalized.startsWith("https://www.waze.com/");
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    record PickupAddressDisplay(String text, String mapUrl) {
    }

    private java.math.BigDecimal sumMoney(
            List<Reservation> reservations,
            java.util.function.Function<Reservation, java.math.BigDecimal> extractor) {
        return reservations.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // 💳 3. Confirmación asíncrona de pago
    @PostMapping("/api/agenda/confirmar-pago/{id}")
    @ResponseBody
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@PathVariable UUID id) {
        try {
            Reservation reservation = confirmPaymentUseCase.execute(id);
            List<UUID> synchronizedReservationIds = linkedReservations(reservation).stream()
                    .map(Reservation::getId)
                    .toList();

            return ResponseEntity.ok(new PaymentVerificationResponse(
                    synchronizedReservationIds, true, "CONFIRMED"));
        } catch (IllegalArgumentException | DomainValidationException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).build();
        }
    }

    private List<Reservation> linkedReservations(Reservation reservation) {
        String reservationCode = reservation.getReservationCode();
        if (reservationCode == null || reservationCode.isBlank()) {
            return List.of(reservation);
        }
        String groupCode = reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
        List<Reservation> linked = reservationRepository.findReservationGroup(groupCode);
        return linked.isEmpty() ? List.of(reservation) : linked;
    }

    // 📄 4. Descarga del comprobante
    @GetMapping("/api/agenda/comprobante-descarga/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || !receiptUrl.contains("v20.0/")) {
            return ResponseEntity.notFound().build();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse =
                    restTemplate.exchange(receiptUrl, HttpMethod.GET, entity, JsonNode.class);

            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse =
                    restTemplate.exchange(actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                    .body(imageResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 🚖 5. Envío de Hoja de Ruta AL CHOFER CORREGIDO CON PLANTILLA Y ASIGNACIÓN
    @PostMapping("/api/agenda/enviar-hoja-ruta")
    public ResponseEntity<?> enviarHojaRuta(@RequestBody EnviarHojaRutaRequest request) {
        String choferPhone = request.phone();
        List<UUID> reservationIds = request.reservationIds();

        if (reservationIds == null || reservationIds.isEmpty()
                || request.driverId() == null && (choferPhone == null || choferPhone.isBlank())) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Seleccioná un chofer y al menos una reserva."));
        }

        // El UUID del dropdown es la identidad canónica; el teléfono queda como compatibilidad.
        String normalizedPhone = normalizeWhatsAppNumber(choferPhone);
        String lookupPhone = normalizedPhone;
        java.util.Optional<Driver> driverOpt = request.driverId() == null
                ? java.util.Optional.empty()
                : driverRepository.findById(request.driverId());
        if (driverOpt.isEmpty() && !normalizedPhone.isBlank()) {
            driverOpt = driverRepository.findFirstByPhone(normalizedPhone);
        }
        if (driverOpt.isEmpty()) {
            List<Driver> allDrivers = driverRepository.findAll();
            driverOpt = allDrivers.stream()
                    .filter(d -> normalizeWhatsAppNumber(d.getPhone()).equals(lookupPhone))
                    .findFirst();
        }

        if (driverOpt.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn("No se encontró chofer con el teléfono: {}", choferPhone);
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "message", "No se encontró el chofer seleccionado."));
        }

        Driver driver = driverOpt.get();
        normalizedPhone = normalizeWhatsAppNumber(driver.getPhone());

        Reservation firstReservation = reservationRepository.findById(reservationIds.get(0)).orElse(null);
        if (firstReservation == null || firstReservation.getTravelDate() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Las reservas seleccionadas no tienen una fecha válida."));
        }
        String assignedSchedule = firstReservation.getDepartureSchedule() == null
                || firstReservation.getDepartureSchedule().isBlank()
                ? "03:00" : firstReservation.getDepartureSchedule();
        RouteOriginResolution dispatchOrigin = resolveEffectiveTripOriginUseCase.resolve(
                firstReservation.getTravelDate(), assignedSchedule);
        List<Reservation> routeReservations;
        try {
            routeReservations = driverRouteService.replaceRoute(
                    driver, firstReservation.getTravelDate(), reservationIds,
                    AdminDashboardController.dynamicRouteComparator(dispatchOrigin));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", exception.getMessage()));
        }
        org.slf4j.LoggerFactory.getLogger(getClass()).info(dispatchOrigin.summary());

        try {
            routeReservations.stream()
                    .filter(reservation -> reservation.getPassenger() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            reservation -> normalizeWhatsAppNumber(
                                    reservation.getPassenger().getPhone()),
                            reservation -> reservation,
                            (first, ignored) -> first,
                            java.util.LinkedHashMap::new))
                    .forEach((passengerPhone, reservation) -> {
                        String passengerName = reservation.getPassenger().getFirstName();
                        whatsAppService.sendChoferAsignadoTemplate(
                                passengerPhone,
                                passengerName,
                                driver.getFullName(),
                                driver.getPhone());
                    });
        } catch (Exception exception) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "El chofer fue asignado, pero falló un aviso a pasajeros.", exception);
        }

        // 3. Enviar plantilla al chofer para abrir su hoja de ruta.
        try {
            String navigationUrl =
                    GoogleMapsParameterFormatter.buildDirectionsUrl(routeReservations);
            var dispatchResult = whatsAppService.sendDriverRouteDispatch(
                    normalizedPhone,
                    driver.getFullName(),
                    navigationUrl,
                    routeReservations);
            if (dispatchResult.success()) {
                driverRouteService.markRouteSent(routeReservations.stream()
                        .map(Reservation::getId).toList());
            }
            String notice = dispatchResult.success()
                    ? "Success: " + dispatchResult.message()
                    : "Warning: " + dispatchResult.message();
            return ResponseEntity.ok(java.util.Map.of(
                    "assigned", true,
                    "whatsAppStatus", dispatchResult.success() ? "Success" : "Warning",
                    "message", dispatchOrigin.summary() + " Chofer asignado correctamente en sistema. "
                            + "(Aviso de WhatsApp: " + notice + ")"));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("Error al enviar la plantilla despierta_chofer al chofer", e);
            return ResponseEntity.ok(java.util.Map.of(
                    "assigned", true,
                    "whatsAppStatus", "Warning",
                    "message", "Chofer asignado correctamente en sistema. "
                            + "(Aviso de WhatsApp: Warning: no se pudo enviar la hoja de ruta.)"));
        }
    }

    static java.util.Comparator<Reservation> dispatchedLastComparator() {
        return java.util.Comparator
                .comparing((Reservation reservation) ->
                        reservation.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT)
                .thenComparing(reservation -> reservation.getDriver() != null)
                .thenComparing(Reservation::getRouteSequence,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
    }

    private String normalizeWhatsAppNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        return clean.startsWith("549") ? "54" + clean.substring(3) : clean;
    }

    // 💬 6. Habilita http://localhost:8080/chat-room apuntando adentro de admin/
    @GetMapping("/chat-room")
    public String showChatRoom(Model model) {
        model.addAttribute("historial", new java.util.ArrayList<>());
        
        // El salvavidas para que el HTML no explote exigiendo el th:object
        model.addAttribute("reservation", new com.lunaris.ansenuza.domain.model.Reservation());
        
        // Listas para que se llenen los desplegables de localidad
        model.addAttribute("origenes", List.of("Morteros", "Brinkmann", "San Guillermo", "Porteña", "Suardi")); 
        model.addAttribute("destinos", List.of("Córdoba", "Aeropuerto Córdoba"));

        return "admin/chat-room"; // 👈 Corregido: va a buscar a templates/admin/chat-room.html
    }

    // 🤖 7. Habilita http://localhost:8080/bot-monitor apuntando adentro de admin/
    @GetMapping("/bot-monitor")
    public String showBotMonitor(Model model) {
        model.addAttribute("logs", new java.util.ArrayList<>());
        return "admin/bot-monitor"; // 👈 Corregido: va a buscar a templates/admin/bot-monitor.html
    }

    // 📋 8. Habilita http://localhost:8080/hoja-ruta apuntando adentro de admin/
    @GetMapping("/hoja-ruta")
    public String showHojaRuta(
            @RequestParam UUID driverId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate travelDate,
            org.springframework.security.core.Authentication authentication,
            Model model) {
        driverAuthorizationService.assertCanAccessDriver(authentication, driverId);
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            model.addAttribute("routeError", "No se encontró el chofer indicado.");
            model.addAttribute("reservas", List.of());
            model.addAttribute("totalYendo", 0);
            model.addAttribute("totalVolviendo", 0);
        } else {
            List<Reservation> reservations =
                    reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                            driverId, travelDate);
            if (!reservations.isEmpty()) {
                TripRouteCalculatorService calculator = new TripRouteCalculatorService();
                Reservation routeHead = reservations.getFirst();
                reservations = reservations.stream()
                        .filter(candidate -> calculator.sameManifest(routeHead, candidate))
                        .toList();
            }
            String scheduleBlock = reservations.stream().map(Reservation::getDepartureSchedule)
                    .filter(schedule -> schedule != null && !schedule.isBlank()).findFirst().orElse("03:00");
            RouteOriginResolution originResolution = resolveEffectiveTripOriginUseCase.resolve(travelDate, scheduleBlock);
            reservations = reservations.stream()
                    .sorted(AdminDashboardController.dynamicRouteComparator(originResolution)).toList();
            model.addAttribute("driver", driver);
            model.addAttribute("reservas", reservations);
            model.addAttribute("totalYendo", countSeats(reservations, false));
            model.addAttribute("totalVolviendo", countSeats(reservations, true));
            model.addAttribute(
                    "navigationUrl",
                    GoogleMapsParameterFormatter.buildDirectionsUrl(reservations));
            AdminDashboardController.addOriginAttributes(model, originResolution);
        }
        model.addAttribute("fechaSeleccionada", travelDate);
        model.addAttribute("pasajeros0800Count", 0);
        model.addAttribute("hubActivado", false);
        return "admin/hoja-ruta"; // 👈 Corregido: va a buscar a templates/admin/hoja-ruta.html
    }

    private int countSeats(List<Reservation> reservations, boolean fromCordoba) {
        return reservations.stream()
                .filter(reservation -> fromCordoba
                        == "Córdoba".equalsIgnoreCase(reservation.getPickupLocality()))
                .mapToInt(reservation ->
                        reservation.getPassengerCount() == null
                                ? 1
                                : reservation.getPassengerCount())
                .sum();
    }

    public record Chofer(String nombre, String telefono) {
    }

    public record PaymentVerificationResponse(
            List<UUID> reservationIds, boolean paymentVerified, String status) {
    }
}
