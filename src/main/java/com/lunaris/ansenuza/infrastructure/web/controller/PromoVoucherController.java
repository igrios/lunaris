package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.PromoVoucherService;
import com.lunaris.ansenuza.application.usecase.PromoVoucherService.Registration;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.PromoVoucher;
import com.lunaris.ansenuza.domain.model.PromoVoucher.Status;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.web.mapper.ManualTripWebMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations/promos")
@RequiredArgsConstructor
public class PromoVoucherController {
    private final PromoVoucherService promos;

    @GetMapping
    public String list(@RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("promos", promos.search(status, q, page));
        model.addAttribute("statuses", Status.values());
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        return "promo-list";
    }

    @PostMapping
    public String register(@ModelAttribute Registration registration, BindingResult binding, RedirectAttributes redirect) {
        try {
            requireValidBinding(binding);
            PromoVoucher promo = promos.register(registration);
            redirect.addAttribute("number", promo.getNumber());
            return "redirect:/reservations/promos/{number}";
        } catch (DomainValidationException invalid) {
            redirect.addFlashAttribute("promoInput", registration);
            redirect.addFlashAttribute("errorMessage", invalid.getMessage());
            return "redirect:/reservations/new?promo=true";
        }
    }

    @GetMapping("/{number}")
    public String detail(@PathVariable String number, Model model) {
        PromoVoucher promo = promos.find(number);
        model.addAttribute("promo", promo);
        promos.linkedReservation(promo).ifPresent(reservation -> model.addAttribute("linkedReservation", reservation));
        model.addAttribute("whatsAppUrl", "https://wa.me/" + promo.getPhone() + "?text="
                + URLEncoder.encode(promo.getPassengerMessage(), StandardCharsets.UTF_8).replace("+", "%20"));
        return "promo-detail";
    }

    @GetMapping("/{number}/redeem")
    public String redemptionForm(@PathVariable String number, Model model) {
        PromoVoucher promo = promos.find(number);
        promo.requireAvailable();
        var form = (CreateReservationForm) model.getAttribute("reservation");
        if (form == null) form = new CreateReservationForm();
        form.setFirstName(promo.getFirstName());
        form.setLastName(promo.getLastName());
        form.setPhone(promo.getPhone());
        form.setCuil(promo.getCuil());
        model.addAttribute("reservation", form);
        model.addAttribute("initialTripCategory", form.getTripCategory().name());
        model.addAttribute("initialTravelDate", form.getTravelDate());
        model.addAttribute("redemptionPromo", promo);
        return "reservation-form";
    }

    @PostMapping("/{number}/redeem")
    public String redeem(@PathVariable String number, @ModelAttribute CreateReservationForm form,
            BindingResult binding, RedirectAttributes redirect) {
        try {
            requireValidBinding(binding);
            promos.redeem(number, ManualTripWebMapper.toReservation(form), form.getReturnDepartureSchedule());
            redirect.addFlashAttribute("successMessage", "Promo canjeada y viaje cargado en la agenda.");
        } catch (DomainValidationException invalid) {
            redirect.addFlashAttribute("errorMessage", invalid.getMessage());
            if (promos.find(number).getStatus() == Status.DISPONIBLE) {
                redirect.addFlashAttribute("reservation", form);
                redirect.addAttribute("number", number);
                return "redirect:/reservations/promos/{number}/redeem";
            }
        }
        redirect.addAttribute("number", number);
        return "redirect:/reservations/promos/{number}";
    }

    @PostMapping("/{number}/cancel")
    public String cancel(@PathVariable String number, RedirectAttributes redirect) {
        promos.cancel(number);
        redirect.addFlashAttribute("successMessage", "Promo anulada.");
        redirect.addAttribute("number", number);
        return "redirect:/reservations/promos/{number}";
    }

    @ExceptionHandler(DomainValidationException.class)
    public String handlePromoError(DomainValidationException invalid, RedirectAttributes redirect) {
        redirect.addFlashAttribute("errorMessage", invalid.getMessage());
        return "redirect:/reservations/promos";
    }

    private static void requireValidBinding(BindingResult binding) {
        if (binding.hasErrors()) {
            throw new DomainValidationException("Revisá los campos y el formato de los importes y fechas.");
        }
    }
}
