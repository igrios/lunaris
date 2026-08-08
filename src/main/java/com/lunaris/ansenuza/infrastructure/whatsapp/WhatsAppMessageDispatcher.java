package com.lunaris.ansenuza.infrastructure.whatsapp;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WhatsAppMessageDispatcher {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentHashMap<String, CompletableFuture<Void>> pendingByPhone =
            new ConcurrentHashMap<>();

    @Async("taskExecutor")
    public void dispatch(String phoneNumber, Runnable processing) {
        String key = phoneNumber == null ? "" : phoneNumber;
        pendingByPhone.compute(key, (ignored, previous) -> {
            CompletableFuture<Void> start = previous == null
                    ? CompletableFuture.completedFuture(null)
                    : previous.handle((result, error) -> null);
            CompletableFuture<Void> next = start.thenRunAsync(() -> {
                try {
                    processing.run();
                } catch (Exception exception) {
                    log.error("Error asincrónico procesando webhook para {}.", key, exception);
                }
            }, executor);
            next.whenCompleteAsync(
                    (result, error) -> pendingByPhone.remove(key, next), executor);
            return next;
        });
    }

    @PreDestroy
    void close() {
        executor.close();
    }
}
