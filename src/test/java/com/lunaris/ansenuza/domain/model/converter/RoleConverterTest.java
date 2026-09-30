package com.lunaris.ansenuza.domain.model.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lunaris.ansenuza.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RoleConverterTest {
    private final RoleConverter converter = new RoleConverter();

    @ParameterizedTest
    @CsvSource({
            "ROLE_CHOFER, CHOFER", "ROLE_DRIVER, CHOFER",
            "' role_chofer ', CHOFER", "' role_driver ', CHOFER",
            "DRIVER, CHOFER", "CHOFER, CHOFER",
            "ROLE_ADMIN, ADMIN", "ROLE_OPERADOR, OPERADOR", "ROLE_FACTURACION, FACTURACION"
    })
    void readsCanonicalLegacyAndPrefixedRoles(String stored, Role expected) {
        assertThat(converter.convertToEntityAttribute(stored)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void nullOrBlankRolesReturnNull(String stored) {
        assertThat(converter.convertToEntityAttribute(stored)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SUPERUSER", "ROLE_SUPERUSER", "ROLE_", "ROLE_ROLE_CHOFER"})
    void unknownRolesAreRejectedWithoutGrantingPermissions(String stored) {
        assertThatThrownBy(() -> converter.convertToEntityAttribute(stored))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rol persistido no reconocido.");
    }

    @Test
    void writesCanonicalNamesWithoutSpringSecurityPrefix() {
        for (Role role : Role.values()) {
            assertThat(converter.convertToDatabaseColumn(role)).isEqualTo(role.name());
        }
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }
}
