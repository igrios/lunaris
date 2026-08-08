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
        messagingPort.sendText(storedPhone,
                "Tu código de acceso a Lunaris Ansenuza es: " + code
                        + ". Vence en " + otpTtl.toMinutes() + " minutos.");
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
