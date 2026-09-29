package com.lunaris.ansenuza.service.interurban;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Formato: nonce de 12 bytes || PNG cifrado || tag GCM. AAD = reservationId. */
public class QrArtifactCipher {
    private final SecretKeySpec key;
    private final String keyId;
    private final SecureRandom random = new SecureRandom();

    public QrArtifactCipher(String base64Key, String keyId) {
        byte[] bytes = Base64.getDecoder().decode(base64Key);
        if (bytes.length != 32 || keyId == null || !keyId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("QR requiere clave AES de 256 bits y key-id.");
        }
        this.key = new SecretKeySpec(bytes, "AES");
        this.keyId = keyId;
    }

    public String keyId() { return keyId; }

    public byte[] encrypt(UUID reservationId, byte[] png) {
        byte[] nonce = new byte[12];
        random.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            cipher.updateAAD(reservationId.toString().getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(png);
            return ByteBuffer.allocate(nonce.length + ciphertext.length).put(nonce).put(ciphertext).array();
        } catch (Exception e) {
            throw new InterurbanPaymentException(InterurbanPaymentException.Code.QR_FAILURE);
        }
    }
}
