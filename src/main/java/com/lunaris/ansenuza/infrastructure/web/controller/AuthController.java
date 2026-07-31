package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PassengerOtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.phone(), request.fullName());
        return ResponseEntity.accepted().body(Map.of(
                "message", "El código fue enviado por WhatsApp."));
    }

    @PostMapping("/verify-otp")
    public TokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        var result = otpService.verifyOtp(request.phone(), request.code());
        return new TokenResponse(result.accessToken(), "Bearer", result.expiresAt());
    }

    public record SendOtpRequest(@NotBlank String phone, String fullName) {
        public SendOtpRequest(String phone) {
            this(phone, null);
        }
    }

    public record VerifyOtpRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "[0-9]{4}", message = "El código debe tener exactamente 4 dígitos.") String code) {
    }

    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {
    }
}
