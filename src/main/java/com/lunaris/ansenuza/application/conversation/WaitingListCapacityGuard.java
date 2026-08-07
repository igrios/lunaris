package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.CapacityLockRepository;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitingListCapacityGuard {

    private final ReservationRepository reservationRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ConversationSessionRepository conversationSessionRepository;
    private final MessagingPort messaging;
    private final WaitingListService waitingListService;
    private final CapacityLockRepository capacityLockRepository;

    public WaitingListCapacityGuard(ReservationRepository reservationRepository,
            SystemConfigurationService systemConfigurationService,
            ConversationSessionRepository conversationSessionRepository,
            MessagingPort messaging, WaitingListService waitingListService) {
        this(reservationRepository, systemConfigurationService, conversationSessionRepository,
                messaging, waitingListService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public WaitingListCapacityGuard(ReservationRepository reservationRepository,
            SystemConfigurationService systemConfigurationService,
            ConversationSessionRepository conversationSessionRepository,
            MessagingPort messaging, WaitingListService waitingListService,
            CapacityLockRepository capacityLockRepository) {
        this.reservationRepository = reservationRepository;
        this.systemConfigurationService = systemConfigurationService;
        this.conversationSessionRepository = conversationSessionRepository;
        this.messaging = messaging;
        this.waitingListService = waitingListService;
        this.capacityLockRepository = capacityLockRepository;
    }

    @Transactional
    public boolean offerWaitingListWhenFull(ConversationSession session) {
        int requestedSeats = session.getPassengerCount() == null
                ? 1 : Math.max(1, session.getPassengerCount());
        String schedule = session.getScheduleBlock() == null
                || session.getScheduleBlock().isBlank()
                ? "03:00 AM" : session.getScheduleBlock().trim();
        // La fila se bloquea dentro de la misma transacción que crea la reserva.
        // Los tests unitarios antiguos no proveen el repositorio, por eso conservan
        // el comportamiento de conteo sin persistencia.
        if (capacityLockRepository != null && session.getTravelDate() != null) {
            String direction = isCordoba(session.getPickupLocality()) ? "RETURN" : "OUTBOUND";
            String key = session.getTravelDate() + "|" + normalize(schedule) + "|" + direction;
            capacityLockRepository.ensureExists(key);
            if (capacityLockRepository.findForUpdate(key) == null) {
                throw new IllegalStateException("No se pudo bloquear la capacidad del turno.");
            }
        }
        int occupiedSeats = Math.toIntExact(reservationRepository.countReservedSeats(
                session.getTravelDate(), schedule));
        int maxCapacity = systemConfigurationService.getPrimaryVehicleCapacity();

        if (occupiedSeats + requestedSeats <= maxCapacity) {
            return false;
        }

        waitingListService.join(session);
        messaging.sendText(session.getPhoneNumber(),
                "⏳ La unidad principal de " + maxCapacity + " pasajeros para el " + schedule
                        + " está completa. Te agregamos a la Lista de Espera y te avisaremos "
                        + "por WhatsApp cuando se libere un lugar.");
        conversationSessionRepository.delete(session);
        conversationSessionRepository.flush();
        return true;
    }

    private static boolean isCordoba(String locality) {
        return locality != null && normalize(locality).contains("cordoba");
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
