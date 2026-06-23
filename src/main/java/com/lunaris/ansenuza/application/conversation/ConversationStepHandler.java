package com.lunaris.ansenuza.application.conversation;

import com.lunaris.ansenuza.domain.model.ConversationSession;

/**
 * Estrategia que resuelve un único paso (currentStep) de la máquina de estados
 * conversacional del bot. Cada implementación es responsable de un solo estado,
 * evitando la god class original.
 *
 * <p>El {@link ConversationOrchestrator} registra todos los handlers por su
 * {@link #step()} y rutea cada mensaje entrante al que corresponda.
 */
public interface ConversationStepHandler {

    /** Identificador del paso (currentStep) que este handler atiende. */
    String step();

    /** Procesa el mensaje entrante para la sesión dada (mutando y persistiendo la sesión). */
    void handle(ConversationSession session, IncomingMessage message);
}
