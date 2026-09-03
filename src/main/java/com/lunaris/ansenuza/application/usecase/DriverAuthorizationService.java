package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.shared.PhoneUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverAuthorizationService {

    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public void assertCanAccessDriver(Authentication authentication, UUID requestedDriverId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Se requiere autenticación.");
        }
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (!hasRole(authentication, "ROLE_CHOFER") || requestedDriverId == null) {
            throw new AccessDeniedException("No tiene acceso a esta hoja de ruta.");
        }
        UUID authenticatedDriverId = resolveActiveDriverId(authentication.getName());
        if (!requestedDriverId.equals(authenticatedDriverId)) {
            throw new AccessDeniedException("La hoja de ruta pertenece a otro chofer.");
        }
    }

    private UUID resolveActiveDriverId(String username) {
        String normalizedUsername = normalize(username);
        return driverRepository.findFirstByPhone(username)
                .filter(Driver::isActive)
                .or(() -> driverRepository.findByActiveTrue().stream()
                        .filter(driver -> normalize(driver.getPhone()).equals(normalizedUsername))
                        .findFirst())
                .map(Driver::getId)
                .orElseThrow(() -> new AccessDeniedException(
                        "La cuenta autenticada no corresponde a un chofer activo."));
    }

    private boolean hasRole(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private String normalize(String phone) {
        try {
            return PhoneUtils.normalizeArgentinePhone(phone);
        } catch (RuntimeException exception) {
            return "";
        }
    }
}
