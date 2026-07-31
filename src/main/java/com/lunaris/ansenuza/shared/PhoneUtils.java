package com.lunaris.ansenuza.shared;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;

/** Normaliza teléfonos argentinos al formato internacional requerido por WhatsApp. */
public final class PhoneUtils {

    private static final String ARGENTINA_COUNTRY_CODE = "54";

    private PhoneUtils() {
    }

    public static String normalizeArgentinePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new DomainValidationException("El teléfono es obligatorio.");
        }

        String nationalNumber = rawPhone.replaceAll("\\D", "").replaceFirst("^0+", "");
        if (nationalNumber.startsWith(ARGENTINA_COUNTRY_CODE)) {
            nationalNumber = nationalNumber.substring(ARGENTINA_COUNTRY_CODE.length());
        }
        nationalNumber = nationalNumber.replaceFirst("^0+", "");
        if (nationalNumber.startsWith("9")) {
            nationalNumber = nationalNumber.substring(1);
        }
        nationalNumber = nationalNumber.replaceFirst("^0+", "");
        if (nationalNumber.startsWith("15")) {
            nationalNumber = nationalNumber.substring(2);
        } else if (nationalNumber.length() == 12) {
            nationalNumber = removeLegacyMobilePrefix(nationalNumber);
        }

        if (!nationalNumber.matches("[1-9][0-9]{9}")) {
            throw new DomainValidationException("El teléfono no es válido.");
        }
        return ARGENTINA_COUNTRY_CODE + nationalNumber;
    }

    private static String removeLegacyMobilePrefix(String number) {
        for (int areaCodeLength = 2; areaCodeLength <= 4; areaCodeLength++) {
            if (number.startsWith("15", areaCodeLength)) {
                return number.substring(0, areaCodeLength) + number.substring(areaCodeLength + 2);
            }
        }
        return number;
    }
}
