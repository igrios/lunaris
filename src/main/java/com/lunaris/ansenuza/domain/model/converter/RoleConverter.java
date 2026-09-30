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
    public Role convertToEntityAttribute(String value) {
        if (value == null) return null;
        String normalized = value.strip().toUpperCase(Locale.ROOT);
        return "DRIVER".equals(normalized) ? Role.CHOFER : Role.valueOf(normalized);
    }
}
