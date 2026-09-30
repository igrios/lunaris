package com.lunaris.ansenuza.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.infrastructure.config.SecurityConfig;
import com.lunaris.ansenuza.service.interurban.ExistingDriverIdentityAdapter;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DriverAccountIdentityIntegrationTest {
    @Autowired AccountRepository accounts;
    @Autowired DriverRepository drivers;
    @Autowired EntityManager entityManager;

    private Account account(String username) {
        return accounts.saveAndFlush(Account.builder().username(username).displayName(username)
                .passwordHash("unused-test-hash").roles(Set.of(Role.CHOFER)).build());
    }

    private Driver driver(UUID accountId, boolean active) {
        var driver = new Driver();
        driver.setAccountId(accountId);
        driver.setFullName("Nombre independiente de la cuenta");
        driver.setPhone("543562123456");
        driver.setActive(active);
        return drivers.saveAndFlush(driver);
    }

    private UUID resolve(String username) {
        entityManager.clear();
        var principal = new SecurityConfig().userDetailsService(accounts).loadUserByUsername(username);
        entityManager.clear();
        var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        return new ExistingDriverIdentityAdapter(drivers).requireDriver(auth);
    }

    @Test void resolvesStoredAccountIdDespiteSharedPhonesAndDifferentUsernames() {
        var mine = account("chofer-unido");
        var other = account("otro-chofer");
        var linked = driver(mine.getId(), true);
        driver(other.getId(), true);
        driver(null, true);

        assertThat(resolve("CHOFER-UNIDO")).isEqualTo(linked.getId());
    }

    @Test void inactiveAndUnlinkedAccountsAreDeniedWithoutPhoneFallback() {
        var inactive = account("inactivo");
        account("543562123456");
        driver(inactive.getId(), false);
        driver(null, true);

        assertThrows(AccessDeniedException.class, () -> resolve("inactivo"));
        assertThrows(AccessDeniedException.class, () -> resolve("543562123456"));
    }

    @Test void accountCannotBeLinkedToTwoDrivers() {
        var account = account("unico");
        driver(account.getId(), true);
        assertThrows(DataIntegrityViolationException.class, () -> driver(account.getId(), true));
    }
}
