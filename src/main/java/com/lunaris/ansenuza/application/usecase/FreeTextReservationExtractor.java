package com.lunaris.ansenuza.application.usecase;

import java.util.regex.Pattern;

/** Sugerencias conservadoras; el texto original sigue disponible para revisión humana. */
public final class FreeTextReservationExtractor {
    private static final Pattern ADDRESS_LABEL = Pattern.compile(
            "(?iu)(?:direcci[oó]n(?: de retiro)?|domicilio|retir(?:o|ar)(?: en| por)?)[ \\t]*:[ \\t]*([^\\n;]+)");
    private static final Pattern STREET = Pattern.compile(
            "(?iu)^(?:(?:calle|av\\.?|avenida|bv\\.?|boulevard|pasaje)\\s+)?[\\p{L}][\\p{L} .'-]{2,60}\\s+\\d{1,5}(?:\\s*(?:,|piso|dpto|depto|puerta).*)?$");
    private static final Pattern PHONE = Pattern.compile(
            "(?<!\\d)(?:\\+?54[ ()-]*)?(?:9[ ()-]*)?\\d{2,4}[ ()-]*\\d{3,4}[ ()-]*\\d{4}(?!\\d)");
    private static final Pattern REFERENCE = Pattern.compile(
            "(?iu)(?:referencia|referencias|indicaciones)[ \\t]*:[ \\t]*([^\\n;]+)");

    private FreeTextReservationExtractor() {}

    public record Suggestions(String pickupAddress, String phone, String references) {}

    public static Suggestions extract(String text) {
        if (text == null || text.isBlank()) return new Suggestions(null, null, null);
        var phoneMatch = PHONE.matcher(text);
        String phone = phoneMatch.find() ? phoneMatch.group().replaceAll("\\D", "") : null;
        if (phone != null && (phone.length() < 10 || phone.length() > 15)) phone = null;
        var referenceMatch = REFERENCE.matcher(text);
        String references = referenceMatch.find() ? referenceMatch.group(1).trim() : null;
        var addressMatch = ADDRESS_LABEL.matcher(text);
        String address = addressMatch.find() ? addressMatch.group(1).trim() : null;
        if (address == null) {
            for (String part : text.split("[\\n;]")) {
                String candidate = PHONE.matcher(part).replaceAll("").trim();
                if (STREET.matcher(candidate).matches() && !candidate.matches("(?iu).*(?:pasajeros?|personas?|fecha|horario|precio|total).*")) {
                    address = candidate;
                    break;
                }
            }
        }
        if (address != null) {
            address = address.split("(?iu)[,;]?\\s*(?:referencias?|tel[eé]fono|celular)\\s*:", 2)[0].trim();
            if (address.isBlank() || address.length() > 255) address = null;
        }
        return new Suggestions(address, phone, references);
    }
}
