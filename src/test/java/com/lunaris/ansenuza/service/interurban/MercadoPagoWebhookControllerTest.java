package com.lunaris.ansenuza.service.interurban;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MercadoPagoWebhookControllerTest {
    final MercadoPagoWebhookService service = mock(MercadoPagoWebhookService.class);
    final MockMvc mvc = MockMvcBuilders.standaloneSetup(new MercadoPagoWebhookController(service))
            .setControllerAdvice(new InterurbanPaymentExceptionHandler()).build();
    final String body = """
            {"id":99,"type":"payment","action":"payment.updated","data":{"id":"12345"}}
            """;

    @Test void durableAcceptanceReturnsEmpty200WithoutQrOrToken() throws Exception {
        mvc.perform(post("/webhook/interurban/mercadopago").queryParam("data.id", "12345")
                .header("x-request-id", "request").header("x-signature", "signature")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(content().string(""));
        verify(service).receive(eq("12345"), eq("request"), eq("signature"), any());
    }

    @Test void invalidSignatureIsUnauthorized() throws Exception {
        doThrow(new InterurbanPaymentException(InterurbanPaymentException.Code.INVALID_SIGNATURE))
                .when(service).receive(any(), any(), any(), any());
        mvc.perform(post("/webhook/interurban/mercadopago").queryParam("data.id", "12345")
                .header("x-request-id", "request").header("x-signature", "bad")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isUnauthorized());
    }

    @Test void inboxFailureReturnsRetryableStatus() throws Exception {
        doThrow(new DataAccessResourceFailureException("fixture")).when(service).receive(any(), any(), any(), any());
        mvc.perform(post("/webhook/interurban/mercadopago").queryParam("data.id", "12345")
                .header("x-request-id", "request").header("x-signature", "signature")
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isServiceUnavailable());
    }
}
