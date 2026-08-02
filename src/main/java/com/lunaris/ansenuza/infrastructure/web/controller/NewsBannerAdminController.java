package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/novedades")
@RequiredArgsConstructor
public class NewsBannerAdminController {

    private final NewsBannerService service;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("banners", service.findAll());
        return "admin/novedades";
    }

    @PostMapping
    public String create(
            @RequestParam String title,
            @RequestParam(defaultValue = "false") boolean active,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validUntil,
            @RequestParam("image") MultipartFile image,
            RedirectAttributes redirectAttributes) {
        service.create(title, active, validUntil, image);
        redirectAttributes.addFlashAttribute("successMessage", "Novedad publicada correctamente.");
        return "redirect:/admin/novedades";
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Novedad eliminada.");
        return "redirect:/admin/novedades";
    }
}
