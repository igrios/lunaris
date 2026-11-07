package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class ChoferAuthService {
    public void validateAgendaAccess(String usuario, String choferHeader, Authentication authentication) {
        boolean identified = usuario != null || choferHeader != null;
        if (identified) {
            if ((usuario != null && !"hector".equals(usuario))
                    || (choferHeader != null && !"hector".equals(choferHeader))) {
                throw new BadCredentialsException("Chofer no autorizado.");
            }
            return;
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Se requiere identificación del chofer.");
        }
        if (authentication.getAuthorities().stream().noneMatch(authority ->
                List.of("ROLE_CHOFER", "ROLE_ADMIN", "ROLE_OPERADOR").contains(authority.getAuthority()))) {
            throw new org.springframework.security.access.AccessDeniedException("No tiene acceso a la agenda.");
        }
    }

    public Authentication login(String usuario, String password) {
        if (!"hector".equals(usuario) || !"cantorlunaris123".equals(password)) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                "hector", null, List.of(new SimpleGrantedAuthority("ROLE_CHOFER")));
    }
}
