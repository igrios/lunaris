package com.lunaris.ansenuza.infrastructure.web.dto.hojaruta;

import java.time.LocalDate;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import lombok.Value;

@Value
public class HojaRutaViewModel {
    LocalDate fechaSeleccionada;
    long totalYendo;
    long totalVolviendo;
    boolean hubActivado;
    long pasajeros0800Count;
    List<Reservation> reservas;
    List<ConversationSession> sesionesChat;
}