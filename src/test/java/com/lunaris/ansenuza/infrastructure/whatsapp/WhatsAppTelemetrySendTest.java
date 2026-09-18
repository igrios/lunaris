package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lunaris.ansenuza.application.port.Button;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class WhatsAppTelemetrySendTest {
    private final RestTemplate rest = mock(RestTemplate.class);
    private final WhatsAppService service = new WhatsAppService(rest, () -> 0L, ignored -> {});
    private final List<Button> buttons = List.of(new Button("confirm_ok", "Confirmar"));

    @AfterEach
    void clearTransaction() { TransactionSynchronizationManager.clear(); }

    @Test
    void acceptedRequestReportsSuccessExactlyOnce() {
        setup();
        when(rest.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        Consumer<Boolean> outcome = mock(Consumer.class);
        service.sendButtons("3515551234", "Resumen", "Contenido", buttons, outcome);
        verify(outcome).accept(true);
        verifyNoMoreInteractions(outcome);
    }

    @Test
    void networkFailureDoesNotReportASentPrice() {
        setup();
        when(rest.postForEntity(anyString(), any(), eq(String.class))).thenThrow(new ResourceAccessException("offline"));
        Consumer<Boolean> outcome = mock(Consumer.class);
        service.sendButtons("3515551234", "Precio", "Contenido", buttons, outcome);
        verify(outcome).accept(false);
        verify(outcome, never()).accept(true);
    }

    @Test
    void deferredSendCarriesItsCallbackUntilActualPostAfterCommit() throws Exception {
        setup();
        when(rest.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        service.sendButtons("3515551234", "Resumen", "Contenido", buttons, result::complete);
        assertThat(result).isNotDone();
        verifyNoInteractions(rest);
        var synchronizations = TransactionSynchronizationManager.getSynchronizations();
        TransactionSynchronizationManager.clear();
        synchronizations.forEach(TransactionSynchronization::afterCommit);
        assertThat(result.get(10, TimeUnit.SECONDS)).isTrue();
        verify(rest).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void rollbackDoesNotSendOrEmitSuccess() {
        setup();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        Consumer<Boolean> outcome = mock(Consumer.class);
        service.sendButtons("3515551234", "Resumen", "Contenido", buttons, outcome);
        TransactionSynchronizationManager.getSynchronizations().forEach(
                sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(rest, outcome);
    }

    @Test
    void analyticsCallbackFailureDoesNotFailOrRetrySuccessfulSend() {
        setup();
        when(rest.postForEntity(anyString(), any(), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        assertThatCode(() -> service.sendButtons("3515551234", "Resumen", "Contenido", buttons,
                sent -> { throw new IllegalStateException("analytics unavailable"); })).doesNotThrowAnyException();
        verify(rest).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void devTrackedSendStaysInSimulatorWithoutNetwork() {
        var dev = new WhatsAppServiceDevMock();
        Consumer<Boolean> outcome = mock(Consumer.class);
        dev.sendButtons("3515551234", "Resumen", "Contenido", buttons, outcome);
        verify(outcome).accept(true);
        assertThat(dev.messagesFor("3515551234")).hasSize(1);
    }

    private void setup() {
        ReflectionTestUtils.setField(service, "phoneNumberId", "test-id");
        ReflectionTestUtils.setField(service, "accessToken", "test-token");
    }
}
