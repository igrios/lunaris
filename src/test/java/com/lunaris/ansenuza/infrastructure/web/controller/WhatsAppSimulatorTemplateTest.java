package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class WhatsAppSimulatorTemplateTest {

    @Test
    void exposesComposerAndInteractiveButtonHandling() throws Exception {
        String html = new ClassPathResource("templates/admin/bot-simulator.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("/api/v1/dev/whatsapp-simulator", "data-payload",
                "send-user-reply", "reset-session");
    }
}
