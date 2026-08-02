package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/postulaciones")
@RequiredArgsConstructor
public class DriverApplicationViewController {

    private final DriverApplicationManagementService managementService;
    private final DriverApplicationRepository applicationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String panel(Model model) {
        List<DriverApplication> postulaciones = Optional.ofNullable(
                        applicationRepository.findByStatusOrderByCreatedAtAsc(
                                DriverApplication.Status.PENDING))
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .toList();
        model.addAttribute("postulaciones", postulaciones);
        return "admin/postulaciones";
    }

    @PostMapping("/{id}/aprobar")
    public String approve(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación aprobada y chofer activado.");
        return "redirect:/admin/postulaciones?approved=true";
    }

    @PostMapping("/{id}/rechazar")
    public String reject(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación rechazada.");
        return "redirect:/admin/postulaciones";
    }
}
