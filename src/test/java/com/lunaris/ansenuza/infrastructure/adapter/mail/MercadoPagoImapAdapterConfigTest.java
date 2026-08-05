package com.lunaris.ansenuza.infrastructure.adapter.mail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MercadoPagoImapAdapterConfigTest {

    @Test
    void allowsBuiltInManualTestSender() {
        var config = new MercadoPagoImapAdapterConfig();

        assertTrue(config.isAllowedSender("ignarios1@gmail.com", ""));
        assertTrue(config.isAllowedSender("IGNARIOS1@GMAIL.COM", ""));
    }
}
