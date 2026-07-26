package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PassengerOtpService {

    private static final int MAX_ATTEMPTS = 5;

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
            @Value("${lunaris.auth.otp-ttl:PT5M}") Duration otpTtl,
            @Value("${lunaris.auth.token-ttl:PT12H}") Duration tokenTtl) {
        this.passengerRepository = passengerRepository;
        this.messagingPort = messagingPort;
        this.otpTtl = otpTtl;
        this.tokenTtl = tokenTtl;
    }

    public void sendOtp(String rawPhone) {
        String phone = normalizePhone(rawPhone);
        String storedPhone = passengerRepository.findByPhone(rawPhone.trim())
                .or(() -> passengerRepository.findByPhone(phone))
                .map(passenger -> passenger.getPhone())
                .orElseThrow(() ->
                        new DomainValidationException("No existe un pasajero registrado con ese teléfono."));
        if (storedPhone == null) {
            throw new DomainValidationException("No existe un pasajero registrado con ese teléfono.");
        }
        String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
        challenges.put(phone, new OtpChallenge(code, Instant.now().plus(otpTtl), 0, storedPhone));
        messagingPort.sendText(storedPhone,
                "Tu código de acceso a Lunaris Ansenuza es " + code
                        + ". Vence en " + otpTtl.toMinutes() + " minutos.");
    }

    public TokenResult verifyOtp(String rawPhone, String code) {
        String phone = normalizePhone(rawPhone);
        OtpChallenge challenge = challenges.get(phone);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            challenges.remove(phone);
            throw new DomainValidationException("El código venció o no fue solicitado.");
        }
        if (challenge.attempts() >= MAX_ATTEMPTS || !challenge.code().equals(code)) {
            int attempts = challenge.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                challenges.remove(phone);
            } else {
                challenges.put(phone, new OtpChallenge(
                        challenge.code(), challenge.expiresAt(), attempts, challenge.storedPhone()));
            }
            throw new DomainValidationException("El código ingresado no es válido.");
        }

        challenges.remove(phone);
        Instant expiresAt = Instant.now().plus(tokenTtl);
        String token = generateToken();
        tokens.put(token, new AccessToken(challenge.storedPhone(), expiresAt));
        return new TokenResult(token, expiresAt);
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

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new DomainValidationException("El teléfono es obligatorio.");
        }
        String normalized = phone.trim().replaceAll("[^0-9+]", "");
        if (normalized.length() < 8) {
            throw new DomainValidationException("El teléfono no es válido.");
        }
        return normalized;
    }

    private record OtpChallenge(String code, Instant expiresAt, int attempts, String storedPhone) {
    }

    private record AccessToken(String phone, Instant expiresAt) {
    }

    public record TokenResult(String accessToken, Instant expiresAt) {
    }
}
