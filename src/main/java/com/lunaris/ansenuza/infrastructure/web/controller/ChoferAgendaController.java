package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ChoferAgendaService;
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
@RequestMapping("/api/choferes/agenda")
@RequiredArgsConstructor
public class ChoferAgendaController {
    private final ChoferAgendaService agendaService;

    @GetMapping
    public ResponseEntity<List<ServicioAgenda>> agenda(
            @RequestParam(name = "fecha", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(agendaService.execute(fecha));
    }
}
