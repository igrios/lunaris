package com.lunaris.ansenuza.infrastructure.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataAccessException;

/**
 * ABM web (Alta/Baja/Modificación) de choferes de la flota.
 *
 * <p>Se monta en {@code /choferes} para no colisionar con {@link DriverController},
 * que expone la API REST/JSON en {@code /drivers}. Pensado para uso operativo simple:
 * un formulario arriba y la grilla de choferes con acciones directas.
 */
@Controller
@RequestMapping("/choferes")
@RequiredArgsConstructor
@Slf4j
public class DriverViewController {

    private final DriverRepository driverRepository;

    @InitBinder("driver")
    void initDriverBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? null : UUID.fromString(text.trim()));
            }
        });
    }

    @GetMapping
    public String panel(Model model) {
        preparePanel(model);
        if (!model.containsAttribute("driver")) {
            model.addAttribute("driver", new DriverForm());
        }
        return "choferes";
    }

    /** Alta (id vacío) o modificación (id presente) de un chofer. */
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("driver") DriverForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logBindingErrors(bindingResult);
            preparePanel(model);
            return "choferes";
        }

        Driver driver;
        if (form.getId() == null) {
            driver = new Driver();
        } else {
            driver = driverRepository.findById(form.getId()).orElse(null);
            if (driver == null) {
                bindingResult.rejectValue(
                        "id", "driver.notFound", "El chofer seleccionado ya no existe.");
                logBindingErrors(bindingResult);
                preparePanel(model);
                return "choferes";
            }
        }

        try {
            driver.setFullName(form.getFullName());
            driver.setPhone(form.getPhone());
            driver.setRanking(parseRanking(form.getRanking()));
            driver.setActive(form.isActive());
            driverRepository.saveAndFlush(driver);
        } catch (DataAccessException exception) {
            log.error("[Choferes] Error de persistencia al guardar el chofer: {}",
                    exception.getMessage(), exception);
            return "redirect:/choferes?error=true";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                form.getId() == null
                        ? "Chofer creado correctamente."
                        : "Chofer actualizado correctamente.");
        return "redirect:/choferes";
    }

    private int parseRanking(String rawRanking) {
        try {
            int ranking = Integer.parseInt(rawRanking);
            return ranking >= 1 && ranking <= 5 ? ranking : 5;
        } catch (NumberFormatException | NullPointerException exception) {
            return 5;
        }
    }

    private void preparePanel(Model model) {
        model.addAttribute("choferes", driverRepository.findAll());
    }

    private void logBindingErrors(BindingResult bindingResult) {
        bindingResult.getFieldErrors().forEach(error -> log.warn(
                "[Choferes] Error de binding. field={}, rejectedValue={}, message={}",
                error.getField(), error.getRejectedValue(), error.getDefaultMessage()));
        bindingResult.getGlobalErrors().forEach(error -> log.warn(
                "[Choferes] Error de binding global. object={}, message={}",
                error.getObjectName(), error.getDefaultMessage()));
    }

    /** Baja/alta lógica: alterna el estado activo del chofer sin borrarlo. */
    @PostMapping("/estado/{id}")
    public String alternarEstado(@PathVariable UUID id) {
        driverRepository.findById(id).ifPresent(driver -> {
            driver.setActive(!driver.isActive());
            driverRepository.save(driver);
        });
        return "redirect:/choferes";
    }

    /** Baja física definitiva del chofer. */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable UUID id) {
        driverRepository.deleteById(id);
        return "redirect:/choferes";
    }

    @Getter
    @Setter
    public static class DriverForm {

        private UUID id;

        @NotBlank(message = "El nombre y apellido son obligatorios.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        private String fullName;

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        private String phone;

        private String ranking = "5";

        private boolean active = true;
    }
}
