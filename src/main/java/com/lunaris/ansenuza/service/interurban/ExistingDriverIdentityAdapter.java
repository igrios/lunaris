package com.lunaris.ansenuza.service.interurban;

import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.infrastructure.config.UserPrincipal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/** Resuelve el chofer activo mediante la vinculación explícita con su cuenta. */
public class ExistingDriverIdentityAdapter implements DriverIdentityPort {
    private final DriverRepository drivers;

    public ExistingDriverIdentityAdapter(DriverRepository drivers) { this.drivers = drivers; }

    @Override public UUID requireDriver(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_CHOFER".equals(a.getAuthority())
                        || "ROLE_ADMIN".equals(a.getAuthority()))) throw denied();
        if (!(authentication.getPrincipal() instanceof UserPrincipal principal)) throw denied();
        return drivers.findByAccountIdAndActiveTrue(principal.getAccountId())
                .map(driver -> driver.getId())
                .orElseThrow(this::denied);
    }

    private AccessDeniedException denied() { return new AccessDeniedException("No hay un chofer activo inequívoco para esta cuenta."); }
}
