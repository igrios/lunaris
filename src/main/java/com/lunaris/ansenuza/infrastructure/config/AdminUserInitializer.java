package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AdminUserInitializer implements CommandLineRunner {

    static final String ADMIN_USERNAME = "ignacio";
    static final String ADMIN_PASSWORD = "Admin123!";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Account account = accountRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)
                .orElseGet(this::newAdminAccount);

        account.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
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
                .username(ADMIN_USERNAME)
                .displayName("Ignacio Admin")
                .active(true)
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .build();
    }
}
