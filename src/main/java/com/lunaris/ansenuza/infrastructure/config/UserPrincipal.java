package com.lunaris.ansenuza.infrastructure.config;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/** Identidad persistente de la cuenta; conserva el contrato de UserDetails. */
public final class UserPrincipal extends User {
    private static final long serialVersionUID = 1L;
    private final UUID accountId;

    public UserPrincipal(UUID accountId, String username, String password, boolean enabled,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.accountId = java.util.Objects.requireNonNull(accountId, "accountId");
    }

    public UUID getAccountId() {
        return accountId;
    }
}
