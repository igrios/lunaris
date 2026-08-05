package com.lunaris.ansenuza.infrastructure.adapter.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.imap")
public record MercadoPagoImapProperties(
        boolean enabled,
        String host,
        int port,
        long pollDelay,
        String username,
        String password,
        String testSenders) {}
