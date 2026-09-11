package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.OperatorNotification;
import com.lunaris.ansenuza.domain.repository.OperatorNotificationPhoneRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Executor;

@Service @Slf4j
public class OperatorNotificationService {
    private final OperatorNotificationPhoneRepository phones;
    private final WhatsAppService whatsApp;
    private final Executor executor;

    public OperatorNotificationService(OperatorNotificationPhoneRepository phones, WhatsAppService whatsApp,
            @Qualifier("taskExecutor") Executor executor) {
        this.phones = phones;
        this.whatsApp = whatsApp;
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyAfterCommit(OperatorNotification notification) {
        try {
            executor.execute(() -> send(notification));
        } catch (RuntimeException exception) {
            log.error("No se pudo programar la alerta de operadores", exception);
        }
    }

    private void send(OperatorNotification notification) {
        try {
            for (var operator : phones.findByActiveTrueOrderByCreatedAtAsc()) {
                try {
                    whatsApp.sendMessage(operator.getPhone(), notification.message());
                } catch (RuntimeException exception) {
                    log.error("No se pudo alertar al operador {}", operator.getId(), exception);
                }
            }
        } catch (RuntimeException exception) {
            log.error("No se pudieron consultar los operadores activos", exception);
        }
    }
}
