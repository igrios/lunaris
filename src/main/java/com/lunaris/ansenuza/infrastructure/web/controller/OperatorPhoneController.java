package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.OperatorPhoneService;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller @RequiredArgsConstructor
@RequestMapping("/admin/operadores")
public class OperatorPhoneController {
    private final OperatorPhoneService operators;
    @GetMapping
    public String list(Model model) {
        model.addAttribute("operators", operators.list());
        return "admin/operadores";
    }
    @PostMapping
    public String add(@RequestParam String name, @RequestParam String phone) {
        operators.add(name, phone);
        return "redirect:/admin/operadores";
    }
    @PostMapping("/{id}/estado")
    public String status(@PathVariable UUID id, @RequestParam boolean active) {
        operators.setActive(id, active);
        return "redirect:/admin/operadores";
    }
    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id) {
        operators.delete(id);
        return "redirect:/admin/operadores";
    }
    @ExceptionHandler({DomainValidationException.class, DataIntegrityViolationException.class})
    public String invalid(RuntimeException exception, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", exception instanceof DomainValidationException
                ? exception.getMessage() : "El teléfono ya está registrado o no se pudo guardar.");
        return "redirect:/admin/operadores";
    }
}
