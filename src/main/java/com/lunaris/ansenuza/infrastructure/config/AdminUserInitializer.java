package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AdminUserInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserInitializer(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.security.admin.username:admin}") String adminUsername,
            @Value("${app.security.admin.password:}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (isProduction() && !hasPassword()) {
            throw new IllegalStateException(
                    "ADMIN_INITIAL_PASSWORD es obligatoria en producción.");
        }
        accountRepository.findByUsernameIgnoreCase(adminUsername)
                .ifPresentOrElse(this::updateManagedAdmin, this::createAdmin);
    }

    private void updateManagedAdmin(Account account) {
        if (hasPassword() && (account.getPasswordHash() == null
                || environment.acceptsProfiles(Profiles.of("dev")))) {
            account.setPasswordHash(passwordEncoder.encode(adminPassword));
        }
        account.setActive(true);
        Set<Role> roles = account.getRoles() == null
                ? new HashSet<>()
                : new HashSet<>(account.getRoles());
        roles.add(Role.ADMIN);
        account.setRoles(roles);
        // No se invoca save(): la entidad pertenece a esta transacción y Hibernate
        // persiste los cambios mediante dirty checking al confirmar el commit.
    }

    private void createAdmin() {
        if (!hasPassword()) {
            return;
        }
        Account account = newAdminAccount();
        account.setPasswordHash(passwordEncoder.encode(adminPassword));
        accountRepository.save(account);
    }

    private boolean isProduction() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }

    private boolean hasPassword() {
        return adminPassword != null && !adminPassword.isBlank();
    }

    private Account newAdminAccount() {
        return Account.builder()
                .username(adminUsername)
                .displayName("Administrador")
                .active(true)
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .build();
    }
}
