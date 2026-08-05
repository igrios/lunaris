package com.lunaris.ansenuza.infrastructure.adapter.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.mail.Folder;
import jakarta.mail.FolderClosedException;
import jakarta.mail.Part;
import org.junit.jupiter.api.Test;

class MercadoPagoImapAdapterConfigTest {

    @Test
    void allowsBuiltInManualTestSender() {
        var config = new MercadoPagoImapAdapterConfig();

        assertTrue(config.isAllowedSender("ignarios1@gmail.com", ""));
        assertTrue(config.isAllowedSender("IGNARIOS1@GMAIL.COM", ""));
    }

    @Test
    void closedFolderDuringLazyBodyReadIsHandledGracefully() throws Exception {
        Part part = mock(Part.class);
        when(part.isMimeType("text/plain")).thenReturn(true);
        when(part.getContent()).thenThrow(new FolderClosedException(mock(Folder.class)));

        assertEquals("", new MercadoPagoImapAdapterConfig().content(part));
    }
}
