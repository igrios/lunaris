package com.lunaris.ansenuza.domain.model.service;

/**
 * 🧮 Utilidad para obtener el CUIL de una persona a partir de lo que haya cargado:
 * - Si ya es un CUIL/CUIT de 11 dígitos, lo devuelve formateado.
 * - Si es un DNI (7 u 8 dígitos), calcula el CUIL sugerido (prefijo 20 por defecto)
 *   con su dígito verificador. La operadora debe verificar el sexo (20/27) al facturar.
 */
public final class CuilCalculator {

    private static final int[] WEIGHTS = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private CuilCalculator() {
    }

    /** Devuelve el CUIL formateado (XX-XXXXXXXX-X) o null si no se puede calcular. */
    public static String suggestCuil(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() == 11) {
            return format(digits);
        }
        if (digits.length() == 7 || digits.length() == 8) {
            String dni = String.format("%08d", Long.parseLong(digits));
            return format(buildCuil(dni, 20));
        }
        return digits; // No reconocido: devolvemos lo cargado tal cual
    }

    private static String buildCuil(String dni8, int prefix) {
        String base = String.format("%02d", prefix) + dni8; // 10 dígitos
        int verifier = verifier(base);
        if (verifier == 10) {
            // Caso especial: el tipo pasa a 23 y el verificador se recalcula
            base = "23" + dni8;
            verifier = verifier(base);
        }
        return base + verifier;
    }

    private static int verifier(String tenDigits) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (tenDigits.charAt(i) - '0') * WEIGHTS[i];
        }
        int mod = sum % 11;
        int verifier = 11 - mod;
        if (verifier == 11) {
            return 0;
        }
        return verifier; // puede ser 10 (se maneja arriba)
    }

    private static String format(String eleven) {
        if (eleven.length() != 11) {
            return eleven;
        }
        return eleven.substring(0, 2) + "-" + eleven.substring(2, 10) + "-" + eleven.substring(10);
    }
}
