package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

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
public class DriverViewController {

    private final DriverRepository driverRepository;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("choferes", driverRepository.findAll());
        return "choferes";
    }

    /** Alta (id vacío) o modificación (id presente) de un chofer. */
    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) String id,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) Integer ranking,
            @RequestParam(defaultValue = "false") boolean active) {

        Driver driver = (id != null && !id.isBlank())
                ? driverRepository.findById(UUID.fromString(id)).orElseGet(Driver::new)
                : new Driver();

        driver.setFullName(fullName.trim());
        driver.setPhone(phone.trim());
        driver.setRanking(ranking);
        driver.setActive(active);
        if (driver.getId() == null) {
            driver.setId(UUID.randomUUID());
        }
        driverRepository.save(driver);

        return "redirect:/choferes";
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
}
