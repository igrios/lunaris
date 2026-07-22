package com.lunaris.ansenuza.domain.model.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhatsAppConversationWindowService {

    private static final long WINDOW_HOURS = 24;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> expirationFor(String phoneNumber) {
        return chatMessageRepository
                .findFirstByPhoneNumberAndFromOperatorFalseOrderByTimestampDesc(phoneNumber)
                .map(message -> message.getTimestamp().plusHours(WINDOW_HOURS));
    }

    @Transactional(readOnly = true)
    public boolean isActive(String phoneNumber) {
        return expirationFor(phoneNumber)
                .map(expiration -> !LocalDateTime.now().isAfter(expiration))
                .orElse(false);
    }
}
