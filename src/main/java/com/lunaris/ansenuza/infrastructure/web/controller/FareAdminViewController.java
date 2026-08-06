package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fares")
@RequiredArgsConstructor
@Slf4j
public class FareAdminViewController {
    private final GetFaresQuery query;
    private final CreateFareLocalityUseCase createUseCase;
    private final UpdateLocalityFareUseCase updateUseCase;
    private final DeleteFareLocalityUseCase deleteUseCase;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("tarifas", query.getAll());
        return "admin/fares";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam(required = false) Integer kmsToCordoba,
            @RequestParam(required = false) Integer minutesFromOrigin, @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        try {
            createUseCase.create(name, kmsToCordoba, minutesFromOrigin, amount);
            redirectAttributes.addFlashAttribute("success", "Localidad y tarifa agregadas exitosamente.");
        } catch (DomainValidationException | IllegalArgumentException | DataIntegrityViolationException exception) {
            log.warn("No se pudo crear la localidad y su tarifa: {}", exception.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + exception.getMessage());
        } catch (Exception exception) {
            log.error("Error inesperado al crear la localidad y su tarifa.", exception);
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al procesar la tarifa.");
        }
        return redirect();
    }

    @PostMapping("/{localityId}/editar")
    public String update(@PathVariable UUID localityId, @RequestParam String name,
            @RequestParam(required = false) Integer kmsToCordoba,
            @RequestParam(required = false) Integer minutesFromOrigin, @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        updateUseCase.updateLocalityAndFare(localityId, name, kmsToCordoba, minutesFromOrigin, amount);
        redirectAttributes.addFlashAttribute("successMessage", "Localidad y tarifa actualizadas.");
        return redirect();
    }

    @PostMapping("/{fareId}/eliminar")
    public String delete(@PathVariable UUID fareId, RedirectAttributes redirectAttributes) {
        deleteUseCase.delete(fareId);
        redirectAttributes.addFlashAttribute("successMessage", "Localidad y tarifa eliminadas.");
        return redirect();
    }

    private String redirect() {
        return "redirect:/admin/fares";
    }
}
