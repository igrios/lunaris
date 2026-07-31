package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/postulaciones")
@RequiredArgsConstructor
public class DriverApplicationViewController {

    private final DriverApplicationManagementService managementService;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("applications", managementService.findPending());
        return "admin/postulaciones";
    }

    @PostMapping("/{id}/aprobar")
    public String approve(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación aprobada y chofer activado.");
        return "redirect:/admin/postulaciones";
    }

    @PostMapping("/{id}/rechazar")
    public String reject(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación rechazada.");
        return "redirect:/admin/postulaciones";
    }
}
