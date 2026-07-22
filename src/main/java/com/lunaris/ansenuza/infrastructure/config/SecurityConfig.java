package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Integraciones externas y documentación pública.
                        .requestMatchers("/whatsapp/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**")
                        .permitAll()

                        // Administración completa y configuración sensible.
                        .requestMatchers("/admin/configuraciones/**", "/api/configurations/**", "/choferes/**", "/drivers/**", "/vehicles/**")
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

                        // Operación diaria: viajes, agenda, reservas, chat y rutas.
                        .requestMatchers("/agenda/**", "/api/agenda/**", "/dashboard/**", "/reservas-panel/**", "/reservations/**", "/admin/reservations/**", "/admin/bot/monitor/**", "/admin/chat/**", "/chat-room", "/bot-monitor", "/passengers/**", "/localities", "/fares")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())

                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .successHandler((request, response, authentication) -> {
                            boolean billing = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority()
                                            .equals("ROLE_" + Role.FACTURACION.name()));
                            boolean driver = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority()
                                            .equals("ROLE_" + Role.CHOFER.name()));
                            response.sendRedirect(billing ? "/facturacion"
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
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("ignacio")
                .password(passwordEncoder.encode("Ignacio2026!"))
                .roles(Role.ADMIN.name())
                .build();

        UserDetails operator = User.builder()
                .username("martin")
                .password(passwordEncoder.encode("MartinLunaris2026"))
                .roles(Role.OPERADOR.name())
                .build();

        return new InMemoryUserDetailsManager(admin, operator);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
