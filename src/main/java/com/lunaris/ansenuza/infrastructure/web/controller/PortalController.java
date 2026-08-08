package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetPassengerProfileUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación del portal: verificar OTP no crea ni modifica reservas. */
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PassengerOtpService otpService;
    private final GetPassengerProfileUseCase profileUseCase;

    @PostMapping("/verify-otp")
    public PortalLoginResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        PassengerOtpService.TokenResult token = otpService.verifyOtp(request.phone(), request.code());
        return new PortalLoginResponse(token.accessToken(), "Bearer", token.expiresAt());
    }

    public record VerifyOtpRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "[0-9]{4}") String code) {
    }

    public record PortalLoginResponse(String accessToken, String tokenType, Instant expiresAt) {
    }
}
