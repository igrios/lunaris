package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            PassengerBearerAuthenticationFilter passengerBearerAuthenticationFilter)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/schedules").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/reservations",
                                "/api/drivers/apply",
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/passengers/me")
                        .hasRole("PASSENGER")
                        // Integraciones externas y documentación pública.
                        .requestMatchers("/whatsapp/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**")
                        .permitAll()

                        // Administración completa y configuración sensible.
                        .requestMatchers("/admin/usuarios/**", "/admin/configuraciones/**", "/api/configurations/**", "/choferes/**", "/drivers/**", "/vehicles/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/admin/bot/toggle-bot", "/admin/bot/toggle-jornada", "/admin/bot/configurar-jornada")
                        .hasRole(Role.ADMIN.name())

                        // Facturas, saldos y comprobantes de facturación.
                        .requestMatchers("/facturacion/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.FACTURACION.name())

                        // Hoja de ruta y confirmación de asistencia del chofer.
                        .requestMatchers(HttpMethod.GET, "/admin/hoja-ruta", "/hoja-ruta")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name(), Role.CHOFER.name())
                        .requestMatchers(HttpMethod.POST, "/api/driver/confirm-assistance")
                        .hasAnyRole(Role.ADMIN.name(), Role.CHOFER.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/travel-status")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name(), Role.CHOFER.name())
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/*/travel-status")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name(), Role.CHOFER.name())

                        // Operación diaria: viajes, agenda, reservas, chat y rutas.
                        .requestMatchers("/agenda/**", "/api/agenda/**", "/dashboard/**", "/reservas-panel/**", "/reservations/**", "/admin/reservations/**", "/admin/bot/monitor/**", "/admin/chat/**", "/chat-room", "/bot-monitor", "/passengers/**", "/localities", "/fares")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())

                        .anyRequest().authenticated())
                .addFilterBefore(
                        passengerBearerAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> {})
                .formLogin(login -> login
                        .successHandler((request, response, authentication) -> {
                            boolean admin = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority()
                                            .equals("ROLE_" + Role.ADMIN.name()));
                            boolean billing = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority()
                                            .equals("ROLE_" + Role.FACTURACION.name()));
                            boolean driver = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority()
                                            .equals("ROLE_" + Role.CHOFER.name()));
                            response.sendRedirect(admin ? "/dashboard" : billing ? "/facturacion"
                                    : driver ? "/admin/hoja-ruta" : "/dashboard");
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://*.vercel.app",
                "https://lunarisansenuza.com.ar",
                "http://localhost:5173",
                "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsernameIgnoreCase(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    public ApplicationRunner bootstrapAccounts(AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            createIfMissing(accountRepository, passwordEncoder, "ignacio", "Ignacio", "Ignacio2026!", Role.ADMIN);
            createIfMissing(accountRepository, passwordEncoder, "martin", "Martín", "MartinLunaris2026", Role.OPERADOR);
        };
    }

    private void createIfMissing(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
            String username, String displayName, String password, Role role) {
        if (!accountRepository.existsByUsernameIgnoreCase(username)) {
            accountRepository.save(Account.builder()
                    .username(username)
                    .displayName(displayName)
                    .passwordHash(passwordEncoder.encode(password))
                    .active(true)
                    .roles(new HashSet<>(Set.of(role)))
                    .build());
        }
    }

    private org.springframework.security.core.userdetails.UserDetails toUserDetails(Account account) {
        String[] roles = account.getRoles().stream().map(Role::name).toArray(String[]::new);
        return User.builder()
                .username(account.getUsername())
                .password(account.getPasswordHash())
                .roles(roles)
                .disabled(!account.isActive())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
