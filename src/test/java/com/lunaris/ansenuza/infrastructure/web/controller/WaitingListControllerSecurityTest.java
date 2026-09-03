package com.lunaris.ansenuza.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunaris.ansenuza.application.usecase.WaitingListOtpService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import org.junit.jupiter.api.Test;

class WaitingListControllerSecurityTest {

    @Test
    void otpIsNeverSerializedInHttpResponse() throws Exception {
        WaitingListOtpService otpService = mock(WaitingListOtpService.class);
        when(otpService.request("3511111111", "Ada Lovelace")).thenReturn("928341");
        WaitingListController controller = new WaitingListController(
                mock(WaitingListService.class), mock(WaitingListReengagementService.class),
                otpService);

        var response = controller.requestOtp(
                new WaitingListController.OtpRequest(
                        "Ada Lovelace", "3511111111", null, null));
        String json = new ObjectMapper().writeValueAsString(response);

        assertFalse(json.contains("928341"));
        assertFalse(json.contains("otp"));
        verify(otpService).request("3511111111", "Ada Lovelace");
    }
}
