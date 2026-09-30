package com.lunaris.ansenuza.domain.model.converter;

import com.lunaris.ansenuza.domain.model.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

/** Lee nombres históricos y persiste exclusivamente los roles canónicos del dominio. */
@Converter
public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        String cleanedValue = dbData.trim().toUpperCase(Locale.ROOT);
        if (cleanedValue.startsWith("ROLE_")) cleanedValue = cleanedValue.substring(5);
        if ("DRIVER".equals(cleanedValue)) return Role.CHOFER;
        try {
            return Role.valueOf(cleanedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Rol persistido no reconocido.");
        }
    }
}
