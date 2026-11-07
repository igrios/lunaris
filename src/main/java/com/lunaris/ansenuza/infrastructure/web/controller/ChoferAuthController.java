package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ChoferAuthService;
import com.lunaris.ansenuza.infrastructure.web.dto.LoginRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChoferAuthController {
    private final ChoferAuthService loginService;

    @PostMapping("/api/choferes/login")
    public LoginResponse login(@RequestBody LoginRequestDTO credentials,
            HttpServletRequest request, HttpServletResponse response) {
        var authentication = loginService.login(credentials.usuario(), credentials.password());
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
        response.setHeader("Cache-Control", "no-store");
        return new LoginResponse(authentication.getName(), "Hector", "CHOFER");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<LoginError> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginError("Credenciales inválidas."));
    }

    public record LoginResponse(String usuario, String nombre, String rol) { }
    public record LoginError(String message) { }
}
