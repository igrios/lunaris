package com.lunaris.ansenuza.service.interurban;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class QrGeneratorService {
    private final InterurbanPaymentRepository repository;
    private final QrArtifactCipher cipher;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public QrGeneratorService(InterurbanPaymentRepository repository, QrArtifactCipher cipher, Clock clock) {
        this.repository = repository;
        this.cipher = cipher;
        this.clock = clock;
    }

    /** Debe participar de la transacción de confirmación; nunca genera pases para HELD. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void generate(UUID reservationId, Instant expiresAt) {
        if (!repository.lockPaidReservation(reservationId)) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
        if (repository.hasQr(reservationId)) return;
        if (expiresAt == null || !expiresAt.isAfter(clock.instant())) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
        try {
            byte[] entropy = new byte[32];
            random.nextBytes(entropy);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
            var matrix = new QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, 320, 320);
            var png = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", png);
            repository.saveQr(reservationId, hash, expiresAt, cipher.encrypt(reservationId, png.toByteArray()), cipher.keyId());
        } catch (InterurbanPaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }
}
