package com.lunaris.ansenuza.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SecurityConfigAuthenticationTest {

    private static final String ADMIN_HASH =
            "$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQvq4a.";

    @Test
    void loadsStoredBcryptHashAndMapsAdminRoleForAuthentication() {
        AccountRepository accounts = mock(AccountRepository.class);
        Account ignacio = Account.builder()
                .username("ignacio")
                .displayName("Ignacio")
                .passwordHash(ADMIN_HASH)
                .active(true)
                .roles(Set.of(Role.ADMIN))
                .build();
        when(accounts.findByUsernameIgnoreCase("IGNACIO"))
                .thenReturn(Optional.of(ignacio));
        SecurityConfig configuration = new SecurityConfig();

        var user = configuration.userDetailsService(accounts)
                .loadUserByUsername("IGNACIO");

        assertEquals(ADMIN_HASH, user.getPassword());
        assertTrue(user.isEnabled());
        assertTrue(user.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void exposesBcryptPasswordEncoder() {
        var encoder = new SecurityConfig().passwordEncoder();

        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
        assertTrue(encoder.matches("verification-password",
                encoder.encode("verification-password")));
        assertFalse(encoder.matches("incorrect-password",
                encoder.encode("verification-password")));
    }
}
