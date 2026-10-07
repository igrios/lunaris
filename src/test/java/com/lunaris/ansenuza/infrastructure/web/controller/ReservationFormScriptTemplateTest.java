package com.lunaris.ansenuza.infrastructure.web.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservationFormScriptTemplateTest {
    @Test
    void thymeleafPreservesModeClickHandlersAsLiteralJavaScript() throws Exception {
        String template = Files.readString(Path.of("src/main/resources/templates/reservation-form.html"));
        int start = template.indexOf("<script");
        String script = template.substring(start, template.indexOf("</script>", start) + "</script>".length());
        String rendered = new SpringTemplateEngine().process(script, new Context());
        assertEquals(script.substring(script.indexOf('>') + 1, script.indexOf("</script>")),
                rendered.substring(rendered.indexOf('>') + 1, rendered.indexOf("</script>")));
    }
}
