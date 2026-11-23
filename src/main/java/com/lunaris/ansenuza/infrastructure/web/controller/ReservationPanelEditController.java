package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.dto.PanelReservationEdit;
import com.lunaris.ansenuza.application.usecase.UpdateReservationUseCase;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.ReservationNotFoundException;
import com.lunaris.ansenuza.domain.exception.ReservationUpdateConflictException;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservas-panel/{id}/editar")
@RequiredArgsConstructor
public class ReservationPanelEditController {
    private final UpdateReservationUseCase update;

    @GetMapping
    public String editForm(@PathVariable UUID id, Model model) {
        var state = update.getPanelState(id);
        model.addAttribute("state", state);
        model.addAttribute("operationId", UUID.randomUUID().toString());
        model.addAttribute("schedule", UpdateReservationUseCase.normalizedSchedule(state.selected()));
        return "reservation-panel-edit";
    }

    @PostMapping
    public String save(@PathVariable UUID id, @ModelAttribute PanelReservationEdit edit, BindingResult binding,
            Principal principal, RedirectAttributes redirect) {
        try {
            if (binding.hasErrors()) throw new DomainValidationException("Revisá el formato de las fechas, horarios e importes.");
            update.editFromPanel(id, edit, principal.getName());
            redirect.addFlashAttribute("successMessage", "Cambios guardados.");
            return "redirect:/reservas-panel";
        } catch (DomainValidationException | ReservationUpdateConflictException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
            redirect.addAttribute("id", id);
            return "redirect:/reservas-panel/{id}/editar";
        }
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public String missing(RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", "No se encontró la reserva.");
        return "redirect:/reservas-panel";
    }
}
