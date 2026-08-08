package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.BookingVerificationData;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.model.TripType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PassengerOtpService otpService;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.phone(), request.fullName());
        return ResponseEntity.accepted().body(Map.of(
                "message", "El código fue enviado por WhatsApp."));
    }

    @PostMapping(value = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return verify(request, null);
    }

    @PostMapping(value = "/verify-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TokenResponse verifyOtpMultipart(
            @RequestPart(value = "reservation", required = false) @Valid VerifyOtpRequest reservation,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "code", required = false) String code,
            @RequestPart(value = "receipt", required = false) MultipartFile receiptFile) {
        VerifyOtpRequest effectiveRequest = reservation != null
                ? reservation : new VerifyOtpRequest(phone, code);
        return verify(effectiveRequest, receiptFile);
    }

    private TokenResponse verify(VerifyOtpRequest request, MultipartFile receiptFile) {
        validateVerificationRequest(request);
        var result = otpService.verifyOtp(request.phone(), request.code());
        var reservation = processPaymentReceiptUseCase.confirmOrCreateWebBooking(
                request.phone(),
                receiptFile,
                new BookingVerificationData(
                        request.travelDate(),
                        request.scheduleBlock(),
                        request.pickupLocality(),
                        request.destination(),
                        request.passengerCount(),
                        request.tripType(),
                        request.totalAmount()));
        return new TokenResponse(result.accessToken(), "Bearer", result.expiresAt(),
                reservation == null ? null : reservation.getReservationCode(),
                reservation == null ? null : reservation.getBookingGroupCode(),
                "Reserva confirmada con éxito");
    }

    private void validateVerificationRequest(VerifyOtpRequest request) {
        if (request == null || request.phone() == null || request.phone().isBlank()
                || request.code() == null || !request.code().matches("[0-9]{4}")) {
            throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                    "Teléfono y código OTP de cuatro dígitos son obligatorios.");
        }
    }

    public record SendOtpRequest(@NotBlank String phone, String fullName) {
        public SendOtpRequest(String phone) {
            this(phone, null);
        }
    }

    public record VerifyOtpRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "[0-9]{4}", message = "El código debe tener exactamente 4 dígitos.") String code,
            LocalDate travelDate,
            String scheduleBlock,
            String pickupLocality,
            String destination,
            Integer passengerCount,
            TripType tripType,
            BigDecimal totalAmount) {

        public VerifyOtpRequest(String phone, String code) {
            this(phone, code, null, null, null, null, null, null, null);
        }
    }

    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt,
            String reservationCode, String bookingGroupCode, String message) {
        public TokenResponse(String accessToken, String tokenType, Instant expiresAt) {
            this(accessToken, tokenType, expiresAt, null, null, null);
        }
    }
}
