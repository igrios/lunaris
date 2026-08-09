package com.lunaris.ansenuza.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import com.lunaris.ansenuza.infrastructure.web.controller.WaitingListController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class WaitingListOtpServiceTest {

    @Test
    void requestAcceptsPhoneAndPhoneNumberProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        var withPhone = mapper.readValue("{\"phone\":\"3511111111\"}",
                WaitingListController.OtpRequest.class);
        var withPhoneNumber = mapper.readValue("{\"phoneNumber\":\"3522222222\"}",
                WaitingListController.OtpRequest.class);

        assertEquals("3511111111", withPhone.phone());
        assertEquals("3522222222", withPhoneNumber.phone());
    }

    @Test
    void storesFourDigitOtpAndDispatchesItThroughWhatsAppService() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        WaitingListOtpService service = new WaitingListOtpService(
                whatsAppService, Duration.ofMinutes(5));

        String otp = service.request("+54 9 351-2282251");

        verify(whatsAppService).sendOtpMessage(
                eq("543512282251"), matches("[0-9]{4}"));
        assertDoesNotThrow(() -> service.verify("351 228-2251", otp));
        assertThrows(DomainValidationException.class,
                () -> service.verify("351 228-2251", otp));
    }
}
