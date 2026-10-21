package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.AssistedReservationDraftService;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AssistedReservationDraftController {
    private final AssistedReservationDraftService drafts;

    @PostMapping("/reservations/draft")
    public String save(@RequestParam String sourcePhone,
            @RequestParam(defaultValue = "false") boolean returnToChat,
            @ModelAttribute CreateReservationForm form, RedirectAttributes redirect) {
        drafts.save(sourcePhone, form);
        redirect.addFlashAttribute("successMessage", "Borrador guardado. Podés completar los datos pendientes.");
        if (returnToChat) {
            redirect.addAttribute("phone", sourcePhone);
            return "redirect:/admin/chat/{phone}";
        }
        redirect.addAttribute("chatPhone", sourcePhone);
        return "redirect:/reservations/new";
    }
}
