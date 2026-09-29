package com.lunaris.ansenuza.service.interurban;

import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/** Lectura del catálogo existente, sin cambiar su esquema ni su autorización actual. */
public class ExistingDriverIdentityAdapter implements DriverIdentityPort {
    private final DriverRepository drivers;

    public ExistingDriverIdentityAdapter(DriverRepository drivers) { this.drivers = drivers; }

    @Override public UUID requireDriver(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_CHOFER".equals(a.getAuthority())
                        || "ROLE_ADMIN".equals(a.getAuthority()))) throw denied();
        String phone = normalize(authentication.getName());
        if (phone == null) throw denied();
        var matches = drivers.findByActiveTrue().stream()
                .filter(driver -> phone.equals(normalize(driver.getPhone()))).toList();
        // Nunca escoger arbitrariamente un chofer si dos teléfonos representan la misma identidad.
        if (matches.size() != 1 || matches.getFirst().getId() == null) throw denied();
        return matches.getFirst().getId();
    }

    private String normalize(String phone) {
        try { return PhoneUtils.normalizeArgentinePhone(phone); }
        catch (RuntimeException e) { return null; }
    }

    private AccessDeniedException denied() { return new AccessDeniedException("No hay un chofer activo inequívoco para esta cuenta."); }
}
