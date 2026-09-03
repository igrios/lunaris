package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            PassengerBearerAuthenticationFilter passengerBearerAuthenticationFilter)
            throws Exception {
        http
                .securityMatcher("/api/**", "/webhook/**", "/whatsapp/**", "/actuator/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news-banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/schedules/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/localities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/fares/localities").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/drivers/apply",
                                "/api/drivers/applications")
                        .permitAll()
                        .requestMatchers(
                                "/actuator/**",
                                "/api/public/**",
                                "/api/v1/reservations",
                                "/api/schedules/**",
                                "/api/reservations/**",
                                "/api/auth/**",
                                "/api/v1/portal/**",
                                "/api/v1/reservations/**",
                                "/api/v1/waiting-list/request-otp",
                                "/api/v1/waiting-list/confirm",
                                "/webhook/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/whatsapp/test")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/whatsapp/webhook").permitAll()
                        .requestMatchers("/api/drivers", "/api/drivers/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/admin/driver-applications/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/admin/fares/**", "/api/admin/special-trips/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())
                        .requestMatchers("/api/v1/dev/whatsapp-simulator/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/v1/waiting-list/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())
                        .requestMatchers(HttpMethod.GET, "/api/passengers/me", "/api/passengers/profile")
                        .hasRole("PASSENGER")
                        .requestMatchers("/api/configurations/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/driver/confirm-assistance")
                        .hasAnyRole(Role.ADMIN.name(), Role.CHOFER.name())
                        .requestMatchers("/api/agenda/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())
                        .anyRequest().authenticated())
                .addFilterBefore(
                        passengerBearerAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(basic -> {});

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/choferes/**",
                        "/drivers/**"))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/api/special-trips/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/invoices/*.pdf")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/hoja-ruta")
                        .hasAnyRole(Role.ADMIN.name(), Role.CHOFER.name())
                        .requestMatchers(
                                "/admin/usuarios/**",
                                "/admin/configuraciones/**",
                                "/choferes/**",
                                "/drivers/**",
                                "/vehicles/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/api/admin/fares/**", "/api/admin/special-trips/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers(
                                "/admin/bot/toggle-bot",
                                "/admin/bot/toggle-jornada",
                                "/admin/bot/configurar-jornada")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/facturacion/**")
                        .hasAnyRole(Role.ADMIN.name(), Role.FACTURACION.name())
                        .requestMatchers(HttpMethod.GET, "/admin/hoja-ruta")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name(), Role.CHOFER.name())
                        .requestMatchers(
                                "/admin/dashboard",
                                "/dashboard/**",
                                "/agenda/**",
                                "/reservas-panel/**",
                                "/reservations/**",
                                "/admin/reservations/**",
                                "/admin/bot/monitor/**",
                                "/admin/chat/**",
                                "/chat-room",
                                "/bot-monitor",
                                "/passengers/**",
                                "/localities",
                                "/fares")
                        .hasAnyRole(Role.ADMIN.name(), Role.OPERADOR.name())
                        .requestMatchers("/admin/**").hasRole(Role.ADMIN.name())
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                org.springframework.http.HttpStatus.UNAUTHORIZED),
                        request -> "/hoja-ruta".equals(request.getServletPath())))
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
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
                "https://www.lunarisansenuza.com.ar",
                "https://lunarisansenuza.com.ar",
                "https://lunaris-web-reload.vercel.app",
                "https://*.vercel.app",
                "http://localhost:5173",
                "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration publicNewsBanners = new CorsConfiguration();
        publicNewsBanners.setAllowedOrigins(List.of("*"));
        publicNewsBanners.setAllowedMethods(List.of("GET", "OPTIONS"));
        publicNewsBanners.setAllowedHeaders(List.of("*"));
        publicNewsBanners.setAllowCredentials(false);
        publicNewsBanners.setMaxAge(3600L);
        source.registerCorsConfiguration("/api/v1/news-banners/**", publicNewsBanners);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsernameIgnoreCase(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
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
