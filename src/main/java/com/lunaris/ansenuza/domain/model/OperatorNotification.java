package com.lunaris.ansenuza.domain.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Snapshot inmutable: no transporta entidades JPA al hilo de envío. */
public record OperatorNotification(String message) {
    public static OperatorNotification inquiry(Inquiry inquiry) {
        return new OperatorNotification("""
                💬 *NUEVA CONSULTA / VIAJE ESPECIAL*
                👤 Pasajero: %s (%s)
                📝 Mensaje: "%s"
                👉 Responder en el panel: /admin/consultas""".formatted(
                text(inquiry.getPassengerName()), inquiry.getPhone(), inquiry.getMessage()));
    }

    public static OperatorNotification reservation(List<Reservation> reservations) {
        Reservation main = reservations.getFirst();
        Passenger passenger = main.getPassenger();
        BigDecimal total = reservations.stream().map(r -> amount(r.getAmount()).add(amount(r.getExtraAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OperatorNotification("""
                🚨 *NUEVA RESERVA REGISTRADA*
                👤 Pasajero: %s (%s)
                🚌 Viaje: %s ➡️ %s
                📅 Fecha: %s - %s
                💺 Asientos: %s
                💵 Monto: $%s""".formatted(
                passenger == null ? "Sin nombre" : text(passenger.getFirstName()) + " " + text(passenger.getLastName()),
                passenger == null ? "Sin teléfono" : text(passenger.getPhone()),
                text(main.getPickupLocality()), text(main.getDestination()),
                main.getTravelDate() == null ? "A confirmar" : main.getTravelDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                text(main.getDepartureSchedule()), main.getTotalSeats(), total.toPlainString()));
    }

    private static BigDecimal amount(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private static String text(String value) { return value == null || value.isBlank() ? "Sin informar" : value; }
}
