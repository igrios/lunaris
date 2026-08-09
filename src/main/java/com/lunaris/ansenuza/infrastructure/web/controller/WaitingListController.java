package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;
import com.lunaris.ansenuza.application.usecase.WaitingListOtpService;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@RestController
@RequestMapping("/api/v1/waiting-list")
@RequiredArgsConstructor
public class WaitingListController {

    private final WaitingListService service;
    private final WaitingListReengagementService reengagementService;
    private final WaitingListOtpService otpService;
    private final Environment environment;

    @GetMapping
    public List<WaitingListResponse> find(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(required = false) String status) {
        List<WaitingListEntry> entries = travelDate == null
                && (status == null || status.isBlank())
                        ? service.findAllActiveWaiting()
                        : service.find(travelDate, status);
        return entries.stream().map(WaitingListResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingListResponse create(@RequestBody CreateWaitingListRequest request) {
        return WaitingListResponse.from(service.create(
                request.phoneNumber(), request.passengerName(), request.travelDate(),
                request.pickupLocality(), request.destination(), request.passengerCount(),
                request.notes(), request.eventType()));
    }

    @PostMapping("/request-otp")
    public OtpResponse requestOtp(@RequestBody OtpRequest request) {
        String otp = otpService.request(request.phone());
        boolean production = environment.acceptsProfiles(Profiles.of("prod", "production"));
        return new OtpResponse("El código fue enviado por WhatsApp.", production ? null : otp);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingListResponse confirm(@RequestBody ConfirmWaitingListRequest request) {
        otpService.verify(request.phoneNumber(), request.otpCode());
        return WaitingListResponse.from(service.create(
                request.phoneNumber(), request.passengerName(), request.travelDate(),
                request.pickupLocality(), request.destination(), request.passengerCount(),
                request.notes(), request.eventType()));
    }

    @PatchMapping("/{id}/status")
    public WaitingListResponse updateStatus(
            @PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return WaitingListResponse.from(service.updateStatus(id, request.status()));
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/promote")
    public WaitingListResponse promote(@PathVariable Long id) {
        return WaitingListResponse.from(reengagementService.promote(id));
    }

    public record UpdateStatusRequest(String status) {
    }

    public record CreateWaitingListRequest(
            String phoneNumber, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String notes, String eventType) {
    }

    public record OtpRequest(
            String fullName,
            @JsonAlias("phoneNumber") String phone,
            String eventType,
            String notes) {
    }

    public record OtpResponse(String message, String otp) {
    }

    public record ConfirmWaitingListRequest(
            String phoneNumber, String otpCode, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String notes, String eventType) {
    }

    public record WaitingListResponse(
            Long id, String phoneNumber, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String status, OffsetDateTime createdAt, String notes, String eventType) {

        private static WaitingListResponse from(WaitingListEntry entry) {
            return new WaitingListResponse(
                    entry.getId(), entry.getPhoneNumber(), entry.getPassengerName(),
                    entry.getTravelDate(), entry.getPickupLocality(), entry.getDestination(),
                    entry.getPassengerCount(), entry.getStatus(), entry.getCreatedAt(),
                    entry.getNotes(), entry.getEventType());
        }
    }
}
