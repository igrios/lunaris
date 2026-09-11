package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/admin/consultas")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiries;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inquiries", inquiries.list());
        return "inquiries";
    }

    @PostMapping("/{id}/estado")
    public String update(@PathVariable UUID id, @RequestParam InquiryStatus status) {
        inquiries.updateStatus(id, status);
        return "redirect:/admin/consultas";
    }
}
