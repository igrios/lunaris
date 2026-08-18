package com.lunaris.ansenuza.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.env.MockEnvironment;

class AdminUserInitializerTest {

    @Test
    void updatesExistingAdminPasswordActivationAndRole() {
        AccountRepository accounts = mock(AccountRepository.class);
        var encoder = new BCryptPasswordEncoder();
        Account existing = Account.builder()
                .username("admin")
                .displayName("Administrador")
                .passwordHash(encoder.encode("old-password"))
                .active(false)
                .roles(Set.of(Role.OPERADOR))
                .build();
        when(accounts.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.of(existing));
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");
        AdminUserInitializer initializer = new AdminUserInitializer(
                accounts, encoder, environment, "admin", "admin123");

        initializer.run();

        assertTrue(existing.isActive());
        assertTrue(existing.getRoles().contains(Role.ADMIN));
        assertTrue(existing.getRoles().contains(Role.OPERADOR));
        assertTrue(encoder.matches("admin123", existing.getPasswordHash()));
        assertNotNull(existing.getId());
        verify(accounts).save(existing);
    }

    @Test
    void createsMissingAdminWithExpectedCredentials() {
        AccountRepository accounts = mock(AccountRepository.class);
        var encoder = new BCryptPasswordEncoder();
        when(accounts.findByUsernameIgnoreCase("admin"))
                .thenReturn(Optional.empty());
        AdminUserInitializer initializer = new AdminUserInitializer(
                accounts, encoder, new MockEnvironment(), "admin", "admin123");

        initializer.run();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(captor.capture());
        Account created = captor.getValue();
        assertEquals("admin", created.getUsername());
        assertEquals("Administrador", created.getDisplayName());
        assertTrue(created.isActive());
        assertEquals(Set.of(Role.ADMIN), created.getRoles());
        assertTrue(encoder.matches("admin123", created.getPasswordHash()));
        assertNotNull(created.getId());
    }

    @Test
    void preservesExistingPasswordOutsideDev() {
        AccountRepository accounts = mock(AccountRepository.class);
        var encoder = new BCryptPasswordEncoder();
        String existingHash = encoder.encode("production-secret");
        Account existing = Account.builder()
                .username("admin")
                .displayName("Administrador")
                .passwordHash(existingHash)
                .active(true)
                .roles(Set.of(Role.ADMIN))
                .build();
        when(accounts.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(existing));

        new AdminUserInitializer(accounts, encoder, new MockEnvironment(),
                "admin", "admin123").run();

        assertEquals(existingHash, existing.getPasswordHash());
        verify(accounts).save(existing);
    }
}
