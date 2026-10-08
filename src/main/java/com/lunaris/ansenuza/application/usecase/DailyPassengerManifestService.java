package com.lunaris.ansenuza.application.usecase;

import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Manifiesto PDF operativo consolidado, generado sin alterar el esquema de datos. */
@Service
public class DailyPassengerManifestService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePdf(LocalDate date, List<Reservation> reservations) {
        return generatePdf(date, reservations, List.of());
    }

    public byte[] generatePdf(LocalDate date, List<Reservation> reservations, List<Reservation> specialTrips) {
        List<String> stream = new ArrayList<>();
        line(stream, "Lunaris Ansenuza - Manifiesto Diario de Pasajeros");
        line(stream, "Fecha: " + DATE.format(date) + " | Pasajeros únicos: "
                + uniquePassengers(reservations) + " | Butacas reservadas: " + reservedSeats(reservations));
        section(stream, "TRAMOS DE IDA (Pueblos -> Cordoba)", reservations, false);
        section(stream, "TRAMOS DE VUELTA (Cordoba -> Pueblos)", reservations, true);
        line(stream, "");
        line(stream, "Viajes Especiales");
        line(stream, "Horario | Codigo | Pasajero principal | Trayecto | Pasajeros | Pago");
        if (specialTrips.isEmpty()) {
            line(stream, "Sin viajes especiales activos para la fecha.");
        }
        specialTrips.stream().sorted(Comparator.comparing(this::schedule).thenComparing(this::name))
                .forEach(r -> line(stream, text(r.getDepartureSchedule()) + " | "
                        + text(r.getReservationCode()) + " | " + name(r) + " | "
                        + routeEndpoint(r.getOriginCustom(), r.getPickupLocality()) + " -> "
                        + routeEndpoint(r.getDestinationCustom(), r.getDestination()) + " | "
                        + r.getPassengerCount() + " | "
                        + (Boolean.TRUE.equals(r.getPaymentVerified()) ? "VERIFICADO" : "PENDIENTE")));
        return buildPdf(stream);
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

    private void section(List<String> out, String title, List<Reservation> all, boolean returns) {
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

    private byte[] buildPdf(List<String> lines) {
        int linesPerPage = 39;
        int pageCount = (lines.size() + linesPerPage - 1) / linesPerPage;
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R>>");
        StringBuilder kids = new StringBuilder();
        for (int page = 0; page < pageCount; page++) {
            kids.append(4 + page * 2).append(" 0 R ");
        }
        objects.add("<< /Type /Pages /Kids [" + kids + "] /Count " + pageCount + ">>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding>>");
        for (int page = 0; page < pageCount; page++) {
            StringBuilder stream = new StringBuilder("BT /F1 9 Tf 30 550 Td ");
            for (String value : lines.subList(page * linesPerPage,
                    Math.min(lines.size(), (page + 1) * linesPerPage))) {
                stream.append('(').append(value.replace("\\", "\\\\")
                        .replace("(", "\\(").replace(")", "\\)"))
                        .append(") Tj 0 -13 Td ");
            }
            stream.append("ET");
            objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] "
                    + "/Resources<< /Font<< /F1 3 0 R>>>> /Contents " + (5 + page * 2) + " 0 R>>");
            objects.add("<< /Length " + stream.toString().getBytes(StandardCharsets.ISO_8859_1).length
                    + ">>stream\n" + stream + "\nendstream");
        }
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.length());
            pdf.append(i + 1).append(" 0 obj").append(objects.get(i)).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n0000000000 65535 f \n");
        offsets.forEach(offset -> pdf.append(String.format("%010d 00000 n \n", offset)));
        pdf.append("trailer<< /Size ").append(objects.size() + 1)
                .append(" /Root 1 0 R>>\nstartxref\n").append(xref).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private void line(List<String> out, String value) {
        // Keep long custom routes inside the printable width without dropping information.
        String normalized = new String(value.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.ISO_8859_1).replace('\n', ' ').replace('\r', ' ');
        while (normalized.length() > 140) {
            out.add(normalized.substring(0, 140));
            normalized = normalized.substring(140);
        }
        out.add(normalized);
    }

    private String routeEndpoint(String custom, String fallback) {
        return text(custom).isBlank() ? text(fallback) : text(custom);
    }
    private boolean isReturn(Reservation r) { return "VUELTA".equalsIgnoreCase(r.getRouteDirection()) || TripRouteCalculatorService.isCordoba(r.getPickupLocality()); }
    private String schedule(Reservation r) { return text(r.getDepartureSchedule()).isBlank() ? "03:00" : text(r.getDepartureSchedule()); }
    private String name(Reservation r) { return r.getPassenger() == null ? "Sin pasajero" : (text(r.getPassenger().getFirstName()) + " " + text(r.getPassenger().getLastName())).trim(); }
    private String phone(Reservation r) { return r.getPassenger() == null ? "" : text(r.getPassenger().getPhone()); }
    private String text(String value) { return value == null ? "" : value.trim(); }
}
