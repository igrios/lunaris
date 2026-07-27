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

class AdminUserInitializerTest {

    @Test
    void updatesExistingAdminPasswordActivationAndRole() {
        AccountRepository accounts = mock(AccountRepository.class);
        var encoder = new BCryptPasswordEncoder();
        Account existing = Account.builder()
                .username("Ignacio")
                .displayName("Ignacio")
                .passwordHash(encoder.encode("old-password"))
                .active(false)
                .roles(Set.of(Role.OPERADOR))
                .build();
        when(accounts.findByUsernameIgnoreCase("ignacio"))
                .thenReturn(Optional.of(existing));
        AdminUserInitializer initializer = new AdminUserInitializer(accounts, encoder);

        initializer.run();

        assertTrue(existing.isActive());
        assertTrue(existing.getRoles().contains(Role.ADMIN));
        assertTrue(existing.getRoles().contains(Role.OPERADOR));
        assertTrue(encoder.matches("Admin123!", existing.getPasswordHash()));
        assertNotNull(existing.getId());
        verify(accounts).save(existing);
    }

    @Test
    void createsMissingAdminWithExpectedCredentials() {
        AccountRepository accounts = mock(AccountRepository.class);
        var encoder = new BCryptPasswordEncoder();
        when(accounts.findByUsernameIgnoreCase("ignacio"))
                .thenReturn(Optional.empty());
        AdminUserInitializer initializer = new AdminUserInitializer(accounts, encoder);

        initializer.run();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(captor.capture());
        Account created = captor.getValue();
        assertEquals("ignacio", created.getUsername());
        assertEquals("Ignacio Admin", created.getDisplayName());
        assertTrue(created.isActive());
        assertEquals(Set.of(Role.ADMIN), created.getRoles());
        assertTrue(encoder.matches("Admin123!", created.getPasswordHash()));
        assertNotNull(created.getId());
    }
}
