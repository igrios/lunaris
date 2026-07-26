package com.lunaris.ansenuza.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SecurityConfigCorsTest {

    @Test
    void exposesExpectedCorsOriginsAndMethods() {
        var source = new SecurityConfig().corsConfigurationSource();
        var configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertTrue(configuration.getAllowedOriginPatterns().containsAll(List.of(
                "https://*.vercel.app",
                "https://lunarisansenuza.com.ar",
                "http://localhost:5173",
                "http://localhost:3000")));
        assertTrue(configuration.getAllowedMethods().containsAll(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
        assertEquals(List.of("*"), configuration.getAllowedHeaders());
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }
}
