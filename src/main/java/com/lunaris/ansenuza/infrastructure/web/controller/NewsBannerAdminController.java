package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.infrastructure.web.dto.NewsBannerDto;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/novedades")
@Slf4j
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
        model.addAttribute("newsBannerForm", new NewsBannerDto());
        model.addAttribute("cloudinaryCloudName", cloudinaryCloudName);
        try {
            model.addAttribute("banners", service.findAll());
            var specialTrips = specialTripsQuery == null ? null : specialTripsQuery.getAll();
            model.addAttribute("specialTrips",
                    specialTrips == null ? java.util.List.of() : specialTrips);
        } catch (RuntimeException exception) {
            log.error("Error processing news banner: ", exception);
            model.addAttribute("banners", java.util.List.of());
            model.addAttribute("specialTrips", java.util.List.of());
            model.addAttribute("errorMessage", "No se pudieron cargar las novedades.");
        }
        return "admin/novedades";
    }

    @PostMapping
    public String create(
            @ModelAttribute("newsBannerForm") NewsBannerDto form,
            RedirectAttributes redirectAttributes) {
        if (form == null) {
            form = new NewsBannerDto();
        }
        if (form.getHasWaitingList() == null) form.setHasWaitingList(false);
        if (form.getEventType() == null || form.getEventType().isBlank()) {
            form.setEventType("GENERAL");
        }
        if (form.getActive() == null) form.setActive(true);
        try {
            service.save(form.getId(), form.getTitle(), form.getDescription(), form.getEventType(),
                    form.getHasWaitingList(), form.getActive(), form.getValidUntil(),
                    form.getImageUrl(), form.getImage());
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Novedad publicada correctamente.");
        } catch (RuntimeException exception) {
            log.error("Error processing news banner: ", exception);
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "No se pudo procesar la novedad. Revisá el flyer y los datos.");
        }
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
