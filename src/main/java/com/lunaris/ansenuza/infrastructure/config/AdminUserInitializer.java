package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
            @Value("${app.security.admin.password:admin123}") String adminPassword) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Account account = accountRepository.findByUsernameIgnoreCase(adminUsername)
                .orElseGet(this::newAdminAccount);

        if (account.getPasswordHash() == null
                || environment.acceptsProfiles(Profiles.of("dev"))) {
            account.setPasswordHash(passwordEncoder.encode(adminPassword));
        }
        account.setActive(true);
        Set<Role> roles = account.getRoles() == null
                ? new HashSet<>()
                : new HashSet<>(account.getRoles());
        roles.add(Role.ADMIN);
        account.setRoles(roles);

        if (account.getId() == null) {
            account.setId(UUID.randomUUID());
        }
        accountRepository.save(account);
    }

    private Account newAdminAccount() {
        return Account.builder()
                .id(UUID.randomUUID())
                .username(adminUsername)
                .displayName("Administrador")
                .active(true)
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .build();
    }
}
