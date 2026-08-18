package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("dev")
public class WhatsAppSimulatorDevViewController {

    @GetMapping("/admin/bot-simulator")
    public String simulator() {
        return "admin/bot-simulator";
    }
}
