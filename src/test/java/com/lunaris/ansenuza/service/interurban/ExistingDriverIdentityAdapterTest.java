package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.infrastructure.config.UserPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

class ExistingDriverIdentityAdapterTest {
    private final DriverRepository repository = mock(DriverRepository.class);
    private final ExistingDriverIdentityAdapter adapter = new ExistingDriverIdentityAdapter(repository);
    private final UUID accountId = UUID.randomUUID();

    private UsernamePasswordAuthenticationToken authentication(String role) {
        var authorities = AuthorityUtils.createAuthorityList(role);
        var principal = new UserPrincipal(accountId, "username-without-phone", "", true, authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    @Test void resolvesByAccountIdForDriverAndAdminWithoutScanningPhones() {
        var driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setAccountId(accountId);
        when(repository.findByAccountIdAndActiveTrue(accountId)).thenReturn(Optional.of(driver));
        assertEquals(driver.getId(), adapter.requireDriver(authentication("ROLE_CHOFER")));
        assertEquals(driver.getId(), adapter.requireDriver(authentication("ROLE_ADMIN")));
        verify(repository, times(2)).findByAccountIdAndActiveTrue(accountId);
        verifyNoMoreInteractions(repository);
    }

    @Test void missingOrInactiveBindingFailsClosed() {
        when(repository.findByAccountIdAndActiveTrue(accountId)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(authentication("ROLE_CHOFER")));
    }

    @Test void driverWithoutIdFailsClosed() {
        when(repository.findByAccountIdAndActiveTrue(accountId)).thenReturn(Optional.of(new Driver()));
        assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(authentication("ROLE_CHOFER")));
    }

    @Test void otherRolesCannotResolveDriver() {
        for (String role : new String[]{"ROLE_PASSENGER", "ROLE_OPERADOR", "ROLE_FACTURACION"}) {
            assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(authentication(role)));
        }
        verifyNoInteractions(repository);
    }

    @Test void legacyPrincipalCannotFallBackToPhoneMatching() {
        var auth = new UsernamePasswordAuthenticationToken("543562123456", "",
                AuthorityUtils.createAuthorityList("ROLE_CHOFER"));
        assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(auth));
        verifyNoInteractions(repository);
    }

    @Test void unauthenticatedRequestsCannotQueryDrivers() {
        assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(null));
        var auth = UsernamePasswordAuthenticationToken.unauthenticated(authentication("ROLE_CHOFER").getPrincipal(), "");
        assertThrows(AccessDeniedException.class, () -> adapter.requireDriver(auth));
        verifyNoInteractions(repository);
    }
}
