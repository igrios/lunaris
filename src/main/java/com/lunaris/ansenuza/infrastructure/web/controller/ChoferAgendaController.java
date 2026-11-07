package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ChoferAgendaService;
import com.lunaris.ansenuza.application.usecase.ChoferAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import com.lunaris.ansenuza.application.usecase.ChoferAgendaService.ServicioAgenda;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/choferes/agenda", "/api/viajes"})
@RequiredArgsConstructor
public class ChoferAgendaController {
    private final ChoferAgendaService agendaService;
    private final ChoferAuthService authService;

    @GetMapping
    public ResponseEntity<List<ServicioAgenda>> agenda(
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(value = "usuario", required = false) String usuario,
            @RequestHeader(value = "X-Chofer-Usuario", required = false) String choferHeader,
            Authentication authentication) {
        authService.validateAgendaAccess(usuario, choferHeader, authentication);
        return ResponseEntity.ok(agendaService.execute(fecha));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<java.util.Map<String, String>> unauthorized(AuthenticationException exception) {
        return ResponseEntity.status(401).body(java.util.Map.of("message", exception.getMessage()));
    }
}
