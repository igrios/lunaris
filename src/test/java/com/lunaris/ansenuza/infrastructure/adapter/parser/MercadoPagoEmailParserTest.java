package com.lunaris.ansenuza.infrastructure.adapter.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MercadoPagoEmailParserTest {

    private final MercadoPagoEmailParser parser = new MercadoPagoEmailParser();

    @Test
    void parsesArgentineMercadoPagoNotification() {
        String body = """
                Recibiste un pago de Ada Lovelace
                Monto: $ 10.500,00
                Número de operación: MP-998877
                Código de reserva: MOR-COR-001-IDA
                """;

        var result = parser.parse("message-123", "Recibiste dinero", body, Instant.EPOCH);

        assertTrue(result.isPresent());
        assertEquals("MP-998877", result.get().transactionId());
        assertEquals(new BigDecimal("10500.00"), result.get().amount());
        assertEquals("Ada Lovelace", result.get().payerName());
        assertEquals("MOR-COR-001-IDA", result.get().reservationCode());
    }

    @Test
    void rejectsIncompleteEmail() {
        assertTrue(parser.parse(
                "message-124", "Recibiste dinero", "Monto: $ 10.500,00", Instant.EPOCH)
                .isEmpty());
    }
}
