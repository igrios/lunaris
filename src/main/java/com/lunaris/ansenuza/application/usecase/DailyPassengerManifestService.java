package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Manifiesto PDF operativo consolidado, generado sin alterar el esquema de datos. */
@Service
public class DailyPassengerManifestService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(LocalDate date, List<Reservation> reservations) {
        StringBuilder stream = new StringBuilder("BT /F1 9 Tf 30 550 Td ");
        line(stream, "Lunaris Ansenuza - Manifiesto Diario de Pasajeros");
        line(stream, "Fecha: " + DATE.format(date) + " | Pasajeros únicos: "
                + uniquePassengers(reservations) + " | Butacas reservadas: " + reservedSeats(reservations));
        section(stream, "TRAMOS DE IDA (Pueblos -> Cordoba)", reservations, false);
        section(stream, "TRAMOS DE VUELTA (Cordoba -> Pueblos)", reservations, true);
        stream.append("ET");
        return buildPdf(stream.toString());
    }

    /** Personas físicas, deduplicadas entre los tramos de ida y vuelta del manifiesto. */
    public long uniquePassengers(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Objects::nonNull)
                .map(Reservation::getPassenger)
                .filter(Objects::nonNull)
                .map(Passenger::getId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    /** Suma de butacas de todos los tramos, sin deduplicar pasajeros. */
    public int reservedSeats(List<Reservation> reservations) {
        return reservations.stream()
                .filter(Objects::nonNull)
                .map(Reservation::getPassengerCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void section(StringBuilder out, String title, List<Reservation> all, boolean returns) {
        line(out, "");
        line(out, title);
        line(out, "Horario | Pasajero | Telefono | Origen/Punto de ascenso | Destino | Asientos | Pago");
        all.stream().filter(r -> isReturn(r) == returns)
                .sorted(Comparator.comparing(this::schedule).thenComparing(this::name))
                .forEach(r -> line(out, schedule(r) + " | " + name(r) + " | " + phone(r) + " | "
                        + text(r.getPickupLocality()) + " - " + text(r.getPickupAddress()) + " | "
                        + text(r.getDestination()) + " | " + r.getTotalSeats() + " | "
                        + (Boolean.TRUE.equals(r.getPaymentVerified()) ? "VERIFICADO" : "PENDIENTE")));
    }

    private byte[] buildPdf(String stream) {
        byte[] content = stream.getBytes(StandardCharsets.ISO_8859_1);
        String objects = "1 0 obj<< /Type /Catalog /Pages 2 0 R>>endobj\n"
                + "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1>>endobj\n"
                + "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources<< /Font<< /F1 4 0 R>>>> /Contents 5 0 R>>endobj\n"
                + "4 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica>>endobj\n"
                + "5 0 obj<< /Length " + content.length + ">>stream\n" + stream + "\nendstream endobj\n";
        String header = "%PDF-1.4\n";
        int start = header.length();
        String xref = "xref\n0 6\n0000000000 65535 f \n"
                + String.format("%010d 00000 n \n", start)
                + String.format("%010d 00000 n \n", start + objects.indexOf("2 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("3 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("4 0 obj"))
                + String.format("%010d 00000 n \n", start + objects.indexOf("5 0 obj"));
        String trailer = "trailer<< /Size 6 /Root 1 0 R>>\nstartxref\n" + (start + objects.length()) + "\n%%EOF";
        return (header + objects + xref + trailer).getBytes(StandardCharsets.ISO_8859_1);
    }

    private void line(StringBuilder out, String value) {
        out.append('(').append(value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)"))
                .append(") Tj 0 -13 Td ");
    }
    private boolean isReturn(Reservation r) { return "VUELTA".equalsIgnoreCase(r.getRouteDirection()) || TripRouteCalculatorService.isCordoba(r.getPickupLocality()); }
    private String schedule(Reservation r) { return text(r.getDepartureSchedule()).isBlank() ? "03:00" : text(r.getDepartureSchedule()); }
    private String name(Reservation r) { return r.getPassenger() == null ? "Sin pasajero" : (text(r.getPassenger().getFirstName()) + " " + text(r.getPassenger().getLastName())).trim(); }
    private String phone(Reservation r) { return r.getPassenger() == null ? "" : text(r.getPassenger().getPhone()); }
    private String text(String value) { return value == null ? "" : value.trim(); }
}
