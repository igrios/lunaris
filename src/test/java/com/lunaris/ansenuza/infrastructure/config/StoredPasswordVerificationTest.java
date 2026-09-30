package com.lunaris.ansenuza.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Comprobación local optativa: no conecta a la BD ni imprime secretos. */
@EnabledIfEnvironmentVariable(named = "VERIFY_PASSWORD_HASH", matches = ".+")
class StoredPasswordVerificationTest {
    @Test
    void suppliedPasswordMatchesStoredHashUsingApplicationEncoder() {
        String password = System.getenv("VERIFY_PASSWORD");
        assertNotNull(password, "Falta la contraseña de verificación.");
        assertTrue(new SecurityConfig().passwordEncoder().matches(password, System.getenv("VERIFY_PASSWORD_HASH")),
                "La contraseña no coincide con el hash usando el encoder de la aplicación.");
    }
}
