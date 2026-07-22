package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AccountAdminController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("usuarios", accountRepository.findAll());
        model.addAttribute("roles", Role.values());
        return "admin/usuarios";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) UUID id,
            @RequestParam String username,
            @RequestParam String displayName,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Set<Role> roles,
            @RequestParam(defaultValue = "false") boolean active,
            RedirectAttributes redirectAttributes) {
        Account account = id == null ? new Account() : accountRepository.findById(id).orElse(null);
        if (account == null && id != null) {
            redirectAttributes.addFlashAttribute("error", "El usuario a editar no existe.");
            return "redirect:/admin/usuarios";
        }

        boolean newAccount = account == null;
        if (newAccount) {
            account = new Account();
        }
        String normalizedUsername = username.trim();

        if (normalizedUsername.isBlank() || displayName.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Usuario y nombre son obligatorios.");
            return "redirect:/admin/usuarios";
        }
        if (newAccount && (password == null || password.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es obligatoria para un usuario nuevo.");
            return "redirect:/admin/usuarios";
        }
        if (roles == null || roles.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seleccioná al menos un rol.");
            return "redirect:/admin/usuarios";
        }
        Account accountWithSameUsername = accountRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElse(null);
        if (accountWithSameUsername != null
                && !accountWithSameUsername.getId().equals(account.getId())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso.");
            return "redirect:/admin/usuarios";
        }

        account.setUsername(normalizedUsername);
        account.setDisplayName(displayName.trim());
        account.setActive(active);
        account.setRoles(EnumSet.copyOf(roles));
        if (password != null && !password.isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(password));
        }
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("ok", newAccount ? "Usuario creado." : "Usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable UUID id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
        } else if (account.getUsername().equalsIgnoreCase(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "No podés eliminar tu propia cuenta.");
        } else {
            accountRepository.delete(account);
            redirectAttributes.addFlashAttribute("ok", "Usuario eliminado.");
        }
        return "redirect:/admin/usuarios";
    }
}
