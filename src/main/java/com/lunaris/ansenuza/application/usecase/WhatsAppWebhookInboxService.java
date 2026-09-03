package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.infrastructure.persistence.repository.WhatsAppWebhookInboxRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WhatsAppWebhookInboxService {

    private final WhatsAppWebhookInboxRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(String messageId) {
        return messageId != null && !messageId.isBlank()
                && repository.claim(messageId, Instant.now()) == 1;
    }
}
