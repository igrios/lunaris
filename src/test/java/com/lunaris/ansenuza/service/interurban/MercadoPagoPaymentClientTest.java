package com.lunaris.ansenuza.service.interurban;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MercadoPagoPaymentClientTest {
    @Test void queriesAuthoritativePaymentWithBearerAndMapsNumericIds() {
        var builder = RestClient.builder().baseUrl("https://api.mercadopago.com").defaultHeader("Authorization", "Bearer test-token");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.mercadopago.com/v1/payments/12345"))
                .andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("""
                        {"id":12345,"status":"approved","external_reference":"INTERURBAN:fixture",
                         "transaction_amount":100.00,"currency_id":"ARS","collector_id":999,
                         "live_mode":false,"transaction_amount_refunded":0,"unused_field":"ignored"}
                        """, MediaType.APPLICATION_JSON));
        var payment = new MercadoPagoPaymentClient(builder.build()).getPayment("12345");
        assertEquals("12345", payment.id());
        assertEquals("999", payment.collectorId());
        assertEquals(0, new BigDecimal("100").compareTo(payment.amount()));
        server.verify();
    }

    @Test void providerErrorDoesNotExposeItsBody() {
        var builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.mercadopago.com/v1/payments/12345"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("sensitive-provider-response"));
        var error = assertThrows(InterurbanPaymentException.class,
                () -> new MercadoPagoPaymentClient(builder.build()).getPayment("12345"));
        assertEquals("PROVIDER_UNAVAILABLE", error.getMessage());
        assertNull(error.getCause());
        server.verify();
    }
}
