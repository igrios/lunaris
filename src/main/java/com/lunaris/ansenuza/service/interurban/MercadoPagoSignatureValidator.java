package com.lunaris.ansenuza.service.interurban;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MercadoPagoSignatureValidator {
    private final byte[] secret;
    private final Clock clock;
    private final Duration maxAge;

    public MercadoPagoSignatureValidator(String secret, Clock clock, Duration maxAge) {
        if (secret == null || secret.isBlank() || maxAge.isNegative() || maxAge.isZero()) {
            throw new IllegalArgumentException("Secret y ventana HMAC obligatorios.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
        this.maxAge = maxAge;
    }

    public void validate(String paymentId, String requestId, String signature) {
        try {
            if (paymentId == null || !paymentId.matches("[0-9]{1,64}")
                    || requestId == null || !requestId.matches("[A-Za-z0-9_-]{1,128}")
                    || signature == null || signature.length() > 256) throw invalid();
            var parts = new HashMap<String, String>();
            for (String part : signature.split(",")) {
                String[] pair = part.trim().split("=", -1);
                if (pair.length != 2 || parts.putIfAbsent(pair[0].trim(), pair[1].trim()) != null) throw invalid();
            }
            String ts = parts.get("ts");
            String hash = parts.get("v1");
            if (ts == null || !ts.matches("[0-9]{10}|[0-9]{13}")
                    || hash == null || !hash.matches("[a-fA-F0-9]{64}")) throw invalid();
            // MP presenta ejemplos en segundos y milisegundos. Firmar SIEMPRE el ts original.
            long timestamp = Long.parseLong(ts);
            Instant sentAt = ts.length() == 13 ? Instant.ofEpochMilli(timestamp) : Instant.ofEpochSecond(timestamp);
            Instant now = clock.instant();
            if (sentAt.isBefore(now.minus(maxAge)) || sentAt.isAfter(now.plusSeconds(60))) throw invalid();
            String manifest = "id:" + paymentId.toLowerCase(Locale.ROOT) + ";request-id:" + requestId + ";ts:" + ts + ";";
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            if (!MessageDigest.isEqual(mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8)),
                    HexFormat.of().parseHex(hash))) throw invalid();
        } catch (InterurbanPaymentException e) {
            throw e;
        } catch (Exception e) {
            throw invalid();
        }
    }

    private InterurbanPaymentException invalid() {
        return new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_SIGNATURE);
    }
}
