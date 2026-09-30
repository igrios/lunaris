package com.lunaris.ansenuza.service.interurban;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanPaymentConfiguration {
    @Bean public InterurbanPaymentRepository interurbanPaymentRepository(NamedParameterJdbcTemplate jdbc) {
        return new JdbcInterurbanPaymentRepository(jdbc);
    }

    @Bean public QrArtifactCipher interurbanQrCipher(
            @Value("${lunaris.interurban.payments.qr-encryption-key}") String key,
            @Value("${lunaris.interurban.payments.qr-key-id}") String keyId) {
        return new QrArtifactCipher(key, keyId);
    }

    @Bean public QrGeneratorService interurbanQrGenerator(InterurbanPaymentRepository repository, QrArtifactCipher cipher) {
        return new QrGeneratorService(repository, cipher, Clock.systemUTC());
    }

    @Bean public MercadoPagoSignatureValidator interurbanPaymentSignatures(
            @Value("${lunaris.interurban.payments.webhook-secret}") String secret,
            @Value("${lunaris.interurban.payments.signature-max-age:PT24H}") String maxAge) {
        return new MercadoPagoSignatureValidator(secret, Clock.systemUTC(), Duration.parse(maxAge));
    }

    @Bean public PaymentQueryPort interurbanPaymentQuery(
            @Value("${lunaris.interurban.payments.access-token}") String accessToken) {
        if (accessToken.isBlank()) throw new IllegalArgumentException("Access token MP obligatorio.");
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return new MercadoPagoPaymentClient(RestClient.builder().baseUrl("https://api.mercadopago.com")
                .requestFactory(factory).defaultHeader("Authorization", "Bearer " + accessToken).build());
    }

    @Bean public MercadoPagoWebhookService interurbanPaymentWebhook(MercadoPagoSignatureValidator signatures,
            PaymentQueryPort payments, InterurbanPaymentRepository repository, QrGeneratorService qr,
            PlatformTransactionManager transactions,
            @Value("${lunaris.interurban.payments.collector-id}") String collectorId,
            @Value("${lunaris.interurban.payments.live-mode:true}") boolean liveMode) {
        if (!collectorId.matches("[0-9]+")) throw new IllegalArgumentException("Collector ID MP obligatorio.");
        return new MercadoPagoWebhookService(signatures, payments, repository, qr, transactions,
                collectorId, liveMode, Clock.systemUTC());
    }
}
