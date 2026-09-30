package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
class AccountRepositoryTest {
    @Autowired AccountRepository accounts;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;

    private Account save(String username, Role... roles) {
        return accounts.saveAndFlush(Account.builder()
                .username(username)
                .displayName(username)
                .passwordHash("unused-test-hash")
                .roles(new HashSet<>(Set.of(roles)))
                .build());
    }

    @Test
    void ordinaryLookupLeavesRolesLazy() {
        var account = save("driver", Role.CHOFER);
        entityManager.clear();

        var loaded = accounts.findById(account.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(loaded.getRoles())).isFalse();
    }

    @Test
    void panelLoadsAllAccountsAndRolesInOneQueryIncludingAccountsWithoutRoles() {
        var admin = save("admin", Role.ADMIN, Role.OPERADOR);
        var driver = save("driver", Role.CHOFER);
        var noRoles = save("no-roles");
        entityManager.clear();
        var statistics = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        var loaded = accounts.findAllWithRoles();
        entityManager.clear();

        assertThat(loaded).extracting(Account::getId)
                .containsExactlyInAnyOrder(admin.getId(), driver.getId(), noRoles.getId());
        assertThat(loaded).allSatisfy(account -> {
            assertThat(Hibernate.isInitialized(account.getRoles())).isTrue();
            assertThat(account.getRoles()).isEqualTo(
                    account.getId().equals(admin.getId()) ? Set.of(Role.ADMIN, Role.OPERADOR)
                    : account.getId().equals(driver.getId()) ? Set.of(Role.CHOFER) : Set.of());
        });
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void authenticationLookupLoadsRolesForUseAfterDetachment() {
        save("driver", Role.CHOFER);
        entityManager.clear();

        var loaded = accounts.findByUsernameIgnoreCase("DRIVER").orElseThrow();
        entityManager.clear();

        assertThat(Hibernate.isInitialized(loaded.getRoles())).isTrue();
        assertThat(loaded.getRoles()).containsExactly(Role.CHOFER);
    }

    @ParameterizedTest
    @CsvSource({
            "DRIVER, CHOFER", "driver, CHOFER", "' Driver ', CHOFER",
            "CHOFER, CHOFER", "chofer, CHOFER", "admin, ADMIN",
            "operador, OPERADOR", "facturacion, FACTURACION"
    })
    void loadsLegacyAndCaseVariantRolesForPanelAndAuthentication(String stored, Role expected) {
        var account = save("legacy");
        jdbc.update("INSERT INTO account_roles(account_id, role) VALUES (?, ?)", account.getId(), stored);
        entityManager.clear();

        assertThat(accounts.findAllWithRoles()).singleElement()
                .satisfies(loaded -> assertThat(loaded.getRoles()).containsExactly(expected));
        entityManager.clear();
        var principal = new SecurityConfig().userDetailsService(accounts).loadUserByUsername("legacy");
        assertThat(principal.getAuthorities()).extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_" + expected.name());
    }

    @Test
    void persistsCanonicalDriverRole() {
        var account = save("canonical", Role.CHOFER);

        assertThat(jdbc.queryForObject("SELECT role FROM account_roles WHERE account_id = ?",
                String.class, account.getId())).isEqualTo("CHOFER");
    }

    @Test
    void rejectsUnknownStoredRoleRatherThanGrantingPermissions() {
        var account = save("unknown");
        jdbc.update("INSERT INTO account_roles(account_id, role) VALUES (?, ?)", account.getId(), "SUPERUSER");
        entityManager.clear();

        assertThatThrownBy(() -> accounts.findAllWithRoles())
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }
}
