package com.lunaris.ansenuza.infrastructure.whatsapp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class WhatsAppMessageDispatcherTest {

    @Test
    void processesMessagesForSamePhoneSequentiallyEvenAfterAnError() throws Exception {
        WhatsAppMessageDispatcher dispatcher = new WhatsAppMessageDispatcher();
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch completed = new CountDownLatch(1);
        try {
            dispatcher.dispatch("543512282251", () -> {
                order.add(1);
                firstStarted.countDown();
                await(releaseFirst);
                throw new IllegalStateException("fallo simulado");
            });
            assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
            dispatcher.dispatch("543512282251", () -> {
                order.add(2);
                completed.countDown();
            });

            releaseFirst.countDown();

            assertTrue(completed.await(2, TimeUnit.SECONDS));
            assertEquals(List.of(1, 2), order);
        } finally {
            dispatcher.close();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
