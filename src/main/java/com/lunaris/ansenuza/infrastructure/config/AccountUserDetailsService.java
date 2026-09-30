package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class AccountUserDetailsService implements UserDetailsService {
    private final AccountRepository accounts;

    public AccountUserDetailsService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        try {
            var account = accounts.findByUsernameIgnoreCase(username).orElseThrow(() -> {
                log.warn("Authentication lookup failed: reason=USER_NOT_FOUND");
                return new UsernameNotFoundException("Usuario no encontrado");
            });
            String hash = account.getPasswordHash();
            if (hash == null || !hash.matches("\\A\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}\\z")) {
                String reason = hash != null && hash.startsWith("{bcrypt}")
                        ? "BCRYPT_PREFIX_UNSUPPORTED" : "INVALID_BCRYPT_FORMAT";
                log.warn("Authentication password format: accountId={} reason={}", account.getId(), reason);
            }
            var authorities = account.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
            return new UserPrincipal(account.getId(), account.getUsername(), hash,
                    account.isActive(), authorities);
        } catch (UsernameNotFoundException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            Throwable root = exception;
            while (root.getCause() != null && root.getCause() != root) root = root.getCause();
            // No registrar mensajes ni stacktraces que puedan contener SQL, hashes o credenciales.
            log.error("Authentication lookup failed: reason=ACCOUNT_LOAD_FAILED exception={} rootCause={}",
                    exception.getClass().getSimpleName(), root.getClass().getSimpleName());
            throw new InternalAuthenticationServiceException("No se pudo cargar la cuenta.", exception);
        }
    }
}
