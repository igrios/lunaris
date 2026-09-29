package com.lunaris.ansenuza.service.interurban;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

class InterurbanPaymentConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(
            InterurbanPaymentConfiguration.class, MercadoPagoWebhookController.class, InterurbanPaymentExceptionHandler.class);

    @Test void noFlagsRegistersNoPaymentComponents() {
        runner.run(context -> assertThat(context).hasNotFailed()
                .doesNotHaveBean(MercadoPagoWebhookService.class).doesNotHaveBean(MercadoPagoWebhookController.class)
                .doesNotHaveBean(InterurbanPaymentRepository.class).doesNotHaveBean(QrGeneratorService.class));
    }

    @Test void capacityFlagAloneDoesNotActivatePaymentsOrRequireSecrets() {
        runner.withPropertyValues("lunaris.interurban.enabled=true").run(context -> assertThat(context)
                .hasNotFailed().doesNotHaveBean(MercadoPagoWebhookService.class).doesNotHaveBean(MercadoPagoWebhookController.class));
    }

    @Test void paymentFlagCannotOverrideDisabledModule() {
        runner.withPropertyValues("lunaris.interurban.enabled=false", "lunaris.interurban.payments.enabled=true")
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(MercadoPagoWebhookService.class));
    }

    @Test void enabledRegistersComponentsWithoutContactingDatabaseOrProvider() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        runner.withPropertyValues("lunaris.interurban.enabled=true", "lunaris.interurban.payments.enabled=true",
                "lunaris.interurban.payments.access-token=test-token", "lunaris.interurban.payments.collector-id=999",
                "lunaris.interurban.payments.webhook-secret=test-secret", "lunaris.interurban.payments.qr-key-id=test-key",
                "lunaris.interurban.payments.qr-encryption-key=" + java.util.Base64.getEncoder().encodeToString(new byte[32]))
                .withBean(NamedParameterJdbcTemplate.class, () -> jdbc)
                .withBean(PlatformTransactionManager.class, () -> mock(PlatformTransactionManager.class))
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(MercadoPagoWebhookService.class)
                            .hasSingleBean(QrGeneratorService.class).hasSingleBean(MercadoPagoWebhookController.class);
                    verifyNoInteractions(jdbc);
                });
    }
}
