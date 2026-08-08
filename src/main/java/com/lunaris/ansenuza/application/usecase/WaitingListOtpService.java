package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Gestiona el desafío OTP específico para altas públicas de lista de espera. */
@Service
public class WaitingListOtpService {

    private static final Logger logger = LoggerFactory.getLogger(WaitingListOtpService.class);

    private static final String INVALID_CODE = "Código OTP inválido o vencido";

    private final MessagingPort messagingPort;
    private final Duration ttl;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();

    public WaitingListOtpService(
            MessagingPort messagingPort,
            @Value("${lunaris.waiting-list.otp-ttl:PT5M}") Duration ttl) {
        this.messagingPort = messagingPort;
        this.ttl = ttl;
    }

    public String request(String rawPhone) {
        String phone = normalize(rawPhone);
        String code = String.format("%04d", random.nextInt(10_000));
        challenges.put(phone, new Challenge(code, Instant.now().plus(ttl)));
        try {
            messagingPort.sendText(phone, "Tu código de verificación para Lunaris es: " + code);
        } catch (RuntimeException exception) {
            logger.warn("No se pudo enviar OTP por WhatsApp para {}. Se conserva el desafío.", phone,
                    exception);
            logger.info("OTP generated for {}: {}", phone, code);
        }
        return code;
    }

    public void verify(String rawPhone, String code) {
        String phone;
        try {
            phone = normalize(rawPhone);
        } catch (DomainValidationException exception) {
            throw new DomainValidationException(INVALID_CODE);
        }
        Challenge challenge = challenges.get(phone);
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())
                || code == null || !code.matches("\\d{4}")
                || !challenge.code().equals(code)) {
            if (challenge != null && challenge.expiresAt().isBefore(Instant.now())) {
                challenges.remove(phone, challenge);
            }
            throw new DomainValidationException(INVALID_CODE);
        }
        challenges.remove(phone, challenge);
    }

    private String normalize(String rawPhone) {
        return PhoneUtils.normalizeArgentinePhone(rawPhone);
    }

    private record Challenge(String code, Instant expiresAt) {
    }
}
