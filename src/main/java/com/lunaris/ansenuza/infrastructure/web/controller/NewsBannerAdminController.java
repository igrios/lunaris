package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.in.ToggleSpecialTripStatusUseCase;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/novedades")
public class NewsBannerAdminController {

    private final NewsBannerService service;
    private final GetSpecialTripsQuery specialTripsQuery;
    private final CreateSpecialTripUseCase createSpecialTripUseCase;
    private final UpdateSpecialTripUseCase updateSpecialTripUseCase;
    private final ToggleSpecialTripStatusUseCase toggleSpecialTripStatusUseCase;
    private final String cloudinaryCloudName;

    @Autowired
    public NewsBannerAdminController(NewsBannerService service, GetSpecialTripsQuery specialTripsQuery,
            CreateSpecialTripUseCase createSpecialTripUseCase, UpdateSpecialTripUseCase updateSpecialTripUseCase,
            ToggleSpecialTripStatusUseCase toggleSpecialTripStatusUseCase,
            @Value("${cloudinary.cloud-name}") String cloudinaryCloudName) {
        this.service = service;
        this.specialTripsQuery = specialTripsQuery;
        this.createSpecialTripUseCase = createSpecialTripUseCase;
        this.updateSpecialTripUseCase = updateSpecialTripUseCase;
        this.toggleSpecialTripStatusUseCase = toggleSpecialTripStatusUseCase;
        this.cloudinaryCloudName = cloudinaryCloudName;
    }

    NewsBannerAdminController(NewsBannerService service) {
        this(service, null, null, null, null, "dgrwrcb5p");
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("banners", service.findAll());
        model.addAttribute("specialTrips", specialTripsQuery == null ? java.util.List.of() : specialTripsQuery.getAll());
        model.addAttribute("cloudinaryCloudName", cloudinaryCloudName);
        return "admin/novedades";
    }

    @PostMapping
    public String create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "false") boolean hasWaitingList,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validUntil,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        service.create(title, description, eventType, hasWaitingList, active,
                validUntil, imageUrl, image);
        redirectAttributes.addFlashAttribute("successMessage", "Novedad publicada correctamente.");
        return "redirect:/admin/novedades";
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Novedad eliminada.");
        return "redirect:/admin/novedades";
    }

    @PostMapping("/viajes")
    public String createSpecialTrip(@RequestParam String title, @RequestParam(required = false) String description,
            @RequestParam(required = false) String origin, @RequestParam(required = false) String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal price, @RequestParam Integer maxPassengers,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirectAttributes) {
        createSpecialTripUseCase.create(command(title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active));
        redirectAttributes.addFlashAttribute("successMessage", "Viaje especial creado correctamente.");
        return redirect();
    }

    @PostMapping("/viajes/{id}/editar")
    public String updateSpecialTrip(@PathVariable Long id, @RequestParam String title,
            @RequestParam(required = false) String description, @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal price, @RequestParam Integer maxPassengers,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirectAttributes) {
        updateSpecialTripUseCase.update(id, command(title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active));
        redirectAttributes.addFlashAttribute("successMessage", "Viaje especial actualizado.");
        return redirect();
    }

    @PostMapping("/viajes/{id}/estado")
    public ResponseEntity<Void> toggleSpecialTrip(@PathVariable Long id, @RequestParam boolean active) {
        toggleSpecialTripStatusUseCase.setActive(id, active);
        return ResponseEntity.noContent().build();
    }

    private SpecialTripCommand command(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active) {
        return new SpecialTripCommand(title, description, origin, destination, startDate, endDate, price,
                maxPassengers, resolveCloudinaryUrl(imageUrl), active);
    }

    private String resolveCloudinaryUrl(String image) {
        if (image == null || image.isBlank() || image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        return "https://res.cloudinary.com/" + cloudinaryCloudName + "/image/upload/" + image.trim();
    }

    private String redirect() {
        return "redirect:/admin/novedades";
    }
}
