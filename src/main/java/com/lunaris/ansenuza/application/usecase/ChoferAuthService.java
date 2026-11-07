package com.lunaris.ansenuza.application.usecase;

import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class ChoferAuthService {
    public Authentication login(String usuario, String password) {
        if (!"hector".equals(usuario) || !"cantor123".equals(password)) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                "hector", null, List.of(new SimpleGrantedAuthority("ROLE_CHOFER")));
    }
}
