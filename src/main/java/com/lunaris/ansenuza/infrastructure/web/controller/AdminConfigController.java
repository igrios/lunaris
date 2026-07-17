package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminConfigController {

    private static final String RETURN_SCHEDULER_TIME_KEY = "return.scheduler.time";
    private static final String RETURN_MESSAGE_HEADER_KEY = "return.message.header";
    private static final String RETURN_MESSAGE_BODY_KEY = "return.message.body";
    private static final String RETURN_BUTTON_YES_TITLE_KEY = "return.button.yes.title";
    private static final String RETURN_BUTTON_LATER_TITLE_KEY = "return.button.later.title";
    private static final String RETURN_BUTTON_NO_TITLE_KEY = "return.button.no.title";
    private static final String SESSION_INACTIVITY_TIMEOUT_KEY = "session.inactivity.timeout.minutes";

    private final SystemConfigurationService configurationService;

    @GetMapping("/configuraciones")
    public String view(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", loadForm());
        }
        return "admin/configuraciones";
    }

    @PostMapping("/configuraciones")
    public String save(@ModelAttribute ConfigurationForm form, RedirectAttributes redirectAttributes) {
        configurationService.save(RETURN_SCHEDULER_TIME_KEY, form.getReturnSchedulerTime());
        configurationService.save(RETURN_MESSAGE_HEADER_KEY, form.getReturnMessageHeader());
        configurationService.save(RETURN_MESSAGE_BODY_KEY, form.getReturnMessageBody());
        configurationService.save(RETURN_BUTTON_YES_TITLE_KEY, form.getReturnButtonYesTitle());
        configurationService.save(RETURN_BUTTON_LATER_TITLE_KEY, form.getReturnButtonLaterTitle());
        configurationService.save(RETURN_BUTTON_NO_TITLE_KEY, form.getReturnButtonNoTitle());
        configurationService.save(SESSION_INACTIVITY_TIMEOUT_KEY, form.getSessionInactivityTimeoutMinutes());

        redirectAttributes.addFlashAttribute("successMessage", "Configuraciones guardadas correctamente.");
        return "redirect:/admin/configuraciones";
    }

    private ConfigurationForm loadForm() {
        ConfigurationForm form = new ConfigurationForm();
        form.setReturnSchedulerTime(configurationService.getValue(RETURN_SCHEDULER_TIME_KEY, "15:00"));
        form.setReturnMessageHeader(configurationService.getValue(RETURN_MESSAGE_HEADER_KEY, "Confirmación de vuelta"));
        form.setReturnMessageBody(configurationService.getValue(RETURN_MESSAGE_BODY_KEY,
                "Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?\nElegí una opción para que podamos organizar las butacas."));
        form.setReturnButtonYesTitle(configurationService.getValue(RETURN_BUTTON_YES_TITLE_KEY, "SÍ, VOLVER ✅"));
        form.setReturnButtonLaterTitle(configurationService.getValue(RETURN_BUTTON_LATER_TITLE_KEY, "OTRO DÍA 📅"));
        form.setReturnButtonNoTitle(configurationService.getValue(RETURN_BUTTON_NO_TITLE_KEY, "NO, CANCELAR ❌"));
        form.setSessionInactivityTimeoutMinutes(configurationService.getValue(SESSION_INACTIVITY_TIMEOUT_KEY, "30"));
        return form;
    }

    @Getter
    @Setter
    public static class ConfigurationForm {
        private String returnSchedulerTime;
        private String returnMessageHeader;
        private String returnMessageBody;
        private String returnButtonYesTitle;
        private String returnButtonLaterTitle;
        private String returnButtonNoTitle;
        private String sessionInactivityTimeoutMinutes;
    }
}
