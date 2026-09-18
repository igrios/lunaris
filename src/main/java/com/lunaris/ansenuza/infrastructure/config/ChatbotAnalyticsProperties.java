package com.lunaris.ansenuza.infrastructure.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chatbot.analytics")
@Getter
@Setter
public class ChatbotAnalyticsProperties {
    private boolean enabled = true;
    private String hmacSecret = "";
    private short subjectKeyVersion = 1;
    private String environment = "local";
    private Duration inactivityTimeout = Duration.ofMinutes(30);
    private Duration retention = Duration.ofDays(90);
    private List<String> internalPhones = List.of();
}

