package com.lunaris.ansenuza.infrastructure.adapter.mail;

import com.lunaris.ansenuza.application.payment.ProcessBankEmailUseCase;
import com.lunaris.ansenuza.infrastructure.adapter.parser.MercadoPagoEmailParser;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.dsl.Mail;

@Configuration
@EnableConfigurationProperties(MercadoPagoImapProperties.class)
@ConditionalOnProperty(prefix = "app.payment.imap", name = "enabled", havingValue = "true")
public class MercadoPagoImapAdapterConfig {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoImapAdapterConfig.class);
    private static final Set<String> TRUSTED_DOMAINS = Set.of(
            "mercadopago.com", "mercadolibre.com");
    private static final Set<String> TRUSTED_TEST_SENDERS = Set.of(
            "ignarios1@gmail.com");

    @Bean
    IntegrationFlow mercadoPagoImapFlow(
            MercadoPagoImapProperties properties,
            MercadoPagoEmailParser parser,
            ProcessBankEmailUseCase useCase) {
        validate(properties);
        ImapMailReceiver receiver = new ImapMailReceiver(imapUrl(properties));
        receiver.setShouldDeleteMessages(false);
        receiver.setShouldMarkMessagesAsRead(true);
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.imaps.ssl.enable", "true");
        mailProperties.setProperty("mail.imaps.connectiontimeout", "10000");
        mailProperties.setProperty("mail.imaps.timeout", "10000");
        receiver.setJavaMailProperties(mailProperties);

        return IntegrationFlow
                .from(Mail.imapInboundAdapter(receiver), endpoint -> endpoint.poller(
                        Pollers.fixedDelay(Duration.ofMillis(properties.pollDelay()))
                                .maxMessagesPerPoll(10)))
                .handle(MimeMessage.class, (message, headers) -> {
                    ingest(message, properties, parser, useCase);
                    return null;
                })
                .get();
    }

    private void ingest(
            MimeMessage message,
            MercadoPagoImapProperties properties,
            MercadoPagoEmailParser parser,
            ProcessBankEmailUseCase useCase) {
        try {
            String sender = sender(message);
            if (!isAllowedSender(sender, properties.testSenders())) {
                log.debug("Ignoring payment email from untrusted sender {}", sender);
                return;
            }
            String messageId = message.getMessageID();
            if (messageId == null || messageId.isBlank()) {
                log.warn("Ignoring payment email without Message-ID from {}", sender);
                return;
            }
            Instant receivedAt = message.getReceivedDate() == null
                    ? Instant.now() : message.getReceivedDate().toInstant();
            parser.parse(messageId, message.getSubject(), content(message), receivedAt)
                    .ifPresentOrElse(useCase::process,
                            () -> log.warn("Could not parse payment email {}", messageId));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not ingest Mercado Pago IMAP message", exception);
        }
    }

    boolean isAllowedSender(String sender, String configuredTestSenders) {
        String normalized = sender.toLowerCase(Locale.ROOT);
        int separator = normalized.lastIndexOf('@');
        String senderDomain = separator < 0 ? "" : normalized.substring(separator + 1);
        boolean trustedDomain = TRUSTED_DOMAINS.stream()
                .anyMatch(domain -> senderDomain.equals(domain)
                        || senderDomain.endsWith("." + domain));
        if (trustedDomain) {
            return true;
        }
        return TRUSTED_TEST_SENDERS.contains(normalized)
                || testSenders(configuredTestSenders).contains(normalized);
    }

    private Set<String> testSenders(String configured) {
        if (configured == null || configured.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(configured.split(","))
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String sender(Message message) throws Exception {
        Address[] from = message.getFrom();
        if (from == null || from.length == 0) {
            return "";
        }
        if (from[0] instanceof InternetAddress address) {
            return address.getAddress();
        }
        return from[0].toString();
    }

    private String content(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return String.valueOf(part.getContent());
        }
        if (part.isMimeType("text/html")) {
            return String.valueOf(part.getContent()).replaceAll("<[^>]+>", " ");
        }
        Object rawContent = part.getContent();
        if (rawContent instanceof Multipart multipart) {
            StringBuilder text = new StringBuilder();
            for (int index = 0; index < multipart.getCount(); index++) {
                BodyPart bodyPart = multipart.getBodyPart(index);
                if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    text.append(content(bodyPart)).append('\n');
                }
            }
            return text.toString();
        }
        return "";
    }

    private String imapUrl(MercadoPagoImapProperties properties) {
        try {
            return new URI("imaps", properties.username() + ":" + properties.password(),
                    properties.host(), properties.port(), "/INBOX", null, null).toString();
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid payment IMAP configuration", exception);
        }
    }

    private void validate(MercadoPagoImapProperties properties) {
        if (properties.username() == null || properties.username().isBlank()
                || properties.password() == null || properties.password().isBlank()) {
            throw new IllegalStateException(
                    "PAYMENT_IMAP_USERNAME and PAYMENT_IMAP_PASSWORD are required when IMAP is enabled");
        }
        if (properties.pollDelay() < 1000) {
            throw new IllegalArgumentException("IMAP poll delay must be at least 1000 ms");
        }
    }
}
