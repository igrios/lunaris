package com.lunaris.ansenuza.application.conversation.steps;

import com.lunaris.ansenuza.application.conversation.BotRoute;

import java.util.Optional;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import org.springframework.stereotype.Component;
import com.lunaris.ansenuza.application.conversation.ConversationStepHandler;
import com.lunaris.ansenuza.application.conversation.IncomingMessage;
import com.lunaris.ansenuza.application.port.MessagingPort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;

/** SELECT_SCHEDULE: el pasajero elige el horario; bifurca según ya exista o no como pasajero. */
@Component
@RequiredArgsConstructor
public class SelectScheduleHandler implements ConversationStepHandler {

    private final ConversationSessionRepository conversationSessionRepository;
    private final PassengerRepository passengerRepository;
    private final MessagingPort messaging;
    private final ScheduleService scheduleService;

    @Override
    public String step() {
        return "SELECT_SCHEDULE";
    }

    @Override
    public void handle(ConversationSession session, IncomingMessage message) {
        String phoneNumber = session.getPhoneNumber();
        String body = message.body().trim().toLowerCase();

        if (BotRoute.fromCordoba(session.getPickupLocality())) {
            String selected = scheduleService.getSchedulesForBot(
                    session.getPickupLocality(), session.getDestination(), session.getTravelDate()).stream()
                    .filter(schedule -> ("schedule_" + schedule.substring(0, 5).replace(':', '_')).equals(body))
                    .findFirst().orElse(null);
            if (selected == null) {
                return;
            }
            session.setScheduleBlock(selected);
        } else if ("schedule_03_00".equals(body) || "time_0300".equals(body)) {
            session.setScheduleBlock("03:00 AM");
        } else if ("schedule_08_00".equals(body) || "time_0800".equals(body)) {
            session.setScheduleBlock("08:00 AM");
        } else {
            return;
        }

        Optional<Passenger> existingPassenger = passengerRepository.findByPhone(phoneNumber);
        if (existingPassenger.isPresent()) {
            session.setPassengerName(existingPassenger.get().getFirstName() + " "
                    + existingPassenger.get().getLastName());
            session.setCurrentStep("ASK_COMPANIONS_COUNT");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "🔢 *Escribí cuántas personas viajan con vos, o 0 si estás solo (0)*");
        } else {
            session.setCurrentStep("ASK_NAME");
            conversationSessionRepository.saveAndFlush(session);
            messaging.sendText(phoneNumber,
                    "👤 *Ingresá Nombre y Apellido del pasajero titular.*\n\n_Ejemplo: Juan Pérez_");
        }
    }
}
