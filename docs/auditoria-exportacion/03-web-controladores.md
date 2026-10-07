# 3. CAPA WEB Y CONTROLADORES

74 archivos. Rutas relativas a la raíz del repositorio. Código original, sin reformatear.

La agrupación es funcional: no modifica paquetes. Cada archivo aparece una sola vez; los archivos con responsabilidades mixtas se asignan a su función principal.

## Índice

- `src/main/java/com/lunaris/ansenuza/application/dto/ScheduleDto.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/AccountUserDetailsService.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/AuthenticationDiagnostics.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/OpenApiConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/PassengerBearerAuthenticationFilter.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/SecurityConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/config/UserPrincipal.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/config/WebMvcConfig.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AccountAdminController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminConfigController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminDashboardController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminReservationApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AgendaDayController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AgendaViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ApiExceptionHandler.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AuthController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/BillingViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/BotMonitorController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ChatController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ConfigurationController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DashboardController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DashboardViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/FareAdminController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/FareAdminViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/HomeController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/InquiryController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/InquiryNavigationAdvice.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/LocalityController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/NewsBannerAdminController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/NewsBannerApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/OperatorPhoneController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PassengerController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PassengerProfileController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PortalController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicCatalogApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicInvoiceController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ReservationApiController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ReservationViewController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SchedulesController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SpecialTripAdminController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SpecialTripPublicController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/VehicleController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WaitingListController.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/CreatePassengerRequest.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/DriverApplicationRequest.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/NewsBannerDto.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/ReservationCreateDTO.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/agenda/AgendaDayView.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/agenda/EnviarHojaRutaRequest.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/BillingPanelView.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/IssuedInvoiceRow.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/PendingInvoiceRow.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/dashboard/DailyOperationSummaryResponse.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/hojaruta/HojaRutaViewModel.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationForm.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationRequest.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationResponse.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/ManualReservationOptions.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/PassengerOption.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/specialtrip/SpecialTripRequest.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/specialtrip/SpecialTripResponse.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/json/StringOrStringListDeserializer.java`
- `src/main/java/com/lunaris/ansenuza/infrastructure/web/mapper/SpecialTripWebMapper.java`
- `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/in/web/ReservationController.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverController.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverExceptionHandler.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverSecurityConfiguration.java`
- `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentExceptionHandler.java`

## Archivo: `src/main/java/com/lunaris/ansenuza/application/dto/ScheduleDto.java`

```java
package com.lunaris.ansenuza.application.dto;

public record ScheduleDto(
        String id,
        String departureTime,
        String label,
        int availableSeats,
        boolean available) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/AccountUserDetailsService.java`

```java
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/AuthenticationDiagnostics.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthenticationDiagnostics {
    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        var events = new DefaultAuthenticationEventPublisher(publisher);
        events.setDefaultAuthenticationFailureEvent(AuthenticationFailureServiceExceptionEvent.class);
        return events;
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        // La clase distingue BadCredentials, Disabled, Locked y errores del servicio.
        // No volcar Authentication: contiene credenciales y datos del principal.
        log.warn("Authentication failed: reason={}", event.getException().getClass().getSimpleName());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/OpenApiConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lunarisApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Lunaris Ansenuza API")
                                .version("1.0.0")
                                .description(
                                        "Sistema de gestión de traslados puerta a puerta"
                                )
                );
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/PassengerBearerAuthenticationFilter.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class PassengerBearerAuthenticationFilter extends OncePerRequestFilter {

    private final PassengerOtpService otpService;

    public PassengerBearerAuthenticationFilter(PassengerOtpService otpService) {
        this.otpService = otpService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorization.substring(7).trim();
            if (!token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                otpService.resolvePhone(token).ifPresent(phone -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            phone, token, List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            }
        } catch (Exception exception) {
            logger.warn("Error procesando token JWT en PassengerBearerAuthenticationFilter: "
                    + exception.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/SecurityConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
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
                                "/api/schedules/**",
                                "/api/auth/**",
                                "/api/v1/portal/**",
                                "/api/v1/waiting-list/request-otp",
                                "/api/v1/waiting-list/confirm",
                                "/webhook/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservations/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservations/*/receipt")
                        .hasRole("PASSENGER")
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
                        .requestMatchers(
                                HttpMethod.PUT, "/api/reservations/*/travel-status")
                        .hasAnyRole(Role.ADMIN.name(), Role.CHOFER.name())
                        .requestMatchers(
                                HttpMethod.PATCH, "/api/reservations/*/travel-status")
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
                                "/admin/consultas",
                                "/admin/consultas/**",
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
        return new AccountUserDetailsService(accountRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/config/UserPrincipal.java`

```java
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
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/config/WebMvcConfig.java`

```java
package com.lunaris.ansenuza.infrastructure.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.local-dir}")
    private String localDir;

    @Value("${storage.invoices-dir}")
    private String invoicesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mapea la URL web a la carpeta real de tu Linux
        registry.addResourceHandler("/comprobantes/**")
                .addResourceLocations("file:" + localDir);

        // 🧾 PDFs de facturas subidos por la operadora
        registry.addResourceHandler("/facturas/**")
                .addResourceLocations("file:" + invoicesDir);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AccountAdminController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Account;
import com.lunaris.ansenuza.domain.model.Role;
import com.lunaris.ansenuza.domain.repository.AccountRepository;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class AccountAdminController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Transactional(readOnly = true)
    public String panel(Model model) {
        model.addAttribute("usuarios", accountRepository.findAllWithRoles());
        model.addAttribute("roles", Role.values());
        return "admin/usuarios";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam(required = false) UUID id,
            @RequestParam String username,
            @RequestParam String displayName,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) Set<Role> roles,
            @RequestParam(defaultValue = "false") boolean active,
            RedirectAttributes redirectAttributes) {
        Account account = id == null ? new Account() : accountRepository.findById(id).orElse(null);
        if (account == null && id != null) {
            redirectAttributes.addFlashAttribute("error", "El usuario a editar no existe.");
            return "redirect:/admin/usuarios";
        }

        boolean newAccount = account == null;
        if (newAccount) {
            account = new Account();
        }
        String normalizedUsername = username.trim();

        if (normalizedUsername.isBlank() || displayName.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Usuario y nombre son obligatorios.");
            return "redirect:/admin/usuarios";
        }
        if (newAccount && (password == null || password.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es obligatoria para un usuario nuevo.");
            return "redirect:/admin/usuarios";
        }
        if (roles == null || roles.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seleccioná al menos un rol.");
            return "redirect:/admin/usuarios";
        }
        Account accountWithSameUsername = accountRepository.findByUsernameIgnoreCase(normalizedUsername)
                .orElse(null);
        if (accountWithSameUsername != null
                && !accountWithSameUsername.getId().equals(account.getId())) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso.");
            return "redirect:/admin/usuarios";
        }

        account.setUsername(normalizedUsername);
        account.setDisplayName(displayName.trim());
        account.setActive(active);
        account.setRoles(EnumSet.copyOf(roles));
        if (password != null && !password.isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(password));
        }
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("ok", newAccount ? "Usuario creado." : "Usuario actualizado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable UUID id, Authentication authentication,
            RedirectAttributes redirectAttributes) {
        Account account = accountRepository.findById(id).orElse(null);
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
        } else if (account.getUsername().equalsIgnoreCase(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "No podés eliminar tu propia cuenta.");
        } else {
            accountRepository.delete(account);
            redirectAttributes.addFlashAttribute("ok", "Usuario eliminado.");
        }
        return "redirect:/admin/usuarios";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminConfigController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminConfigController {

    private static final String RETURN_SCHEDULER_TIME_KEY = "return.scheduler.time";
    private static final String RETURN_MESSAGE_HEADER_KEY = "return.message.header";
    private static final String RETURN_MESSAGE_BODY_KEY = "return.message.body";
    private static final String RETURN_BUTTON_YES_TITLE_KEY = "return.button.yes.title";
    private static final String RETURN_BUTTON_LATER_TITLE_KEY = "return.button.later.title";
    private static final String RETURN_BUTTON_NO_TITLE_KEY = "return.button.no.title";
    private static final String SESSION_INACTIVITY_TIMEOUT_KEY = "session.inactivity.timeout.minutes";

    private final SystemConfigurationService configurationService;

    @GetMapping("/configuraciones")
    public String view(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", loadForm());
        }
        return "admin/configuraciones";
    }

    @PostMapping("/configuraciones")
    public String save(@ModelAttribute ConfigurationForm form, RedirectAttributes redirectAttributes) {
        configurationService.save(RETURN_SCHEDULER_TIME_KEY, form.getReturnSchedulerTime());
        configurationService.save(RETURN_MESSAGE_HEADER_KEY, form.getReturnMessageHeader());
        configurationService.save(RETURN_MESSAGE_BODY_KEY, form.getReturnMessageBody());
        configurationService.save(RETURN_BUTTON_YES_TITLE_KEY, form.getReturnButtonYesTitle());
        configurationService.save(RETURN_BUTTON_LATER_TITLE_KEY, form.getReturnButtonLaterTitle());
        configurationService.save(RETURN_BUTTON_NO_TITLE_KEY, form.getReturnButtonNoTitle());
        configurationService.save(SESSION_INACTIVITY_TIMEOUT_KEY, form.getSessionInactivityTimeoutMinutes());

        redirectAttributes.addFlashAttribute("successMessage", "Configuraciones guardadas correctamente.");
        return "redirect:/admin/configuraciones";
    }

    private ConfigurationForm loadForm() {
        ConfigurationForm form = new ConfigurationForm();
        form.setReturnSchedulerTime(configurationService.getValue(RETURN_SCHEDULER_TIME_KEY, "15:00"));
        form.setReturnMessageHeader(configurationService.getValue(RETURN_MESSAGE_HEADER_KEY, "Confirmación de vuelta"));
        form.setReturnMessageBody(configurationService.getValue(RETURN_MESSAGE_BODY_KEY,
                "Hola, ¿confirmás tu vuelta de hoy con Lunaris Ansenuza?\nElegí una opción para que podamos organizar las butacas."));
        form.setReturnButtonYesTitle(configurationService.getValue(RETURN_BUTTON_YES_TITLE_KEY, "SÍ, VOLVER ✅"));
        form.setReturnButtonLaterTitle(configurationService.getValue(RETURN_BUTTON_LATER_TITLE_KEY, "OTRO DÍA 📅"));
        form.setReturnButtonNoTitle(configurationService.getValue(RETURN_BUTTON_NO_TITLE_KEY, "NO, CANCELAR ❌"));
        form.setSessionInactivityTimeoutMinutes(configurationService.getValue(SESSION_INACTIVITY_TIMEOUT_KEY, "30"));
        return form;
    }

    @Getter
    @Setter
    public static class ConfigurationForm {
        private String returnSchedulerTime;
        private String returnMessageHeader;
        private String returnMessageBody;
        private String returnButtonYesTitle;
        private String returnButtonLaterTitle;
        private String returnButtonNoTitle;
        private String sessionInactivityTimeoutMinutes;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminDashboardController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.application.conversation.GoogleMapsParameterFormatter;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminDashboardController {

    private final ReservationRepository reservationRepository;
    private final PricingAndScheduleService scheduleService;
    private final ConversationSessionRepository sessionRepository; // 💬 ¡Inyectamos las sesiones del bot!
    private final ResolveEffectiveTripOriginUseCase resolveEffectiveTripOriginUseCase;

    @GetMapping("/hoja-ruta")
    public String getHojaRuta(@RequestParam(value = "fecha", required = false) String fechaStr,
            @RequestParam(value = "schedule", defaultValue = "03:00") String scheduleBlock,
            @RequestParam(value = "direction", defaultValue = "OUTBOUND") RouteDirection direction,
            Model model) {
        // 1. Parseamos la fecha elegida o usamos la de hoy por defecto
        LocalDate fecha = (fechaStr == null || fechaStr.isEmpty())
                ? com.lunaris.ansenuza.shared.ArgentinaTime.today()
                : LocalDate.parse(fechaStr);
        
        // 2. Traemos solo las reservas activas, IGNORANDO por completo las canceladas
        RouteOriginResolution originResolution = resolveEffectiveTripOriginUseCase.resolve(fecha, scheduleBlock);
        List<Reservation> reservas = reservationRepository.findActiveManifest(
                        fecha, scheduleBlock, direction == RouteDirection.RETURN)
                .stream()
                .sorted(dynamicRouteComparator(originResolution))
                .toList();
        
        // 3. Calculamos el total yendo desde la zona de los pueblos hacia Córdoba (filtrado automático)
        int totalYendoDesdeZona = reservas.stream()
                .filter(r -> !TripRouteCalculatorService.isCordoba(r.getPickupLocality()))
                .mapToInt(Reservation::getTotalSeats)
                .sum();

        // 4. Calculamos el total volviendo desde Córdoba hacia el norte
        int totalVolviendoDesdeCba = reservas.stream()
                .filter(r -> TripRouteCalculatorService.isCordoba(r.getPickupLocality()))
                .mapToInt(Reservation::getTotalSeats)
                .sum();
                
        // 5. Contamos de manera segura cuántos pasajeros activos viajan en el turno crítico de las 08:00 AM
        int pasajeros0800 = reservas.stream()
                .filter(r -> r.getNotes() != null && r.getNotes().contains("08:00 AM"))
                .mapToInt(Reservation::getTotalSeats)
                .sum();

        // 6. Traemos las conversaciones reales del bot desde la base de datos
        List<ConversationSession> sesionesChat = sessionRepository.findAll();

        // 7. Inyectamos los datos limpios al modelo de Thymeleaf (respetando las variables sin "data.")
        model.addAttribute("fechaSeleccionada", fecha);
        model.addAttribute("pasajeros0800Count", pasajeros0800);
        model.addAttribute("hubActivado", pasajeros0800 > 4);
        model.addAttribute("reservas", reservas);
        model.addAttribute("totalYendo", totalYendoDesdeZona);
        model.addAttribute("totalVolviendo", totalVolviendoDesdeCba);
        model.addAttribute("sesionesChat", sesionesChat); // 👈 ¡Ahora van las reales!
        addOriginAttributes(model, originResolution);
        model.addAttribute("selectedDirection", direction);
        model.addAttribute(
                "navigationUrl",
                GoogleMapsParameterFormatter.buildDirectionsUrl(reservas));

        return "admin/hoja-ruta"; 
    }

    static java.util.Comparator<Reservation> dynamicRouteComparator(RouteOriginResolution resolution) {
        return java.util.Comparator.comparingInt((Reservation reservation) -> {
            if (TripRouteCalculatorService.isCordoba(reservation.getPickupLocality())) return Integer.MAX_VALUE;
            return resolution.minuteOffsets().getOrDefault(reservation.getPickupLocality(), Integer.MAX_VALUE - 1);
        }).thenComparing(Reservation::getRouteSequence,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
    }

    static void addOriginAttributes(Model model, RouteOriginResolution resolution) {
        model.addAttribute("effectiveOrigin", resolution.effectiveOrigin());
        model.addAttribute("originRecalculationMessage", resolution.summary());
        model.addAttribute("routeMinuteOffsets", resolution.minuteOffsets());
        model.addAttribute("selectedScheduleBlock", resolution.scheduleBlock());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AdminReservationApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.ReservationDriverAssignmentService;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationDriverAssignmentService assignmentService;
    private final ReservationRepository reservationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAll(
            @RequestParam(required = false) LocalDate travelDate) {
        List<Reservation> reservations = travelDate != null
                ? reservationRepository.findByTravelDate(travelDate)
                : reservationRepository.findAll();
        return reservations.stream()
                .map(AdminReservationResponse::from)
                .toList();
    }

    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<ReservationDriverResponse> assignDriver(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDriverRequest request) {
        return assignmentService.assign(id, request.driverId())
                .map(ReservationDriverResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/unassign-driver")
    public ResponseEntity<ReservationDriverResponse> unassignDriver(@PathVariable UUID id) {
        return assignmentService.unassign(id)
                .map(ReservationDriverResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record AssignDriverRequest(@NotNull UUID driverId) {
    }

    public record ReservationDriverResponse(
            UUID id,
            UUID driverId,
            String travelStatus) {

        static ReservationDriverResponse from(Reservation reservation) {
            return new ReservationDriverResponse(
                    reservation.getId(),
                    reservation.getDriver() != null ? reservation.getDriver().getId() : null,
                    reservation.getTravelStatus() != null
                            ? reservation.getTravelStatus().name() : null);
        }
    }

    public record AdminReservationResponse(
            UUID id,
            String reservationCode,
            LocalDate travelDate,
            String pickupLocality,
            String pickupAddress,
            String destination,
            BigDecimal amount,
            BigDecimal extraAmount,
            Integer passengerCount,
            String status,
            String source,
            String travelStatus,
            Boolean paymentVerified,
            String paymentReceiptUrl,
            UUID passengerId,
            String passengerName,
            UUID driverId,
            String driverName) {

        static AdminReservationResponse from(Reservation reservation) {
            var passenger = reservation.getPassenger();
            var driver = reservation.getDriver();
            String passengerName = passenger == null
                    ? null
                    : String.join(" ",
                            passenger.getFirstName() != null ? passenger.getFirstName() : "",
                            passenger.getLastName() != null ? passenger.getLastName() : "").trim();
            return new AdminReservationResponse(
                    reservation.getId(),
                    reservation.getReservationCode(),
                    reservation.getTravelDate(),
                    reservation.getPickupLocality(),
                    reservation.getPickupAddress(),
                    reservation.getDestination(),
                    reservation.getAmount(),
                    reservation.getExtraAmount(),
                    reservation.getPassengerCount(),
                    reservation.getStatus(),
                    reservation.getSource() != null ? reservation.getSource().name() : null,
                    reservation.getTravelStatus() != null
                            ? reservation.getTravelStatus().name() : null,
                    reservation.getPaymentVerified(),
                    reservation.getPaymentReceiptUrl(),
                    passenger != null ? passenger.getId() : null,
                    passengerName,
                    driver != null ? driver.getId() : null,
                    driver != null ? driver.getFullName() : null);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AgendaDayController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaDayController {

    private final ReservationRepository reservationRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @GetMapping("/agenda/day")
    public String dayAgenda(
            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE,
                    fallbackPatterns = {"dd/MM/yyyy", "d/M/yy"})
            LocalDate date,
            Model model) {

        List<Reservation> reservations = reservationRepository.findByTravelDate(date);

        List<Reservation> agendaReservations = reservations.stream()
                .filter(r -> r != null)
                .toList();

        model.addAttribute("date", date);
        model.addAttribute("reservations", agendaReservations);

        return "agenda-day";
    }

    @PostMapping("/agenda/verify-payment/{id}")
    @ResponseBody
    public ResponseEntity<Void> verifyPayment(@PathVariable UUID id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    confirmPaymentUseCase.execute(id);

                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 🛡️ CONTROLADOR REPARADO PARA SOPORTAR CARGAS MANUALES Y REDIRECCIONES DIRECTAS
    @GetMapping("/agenda/comprobante/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        // Buscamos la reserva de forma segura sin clavar excepciones orElseThrow
        Reservation reservation = reservationRepository.findById(id).orElse(null);

        if (reservation == null) {
            // Si no se encuentra la reserva por UUID, evitamos el crash y devolvemos un 404 limpio
            return ResponseEntity.notFound().build();
        }

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || receiptUrl.isBlank() || "null".equalsIgnoreCase(receiptUrl)) {
            return ResponseEntity.notFound().build();
        }

        // 💡 Si el campo ya contiene una URL HTTP directa de Cloudinary, render o Supabase
        // Redirigimos el navegador de una para que el operador vea la imagen y no consuma recursos de tu backend
        if (receiptUrl.startsWith("http://") || receiptUrl.startsWith("https://")) {
            if (!receiptUrl.contains("graph.facebook.com")) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, receiptUrl)
                        .build();
            }
        }

        // 🛠️ Fallback: Si es un ID de recurso de la API de Meta, procede con la descarga binaria tradicional
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                    receiptUrl, HttpMethod.GET, entity, JsonNode.class);
            
            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(
                    actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageResponse.getBody());

        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .error("Error al procesar la descarga del adjunto de Meta/WhatsApp", e);
            return ResponseEntity.status(500).build();
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AgendaViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.DriverRouteService;
import com.lunaris.ansenuza.domain.model.service.FleetCapacityService;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.application.usecase.DriverAuthorizationService;
import com.lunaris.ansenuza.application.usecase.DailyPassengerManifestService;
import com.lunaris.ansenuza.application.conversation.GoogleMapsParameterFormatter;
import com.lunaris.ansenuza.domain.port.in.ResolveEffectiveTripOriginUseCase;
import com.lunaris.ansenuza.domain.port.in.RouteOriginResolution;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService;
import com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection;
import com.lunaris.ansenuza.domain.model.service.AirportTripDetector;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.AgendaDayView;
import com.lunaris.ansenuza.infrastructure.web.dto.agenda.EnviarHojaRutaRequest;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AgendaViewController {

    private static final List<String> OUTBOUND_SCHEDULES = List.of("03:00", "08:00");
    private static final List<String> RETURN_SCHEDULES =
            List.of("12:00", "14:00", "16:00", "17:30");

    private final ReservationRepository reservationRepository;
    private final WhatsAppService whatsAppService;
    private final DriverRepository driverRepository;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final DriverRouteService driverRouteService;
    private final FleetCapacityService fleetCapacityService;
    private final WaitingListRepository waitingListRepository;
    private final SystemConfigurationService systemConfigurationService;
    private final ResolveEffectiveTripOriginUseCase resolveEffectiveTripOriginUseCase;
    private final DriverAuthorizationService driverAuthorizationService;

    @Value("${whatsapp.access-token}")
    private String whatsappToken;

    @Value("${lunaris.trips.capacity:12}")
    private int vehicleCapacity = 4;

    // 📅 1. Vista resumen de los próximos 7 días (Semana Completa) BLINDADA CONTRA VUELTAS ABIERTAS Y CANCELADOS
    @GetMapping("/agenda")
    public String agenda(
            @RequestParam(defaultValue = "7") int days,
            Model model) {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        LocalDate fechaCentinela = LocalDate.of(2099, 12, 31);
        int displayedDays = Math.max(7, Math.min(days, 56));
        java.util.Map<LocalDate, List<Reservation>> reservationsByDate =
                reservationRepository.findAgendaBetween(
                                today, today.plusDays(displayedDays - 1L)).stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                Reservation::getTravelDate));

        List<AgendaDayView> agenda =
                java.util.stream.IntStream.range(0, displayedDays)
                        .mapToObj(today::plusDays).map(date -> {
                    List<Reservation> reservations = reservationsByDate.getOrDefault(date, List.of());

                    // 🌟 FILTRO: Excluimos registros con fecha centinela, CANCELLED o passengerCount <= 0
                    List<Reservation> activeReservations = reservations.stream()
                            .filter(r -> r != null)
                            .filter(r -> r.getTravelDate() == null || !r.getTravelDate().equals(fechaCentinela))
                            .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                            .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                            .toList();

                    int totalPassengers = countDistinctBookingSeats(activeReservations);
                    int confirmedPassengers = countDistinctBookingSeats(activeReservations.stream()
                            .filter(r -> Boolean.TRUE.equals(r.getPaymentVerified())
                                    && "CONFIRMED".equals(r.getStatus()))
                            .toList());
                    int waitingListPassengers = Math.toIntExact(
                            waitingListRepository.sumPassengerCountByTravelDateAndStatus(
                                    date, WaitingListEntry.WAITING));
                    int maxCapacity = systemConfigurationService.getScheduleMaxCapacity();

                    int pendingPayments = countDistinctBookings(activeReservations.stream()
                            .filter(r -> !Boolean.TRUE.equals(r.getPaymentVerified())).toList());

                    int safeVehicleCapacity = Math.max(vehicleCapacity, 1);
                    int estimatedVehicles = totalPassengers == 0
                            ? 0
                            : (int) Math.ceil((double) totalPassengers / safeVehicleCapacity);
                    int paidReservations = countDistinctBookings(activeReservations.stream()
                            .filter(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified()))
                            .toList());
                    java.math.BigDecimal totalCollected = activeReservations.stream()
                            .filter(reservation ->
                                    Boolean.TRUE.equals(reservation.getPaymentVerified()))
                            .map(reservation -> {
                                java.math.BigDecimal amount = reservation.getAmount() == null
                                        ? java.math.BigDecimal.ZERO
                                        : reservation.getAmount();
                                java.math.BigDecimal extra =
                                        reservation.getExtraAmount() == null
                                            ? java.math.BigDecimal.ZERO
                                            : reservation.getExtraAmount();
                                return amount.add(extra);
                            })
                            .reduce(
                                    java.math.BigDecimal.ZERO,
                                    java.math.BigDecimal::add);

                    UUID assignedDriverId = activeReservations.stream()
                            .map(Reservation::getDriver)
                            .filter(java.util.Objects::nonNull)
                            .map(Driver::getId)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    return new AgendaDayView(
                            date,
                            totalPassengers,
                            confirmedPassengers,
                            waitingListPassengers,
                            confirmedPassengers > maxCapacity,
                            pendingPayments,
                            estimatedVehicles,
                            safeVehicleCapacity,
                            estimatedVehicles * safeVehicleCapacity,
                            totalCollected,
                            paidReservations,
                            assignedDriverId);
                }).toList();

        model.addAttribute("agenda", agenda);
        model.addAttribute("displayedDays", displayedDays);
        return "agenda";
    }

    static int countDistinctBookingSeats(List<Reservation> reservations) {
        return reservations.stream().collect(java.util.stream.Collectors.toMap(
                        AgendaViewController::bookingGroupKey,
                        Reservation::getTotalSeats,
                        Math::max))
                .values().stream().mapToInt(Integer::intValue).sum();
    }

    static int countDistinctBookings(List<Reservation> reservations) {
        return (int) reservations.stream().map(AgendaViewController::bookingGroupKey).distinct().count();
    }

    private static String bookingGroupKey(Reservation reservation) {
        String code = reservation.getReservationCode();
        if (code != null && !code.isBlank()) {
            return code.replaceFirst("-(IDA|VUELTA)$", "");
        }
        if (Boolean.TRUE.equals(reservation.getRoundTrip()) && reservation.getPassenger() != null
                && reservation.getPassenger().getId() != null) {
            return "ROUND_TRIP_PASSENGER:" + reservation.getPassenger().getId();
        }
        return "RESERVATION:" + reservation.getId();
    }

    
    // 🚐 2. Vista detalle del día (Excluye Pasajeros Fantasma / Cancelados / Conteo <= 0)
    @GetMapping({"/agenda/view-detalle", "/admin/agenda/detalle"})
    public String dayAgenda(
            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE,
                    fallbackPatterns = {"dd/MM/yyyy", "d/M/yy"})
            LocalDate date,
            @RequestParam(value = "schedule", defaultValue = "03:00") String schedule,
            @RequestParam(value = "direction", defaultValue = "OUTBOUND") RouteDirection direction,
            Model model) {

        List<Reservation> reservations = reservationRepository.findActiveManifest(
                date, schedule, direction == RouteDirection.RETURN);
        List<Driver> choferes = driverRepository.findByActiveTrue();

        List<Reservation> activeReservations = reservations.stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == null || !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .filter(r -> r.getPassengerCount() == null || r.getPassengerCount() > 0)
                .sorted(dispatchedLastComparator())
                .toList();

        model.addAttribute("date", date);
        model.addAttribute("reservations", activeReservations);
        List<Reservation> boardReservations = reservationRepository.findDailyManifest(date);
        model.addAttribute("outboundReservations", boardReservations.stream()
                .filter(reservation -> !isManifestReturn(reservation)).toList());
        model.addAttribute("returnReservations", boardReservations.stream()
                .filter(AgendaViewController::isManifestReturn).toList());
        model.addAttribute("choferes", choferes);
        model.addAttribute("selectedSchedule", schedule);
        model.addAttribute("selectedDirection", direction);
        model.addAttribute("outboundSchedules", OUTBOUND_SCHEDULES);
        model.addAttribute("returnSchedules", RETURN_SCHEDULES);
        model.addAttribute("specialReservationIds", activeReservations.stream()
                .filter(AgendaViewController::isSpecialTrip)
                .map(Reservation::getId)
                .collect(java.util.stream.Collectors.toSet()));
        model.addAttribute(
                "pickupAddressTexts",
                activeReservations.stream().collect(java.util.stream.Collectors.toMap(
                        Reservation::getId,
                        reservation -> resolvePickupAddress(reservation).text(),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new)));
        model.addAttribute(
                "pickupMapUrls",
                activeReservations.stream().collect(java.util.stream.Collectors.toMap(
                        Reservation::getId,
                        reservation -> resolvePickupAddress(reservation).mapUrl(),
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new)));
        int occupiedSeats = activeReservations.stream()
                .mapToInt(Reservation::getTotalSeats)
                .sum();
        FleetCapacityService.FleetSummary fleetSummary =
                fleetCapacityService.calculate(occupiedSeats);
        int safeVehicleCapacity = Math.max(vehicleCapacity, 1);
        int estimatedVehicles = occupiedSeats == 0
                ? 0
                : (int) Math.ceil((double) occupiedSeats / safeVehicleCapacity);
        java.math.BigDecimal totalAmount = sumMoney(
                activeReservations, Reservation::getAmount);
        java.math.BigDecimal totalExtraAmount = sumMoney(
                activeReservations, Reservation::getExtraAmount);
        java.math.BigDecimal totalDiscount = sumMoney(
                activeReservations, Reservation::getDiscountAmount);
        model.addAttribute("occupiedSeats", occupiedSeats);
        model.addAttribute("vehicleCapacity", safeVehicleCapacity);
        model.addAttribute("estimatedVehicles", estimatedVehicles);
        model.addAttribute("plannedCapacity", estimatedVehicles * safeVehicleCapacity);
        int plannedCapacity = estimatedVehicles * safeVehicleCapacity;
        model.addAttribute(
                "occupancyPercentage",
                plannedCapacity == 0
                        ? 0
                        : (int) Math.round(occupiedSeats * 100.0 / plannedCapacity));
        model.addAttribute(
                "requiresAdditionalVehicle",
                occupiedSeats > safeVehicleCapacity);
        model.addAttribute("ownFleetCapacity", FleetCapacityService.OWN_FLEET_CAPACITY);
        model.addAttribute("internalPassengers", fleetSummary.internalPassengers());
        model.addAttribute("externalPassengers", fleetSummary.externalPassengers());
        model.addAttribute("externalVehicles", fleetSummary.externalVehicles());
        model.addAttribute(
                "requiresExternalReinforcement",
                fleetSummary.requiresExternalReinforcement());
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalExtraAmount", totalExtraAmount);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute(
                "netBalance",
                totalAmount.add(totalExtraAmount));
        java.math.BigDecimal totalRevenue = activeReservations.stream()
                .filter(reservation ->
                        Boolean.TRUE.equals(reservation.getPaymentVerified()))
                .map(reservation -> {
                    java.math.BigDecimal amount = reservation.getAmount() == null
                            ? java.math.BigDecimal.ZERO
                            : reservation.getAmount();
                    java.math.BigDecimal extra = reservation.getExtraAmount() == null
                            ? java.math.BigDecimal.ZERO
                            : reservation.getExtraAmount();
                    return amount.add(extra);
                })
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute(
                "externalDriverExpense",
                fleetSummary.externalDriverExpense());
        model.addAttribute(
                "netRevenue",
                totalRevenue.subtract(fleetSummary.externalDriverExpense()));

        return "agenda-day";
    }

    @GetMapping("/admin/agenda/manifiesto")
    public String dailyManifestView(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {
        LocalDate operationDate = date == null ? com.lunaris.ansenuza.shared.ArgentinaTime.today() : date;
        List<Reservation> reservations = reservationRepository.findDailyManifest(operationDate);
        model.addAttribute("date", operationDate);
        model.addAttribute("reservations", reservations);
        model.addAttribute("outboundReservations", reservations.stream()
                .filter(reservation -> !isManifestReturn(reservation)).toList());
        model.addAttribute("returnReservations", reservations.stream()
                .filter(AgendaViewController::isManifestReturn).toList());
        model.addAttribute("uniquePassengers", new DailyPassengerManifestService().uniquePassengers(reservations));
        model.addAttribute("reservedSeats", new DailyPassengerManifestService().reservedSeats(reservations));
        return "admin/daily-passenger-manifest";
    }

    private static boolean isManifestReturn(Reservation reservation) {
        return "VUELTA".equalsIgnoreCase(reservation.getRouteDirection())
                || TripRouteCalculatorService.isCordoba(reservation.getPickupLocality());
    }

    @GetMapping(value = "/admin/agenda/manifiesto-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> dailyManifestPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        LocalDate operationDate = date == null ? com.lunaris.ansenuza.shared.ArgentinaTime.today() : date;
        List<Reservation> reservations = reservationRepository.findDailyManifest(operationDate);
        byte[] pdf = new DailyPassengerManifestService().generatePdf(operationDate, reservations);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"manifiesto-pasajeros-" + operationDate + ".pdf\"")
                .body(pdf);
    }

    static boolean isSpecialTrip(Reservation reservation) {
        boolean pendingWithoutPrice = (reservation.getAmount() == null
                || reservation.getAmount().signum() == 0)
                && "PENDING".equalsIgnoreCase(reservation.getStatus());
        return AirportTripDetector.isAirportTrip(
                reservation.getPickupLocality(), reservation.getDestination())
                || pendingWithoutPrice;
    }

    static PickupAddressDisplay resolvePickupAddress(Reservation reservation) {
        String reservationAddress = trimToNull(reservation.getPickupAddress());
        String passengerAddress = reservation.getPassenger() == null
                ? null
                : trimToNull(reservation.getPassenger().getAddress());
        String text = firstMatching(false, reservationAddress, passengerAddress);
        String mapUrl = firstMatching(true, reservationAddress, passengerAddress);
        return new PickupAddressDisplay(
                text == null ? "Sin dirección registrada" : text,
                mapUrl == null ? "" : mapUrl);
    }

    private static String firstMatching(
            boolean mapLocation, String... candidates) {
        return java.util.Arrays.stream(candidates)
                .filter(java.util.Objects::nonNull)
                .filter(candidate -> isMapLocation(candidate) == mapLocation)
                .findFirst()
                .orElse(null);
    }

    private static boolean isMapLocation(String value) {
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        return normalized.startsWith("https://maps.google.")
                || normalized.startsWith("https://www.google.")
                        && normalized.contains("/maps")
                || normalized.startsWith("https://maps.app.")
                || normalized.startsWith("https://waze.com/")
                || normalized.startsWith("https://www.waze.com/");
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    record PickupAddressDisplay(String text, String mapUrl) {
    }

    private java.math.BigDecimal sumMoney(
            List<Reservation> reservations,
            java.util.function.Function<Reservation, java.math.BigDecimal> extractor) {
        return reservations.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    // 💳 3. Confirmación asíncrona de pago
    @PostMapping("/api/agenda/confirmar-pago/{id}")
    @ResponseBody
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@PathVariable UUID id) {
        try {
            Reservation reservation = confirmPaymentUseCase.execute(id);
            List<UUID> synchronizedReservationIds = linkedReservations(reservation).stream()
                    .map(Reservation::getId)
                    .toList();

            return ResponseEntity.ok(new PaymentVerificationResponse(
                    synchronizedReservationIds, true, "CONFIRMED"));
        } catch (IllegalArgumentException | DomainValidationException exception) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).build();
        }
    }

    private List<Reservation> linkedReservations(Reservation reservation) {
        String reservationCode = reservation.getReservationCode();
        if (reservationCode == null || reservationCode.isBlank()) {
            return List.of(reservation);
        }
        String groupCode = reservationCode.replaceFirst("-(IDA|VUELTA)$", "");
        List<Reservation> linked = reservationRepository.findReservationGroup(groupCode);
        return linked.isEmpty() ? List.of(reservation) : linked;
    }

    // 📄 4. Descarga del comprobante
    @GetMapping("/api/agenda/comprobante-descarga/{id}")
    public ResponseEntity<byte[]> getReceiptImage(@PathVariable UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        String receiptUrl = reservation.getPaymentReceiptUrl();
        if (receiptUrl == null || !receiptUrl.contains("v20.0/")) {
            return ResponseEntity.notFound().build();
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> mediaResponse =
                    restTemplate.exchange(receiptUrl, HttpMethod.GET, entity, JsonNode.class);

            String actualDownloadUrl = mediaResponse.getBody().get("url").asText();

            ResponseEntity<byte[]> imageResponse =
                    restTemplate.exchange(actualDownloadUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
                    .body(imageResponse.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 🚖 5. Envío de Hoja de Ruta AL CHOFER CORREGIDO CON PLANTILLA Y ASIGNACIÓN
    @PostMapping("/api/agenda/enviar-hoja-ruta")
    public ResponseEntity<?> enviarHojaRuta(@RequestBody EnviarHojaRutaRequest request) {
        String choferPhone = request.phone();
        List<UUID> reservationIds = request.reservationIds();

        if (reservationIds == null || reservationIds.isEmpty()
                || request.driverId() == null && (choferPhone == null || choferPhone.isBlank())) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Seleccioná un chofer y al menos una reserva."));
        }

        // El UUID del dropdown es la identidad canónica; el teléfono queda como compatibilidad.
        String normalizedPhone = normalizeWhatsAppNumber(choferPhone);
        String lookupPhone = normalizedPhone;
        java.util.Optional<Driver> driverOpt = request.driverId() == null
                ? java.util.Optional.empty()
                : driverRepository.findById(request.driverId());
        if (driverOpt.isEmpty() && !normalizedPhone.isBlank()) {
            driverOpt = driverRepository.findFirstByPhone(normalizedPhone);
        }
        if (driverOpt.isEmpty()) {
            List<Driver> allDrivers = driverRepository.findAll();
            driverOpt = allDrivers.stream()
                    .filter(d -> normalizeWhatsAppNumber(d.getPhone()).equals(lookupPhone))
                    .findFirst();
        }

        if (driverOpt.isEmpty()) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .warn("No se encontró chofer con el teléfono: {}", choferPhone);
            return ResponseEntity.status(404).body(java.util.Map.of(
                    "message", "No se encontró el chofer seleccionado."));
        }

        Driver driver = driverOpt.get();
        normalizedPhone = normalizeWhatsAppNumber(driver.getPhone());

        Reservation firstReservation = reservationRepository.findById(reservationIds.get(0)).orElse(null);
        if (firstReservation == null || firstReservation.getTravelDate() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", "Las reservas seleccionadas no tienen una fecha válida."));
        }
        String assignedSchedule = firstReservation.getDepartureSchedule() == null
                || firstReservation.getDepartureSchedule().isBlank()
                ? "03:00" : firstReservation.getDepartureSchedule();
        RouteOriginResolution dispatchOrigin = resolveEffectiveTripOriginUseCase.resolve(
                firstReservation.getTravelDate(), assignedSchedule);
        List<Reservation> routeReservations;
        try {
            routeReservations = driverRouteService.replaceRoute(
                    driver, firstReservation.getTravelDate(), reservationIds,
                    AdminDashboardController.dynamicRouteComparator(dispatchOrigin));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "message", exception.getMessage()));
        }
        org.slf4j.LoggerFactory.getLogger(getClass()).info(dispatchOrigin.summary());

        try {
            routeReservations.stream()
                    .filter(reservation -> reservation.getPassenger() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            reservation -> normalizeWhatsAppNumber(
                                    reservation.getPassenger().getPhone()),
                            reservation -> reservation,
                            (first, ignored) -> first,
                            java.util.LinkedHashMap::new))
                    .forEach((passengerPhone, reservation) -> {
                        String passengerName = reservation.getPassenger().getFirstName();
                        whatsAppService.sendChoferAsignadoTemplate(
                                passengerPhone,
                                passengerName,
                                driver.getFullName(),
                                driver.getPhone());
                    });
        } catch (Exception exception) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "El chofer fue asignado, pero falló un aviso a pasajeros.", exception);
        }

        // 3. Enviar plantilla al chofer para abrir su hoja de ruta.
        try {
            String navigationUrl =
                    GoogleMapsParameterFormatter.buildDirectionsUrl(routeReservations);
            var dispatchResult = whatsAppService.sendDriverRouteDispatch(
                    normalizedPhone,
                    driver.getFullName(),
                    navigationUrl,
                    routeReservations);
            if (dispatchResult.success()) {
                driverRouteService.markRouteSent(routeReservations.stream()
                        .map(Reservation::getId).toList());
            }
            String notice = dispatchResult.success()
                    ? "Success: " + dispatchResult.message()
                    : "Warning: " + dispatchResult.message();
            return ResponseEntity.ok(java.util.Map.of(
                    "assigned", true,
                    "whatsAppStatus", dispatchResult.success() ? "Success" : "Warning",
                    "message", dispatchOrigin.summary() + " Chofer asignado correctamente en sistema. "
                            + "(Aviso de WhatsApp: " + notice + ")"));
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("Error al enviar la plantilla despierta_chofer al chofer", e);
            return ResponseEntity.ok(java.util.Map.of(
                    "assigned", true,
                    "whatsAppStatus", "Warning",
                    "message", "Chofer asignado correctamente en sistema. "
                            + "(Aviso de WhatsApp: Warning: no se pudo enviar la hoja de ruta.)"));
        }
    }

    static java.util.Comparator<Reservation> dispatchedLastComparator() {
        return java.util.Comparator
                .comparing((Reservation reservation) ->
                        reservation.getTravelStatus() == Reservation.TravelStatus.ROUTE_SENT)
                .thenComparing(reservation -> reservation.getDriver() != null)
                .thenComparing(Reservation::getRouteSequence,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
    }

    private String normalizeWhatsAppNumber(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        return clean.startsWith("549") ? "54" + clean.substring(3) : clean;
    }

    // 💬 6. Habilita http://localhost:8080/chat-room apuntando adentro de admin/
    @GetMapping("/chat-room")
    public String showChatRoom(Model model) {
        model.addAttribute("historial", new java.util.ArrayList<>());
        
        // El salvavidas para que el HTML no explote exigiendo el th:object
        model.addAttribute("reservation", new com.lunaris.ansenuza.domain.model.Reservation());
        
        // Listas para que se llenen los desplegables de localidad
        model.addAttribute("origenes", List.of("Morteros", "Brinkmann", "San Guillermo", "Porteña", "Suardi")); 
        model.addAttribute("destinos", List.of("Córdoba", "Aeropuerto Córdoba"));

        return "admin/chat-room"; // 👈 Corregido: va a buscar a templates/admin/chat-room.html
    }

    // 🤖 7. Habilita http://localhost:8080/bot-monitor apuntando adentro de admin/
    @GetMapping("/bot-monitor")
    public String showBotMonitor(Model model) {
        model.addAttribute("logs", new java.util.ArrayList<>());
        return "admin/bot-monitor"; // 👈 Corregido: va a buscar a templates/admin/bot-monitor.html
    }

    // 📋 8. Habilita http://localhost:8080/hoja-ruta apuntando adentro de admin/
    @GetMapping("/hoja-ruta")
    public String showHojaRuta(
            @RequestParam UUID driverId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate travelDate,
            org.springframework.security.core.Authentication authentication,
            Model model) {
        driverAuthorizationService.assertCanAccessDriver(authentication, driverId);
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) {
            model.addAttribute("routeError", "No se encontró el chofer indicado.");
            model.addAttribute("reservas", List.of());
            model.addAttribute("totalYendo", 0);
            model.addAttribute("totalVolviendo", 0);
        } else {
            List<Reservation> reservations =
                    reservationRepository.findByDriverIdAndTravelDateOrderByRouteSequenceAsc(
                            driverId, travelDate);
            if (!reservations.isEmpty()) {
                TripRouteCalculatorService calculator = new TripRouteCalculatorService();
                Reservation routeHead = reservations.getFirst();
                reservations = reservations.stream()
                        .filter(candidate -> calculator.sameManifest(routeHead, candidate))
                        .toList();
            }
            String scheduleBlock = reservations.stream().map(Reservation::getDepartureSchedule)
                    .filter(schedule -> schedule != null && !schedule.isBlank()).findFirst().orElse("03:00");
            RouteOriginResolution originResolution = resolveEffectiveTripOriginUseCase.resolve(travelDate, scheduleBlock);
            reservations = reservations.stream()
                    .sorted(AdminDashboardController.dynamicRouteComparator(originResolution)).toList();
            model.addAttribute("driver", driver);
            model.addAttribute("reservas", reservations);
            model.addAttribute("totalYendo", countSeats(reservations, false));
            model.addAttribute("totalVolviendo", countSeats(reservations, true));
            model.addAttribute(
                    "navigationUrl",
                    GoogleMapsParameterFormatter.buildDirectionsUrl(reservations));
            AdminDashboardController.addOriginAttributes(model, originResolution);
        }
        model.addAttribute("fechaSeleccionada", travelDate);
        model.addAttribute("pasajeros0800Count", 0);
        model.addAttribute("hubActivado", false);
        return "admin/hoja-ruta"; // 👈 Corregido: va a buscar a templates/admin/hoja-ruta.html
    }

    private int countSeats(List<Reservation> reservations, boolean fromCordoba) {
        return reservations.stream()
                .filter(reservation -> fromCordoba
                        == "Córdoba".equalsIgnoreCase(reservation.getPickupLocality()))
                .mapToInt(reservation ->
                        reservation.getPassengerCount() == null
                                ? 1
                                : reservation.getPassengerCount())
                .sum();
    }

    public record Chofer(String nombre, String telefono) {
    }

    public record PaymentVerificationResponse(
            List<UUID> reservationIds, boolean paymentVerified, String status) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ApiExceptionHandler.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.exception.SeatCapacityExceededException;
import com.lunaris.ansenuza.domain.exception.DriverApplicationNotFoundException;
import com.lunaris.ansenuza.domain.exception.SpecialTripNotFoundException;
import com.lunaris.ansenuza.domain.exception.FareLocalityInUseException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(com.lunaris.ansenuza.domain.exception.InquiryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> inquiryNotFound(RuntimeException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(FareLocalityInUseException.class)
    public ResponseEntity<Map<String, Object>> fareLocalityInUse(FareLocalityInUseException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(SpecialTripNotFoundException.class)
    public ResponseEntity<Map<String, Object>> specialTripNotFound(SpecialTripNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DriverApplicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> driverApplicationNotFound(
            DriverApplicationNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(SeatCapacityExceededException.class)
    public ResponseEntity<Map<String, Object>> capacity(SeatCapacityExceededException exception) {
        return response(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, Object>> validation(DomainValidationException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> beanValidation(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("La solicitud no es válida.");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", message,
                "timestamp", Instant.now()));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/AuthController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.BookingVerificationData;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.ProcessPaymentReceiptUseCase;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.domain.model.TripType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PassengerOtpService otpService;
    private final ProcessPaymentReceiptUseCase processPaymentReceiptUseCase;
    private final CreateReservationUseCase createReservationUseCase;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request.phone(), request.fullName());
        return ResponseEntity.accepted().body(Map.of(
                "message", "El código fue enviado por WhatsApp."));
    }

    @PostMapping(value = "/verify-otp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return verify(request, null);
    }

    @PostMapping(value = "/verify-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TokenResponse verifyOtpMultipart(
            @RequestPart(value = "reservation", required = false) @Valid VerifyOtpRequest reservation,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "code", required = false) String code,
            @RequestPart(value = "receipt", required = false) MultipartFile receiptFile) {
        VerifyOtpRequest effectiveRequest = reservation != null
                ? reservation : new VerifyOtpRequest(phone, code);
        return verify(effectiveRequest, receiptFile);
    }

    private TokenResponse verify(VerifyOtpRequest request, MultipartFile receiptFile) {
        validateVerificationRequest(request);
        var result = otpService.verifyOtp(request.phone(), request.code());
        var reservation = receiptFile != null && !receiptFile.isEmpty()
                ? processPaymentReceiptUseCase.confirmOrCreateWebBooking(
                        request.phone(), receiptFile, new BookingVerificationData(
                                request.travelDate(), request.departureSchedule(),
                                request.pickupLocality(), request.destination(),
                                request.passengerCount(), request.tripType(), request.totalAmount()))
                : request.travelDate() == null ? null
                : createReservationUseCase.execute(new CreateReservationRequest(
                        null,
                        request.fullName(),
                        request.phone(),
                        request.cuilDni(),
                        request.travelDate(),
                        request.pickupLocality(),
                        request.pickupAddress(),
                        request.destination(),
                        request.departureSchedule(),
                        request.tripType() != TripType.ONE_WAY,
                        request.returnDate(),
                        false,
                        request.notes(),
                        request.passengerCount(),
                        request.companionNames(),
                        ReservationSource.WEB,
                        request.tripType(),
                        request.promotionCode()));
        return new TokenResponse(result.accessToken(), "Bearer", result.expiresAt(),
                reservation == null ? null : reservation.getReservationCode(),
                reservation == null ? null : reservation.getBookingGroupCode(),
                "Reserva confirmada con éxito");
    }

    private void validateVerificationRequest(VerifyOtpRequest request) {
        if (request == null || request.phone() == null || request.phone().isBlank()
                || request.code() == null || !request.code().matches("[0-9]{4}")) {
            throw new com.lunaris.ansenuza.domain.exception.DomainValidationException(
                    "Teléfono y código OTP de cuatro dígitos son obligatorios.");
        }
    }

    public record SendOtpRequest(@NotBlank String phone, String fullName) {
        public SendOtpRequest(String phone) {
            this(phone, null);
        }
    }

    public record VerifyOtpRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "[0-9]{4}", message = "El código debe tener exactamente 4 dígitos.") String code,
            String fullName,
            String cuilDni,
            LocalDate travelDate,
            String departureSchedule,
            String pickupLocality,
            String pickupAddress,
            String destination,
            Integer passengerCount,
            String companionNames,
            TripType tripType,
            LocalDate returnDate,
            String notes,
            BigDecimal totalAmount,
            String promotionCode) {

        public VerifyOtpRequest(String phone, String code) {
            this(phone, code, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
    }

    public record TokenResponse(String accessToken, String tokenType, Instant expiresAt,
            String reservationCode, String bookingGroupCode, String message) {
        public TokenResponse(String accessToken, String tokenType, Instant expiresAt) {
            this(accessToken, tokenType, expiresAt, null, null, null);
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/BillingViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.lunaris.ansenuza.application.usecase.GetBillingPanelUseCase;
import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import com.lunaris.ansenuza.domain.model.Invoice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🧾 Panel de Facturación: registro de ingresos diario/mensual, reservas con pago
 * confirmado pendientes de facturar (con CUIL calculado), y carga + envío del PDF por WhatsApp.
 */
@Controller
@RequestMapping("/facturacion")
@RequiredArgsConstructor
@Slf4j
public class BillingViewController {

    private final GetBillingPanelUseCase getBillingPanelUseCase;
    private final IssueInvoiceUseCase issueInvoiceUseCase;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("panel", getBillingPanelUseCase.execute());
        return "facturacion";
    }

    /** Sirve la factura como PDF inline, independientemente del backend de almacenamiento. */
    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID invoiceId) {
        IssueInvoiceUseCase.InvoiceDocument document = issueInvoiceUseCase.download(invoiceId);
        String safeInvoiceNumber = document.invoiceNumber().replaceAll("[^A-Za-z0-9_-]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"factura-" + safeInvoiceNumber + ".pdf\"")
                .body(document.content());
    }

    /** Sube el PDF de la factura de una reserva y la envía por WhatsApp. */
    @PostMapping("/emitir/{reservationId}")
    public String emitir(@PathVariable UUID reservationId,
            @RequestParam("pdf") MultipartFile pdf,
            RedirectAttributes redirectAttributes) {
        try {
            if (pdf == null || pdf.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tenés que adjuntar el PDF de la factura.");
                return "redirect:/facturacion";
            }
            Invoice invoice = issueInvoiceUseCase.issue(reservationId, pdf.getBytes());
            if (Boolean.TRUE.equals(invoice.getSentViaWhatsapp())) {
                redirectAttributes.addFlashAttribute("ok",
                        "Factura " + invoice.getInvoiceNumber() + " emitida y enviada por WhatsApp.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Factura " + invoice.getInvoiceNumber()
                                + " guardada, pero falló el envío por WhatsApp. Probá 'Reenviar'.");
            }
        } catch (Exception e) {
            log.error("Error al emitir la factura para la reserva {}", reservationId, e);
            redirectAttributes.addFlashAttribute("error", "No se pudo emitir la factura: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** Reenvía por WhatsApp una factura ya emitida. */
    @PostMapping("/reenviar/{invoiceId}")
    public String reenviar(@PathVariable UUID invoiceId, RedirectAttributes redirectAttributes) {
        try {
            Invoice invoice = issueInvoiceUseCase.resend(invoiceId);
            if (Boolean.TRUE.equals(invoice.getSentViaWhatsapp())) {
                redirectAttributes.addFlashAttribute("ok",
                        "Factura " + invoice.getInvoiceNumber() + " reenviada por WhatsApp.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "No se pudo reenviar la factura " + invoice.getInvoiceNumber() + ".");
            }
        } catch (Exception e) {
            log.error("Error al reenviar la factura {}", invoiceId, e);
            redirectAttributes.addFlashAttribute("error", "No se pudo reenviar la factura: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/BotMonitorController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.TripType;
import com.lunaris.ansenuza.domain.model.service.OperationControlService;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/bot")
@AllArgsConstructor
@Slf4j
public class BotMonitorController {

    private final ConversationSessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Inyecciones homologadas para el procesamiento con el Bot y Carga Manual
    private final PassengerRepository passengerRepository;
    private final ChatMessageRepository messageRepository; 
    private final LocalityRepository localityRepository; 
    private final WhatsAppService whatsAppService;
    private final PricingAndScheduleService tarifaService;
    private final ReceiptStoragePort cloudinaryService;
    private final OperationControlService operationControlService;
    private final ReservationService reservationService;

    private final com.lunaris.ansenuza.application.usecase.BotMonitorService botMonitorService;
    private final com.lunaris.ansenuza.application.usecase.CreateManualReservationUseCase createManualReservation;

    @GetMapping("/monitor")
    public String getMonitor(Model model, Principal principal) {
        model.addAttribute("sesiones", botMonitorService.rows());
        model.addAttribute("jornadaActiva", operationControlService.isHumanActionEnabled());
        return "admin/bot-monitor";
    }

    @GetMapping("/monitor/rows")
    public String monitorRows(Model model) {
        model.addAttribute("sesiones", botMonitorService.rows());
        return "admin/bot-monitor :: monitorRows";
    }

    @PostMapping("/monitor/pause")
    @ResponseBody
    public ResponseEntity<Void> pause(@RequestParam long id, @RequestParam boolean paused) {
        botMonitorService.setPaused(id, paused);
        return ResponseEntity.noContent().build();
    }

    // 🖥️ Abre el formulario tradicional de nueva reserva
    @GetMapping("/monitor/nueva-reserva")
    public String mostrarFormularioManual(Model model) {
        // Mantenemos soporte Thymeleaf tradicional por si acaso
        List<String> localidades = localityRepository.findAllWithActiveFare().stream()
                .map(locality -> locality.getName())
                .collect(Collectors.toList());
        
        model.addAttribute("localidades", localidades);
        return "reservation-form"; 
    }

    // 📡 NUEVO ENDPOINT API: Retorna todas las localidades en JSON para que las consuman vía JS en caliente
    @GetMapping("/monitor/localidades")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerLocalidades() {
      try {
            // 🚀 Usamos el método que filtra solo pueblos con tarifas y los ordena de la A a la Z
            List<String> localidades = localityRepository.findAllWithActiveFare().stream()
                    .map(locality -> locality.getName())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(localidades);
        } catch (Exception e) {
            log.error("[API Localidades] Error al consultar de la BD: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 🌙 Acción POST para encender o apagar la jornada de atención humana
    @PostMapping("/toggle-jornada")
    public String toggleJornada(@RequestParam("enabled") boolean enabled) {
        operationControlService.setHumanActionEnabled(enabled);
        log.info("[Jornada Laboral] Modificada por Administrador. ¿Atención humana activa?: {}", enabled);
        return "redirect:/admin/bot/monitor";
    }

    // 🛑 Acción para pausar o activar el bot de forma dinámica por chat individual
    @PostMapping("/toggle-bot")
    public String toggleBot(@RequestParam("id") Long sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId).orElseThrow(
                () -> new IllegalArgumentException("Sesión no encontrada con ID: " + sessionId));

        boolean currentState = session.isBotPaused();
        session.setBotPaused(!currentState);
        session.setManuallyPaused(!currentState);

        sessionRepository.saveAndFlush(session);

        messagingTemplate.convertAndSend("/topic/system-alerts",
                Map.of("action", "TOGGLE_BOT", "sessionId", sessionId, "isPaused", !currentState));

        return "redirect:/admin/bot/monitor";
    }

    // 💰 COTIZACIÓN EN VIVO ASÍNCRONA PARA EL FORMULARIO
    @GetMapping("/monitor/cotizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cotizarFilaManual(
            @RequestParam("pickupLocality") String origen,
            @RequestParam("destination") String destino,
            @RequestParam("passengerCount") int asientos,
            @RequestParam(value = "roundTrip", defaultValue = "true") boolean roundTrip) {

        Map<String, Object> respuesta = new HashMap<>();
        try {
            java.math.BigDecimal montoTramo =
                    tarifaService.calculateReservationAmount(origen, destino, roundTrip, asientos);

            String prefijoCodigo = "---";
            if (origen.length() >= 3 && destino.length() >= 3) {
                prefijoCodigo = origen.substring(0, 3).toUpperCase() + "-"
                        + destino.substring(0, 3).toUpperCase();
            }

            respuesta.put("monto", montoTramo);
            respuesta.put("codigo", prefijoCodigo + "-XXXXX");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("monto", java.math.BigDecimal.ZERO);
            respuesta.put("codigo", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    // 🔍 ENDPOINT EXTENDIDO: Busca pasajero por teléfono y retorna todos sus datos y saldo actual
    @GetMapping("/monitor/pasajero/saldo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerSaldoPasajero(@RequestParam("phone") String phone) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            String telefonoClean = phone.trim();
            java.util.Optional<Passenger> passengerOpt = passengerRepository.findByPhone(telefonoClean);
            
            if (passengerOpt.isPresent()) {
                Passenger p = passengerOpt.get();
                respuesta.put("existe", true);
                respuesta.put("firstName", p.getFirstName() != null ? p.getFirstName() : "");
                respuesta.put("lastName", p.getLastName() != null ? p.getLastName() : "");
                respuesta.put("cuil", p.getCuil() != null ? p.getCuil() : "");
                respuesta.put("saldo", p.getCurrentBalance() != null ? p.getCurrentBalance() : java.math.BigDecimal.ZERO);
            } else {
                respuesta.put("existe", false);
                respuesta.put("saldo", java.math.BigDecimal.ZERO);
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            log.error("[Buscador Pasajero] Error al consultar para el teléfono {}: ", phone, e);
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        }
    }

    // 🚀 CARGA MANUAL ASISTIDA (Desde la pantalla dividida del chat en vivo)
    @PostMapping("/monitor/cargar-reserva")
    public String cargarReservaManualOperador(
            @RequestParam("phone") String phone,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,   
            @RequestParam(value = "cuil", required = false) String cuil,
            @RequestParam("pickupLocality") String pickupLocality,
            @RequestParam("destination") String destination,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("passengerCount") int passengerCount,
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, 
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "chatReceiptUrl", required = false, defaultValue = "null") String chatReceiptUrl,
            @org.springframework.web.bind.annotation.ModelAttribute
            com.lunaris.ansenuza.infrastructure.web.dto.reservation.ManualReservationOptions options,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = Passenger.builder().firstName(firstName).lastName(lastName)
                    .phone(phone).cuil(cuil).build();

            String urlComprobanteCruda = messageRepository.findByPhoneNumberOrderByTimestampAsc(phone).stream()
                    .filter(m -> m != null && !m.isFromOperator()) 
                    .map(m -> m.getMessageText())
                    .filter(text -> text != null && !text.isBlank())
                    .filter(text -> !text.contains("ahí mando") && !text.contains("ahi mando") && !text.contains("gracias"))
                    .filter(text -> text.startsWith("http://") 
                                 || text.startsWith("https://") 
                                 || text.contains("res.cloudinary.com")
                                 || text.matches(".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*"))
                    .reduce((first, second) -> second) 
                    .orElse("null");

            if (chatReceiptUrl != null && !chatReceiptUrl.isBlank() && chatReceiptUrl.startsWith("http")) {
                urlComprobanteCruda = chatReceiptUrl;
            }

            String urlComprobantePermanente = persistirComprobanteEnCloudinary(urlComprobanteCruda, phone);

            String notasAuditoria = urlComprobantePermanente != null
                    ? "Cargado por Operador desde Monitor. Comprobante enlazado y persistido en Cloudinary."
                    : "Cargado por Operador desde Monitor. ⚠️ Comprobante no pudo persistirse.";

            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(options.getAmount());
            ida.setExtraAmount(options.getExtraAmount());
            ida.setCompanionNames(options.getCompanionNames());
            ida.setRouteDirection(options.getRouteDirection());
            ida.setDiscountAmount(options.getDiscountAmount());
            ida.setPaymentReceiptUrl(urlComprobantePermanente); 
            ida.setStatus("PENDING_VERIFICATION");
            ida.setPaymentVerified(false);
            ida.setRoundTrip(roundTrip);
            ida.setTripType(tripType(roundTrip, returnDate));
            ida.setReturnDate(returnDate);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setNotes(options.getNotes() == null || options.getNotes().isBlank() ? notasAuditoria : options.getNotes());

            createManualReservation.execute(ida, options.getReturnDepartureSchedule());

            redirectAttributes.addFlashAttribute("successMessage", "¡Reserva registrada con éxito!");

        } catch (Exception e) {
            log.error("[Carga Manual] Falló el flujo asistido: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/admin/chat/" + phone;
    }

    // 🖥️ CARGA MANUAL WEB TRADICIONAL (Desde el formulario web de administración)
    @PostMapping("/monitor/cargar-reserva-web")
    public String cargarReservaWebTradicional(
            @RequestParam("phone") String phone,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,   
            @RequestParam(value = "cuil", required = false) String cuil,
            @RequestParam("pickupLocality") String pickupLocality,
            @RequestParam("destination") String destination,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("passengerCount") int passengerCount,
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate, 
            @RequestParam("departureSchedule") String departureSchedule,
            @RequestParam(value = "returnDepartureSchedule", required = false) String returnDepartureSchedule,
            @RequestParam(value = "roundTrip", defaultValue = "false") boolean roundTrip,
            @RequestParam(value = "requiresInvoice", defaultValue = "false") boolean requiresInvoice,
            @RequestParam(value = "notes", required = false) String notes,
            @org.springframework.web.bind.annotation.ModelAttribute
            com.lunaris.ansenuza.infrastructure.web.dto.reservation.ManualReservationOptions options,
            RedirectAttributes redirectAttributes) {

        try {
            Passenger passenger = Passenger.builder().firstName(firstName).lastName(lastName)
                    .phone(phone).cuil(cuil).build();

            Reservation ida = new Reservation();
            ida.setPassenger(passenger);
            ida.setTravelDate(travelDate);
            ida.setPickupLocality(pickupLocality);
            ida.setPickupAddress(pickupAddress);
            ida.setDestination(destination);
            ida.setPassengerCount(passengerCount);
            ida.setAmount(options.getAmount());
            ida.setExtraAmount(options.getExtraAmount());
            ida.setCompanionNames(options.getCompanionNames());
            ida.setRouteDirection(options.getRouteDirection());
            ida.setDiscountAmount(options.getDiscountAmount());
            ida.setStatus("CONFIRMED"); 
            ida.setPaymentVerified(false);
            ida.setRoundTrip(roundTrip);
            ida.setTripType(tripType(roundTrip, returnDate));
            ida.setReturnDate(returnDate);
            ida.setDepartureSchedule(departureSchedule);
            ida.setRequiresInvoice(requiresInvoice);
            ida.setNotes(notes != null ? notes : "Cargado manualmente desde la administración web.");

            createManualReservation.execute(ida, returnDepartureSchedule);

            redirectAttributes.addFlashAttribute("successMessage", "¡Reserva manual creada correctamente!");

        } catch (Exception e) {
            log.error("[Carga Web] Error al procesar reserva manual: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/agenda?success=true";
    }

    private TripType tripType(boolean roundTrip, LocalDate returnDate) {
        if (!roundTrip) return TripType.ONE_WAY;
        return returnDate == null ? TripType.OPEN_RETURN : TripType.ROUND_TRIP;
    }

    private String persistirComprobanteEnCloudinary(String urlOrigen, String phone) {
        if (urlOrigen == null || urlOrigen.isBlank() || "null".equalsIgnoreCase(urlOrigen)) {
            return null;
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(urlOrigen))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            java.net.http.HttpResponse<byte[]> response =
                    client.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200 || response.body() == null || response.body().length == 0) {
                log.warn("[Comprobante Cloudinary] Descarga falló con status {} para el teléfono {}", response.statusCode(), phone);
                return null;
            }

            String nombreArchivo = "comprobante-" + phone + "-" + System.currentTimeMillis();

            final byte[] contenido = response.body();
            org.springframework.web.multipart.MultipartFile multipartFile = new org.springframework.web.multipart.MultipartFile() {
                @Override
                public String getName() { return nombreArchivo; }
                @Override
                public String getOriginalFilename() { return nombreArchivo + ".jpg"; }
                @Override
                public String getContentType() { return "image/jpeg"; }
                @Override
                public boolean isEmpty() { return contenido.length == 0; }
                @Override
                public long getSize() { return contenido.length; }
                @Override
                public byte[] getBytes() throws java.io.IOException { return contenido; }
                @Override
                public java.io.InputStream getInputStream() throws java.io.IOException { 
                    return new java.io.ByteArrayInputStream(contenido); 
                }
                @Override
                public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                    java.nio.file.Files.write(dest.toPath(), contenido);
                }
            };

            return cloudinaryService.uploadFile(multipartFile);

        } catch (Exception e) {
            log.error("[Comprobante Cloudinary] Error crítico al procesar la subida para el teléfono {}: ", phone, e);
            return null;
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ChatController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.domain.model.ChatMessage;
import com.lunaris.ansenuza.domain.model.Reservation; // 🚐 Importación de tu modelo de Reserva
import com.lunaris.ansenuza.domain.repository.ChatMessageRepository;
import com.lunaris.ansenuza.domain.repository.ConversationSessionRepository;
import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.application.usecase.TakeOverConversationUseCase;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.model.service.WhatsAppConversationWindowService;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/admin/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatMessageRepository messageRepository;
    private final ConversationSessionRepository sessionRepository;
    private final LocalityService localityService;
    private final PassengerRepository passengerRepository;
    private final WhatsAppConversationWindowService conversationWindowService;
    private final WhatsAppService whatsAppService;
    private final TakeOverConversationUseCase takeOverConversation;

    @PostMapping("/{phoneNumber}/takeover")
    public String takeOver(@PathVariable String phoneNumber, RedirectAttributes redirectAttributes) {
        String phone = takeOverConversation.execute(phoneNumber);
        redirectAttributes.addAttribute("phoneNumber", phone);
        return "redirect:/admin/chat/{phoneNumber}";
    }


    @GetMapping
    public String openChatByPhone(@RequestParam String phone, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("phoneNumber", phone);
        return "redirect:/admin/chat/{phoneNumber}";
    }

    @GetMapping("/{phoneNumber}")
    public String openChat(@PathVariable String phoneNumber, Model model) {
        List<ChatMessage> historial = messageRepository.findByPhoneNumberOrderByTimestampAsc(phoneNumber);

        // 1. Datos del chat originales (Intactos para tu WebSocket actual)
        sessionRepository.findByPhoneNumber(phoneNumber)
                .ifPresent(session -> model.addAttribute("session", session));
        model.addAttribute("historial", historial);
        model.addAttribute("phone", phoneNumber);
        model.addAttribute("chatWindowActive", conversationWindowService.isActive(phoneNumber));
        model.addAttribute("chatWindowExpiresAt",
                conversationWindowService.expirationFor(phoneNumber).orElse(null));

        // 2. Datos dinámicos para habilitar la Nueva Reserva Asistida en espejo
        model.addAttribute("localities", localityService.findAllWithActiveFare());
        model.addAttribute("reservation", new Reservation()); 

        return "admin/chat-room";
    }

    @PostMapping("/{phoneNumber}/contactar")
    public String reopenConversation(@PathVariable String phoneNumber,
            RedirectAttributes redirectAttributes) {
        String passengerName = passengerRepository.findByPhone(phoneNumber)
                .map(passenger -> passenger.getFirstName())
                .filter(name -> !name.isBlank())
                .orElse("Pasajero");
        whatsAppService.sendContactoPasajeroTemplate(phoneNumber, passengerName);
        redirectAttributes.addFlashAttribute("successMessage",
                "Plantilla contacto_pasajero enviada. El chat se habilitará cuando el pasajero responda.");
        return "redirect:/admin/chat/" + phoneNumber;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ConfigurationController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.SystemConfiguration;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfigurationController {

    private final SystemConfigurationService configurationService;

    @GetMapping
    public List<SystemConfiguration> findAll() {
        return configurationService.findAll();
    }

    @GetMapping("/{key}")
    public SystemConfiguration findByKey(@PathVariable String key) {
        return configurationService.findByKey(key);
    }

    @PostMapping
    public ResponseEntity<SystemConfiguration> save(@RequestBody ConfigurationRequest request) {
        SystemConfiguration saved = configurationService.save(request.key(), request.value());
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{key}")
    public ResponseEntity<SystemConfiguration> saveByKey(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {
        SystemConfiguration saved = configurationService.save(key, request.get("value"));
        return ResponseEntity.ok(saved);
    }

    public record ConfigurationRequest(String key, String value) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DashboardController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDailyOperationSummaryUseCase useCase;

    @GetMapping("/date/{travelDate}")
    public DailyOperationSummaryResponse getSummary(
            @PathVariable LocalDate travelDate) {

        return useCase.execute(travelDate);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DashboardViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.application.usecase.GetDailyOperationSummaryUseCase;
import com.lunaris.ansenuza.application.usecase.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.domain.model.Fare;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.domain.repository.WaitingListRepository;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import com.lunaris.ansenuza.domain.model.service.SystemConfigurationService;
import com.lunaris.ansenuza.domain.model.service.AirportTripDetector;
import com.lunaris.ansenuza.infrastructure.web.dto.dashboard.DailyOperationSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardViewController {

    private final GetDailyOperationSummaryUseCase useCase;
    private final ReservationRepository reservationRepository;
    private final FareRepository fareRepository;
    private final ReservationService reservationService;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final WaitingListRepository waitingListRepository;
    private final SystemConfigurationService systemConfigurationService;

    /**
     * 📊 Vista del Dashboard Principal
     */
    @GetMapping({"/dashboard", "/admin/dashboard"})
    public String dashboard(Model model) {
        LocalDate today = com.lunaris.ansenuza.shared.ArgentinaTime.today();
        DailyOperationSummaryResponse summary = useCase.execute(today);
        model.addAttribute("summary", summary);

        model.addAttribute("ingresoHoy", reservationRepository.sumConfirmedIncomeBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay()));
        model.addAttribute("ingresoMes", reservationRepository.sumConfirmedIncomeBetween(
                today.withDayOfMonth(1).atStartOfDay(), today.withDayOfMonth(1).plusMonths(1).atStartOfDay()));
        int confirmedPassengers = reservationRepository.findByTravelDate(today).stream()
                .filter(reservation -> Boolean.TRUE.equals(reservation.getPaymentVerified())
                        && "CONFIRMED".equals(reservation.getStatus()))
                .mapToInt(Reservation::getTotalSeats)
                .sum();
        long waitingListPassengers = waitingListRepository
                .sumPassengerCountByTravelDateAndStatus(today, WaitingListEntry.WAITING);
        model.addAttribute("confirmedPassengers", confirmedPassengers);
        model.addAttribute("waitingListPassengers", waitingListPassengers);
        model.addAttribute("capacityExceeded",
                confirmedPassengers > systemConfigurationService.getScheduleMaxCapacity());

        return "dashboard";
    }

    /**
     * 🗃️ 1. LA GRILLA DE RESERVAS POTENCIADA (Filtrado por Status, Texto y FECHA)
     */
    @GetMapping("/reservas-panel")
    public String grillaReservasPlana(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        log.info("[Grilla] Cargando panel operativo avanzado. Filtro Fecha: {}", date);
        
        List<Reservation> reservations;
        
        if (date != null) {
            reservations = reservationRepository.findByTravelDate(date);
        } else {
            reservations = reservationRepository.findAll();
        }

        // Regla de Ocultamiento de Cancelados
        if ("CANCELLED".equals(status)) {
            reservations = reservations.stream()
                    .filter(r -> r != null && "CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
        } else {
            reservations = reservations.stream()
                    .filter(r -> r != null && !"CANCELLED".equals(r.getStatus()))
                    .collect(Collectors.toList());
            
            if (status != null && !status.isBlank() && !"ALL".equals(status)) {
                reservations = reservations.stream()
                        .filter(r -> status.equals(r.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        // Buscador global interactivo
        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            reservations = reservations.stream()
                    .filter(r -> r.getPassenger() != null && 
                                 ((r.getPassenger().getFirstName() != null && r.getPassenger().getFirstName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getLastName() != null && r.getPassenger().getLastName().toLowerCase().contains(query)) ||
                                 (r.getPassenger().getPhone() != null && r.getPassenger().getPhone().contains(query)) ||
                                 (r.getReservationCode() != null && r.getReservationCode().toLowerCase().contains(query))))
                    .collect(Collectors.toList());
        }

        reservations = reservations.stream()
                .sorted(AgendaViewController.dispatchedLastComparator())
                .toList();
        model.addAttribute("reservas", reservations);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("currentDate", date);
        model.addAttribute("specialReservationIds", reservations.stream()
                .filter(r -> AirportTripDetector.isAirportTrip(r.getPickupLocality(), r.getDestination())
                        || ((r.getAmount() == null || r.getAmount().signum() == 0)
                            && "PENDING".equalsIgnoreCase(r.getStatus())))
                .map(Reservation::getId)
                .collect(java.util.stream.Collectors.toSet()));

        return "reservations-grid"; 
    }

    /**
     * 🎯 2. ACCIÓN DE VERIFICACIÓN CONTROLADA INDIVIDUAL (Firma nativa con UUID)
     */
    @PostMapping("/reservas-panel/verify-tandem/{id}")
    public String verifyPaymentTandemPlano(@PathVariable(value = "id") UUID id) {
        try {
            log.info("[Validación] Procesando confirmación individual para UUID: {}", id);
            
            confirmPaymentUseCase.execute(id);
            log.info("[Validación] Éxito. Tramo validado de forma individual.");
            
        } catch (Exception e) {
            log.error("[Validación] Error crítico al verificar el tramo individual: ", e);
        }
        return "redirect:/reservas-panel";
    }

    @PostMapping("/reservations/{id}/verify-payment")
    public String verifyPayment(@PathVariable UUID id) {
        confirmPaymentUseCase.execute(id);
        return "redirect:/reservas-panel";
    }

    @PostMapping("/reservas-panel/{id}/importe-acordado")
    public String updateAgreedAmount(
            @PathVariable UUID id,
            @RequestParam BigDecimal amount,
            @RequestParam LocalDate date,
            @RequestParam String schedule,
            @RequestParam com.lunaris.ansenuza.domain.model.service.TripRouteCalculatorService.RouteDirection direction,
            RedirectAttributes redirectAttributes) {
        try {
            reservationService.updateAgreedAmount(id, amount);
            redirectAttributes.addFlashAttribute("success", "Importe acordado actualizado.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        redirectAttributes.addAttribute("date", date);
        redirectAttributes.addAttribute("schedule", schedule);
        redirectAttributes.addAttribute("direction", direction);
        return "redirect:/agenda/view-detalle";
    }

    /**
     * ❌ 3. BAJA INTEGRADA CON CUENTA CORRIENTE (Firma nativa con UUID)
     */
    @PostMapping("/reservas-panel/cancel/{id}")
    public String cancelFromGridPlano(
            @PathVariable(value = "id") UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("[Baja Controlada] Procesando cancelación para UUID: {}", id);
            
            reservationService.cancelReservation(id, "ADMIN_PANEL");
            log.info("[Baja Controlada] Éxito. Saldo impactado en la cuenta del pasajero.");
        } catch (IllegalStateException exception) {
            log.warn("[Baja Controlada] Cancelación bloqueada para UUID {}: {}", id, exception.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        } catch (Exception e) {
            log.error("[Baja Controlada] Error crítico en el flujo de cancelación: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo cancelar la reserva.");
        }
        return "redirect:/reservas-panel";
    }

    /**
     * 💵 4. VISTA DE TARIFAS VIGENTES desde Postgres
     */
    @GetMapping("/fares")
    public String verTarifasComerciales(Model model) {
        List<Fare> tarifas = fareRepository.findAll();
        model.addAttribute("tarifas", tarifas);
        return "fares";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase.MultipartSubmission;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class DriverApplicationApiController {

    private final SubmitDriverApplicationUseCase submitDriverApplicationUseCase;

    @PostMapping(
            path = {"/api/drivers/apply", "/api/drivers/applications"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriverApplicationResponse> apply(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(value = "locality", required = false) String locality,
            @RequestParam(required = false) String vehicleModel,
            @RequestParam(required = false) Integer vehicleYear,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(defaultValue = "false") boolean wantsDirectContact,
            @RequestPart(value = "insuranceFile", required = false) MultipartFile insuranceFile,
            @RequestPart(value = "greenCardFile", required = false) MultipartFile greenCardFile,
            @RequestPart(value = "criminalRecordFile", required = false) MultipartFile criminalRecordFile) {
        String effectiveLicensePlate = licensePlate != null ? licensePlate : plateNumber;
        validateRequired(fullName, "fullName");
        validateRequired(phone, "phone");
        if (vehicleYear != null && vehicleYear <= 0) {
            throw new DomainValidationException("vehicleYear debe ser mayor a cero.");
        }

        DriverApplication application = submitDriverApplicationUseCase.execute(
                new MultipartSubmission(
                        fullName, phone,
                        locality,
                        vehicleModel, vehicleYear, effectiveLicensePlate,
                        wantsDirectContact),
                insuranceFile,
                greenCardFile,
                criminalRecordFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DriverApplicationResponse.from(application));
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " es obligatorio.");
        }
    }

    public record DriverApplicationResponse(
            UUID id,
            String status,
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String plateNumber,
            boolean wantsDirectContact) {

        static DriverApplicationResponse from(DriverApplication application) {
            return new DriverApplicationResponse(
                    application.getId(),
                    application.getStatus().name(),
                    application.getFullName(),
                    application.getPhone(),
                    application.getLocality(),
                    application.getVehicleModel(),
                    application.getVehicleYear(),
                    application.getLicensePlate(),
                    application.isWantsDirectContact());
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/driver-applications")
@RequiredArgsConstructor
public class DriverApplicationController {

    private final DriverApplicationManagementService managementService;

    @GetMapping
    public List<DriverApplication> findPending() {
        return managementService.findPending();
    }

    @PutMapping("/{id}/approve")
    public DriverApplication approve(@PathVariable UUID id) {
        return managementService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public DriverApplication reject(@PathVariable UUID id) {
        return managementService.reject(id);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverApplicationViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.DriverApplicationManagementService;
import com.lunaris.ansenuza.domain.model.DriverApplication;
import com.lunaris.ansenuza.domain.repository.DriverApplicationRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequestMapping("/admin/postulaciones")
@RequiredArgsConstructor
public class DriverApplicationViewController {

    private final DriverApplicationManagementService managementService;
    private final DriverApplicationRepository applicationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public String panel(Model model) {
        List<DriverApplication> postulaciones = Optional.ofNullable(
                        applicationRepository.findByStatusOrderByCreatedAtAsc(
                                DriverApplication.Status.PENDING))
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .toList();
        model.addAttribute("postulaciones", postulaciones);
        return "admin/postulaciones";
    }

    @PostMapping("/{id}/aprobar")
    public String approve(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación aprobada y chofer activado.");
        return "redirect:/admin/postulaciones?approved=true";
    }

    @PostMapping("/{id}/rechazar")
    public String reject(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        managementService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Postulación rechazada.");
        return "redirect:/admin/postulaciones";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.Reservation.TravelStatus;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.application.usecase.OnboardPassengerUseCase;
import com.lunaris.ansenuza.application.usecase.DriverManagementService;
import com.lunaris.ansenuza.application.usecase.DriverAuthorizationService;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final ReservationRepository reservationRepository;
    private final OnboardPassengerUseCase onboardPassengerUseCase;
    private final DriverManagementService driverManagementService;
    private final DriverAuthorizationService driverAuthorizationService;

    @GetMapping({"/drivers", "/api/drivers"})
    public List<Driver> findAll() {
        return driverManagementService.findAll();
    }

    @PostMapping({"/drivers", "/api/drivers"})
    public Driver create(
            @RequestBody CreateDriverRequest request) {
        return driverManagementService.create(
                request.fullName(), request.phone(), request.ranking(), request.active());
    }

    @PostMapping("/api/driver/confirm-assistance")
    public ResponseEntity<?> confirmAssistance(
            @RequestBody ConfirmAssistanceRequest request, Authentication authentication) {
        String code = request != null && request.code() != null
                ? request.code().trim().toUpperCase()
                : "";

        if (code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El código de reserva es obligatorio."));
        }

        return reservationRepository.findByReservationCode(code)
                .map(reservation -> {
                    UUID assignedDriverId = reservation.getDriver() == null
                            ? null : reservation.getDriver().getId();
                    driverAuthorizationService.assertCanAccessDriver(
                            authentication, assignedDriverId);
                    Reservation saved = onboardPassengerUseCase.updateTravelStatus(
                            reservation.getId(), TravelStatus.REALIZED);
                    return ResponseEntity.ok(Map.of(
                            "reservationId", saved.getId(),
                            "code", saved.getReservationCode(),
                            "travelStatus", saved.getTravelStatus().name()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró una reserva con el código indicado.")));
    }

    @RequestMapping(
            value = "/api/reservations/{id}/travel-status",
            method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateTravelStatus(
            @PathVariable UUID id,
            @RequestBody(required = false) UpdateTravelStatusRequest request,
            Authentication authentication) {
        String rawTravelStatus = request != null ? request.travelStatus() : null;
        log.info(
                "[TravelStatus API] Incoming travelStatus={} for reservationId={}",
                rawTravelStatus, id);
        if (rawTravelStatus == null || rawTravelStatus.isBlank()) {
            log.warn(
                    "[TravelStatus API] Null or blank travelStatus received. "
                            + "reservationId={}, payload={}",
                    id, request);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El estado de viaje es obligatorio."));
        }
        TravelStatus travelStatus;
        try {
            travelStatus = TravelStatus.valueOf(rawTravelStatus);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[TravelStatus API] Invalid travelStatus received. "
                            + "reservationId={}, payload={}",
                    id, request);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estado de viaje inválido: " + rawTravelStatus));
        }
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada: " + id));
        UUID assignedDriverId = reservation.getDriver() == null
                ? null : reservation.getDriver().getId();
        driverAuthorizationService.assertCanAccessDriver(authentication, assignedDriverId);
        Reservation saved = onboardPassengerUseCase.updateTravelStatus(id, travelStatus);
        return ResponseEntity.ok(Map.of(
                "reservationId", saved.getId(),
                "travelStatus", saved.getTravelStatus().name()));
    }

    public record ConfirmAssistanceRequest(String code) {
    }

    public record UpdateTravelStatusRequest(String travelStatus) {
    }

    public record CreateDriverRequest(
            @JsonAlias("name") String fullName,
            String phone,
            Integer ranking,
            Boolean active) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/DriverViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.beans.PropertyEditorSupport;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataAccessException;
import com.lunaris.ansenuza.shared.PhoneUtils;

/**
 * ABM web (Alta/Baja/Modificación) de choferes de la flota.
 *
 * <p>Se monta en {@code /choferes} para no colisionar con {@link DriverController},
 * que expone la API REST/JSON en {@code /drivers}. Pensado para uso operativo simple:
 * un formulario arriba y la grilla de choferes con acciones directas.
 */
@Controller
@RequestMapping("/choferes")
@RequiredArgsConstructor
@Slf4j
public class DriverViewController {

    private final DriverRepository driverRepository;

    @InitBinder("driver")
    void initDriverBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? null : UUID.fromString(text.trim()));
            }
        });
    }

    @GetMapping
    public String panel(Model model) {
        preparePanel(model);
        if (!model.containsAttribute("driver")) {
            model.addAttribute("driver", new DriverForm());
        }
        return "choferes";
    }

    /** Alta (id vacío) o modificación (id presente) de un chofer. */
    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("driver") DriverForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            logBindingErrors(bindingResult);
            preparePanel(model);
            return "choferes";
        }

        Driver driver;
        if (form.getId() == null) {
            driver = new Driver();
        } else {
            driver = driverRepository.findById(form.getId()).orElse(null);
            if (driver == null) {
                bindingResult.rejectValue(
                        "id", "driver.notFound", "El chofer seleccionado ya no existe.");
                logBindingErrors(bindingResult);
                preparePanel(model);
                return "choferes";
            }
        }

        try {
            driver.setFullName(form.getFullName());
            driver.setPhone(PhoneUtils.normalizeArgentinePhone(form.getPhone()));
            driver.setRanking(parseRanking(form.getRanking()));
            driver.setActive(form.isActive());
            driverRepository.saveAndFlush(driver);
        } catch (DataAccessException exception) {
            log.error("[Choferes] Error de persistencia al guardar el chofer: {}",
                    exception.getMessage(), exception);
            return "redirect:/choferes?error=true";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                form.getId() == null
                        ? "Chofer creado correctamente."
                        : "Chofer actualizado correctamente.");
        return "redirect:/choferes";
    }

    private int parseRanking(String rawRanking) {
        try {
            int ranking = Integer.parseInt(rawRanking);
            return ranking >= 1 && ranking <= 5 ? ranking : 5;
        } catch (NumberFormatException | NullPointerException exception) {
            return 5;
        }
    }

    private void preparePanel(Model model) {
        model.addAttribute("choferes", driverRepository.findAll());
    }

    private void logBindingErrors(BindingResult bindingResult) {
        bindingResult.getFieldErrors().forEach(error -> log.warn(
                "[Choferes] Error de binding. field={}, rejectedValue={}, message={}",
                error.getField(), error.getRejectedValue(), error.getDefaultMessage()));
        bindingResult.getGlobalErrors().forEach(error -> log.warn(
                "[Choferes] Error de binding global. object={}, message={}",
                error.getObjectName(), error.getDefaultMessage()));
    }

    /** Baja/alta lógica: alterna el estado activo del chofer sin borrarlo. */
    @PostMapping("/estado/{id}")
    public String alternarEstado(@PathVariable UUID id) {
        driverRepository.findById(id).ifPresent(driver -> {
            driver.setActive(!driver.isActive());
            driverRepository.save(driver);
        });
        return "redirect:/choferes";
    }

    /** Baja física definitiva del chofer. */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable UUID id) {
        driverRepository.deleteById(id);
        return "redirect:/choferes";
    }

    @Getter
    @Setter
    public static class DriverForm {

        private UUID id;

        @NotBlank(message = "El nombre y apellido son obligatorios.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        private String fullName;

        @NotBlank(message = "El teléfono es obligatorio.")
        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres.")
        private String phone;

        private String ranking = "5";

        private boolean active = true;
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/FareAdminController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.FareLocalityView;
import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateFareUseCase;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/api/admin/fares")
@RequiredArgsConstructor
public class FareAdminController {
    private final GetFaresQuery query;
    private final UpdateFareUseCase updateFareUseCase;
    private final UpdateLocalityFareUseCase updateLocalityFareUseCase;
    private final CreateFareLocalityUseCase createFareLocalityUseCase;
    private final DeleteFareLocalityUseCase deleteFareLocalityUseCase;

    @GetMapping
    public List<FareLocalityView> getAll() {
        return query.getAll();
    }

    @PostMapping
    public ResponseEntity<FareLocalityView> create(@Valid @RequestBody LocalityFareRequest request) {
        FareLocalityView response = createFareLocalityUseCase.create(request.name(), request.kmsToCordoba(),
                request.minutesFromOrigin(), request.amount());
        return ResponseEntity.created(URI.create("/api/admin/fares/" + response.fareId())).body(response);
    }

    @DeleteMapping("/{fareId}")
    public ResponseEntity<Void> delete(@PathVariable UUID fareId) {
        deleteFareLocalityUseCase.delete(fareId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{fareId}")
    public FareLocalityView updateFare(@PathVariable UUID fareId, @Valid @RequestBody FareAmountRequest request) {
        return updateFareUseCase.updateFare(fareId, request.amount());
    }

    @PutMapping("/localities/{localityId}")
    public FareLocalityView updateLocalityAndFare(@PathVariable UUID localityId,
            @Valid @RequestBody LocalityFareRequest request) {
        return updateLocalityFareUseCase.updateLocalityAndFare(localityId, request.name(),
                request.kmsToCordoba(), request.minutesFromOrigin(), request.amount());
    }

    public record FareAmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }

    public record LocalityFareRequest(
            @NotBlank @Size(max = 100) String name,
            @Min(0) Integer kmsToCordoba,
            Integer minutesFromOrigin,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/FareAdminViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.CreateFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.DeleteFareLocalityUseCase;
import com.lunaris.ansenuza.domain.port.in.GetFaresQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateLocalityFareUseCase;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fares")
@RequiredArgsConstructor
@Slf4j
public class FareAdminViewController {
    private final GetFaresQuery query;
    private final CreateFareLocalityUseCase createUseCase;
    private final UpdateLocalityFareUseCase updateUseCase;
    private final DeleteFareLocalityUseCase deleteUseCase;

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("tarifas", query.getAll());
        return "admin/fares";
    }

    @PostMapping
    public String create(@RequestParam String name, @RequestParam(required = false) Integer kmsToCordoba,
            @RequestParam(required = false) Integer minutesFromOrigin, @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        try {
            createUseCase.create(name, kmsToCordoba, minutesFromOrigin, amount);
            redirectAttributes.addFlashAttribute("success", "Localidad y tarifa agregadas exitosamente.");
        } catch (DomainValidationException | IllegalArgumentException | DataIntegrityViolationException exception) {
            log.warn("No se pudo crear la localidad y su tarifa: {}", exception.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + exception.getMessage());
        } catch (Exception exception) {
            log.error("Error inesperado al crear la localidad y su tarifa.", exception);
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado al procesar la tarifa.");
        }
        return redirect();
    }

    @PostMapping("/{localityId}/editar")
    public String update(@PathVariable UUID localityId, @RequestParam String name,
            @RequestParam(required = false) Integer kmsToCordoba,
            @RequestParam(required = false) Integer minutesFromOrigin, @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes) {
        updateUseCase.updateLocalityAndFare(localityId, name, kmsToCordoba, minutesFromOrigin, amount);
        redirectAttributes.addFlashAttribute("successMessage", "Localidad y tarifa actualizadas.");
        return redirect();
    }

    @PostMapping("/{fareId}/eliminar")
    public String delete(@PathVariable UUID fareId, RedirectAttributes redirectAttributes) {
        deleteUseCase.delete(fareId);
        redirectAttributes.addFlashAttribute("successMessage", "Localidad y tarifa eliminadas.");
        return redirect();
    }

    private String redirect() {
        return "redirect:/admin/fares";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/HomeController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/InquiryController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.InquiryService;
import com.lunaris.ansenuza.domain.model.InquiryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/admin/consultas")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiries;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("inquiries", inquiries.list());
        return "inquiries";
    }

    @PostMapping("/{id}/estado")
    public String update(@PathVariable UUID id, @RequestParam InquiryStatus status) {
        inquiries.updateStatus(id, status);
        return "redirect:/admin/consultas";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/InquiryNavigationAdvice.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class InquiryNavigationAdvice {
    private final InquiryService inquiries;

    @ModelAttribute("pendingInquiryCount")
    public Long pendingCount(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_OPERADOR"))) {
            return null;
        }
        return inquiries.pendingCount();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/LocalityController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.domain.model.Locality;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocalityController {

    private final LocalityService localityService;

    @GetMapping("/localities")
    public List<Locality> findAll() {
        return localityService.findAllWithActiveFare();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/NewsBannerAdminController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.infrastructure.web.dto.NewsBannerDto;
import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.domain.port.in.ToggleSpecialTripStatusUseCase;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin/novedades")
@Slf4j
public class NewsBannerAdminController {

    private final NewsBannerService service;
    private final GetSpecialTripsQuery specialTripsQuery;
    private final CreateSpecialTripUseCase createSpecialTripUseCase;
    private final UpdateSpecialTripUseCase updateSpecialTripUseCase;
    private final ToggleSpecialTripStatusUseCase toggleSpecialTripStatusUseCase;
    private final String cloudinaryCloudName;

    @Autowired
    public NewsBannerAdminController(NewsBannerService service, GetSpecialTripsQuery specialTripsQuery,
            CreateSpecialTripUseCase createSpecialTripUseCase, UpdateSpecialTripUseCase updateSpecialTripUseCase,
            ToggleSpecialTripStatusUseCase toggleSpecialTripStatusUseCase,
            @Value("${cloudinary.cloud-name}") String cloudinaryCloudName) {
        this.service = service;
        this.specialTripsQuery = specialTripsQuery;
        this.createSpecialTripUseCase = createSpecialTripUseCase;
        this.updateSpecialTripUseCase = updateSpecialTripUseCase;
        this.toggleSpecialTripStatusUseCase = toggleSpecialTripStatusUseCase;
        this.cloudinaryCloudName = cloudinaryCloudName;
    }

    NewsBannerAdminController(NewsBannerService service) {
        this(service, null, null, null, null, "dgrwrcb5p");
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("newsBannerForm", new NewsBannerDto());
        model.addAttribute("cloudinaryCloudName", cloudinaryCloudName);
        try {
            model.addAttribute("banners", service.findAll());
            var specialTrips = specialTripsQuery == null ? null : specialTripsQuery.getAll();
            model.addAttribute("specialTrips",
                    specialTrips == null ? java.util.List.of() : specialTrips);
        } catch (RuntimeException exception) {
            log.error("Error processing news banner: ", exception);
            model.addAttribute("banners", java.util.List.of());
            model.addAttribute("specialTrips", java.util.List.of());
            model.addAttribute("errorMessage", "No se pudieron cargar las novedades.");
        }
        return "admin/novedades";
    }

    @PostMapping
    public String create(
            @ModelAttribute("newsBannerForm") NewsBannerDto form,
            RedirectAttributes redirectAttributes) {
        if (form == null) {
            form = new NewsBannerDto();
        }
        if (form.getHasWaitingList() == null) form.setHasWaitingList(false);
        if (form.getEventType() == null || form.getEventType().isBlank()) {
            form.setEventType("GENERAL");
        }
        if (form.getActive() == null) form.setActive(true);
        try {
            service.save(form.getId(), form.getTitle(), form.getDescription(), form.getEventType(),
                    form.getHasWaitingList(), form.getActive(), form.getValidUntil(),
                    form.getImageUrl(), form.getImage());
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Novedad publicada correctamente.");
        } catch (RuntimeException exception) {
            log.error("Error processing news banner: ", exception);
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "No se pudo procesar la novedad. Revisá el flyer y los datos.");
        }
        return "redirect:/admin/novedades";
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Novedad eliminada.");
        return "redirect:/admin/novedades";
    }

    @PostMapping("/viajes")
    public String createSpecialTrip(@RequestParam String title, @RequestParam(required = false) String description,
            @RequestParam(required = false) String origin, @RequestParam(required = false) String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal price, @RequestParam Integer maxPassengers,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirectAttributes) {
        createSpecialTripUseCase.create(command(title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active));
        redirectAttributes.addFlashAttribute("successMessage", "Viaje especial creado correctamente.");
        return redirect();
    }

    @PostMapping("/viajes/{id}/editar")
    public String updateSpecialTrip(@PathVariable Long id, @RequestParam String title,
            @RequestParam(required = false) String description, @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam BigDecimal price, @RequestParam Integer maxPassengers,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "false") boolean active, RedirectAttributes redirectAttributes) {
        updateSpecialTripUseCase.update(id, command(title, description, origin, destination, startDate, endDate,
                price, maxPassengers, imageUrl, active));
        redirectAttributes.addFlashAttribute("successMessage", "Viaje especial actualizado.");
        return redirect();
    }

    @PostMapping("/viajes/{id}/estado")
    public ResponseEntity<Void> toggleSpecialTrip(@PathVariable Long id, @RequestParam boolean active) {
        toggleSpecialTripStatusUseCase.setActive(id, active);
        return ResponseEntity.noContent().build();
    }

    private SpecialTripCommand command(String title, String description, String origin, String destination,
            LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
            String imageUrl, boolean active) {
        return new SpecialTripCommand(title, description, origin, destination, startDate, endDate, price,
                maxPassengers, resolveCloudinaryUrl(imageUrl), active);
    }

    private String resolveCloudinaryUrl(String image) {
        if (image == null || image.isBlank() || image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        return "https://res.cloudinary.com/" + cloudinaryCloudName + "/image/upload/" + image.trim();
    }

    private String redirect() {
        return "redirect:/admin/novedades";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/NewsBannerApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import com.lunaris.ansenuza.domain.model.NewsBanner;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news-banners")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NewsBannerApiController {

    private final NewsBannerService service;

    @GetMapping({"", "/active"})
    public List<NewsBannerResponse> findActive() {
        return service.findActive().stream().map(NewsBannerResponse::from).toList();
    }

    public record NewsBannerResponse(
            UUID id,
            String title,
            String description,
            String imageUrl,
            String eventType,
            boolean hasWaitingList) {

        private static NewsBannerResponse from(NewsBanner banner) {
            return new NewsBannerResponse(
                    banner.getId(), banner.getTitle(), banner.getDescription(),
                    banner.getImageUrl(),
                    banner.getEventType() == null || banner.getEventType().isBlank()
                            ? "GENERAL" : banner.getEventType(),
                    banner.isHasWaitingList());
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/OperatorPhoneController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.OperatorPhoneService;
import com.lunaris.ansenuza.domain.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller @RequiredArgsConstructor
@RequestMapping("/admin/operadores")
public class OperatorPhoneController {
    private final OperatorPhoneService operators;
    @GetMapping
    public String list(Model model) {
        model.addAttribute("operators", operators.list());
        return "admin/operadores";
    }
    @PostMapping
    public String add(@RequestParam String name, @RequestParam String phone) {
        operators.add(name, phone);
        return "redirect:/admin/operadores";
    }
    @PostMapping("/{id}/estado")
    public String status(@PathVariable UUID id, @RequestParam boolean active) {
        operators.setActive(id, active);
        return "redirect:/admin/operadores";
    }
    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable UUID id) {
        operators.delete(id);
        return "redirect:/admin/operadores";
    }
    @ExceptionHandler({DomainValidationException.class, DataIntegrityViolationException.class})
    public String invalid(RuntimeException exception, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", exception instanceof DomainValidationException
                ? exception.getMessage() : "El teléfono ya está registrado o no se pudo guardar.");
        return "redirect:/admin/operadores";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PassengerController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreatePassengerUseCase;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.CreatePassengerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
@RequiredArgsConstructor

public class PassengerController {

    private final PassengerRepository repository;
    private final CreatePassengerUseCase createPassengerUseCase;

    @GetMapping
    public List<Passenger> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Passenger findById(@PathVariable UUID id) {

        return repository.findById(id)
                .orElseThrow();
    }
@PostMapping
public Passenger create(
        @Valid
        @RequestBody CreatePassengerRequest request) {

    return createPassengerUseCase.execute(request);
}

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String health() {
        return "Lunaris OK";
    }
}


}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PassengerProfileController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetPassengerProfileUseCase;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerProfileController {

    private final GetPassengerProfileUseCase getPassengerProfileUseCase;

    @GetMapping({"/me", "/profile"})
    public GetPassengerProfileUseCase.PassengerProfile me(Principal principal) {
        return getPassengerProfileUseCase.execute(principal.getName());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PortalController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetPassengerProfileUseCase;
import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Autenticación del portal: verificar OTP no crea ni modifica reservas. */
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PassengerOtpService otpService;
    private final GetPassengerProfileUseCase profileUseCase;

    @PostMapping("/verify-otp")
    public PortalLoginResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        PassengerOtpService.TokenResult token = otpService.verifyOtp(request.phone(), request.code());
        return new PortalLoginResponse(token.accessToken(), "Bearer", token.expiresAt());
    }

    public record VerifyOtpRequest(
            @NotBlank String phone,
            @NotBlank @Pattern(regexp = "[0-9]{4}") String code) {
    }

    public record PortalLoginResponse(String accessToken, String tokenType, Instant expiresAt) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.application.usecase.SubmitDriverApplicationUseCase;
import com.lunaris.ansenuza.application.usecase.GetPublicReservationStatusUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.infrastructure.web.dto.DriverApplicationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicApiController {

    private final CreateReservationUseCase createReservationUseCase;
    private final SubmitDriverApplicationUseCase submitDriverApplicationUseCase;
    private final GetPublicReservationStatusUseCase getPublicReservationStatusUseCase;
    @PostMapping({"/reservations", "/public/reservations", "/v1/reservations"})
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody CreateReservationRequest request) {
        Reservation reservation = createReservationUseCase.executePublic(
                request.withSource(ReservationSource.WEB));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateReservationResponse.from(reservation));
    }

    @GetMapping("/v1/reservations/{code}")
    public GetPublicReservationStatusUseCase.PublicReservationStatus status(
            @PathVariable String code) {
        return getPublicReservationStatusUseCase.execute(code);
    }

    @PostMapping(value = "/drivers/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> apply(
            @Valid @RequestBody DriverApplicationRequest request) {
        var application = submitDriverApplicationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", application.getId(),
                "status", application.getStatus().name()));
    }

}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicCatalogApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.LocalityService;
import com.lunaris.ansenuza.domain.repository.FareRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicCatalogApiController {

    private static final List<String> SCHEDULES = List.of("03:00", "08:00");

    private final LocalityService localityService;
    private final FareRepository fareRepository;

    @GetMapping("/api/public/schedules")
    public ResponseEntity<SchedulesResponse> schedules() {
        return ResponseEntity.ok(new SchedulesResponse(SCHEDULES));
    }

    @GetMapping({"/api/public/localities", "/api/v1/localities", "/api/v1/fares/localities"})
    public ResponseEntity<List<LocalityResponse>> localities() {
        List<LocalityResponse> response = localityService.findAllWithActiveFare().stream()
                .map(locality -> new LocalityResponse(
                        locality.getId(),
                        locality.getName(),
                        locality.getKmsToCordoba(),
                        locality.getMinutesFromOrigin(),
                        fareRepository.findFirstByLocalityNameIgnoreCase(locality.getName())
                                .map(fare -> fare.getAmount())
                                .orElse(null)))
                .toList();
        return ResponseEntity.ok(response);
    }

    public record SchedulesResponse(List<String> schedules) {
    }

    public record LocalityResponse(
            UUID id,
            String name,
            Integer kmsToCordoba,
            Integer minutesFromOrigin,
            BigDecimal amount) {
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/PublicInvoiceController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.IssueInvoiceUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público y directo para que Meta descargue documentos PDF sin redirecciones. */
@RestController
@RequiredArgsConstructor
public class PublicInvoiceController {

    private final IssueInvoiceUseCase issueInvoiceUseCase;

    @GetMapping(value = "/public/invoices/{invoiceId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable UUID invoiceId) {
        IssueInvoiceUseCase.InvoiceDocument document = issueInvoiceUseCase.download(invoiceId);
        String fileName = document.invoiceNumber().replaceAll("[^A-Za-z0-9_-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"factura-" + fileName + ".pdf\"")
                .body(document.content());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ReservationApiController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.port.ReceiptStoragePort;
import com.lunaris.ansenuza.application.usecase.CreateReservationUseCase;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.application.usecase.PersistPaymentReceiptUseCase;
import java.util.Map;
import java.security.Principal;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ReservationApiController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReceiptStoragePort receiptStoragePort;
    private final PersistPaymentReceiptUseCase persistPaymentReceiptUseCase;

    @PostMapping(value = {"/api/reservations", "/api/public/reservations"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestPart("reservation") CreateReservationRequest request,
            @RequestPart(value = "paymentReceipt", required = false) MultipartFile receipt) {
        String receiptUrl = receipt != null && !receipt.isEmpty()
                ? receiptStoragePort.uploadFile(receipt) : null;
        Reservation reservation = createReservationUseCase.executePublic(
                request.withSource(ReservationSource.WEB), receiptUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateReservationResponse.from(reservation));
    }

    @PostMapping(value = "/api/v1/reservations/{reservationCode}/receipt",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadReceipt(
            @org.springframework.web.bind.annotation.PathVariable String reservationCode,
            @RequestPart("file") MultipartFile file,
            Principal principal) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false,
                    "message", "El comprobante es obligatorio."));
        }
        String url = receiptStoragePort.uploadFile(file);
        persistPaymentReceiptUseCase.executeByReservationCodeOwnedBy(
                reservationCode, url, "PASSENGER_WEB", principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "reservationCode", reservationCode,
                "paymentReceiptUrl", url));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/ReservationViewController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.lunaris.ansenuza.application.usecase.WaitingListConversionService;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;
import com.lunaris.ansenuza.application.usecase.NewsBannerService;
import org.springframework.web.server.ResponseStatusException;
import com.lunaris.ansenuza.domain.model.Passenger;
import com.lunaris.ansenuza.domain.model.Driver;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.exception.ReservationAlreadyCompletedException;
import com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService;
import com.lunaris.ansenuza.domain.model.service.ReservationService;
import com.lunaris.ansenuza.domain.repository.LocalityRepository;
import com.lunaris.ansenuza.domain.repository.DriverRepository;
import com.lunaris.ansenuza.domain.repository.PassengerRepository;
import com.lunaris.ansenuza.domain.repository.ReservationRepository;
import com.lunaris.ansenuza.infrastructure.web.dto.reservation.CreateReservationForm;
import com.lunaris.ansenuza.infrastructure.whatsapp.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Slf4j    
public class ReservationViewController {

    private final PassengerRepository passengerRepository;
    private final LocalityRepository localityRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final PricingAndScheduleService pricingAndScheduleService;
    private final DriverRepository driverRepository;
    private final WhatsAppService whatsAppService;
    private final WaitingListService waitingListService;
    private final WaitingListConversionService waitingListConversionService;
    private final WaitingListReengagementService waitingListReengagementService;
    private final NewsBannerService newsBannerService;

    @GetMapping("/new")
    public String newReservation(Model model) {
        model.addAttribute("reservation", new CreateReservationForm());
        
        // 🎯 Usamos el método filtrado para traer solo los pueblos con tarifas comerciales activas
        var localidadesConTarifa = localityRepository.findAllWithActiveFare();
        
        model.addAttribute("origenes", localidadesConTarifa);
        model.addAttribute("destinos", localidadesConTarifa);
        
        return "reservation-form";
    }

   /* @PostMapping("/new")
    public String createReservation(
            @Valid @ModelAttribute("reservation") CreateReservationForm form,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            var localidadesConTarifa = localityRepository.findAllWithActiveFare();
            model.addAttribute("origenes", localidadesConTarifa);
            model.addAttribute("destinos", localidadesConTarifa);
            return "reservation-form";
        }

        Passenger passenger = Passenger.builder()
                .firstName(form.getFirstName())
                .lastName(form.getLastName())
                .phone(form.getPhone())
                .cuil(form.getCuil())
                .build();

        passenger = passengerRepository.save(passenger);

        var computedAmount = pricingAndScheduleService.calculateReservationAmount(
                form.getPickupLocality(),
                form.getDestination(),
                form.getRoundTrip(),
                form.getPassengerCount() != null ? form.getPassengerCount() : 1);

        String schedule = (form.getDepartureSchedule() != null && !form.getDepartureSchedule().isBlank())
                ? form.getDepartureSchedule().trim()
                : "03:00 AM";
        if (Boolean.TRUE.equals(form.getRoundTrip()) && form.getReturnDate() == null) {
            schedule += " (Abierta)";
        }
        String notes = schedule;
        if (form.getNotes() != null && !form.getNotes().isBlank()) {
            notes += " | " + form.getNotes().trim();
        }

        Reservation reservation = Reservation.builder()
                .passenger(passenger)
                .travelDate(form.getTravelDate())
                .pickupLocality(form.getPickupLocality())
                .pickupAddress(form.getPickupAddress())
                .destination(form.getDestination())
                .roundTrip(Boolean.TRUE.equals(form.getRoundTrip()))
                .returnDate(form.getReturnDate())
                .paymentVerified(Boolean.TRUE.equals(form.getPaymentVerified()))
                .amount(computedAmount)
                .notes(notes)
                .passengerCount(form.getPassengerCount() != null ? form.getPassengerCount() : 1)
                .companionNames(form.getCompanionNames())
                .build();

        reservationService.saveReservationFlow(reservation);

        return "redirect:/agenda";
    }
 */
    // 👥 VISTA WEB: Renderiza el panel HTML de pasajeros con link directo a WhatsApp
    @GetMapping("/passengers-panel")
    public String listPassengersPanel(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(defaultValue = "all") String waitingListMode,
            Model model) {
        List<Passenger> todosLosPasajeros = passengerRepository.findAll();
        List<com.lunaris.ansenuza.domain.model.WaitingListEntry> waitingEntries = travelDate == null
                ? waitingListService.findAllActiveWaiting()
                : waitingListService.findWaiting(travelDate);
        model.addAttribute("pasajeros", todosLosPasajeros);
        model.addAttribute("waitingListEntries", waitingEntries);
        long waitingListCount = waitingListService.countAllActiveWaiting();
        model.addAttribute("waitingListCount", waitingListCount);
        model.addAttribute("waitingListTotal", waitingListCount);
        model.addAttribute("waitingListMode", waitingListMode);
        model.addAttribute("selectedTravelDate", travelDate);
        java.util.Map<String, String> eventLabels = new java.util.TreeMap<>();
        newsBannerService.findEventLabels().forEach((type, title) -> {
            if (type != null && !type.isBlank()) eventLabels.put(type, title);
        });
        waitingListService.findDistinctEventTypes().stream()
                .filter(type -> type != null && !type.isBlank() && !"GENERAL".equals(type))
                .forEach(type -> eventLabels.putIfAbsent(
                        type, type.replace('_', ' ')));
        model.addAttribute("eventLabels", eventLabels);
        return "passengers";
    }

    @PostMapping("/waiting-list/{id}/convert")
    public String convertWaitingListEntry(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListReengagementService.promote(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Se notificó al pasajero para confirmar y pagar por WhatsApp.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    @PostMapping("/waiting-list/{id}/promote")
    public String promoteWaitingListEntryToBot(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListReengagementService.promote(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Se notificó al pasajero por WhatsApp.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    @PostMapping("/waiting-list/{id}/cancel")
    public String cancelWaitingListEntry(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            RedirectAttributes redirectAttributes) {
        try {
            waitingListConversionService.cancel(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage", "La entrada fue cancelada.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return passengersPanelRedirect(travelDate);
    }

    private String passengersPanelRedirect(LocalDate travelDate) {
        return travelDate == null
                ? "redirect:/reservations/passengers-panel"
                : "redirect:/reservations/passengers-panel?travelDate=" + travelDate;
    }

    // 🗑️ BAJA DESDE EL PANEL DE ADMINISTRACIÓN (Maneja redirección dinámica por origen)
    // 🗑️ BAJA CONTROLADA PROPORCIONAL (Manejo Seguro de BigDecimal)
    @PostMapping("/delete/{id}")
    public String deleteFromPanel(
            @PathVariable(value = "id") UUID id, 
            @RequestParam(value = "source", defaultValue = "agenda") String source) {
        
        Reservation original = reservationRepository.findById(id).orElse(null);
        
        if (original != null) {
            LocalDate sentinelDate = LocalDate.of(2099, 12, 31);
            boolean isOpenReturn = original.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                    || original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);

            // Caso Crítico: Cancelación parcial de una butaca dentro de un grupo en Vueltas Abiertas
            if (isOpenReturn && original.getPassengerCount() != null && original.getPassengerCount() > 1 && "vueltas".equals(source)) {
                reservationService.cancelOneUnusedReturnSeat(id, "ADMIN_PANEL");
            } else {
                // Caso Ordinario: Si le queda un solo asiento o viene de la agenda general, se da de baja completa
                reservationService.cancelReservation(id, "ADMIN_PANEL");
            }
        }
        
        if ("vueltas".equals(source)) {
            return "redirect:/reservations/vueltas-abiertas";
        }
        return "redirect:/agenda";
    }

@Transactional
@PostMapping("/update/{id}")
public String updateFromPanel(
        @PathVariable(value = "id") UUID id,
        @RequestParam(value = "travelDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
        @RequestParam(value = "departureSchedule", required = false) String departureSchedule,
        @RequestParam(value = "pickupAddress", required = false) String pickupAddress,
        @RequestParam(value = "driverId", required = false) UUID driverId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "travelStatus", required = false) String rawTravelStatus,
        @RequestParam(value = "cantidadVuelven", defaultValue = "1") int cantidadVuelven,
        @RequestParam(value = "source", defaultValue = "agenda") String source) {
        
    log.info(
            "[Reservation Update] Incoming travelStatus={} for reservationId={}",
            rawTravelStatus, id);
    Reservation.TravelStatus travelStatus = parseTravelStatus(id, rawTravelStatus);
    Reservation original = reservationRepository.findById(id).orElse(null);
    
    if (original != null) {
        if ("COMPLETED".equalsIgnoreCase(original.getStatus())
                || original.getTravelStatus() == Reservation.TravelStatus.COMPLETED) {
            throw new ReservationAlreadyCompletedException();
        }
        Driver assignedDriver = driverId == null ? null : driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado: " + driverId));
        LocalDate sentinelDate = LocalDate.of(2099, 12, 31);
        boolean isOpenReturn = original.getTravelStatus() == Reservation.TravelStatus.OPEN_RETURN
                || original.getTravelDate() != null && original.getTravelDate().equals(sentinelDate);
        
        if (isOpenReturn) {
            Reservation scheduledReturn;
            if (travelDate == null || departureSchedule == null || departureSchedule.isBlank()) {
                throw new IllegalArgumentException(
                        "Para programar una vuelta abierta se requieren fecha y horario.");
            }
            int asientosOriginales = original.getPassengerCount() != null ? original.getPassengerCount() : 1;
            String normalizedSchedule = normalizeDepartureSchedule(departureSchedule);
            
            // A. Si eligen volver MENOS pasajeros de los que tiene el grupo actualmente (Split por Bloque)
            if (cantidadVuelven < asientosOriginales) {
                java.math.BigDecimal montoTotal = original.getAmount() != null ? original.getAmount() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal valorButacaIndividual = montoTotal.divide(java.math.BigDecimal.valueOf(asientosOriginales), 2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal montoBloqueDesglosado = valorButacaIndividual.multiply(java.math.BigDecimal.valueOf(cantidadVuelven));

                // Restamos el bloque que se va del registro base que se queda en "Vueltas Abiertas"
                original.setPassengerCount(asientosOriginales - cantidadVuelven);
                original.setAmount(montoTotal.subtract(montoBloqueDesglosado));
                reservationRepository.saveAndFlush(original);

                // Generamos la nueva fila física en la base de datos para los pasajeros que sí viajan
                Reservation tramoIndependiente = new Reservation();
                tramoIndependiente.setPassenger(original.getPassenger());
                tramoIndependiente.setTravelDate(travelDate);
                tramoIndependiente.setPickupLocality(original.getPickupLocality());
                tramoIndependiente.setPickupAddress(pickupAddress != null && !pickupAddress.isBlank() ? pickupAddress : original.getPickupAddress());
                tramoIndependiente.setDestination(original.getDestination());
                tramoIndependiente.setAmount(montoBloqueDesglosado);
                tramoIndependiente.setPassengerCount(cantidadVuelven); // Cantidad exacta elegida por Martín
                tramoIndependiente.setStatus("CONFIRMED");
                tramoIndependiente.setRoundTrip(false); // Desactivado para evitar bucles de combo
                tramoIndependiente.setPaymentVerified(true);
                tramoIndependiente.setDriver(assignedDriver);
                tramoIndependiente.setReturnDate(travelDate);
                tramoIndependiente.setDepartureSchedule(normalizedSchedule);
                tramoIndependiente.setTravelStatus(Reservation.TravelStatus.PENDING);
                tramoIndependiente.setNotes(original.getNotes() != null ? original.getNotes() + " | Split Bloque" : "Split Bloque");
                
                String shortTimestamp = String.valueOf(System.currentTimeMillis()).substring(10);
                tramoIndependiente.setReservationCode("VTA-BLK-" + original.getId().toString().substring(0, 4) + "-" + shortTimestamp);
                tramoIndependiente.setBookingGroupCode(resolveBookingGroupCode(original));
                
                scheduledReturn = reservationRepository.saveAndFlush(tramoIndependiente);
                sendOpenReturnConfirmation(tramoIndependiente);
                log.info("[Split Bloque] Se procesó el regreso de {} pasajeros. Quedan {} en espera.", cantidadVuelven, original.getPassengerCount());
            } 
            // B. Si vuelven TODOS los pasajeros que quedaban en el grupo (Cierre definitivo)
            else {
                original.setTravelDate(travelDate);
                if (pickupAddress != null && !pickupAddress.isBlank()) original.setPickupAddress(pickupAddress);
                original.setRoundTrip(false); // Apagamos el flag de combo de raíz
                original.setReturnDate(travelDate);
                original.setDepartureSchedule(normalizedSchedule);
                original.setTravelStatus(Reservation.TravelStatus.PENDING);
                original.setStatus("CONFIRMED");
                original.setPaymentVerified(true);
                original.setDriver(assignedDriver);
                if (status != null && "CONFIRMED".equals(status)) original.setStatus("CONFIRMED");
                
                scheduledReturn = reservationRepository.saveAndFlush(original);
                sendOpenReturnConfirmation(original);
                log.info("[Cierre Grupo] Volvieron los últimos {} pasajeros del grupo.", cantidadVuelven);
            }
            if (travelStatus != null) {
                Reservation travelStatusUpdate = new Reservation();
                travelStatusUpdate.setTravelStatus(travelStatus);
                reservationService.updateReservation(
                        scheduledReturn.getId(), travelStatusUpdate, "ADMIN_PANEL");
            }
        } else {
            // Caso Ordinario para reservas normales de la agenda
            Reservation updateData = new Reservation();
            updateData.setTravelStatus(null);
            if (travelDate != null) updateData.setTravelDate(travelDate);
            if (pickupAddress != null) updateData.setPickupAddress(pickupAddress);
            if (status != null) {
                updateData.setStatus(status);
                if ("CONFIRMED".equals(status)) updateData.setPaymentVerified(true);
            }
            if (travelStatus != null) updateData.setTravelStatus(travelStatus);
            reservationService.updateReservation(id, updateData, "ADMIN_PANEL");
        }
    }
    
    if ("vueltas".equals(source)) {
        return "redirect:/reservations/vueltas-abiertas";
    }
    return "redirect:/agenda";
    }

    static String normalizeDepartureSchedule(String rawSchedule) {
        String schedule = rawSchedule.trim().toUpperCase(java.util.Locale.ROOT);
        if (schedule.endsWith(" AM") || schedule.endsWith(" PM")) {
            return schedule;
        }
        if (schedule.matches("\\d{2}:\\d{2}")) {
            int hour = Integer.parseInt(schedule.substring(0, 2));
            return schedule + (hour < 12 ? " AM" : " PM");
        }
        throw new IllegalArgumentException("Formato de horario inválido: " + rawSchedule);
    }

    private static String resolveBookingGroupCode(Reservation reservation) {
        if (reservation.getBookingGroupCode() != null
                && !reservation.getBookingGroupCode().isBlank()) {
            return reservation.getBookingGroupCode();
        }
        String code = reservation.getReservationCode();
        return code == null ? null : code.replaceFirst("-(IDA|VUELTA)$", "");
    }

    private Reservation.TravelStatus parseTravelStatus(UUID reservationId, String rawTravelStatus) {
        if (rawTravelStatus == null || rawTravelStatus.isBlank()) {
            log.warn(
                    "[Reservation Update] Null or blank travelStatus received. "
                            + "reservationId={}, payload={}",
                    reservationId, rawTravelStatus);
            return null;
        }
        try {
            return Reservation.TravelStatus.valueOf(rawTravelStatus);
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "[Reservation Update] Invalid travelStatus received. "
                            + "reservationId={}, payload={}",
                    reservationId, rawTravelStatus);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado de viaje inválido: " + rawTravelStatus,
                    exception);
        }
    }

    private void sendOpenReturnConfirmation(Reservation reservation) {
        Passenger passenger = reservation.getPassenger();
        if (passenger == null || passenger.getPhone() == null || passenger.getPhone().isBlank()) {
            return;
        }
        String driverName = reservation.getDriver() != null
                ? reservation.getDriver().getFullName()
                : "a confirmar";
        whatsAppService.sendMessage(
                passenger.getPhone(),
                "✅ Tu vuelta quedó confirmada para el " + reservation.getTravelDate()
                        + ". Chofer: " + driverName + "."
                        + " Código de reserva: " + reservation.getReservationCode() + ".");
    }

    // 🛑 VISTA WEB: Muestra la pantalla de pasajes con Vuelta Abierta bajo /reservations/vueltas-abiertas
    @GetMapping("/vueltas-abiertas")
    public String listOpenReturns(Model model) {
        java.time.LocalDate fechaCentinela = java.time.LocalDate.of(2099, 12, 31);
        List<Reservation> abiertas = reservationRepository.findVueltasAbiertasActive(fechaCentinela);
        abiertas = abiertas.stream()
                .sorted(AgendaViewController.dispatchedLastComparator())
                .toList();
        model.addAttribute("vueltasAbiertas", abiertas);
        model.addAttribute("choferes", driverRepository.findByActiveTrue());
        return "vueltas-abiertas";
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SchedulesController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.dto.ScheduleDto;
import com.lunaris.ansenuza.application.usecase.ScheduleService;
import com.lunaris.ansenuza.shared.ArgentinaTime;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleService scheduleService;

    @GetMapping({"/schedules", "/v1/schedules"})
    public List<ScheduleDto> schedules(
            @RequestParam(required = false) String pickupLocality,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = "dd/MM/yyyy")
            LocalDate travelDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = "dd/MM/yyyy")
            LocalDate date,
            @RequestParam(defaultValue = "false") boolean roundTrip,
            @RequestParam(defaultValue = "false") boolean openReturn,
            @RequestParam(defaultValue = "false") boolean returnTrip) {
        LocalDate requestedDate = travelDate != null ? travelDate : date;
        LocalDate effectiveDate = requestedDate != null ? requestedDate : ArgentinaTime.today();

        return returnTrip
                ? scheduleService.getReturnSchedulesForWeb(effectiveDate)
                : scheduleService.getSchedulesForWeb(pickupLocality, effectiveDate);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SpecialTripAdminController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.CreateSpecialTripUseCase;
import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.domain.port.in.UpdateSpecialTripUseCase;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import com.lunaris.ansenuza.infrastructure.web.mapper.SpecialTripWebMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/special-trips")
@RequiredArgsConstructor
public class SpecialTripAdminController {
    private final CreateSpecialTripUseCase createUseCase;
    private final UpdateSpecialTripUseCase updateUseCase;
    private final GetSpecialTripsQuery query;
    private final SpecialTripWebMapper mapper;

    @GetMapping
    public List<SpecialTripResponse> getAll() {
        return query.getAll().stream().map(mapper::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<SpecialTripResponse> create(@Valid @RequestBody SpecialTripRequest request) {
        SpecialTripResponse response = mapper.toResponse(createUseCase.create(mapper.toCommand(request)));
        return ResponseEntity.created(URI.create("/api/admin/special-trips/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public SpecialTripResponse update(@PathVariable Long id, @Valid @RequestBody SpecialTripRequest request) {
        return mapper.toResponse(updateUseCase.update(id, mapper.toCommand(request)));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/SpecialTripPublicController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.port.in.GetSpecialTripsQuery;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import com.lunaris.ansenuza.infrastructure.web.mapper.SpecialTripWebMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/special-trips")
@RequiredArgsConstructor
public class SpecialTripPublicController {
    private final GetSpecialTripsQuery query;
    private final SpecialTripWebMapper mapper;

    @GetMapping
    public List<SpecialTripResponse> getActive() {
        return query.getActive().stream().map(mapper::toResponse).toList();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/VehicleController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.domain.model.Vehicle;
import com.lunaris.ansenuza.domain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository repository;

    @GetMapping
    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    @PostMapping
    public Vehicle create(@RequestBody Vehicle vehicle) {
        return repository.save(vehicle);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/controller/WaitingListController.java`

```java
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.lunaris.ansenuza.application.usecase.WaitingListService;
import com.lunaris.ansenuza.application.usecase.WaitingListReengagementService;
import com.lunaris.ansenuza.application.usecase.WaitingListOtpService;
import com.lunaris.ansenuza.domain.model.WaitingListEntry;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/waiting-list")
@RequiredArgsConstructor
public class WaitingListController {

    private final WaitingListService service;
    private final WaitingListReengagementService reengagementService;
    private final WaitingListOtpService otpService;

    @GetMapping
    public List<WaitingListResponse> find(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam(required = false) String status) {
        List<WaitingListEntry> entries = travelDate == null
                && (status == null || status.isBlank())
                        ? service.findAllActiveWaiting()
                        : service.find(travelDate, status);
        return entries.stream().map(WaitingListResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingListResponse create(@RequestBody CreateWaitingListRequest request) {
        return WaitingListResponse.from(service.create(
                request.phoneNumber(), request.passengerName(), request.travelDate(),
                request.pickupLocality(), request.destination(), request.passengerCount(),
                request.notes(), request.eventType()));
    }

    @PostMapping("/request-otp")
    public OtpResponse requestOtp(@RequestBody OtpRequest request) {
        otpService.request(request.phone(), request.fullName());
        return new OtpResponse("El código fue enviado por WhatsApp.");
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingListResponse confirm(@RequestBody ConfirmWaitingListRequest request) {
        otpService.verify(request.phoneNumber(), request.otpCode());
        return WaitingListResponse.from(service.create(
                request.phoneNumber(), request.passengerName(), request.travelDate(),
                request.pickupLocality(), request.destination(), request.passengerCount(),
                request.notes(), request.eventType()));
    }

    @PatchMapping("/{id}/status")
    public WaitingListResponse updateStatus(
            @PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        return WaitingListResponse.from(service.updateStatus(id, request.status()));
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/promote")
    public WaitingListResponse promote(@PathVariable Long id) {
        return WaitingListResponse.from(reengagementService.promote(id));
    }

    public record UpdateStatusRequest(String status) {
    }

    public record CreateWaitingListRequest(
            String phoneNumber, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String notes, String eventType) {
    }

    public record OtpRequest(
            String fullName,
            @JsonAlias("phoneNumber") String phone,
            String eventType,
            String notes) {
    }

    public record OtpResponse(String message) {
    }

    public record ConfirmWaitingListRequest(
            String phoneNumber, String otpCode, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String notes, String eventType) {
    }

    public record WaitingListResponse(
            Long id, String phoneNumber, String passengerName, LocalDate travelDate,
            String pickupLocality, String destination, Integer passengerCount,
            String status, OffsetDateTime createdAt, String notes, String eventType) {

        private static WaitingListResponse from(WaitingListEntry entry) {
            return new WaitingListResponse(
                    entry.getId(), entry.getPhoneNumber(), entry.getPassengerName(),
                    entry.getTravelDate(), entry.getPickupLocality(), entry.getDestination(),
                    entry.getPassengerCount(), entry.getStatus(), entry.getCreatedAt(),
                    entry.getNotes(), entry.getEventType());
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/CreatePassengerRequest.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto;
import jakarta.validation.constraints.NotBlank;

public record CreatePassengerRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String cuil,

        String phone,

        String address,

        String locality
) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/DriverApplicationRequest.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DriverApplicationRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Size(max = 30) String phone,
        @Size(max = 120) String locality,
        @Size(max = 120) String vehicleModel,
        @Positive Integer vehicleYear,
        @Size(max = 20) String licensePlate,
        @Size(max = 500) String greenCardFileUrl,
        @Size(max = 500) String insuranceFileUrl) {

    public DriverApplicationRequest(
            String fullName,
            String phone,
            String locality,
            String vehicleModel,
            Integer vehicleYear,
            String licensePlate) {
        this(fullName, phone, locality, vehicleModel, vehicleYear, licensePlate, null, null);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/NewsBannerDto.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class NewsBannerDto {

    private UUID id;
    private String title;
    private String description;
    private String eventType;
    private Boolean hasWaitingList = false;
    private Boolean active = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validUntil;

    private String imageUrl;
    private MultipartFile image;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/ReservationCreateDTO.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto;

import java.time.LocalDate;
import java.util.List;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationCreateDTO {
    // Datos del Pasajero
    private String firstName;
    private String lastName;
    private String phone;
    private String cuil; // DNI / CUIT de facturación

    // Datos del Viaje
    private LocalDate travelDate;
    private Boolean roundTrip;
    private LocalDate returnDate;
    private String departureSchedule; // Bloque horario

    // Trayectos
    private String pickupLocality;
    private String pickupAddress;
    private String destination;

    // Gestión de Asientos
    private Integer passengerCount;
    private List<String> companionNames;

    // Extras
    private Boolean paymentVerified;
    private String notes;
    private ReservationSource source;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/agenda/AgendaDayView.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.agenda;



import java.time.LocalDate;
import java.util.UUID;
import java.math.BigDecimal;

public record AgendaDayView(

        LocalDate date,

        int totalPassengers,

        int confirmedPassengers,

        int waitingListPassengers,

        boolean capacityExceeded,

        int pendingPayments,

        int estimatedVehicles,

        int vehicleCapacity,

        int plannedCapacity,

        BigDecimal totalCollected,

        int paidReservations,

        UUID driverId
) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/agenda/EnviarHojaRutaRequest.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.agenda;

import java.util.List;
import java.util.UUID;

public record EnviarHojaRutaRequest(
        UUID driverId,
        String phone,
        List<UUID> reservationIds
) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/BillingPanelView.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.util.List;

/** Vista completa del panel de Facturación: ingresos + pendientes + emitidas. */
public record BillingPanelView(
        BigDecimal ingresoHoy,
        long countHoy,
        BigDecimal ingresoMes,
        long countMes,
        List<PendingInvoiceRow> pendientes,
        List<IssuedInvoiceRow> emitidas
) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/IssuedInvoiceRow.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IssuedInvoiceRow(
        UUID id,
        String invoiceNumber,
        String passengerName,
        String passengerCuil,
        BigDecimal amount,
        String pdfUrl,
        Boolean sentViaWhatsapp,
        LocalDateTime createdAt,
        String reservationStatus,
        boolean refundedToWallet
) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/billing/PendingInvoiceRow.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Una reserva con pago confirmado que todavía no tiene factura emitida. */
public record PendingInvoiceRow(
        UUID reservationId,
        String reservationCode,
        String passengerName,
        String phone,
        String rawDocument,
        String suggestedCuil,
        BigDecimal amount,
        LocalDate travelDate,
        String route
) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/dashboard/DailyOperationSummaryResponse.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.dashboard;

import java.time.LocalDate;

public record DailyOperationSummaryResponse(
        LocalDate travelDate,
        long totalReservations,
        long totalPassengers,  // 👥 Agregamos este para los asientos totales
        long paidReservations,
        long pendingPayments,
        long estimatedVehicles
) {}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/hojaruta/HojaRutaViewModel.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.hojaruta;

import java.time.LocalDate;
import java.util.List;
import com.lunaris.ansenuza.domain.model.Reservation;
import com.lunaris.ansenuza.domain.model.ConversationSession;
import lombok.Value;

@Value
public class HojaRutaViewModel {
    LocalDate fechaSeleccionada;
    long totalYendo;
    long totalVolviendo;
    boolean hubActivado;
    long pasajeros0800Count;
    List<Reservation> reservas;
    List<ConversationSession> sesionesChat;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationForm.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReservationForm {

    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    private String cuil;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^[0-9]{10,15}$",
        message = "Ingrese un teléfono válido"
    )
    private String phone;

    @NotNull(message = "La fecha del viaje es obligatoria")
    private LocalDate travelDate;

    @NotBlank(message = "La localidad de retiro es obligatoria")
    private String pickupLocality;

    @NotBlank(message = "La dirección de retiro es obligatoria")
    private String pickupAddress;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    private Boolean roundTrip = false;

    private LocalDate returnDate;

    // 🕒 Horario de salida (igual que el bot): "03:00 AM" (primer turno) u "08:00 AM" (segundo turno)
    private String departureSchedule;

    private Boolean paymentVerified;

    private String notes;

    // 🌟 NUEVOS CAMPOS AGREGADOS PARA LA GESTIÓN DE ASIENTOS Y ACOMPAÑANTES
    private Integer passengerCount;

    private String companionNames;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationRequest.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lunaris.ansenuza.infrastructure.web.json.StringOrStringListDeserializer;
import java.time.LocalDate;
import java.util.UUID;
import com.lunaris.ansenuza.domain.model.ReservationSource;
import com.lunaris.ansenuza.domain.model.TripType;

public record CreateReservationRequest(
    UUID passengerId,
    @JsonAlias({"passengerName"}) String fullName,
    @JsonAlias({"passengerPhone"}) String phone,
    @JsonAlias({"documentId", "cuil"}) String cuilDni,
    @JsonAlias({"date"})
    LocalDate travelDate,
    @JsonAlias({"origin", "locality", "originLocality"}) String pickupLocality,
    String pickupAddress,
    String destination,
    @JsonAlias({"schedule", "scheduleBlock"}) String departureSchedule,
    Boolean roundTrip,
    LocalDate returnDate,
    Boolean paymentVerified,
    @JsonDeserialize(using = StringOrStringListDeserializer.class) String notes,
    @JsonAlias({"seatCount", "seats"}) Integer passengerCount,
    @JsonDeserialize(using = StringOrStringListDeserializer.class) String companionNames,
    ReservationSource source,
    TripType tripType,
    String promotionCode
) {
    public CreateReservationRequest(
            UUID passengerId,
            LocalDate travelDate,
            String pickupLocality,
            String pickupAddress,
            String destination,
            Boolean roundTrip,
            LocalDate returnDate,
            Boolean paymentVerified,
            String notes,
            Integer passengerCount,
            String companionNames,
            ReservationSource source) {
        this(passengerId, null, null, null, travelDate, pickupLocality, pickupAddress, destination,
                notes != null && notes.contains("08:00") ? "08:00 AM" : "03:00 AM",
                roundTrip, returnDate, paymentVerified, notes, passengerCount, companionNames, source,
                null, null);
    }

    public CreateReservationRequest withSource(ReservationSource source) {
        return new CreateReservationRequest(
                passengerId,
                fullName,
                phone,
                cuilDni,
                travelDate,
                pickupLocality,
                pickupAddress,
                destination,
                departureSchedule,
                roundTrip,
                returnDate,
                paymentVerified,
                notes,
                passengerCount,
                companionNames,
                source,
                tripType,
                promotionCode);
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/CreateReservationResponse.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import com.lunaris.ansenuza.domain.model.Reservation;
import java.util.UUID;

public record CreateReservationResponse(
        boolean success,
        String reservationCode,
        String bookingGroupCode,
        UUID id) {

    public static CreateReservationResponse from(Reservation reservation) {
        return new CreateReservationResponse(
                true,
                reservation.getReservationCode(),
                reservation.getBookingGroupCode(),
                reservation.getId());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/ManualReservationOptions.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Campos opcionales del operador; null en amount solicita la tarifa vigente. */
@Getter
@Setter
public class ManualReservationOptions {
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal extraAmount;
    private String companionNames;
    private String routeDirection;
    private String notes;
    private String returnDepartureSchedule;
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/reservation/PassengerOption.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.reservation;

import java.util.UUID;

public record PassengerOption(

        UUID id,

        String fullName
) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/specialtrip/SpecialTripRequest.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.specialtrip;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SpecialTripRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @Size(max = 100) String origin,
        @Size(max = 100) String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotNull @Min(1) Integer maxPassengers,
        @Size(max = 500) String imageUrl,
        boolean active) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/dto/specialtrip/SpecialTripResponse.java`

```java
package com.lunaris.ansenuza.infrastructure.web.dto.specialtrip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SpecialTripResponse(
        Long id, String title, String description, String origin, String destination,
        LocalDate startDate, LocalDate endDate, BigDecimal price, Integer maxPassengers,
        String imageUrl, boolean active, LocalDateTime createdAt) {
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/json/StringOrStringListDeserializer.java`

```java
package com.lunaris.ansenuza.infrastructure.web.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** Acepta tanto un texto como un arreglo de textos en payloads públicos. */
public class StringOrStringListDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false)
                    .filter(JsonNode::isValueNode)
                    .map(JsonNode::asText)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.joining(", "));
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        return context.handleUnexpectedToken(String.class, parser).toString();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/infrastructure/web/mapper/SpecialTripWebMapper.java`

```java
package com.lunaris.ansenuza.infrastructure.web.mapper;

import com.lunaris.ansenuza.domain.model.SpecialTrip;
import com.lunaris.ansenuza.domain.port.in.SpecialTripCommand;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripRequest;
import com.lunaris.ansenuza.infrastructure.web.dto.specialtrip.SpecialTripResponse;
import org.springframework.stereotype.Component;

@Component
public class SpecialTripWebMapper {
    public SpecialTripCommand toCommand(SpecialTripRequest request) {
        return new SpecialTripCommand(request.title(), request.description(), request.origin(),
                request.destination(), request.startDate(), request.endDate(), request.price(),
                request.maxPassengers(), request.imageUrl(), request.active());
    }

    public SpecialTripResponse toResponse(SpecialTrip trip) {
        return new SpecialTripResponse(trip.id(), trip.title(), trip.description(), trip.origin(),
                trip.destination(), trip.startDate(), trip.endDate(), trip.price(), trip.maxPassengers(),
                trip.imageUrl(), trip.active(), trip.createdAt());
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/reservation/infrastructure/adapter/in/web/ReservationController.java`

```java
package com.lunaris.ansenuza.reservation.infrastructure.adapter.in.web;

import com.lunaris.ansenuza.reservation.application.port.in.ConfirmPaymentUseCase;
import com.lunaris.ansenuza.reservation.application.port.in.CreateReservationUseCase;
import com.lunaris.ansenuza.reservation.domain.model.Reservation;
import com.lunaris.ansenuza.reservation.domain.model.ReservationStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** API versionada de migración; los endpoints REST históricos permanecen intactos. */
@RestController
@RequestMapping("/api/v2/reservations")
public class ReservationController {
    private final CreateReservationUseCase createReservation;
    private final ConfirmPaymentUseCase confirmPayment;
    private final com.lunaris.ansenuza.domain.repository.ReservationRepository reservationRepository;
    private final com.lunaris.ansenuza.domain.model.service.ReservationService reservationService;
    private final com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService pricingService;

    public ReservationController(CreateReservationUseCase createReservation, ConfirmPaymentUseCase confirmPayment,
            com.lunaris.ansenuza.domain.repository.ReservationRepository reservationRepository,
            com.lunaris.ansenuza.domain.model.service.ReservationService reservationService,
            com.lunaris.ansenuza.domain.model.service.PricingAndScheduleService pricingService) {
        this.createReservation = createReservation;
        this.confirmPayment = confirmPayment;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.pricingService = pricingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody CreateReservationRequest request) {
        BigDecimal officialAmount = pricingService.calculateReservationAmount(
                request.pickupLocality(), request.destination(), false, request.passengerCount());
        Reservation reservation = createReservation.create(request.toDomain(officialAmount));
        return ResponseEntity.created(URI.create("/api/v2/reservations/" + reservation.id()))
                .body(ReservationResponse.from(reservation));
    }

    @PostMapping("/{reservationId}/payment-confirmation")
    public ReservationResponse confirmPayment(@PathVariable UUID reservationId) {
        return ReservationResponse.from(confirmPayment.confirmPayment(reservationId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id, Principal principal) {
        var reservation = reservationRepository.findById(id)
                .filter(candidate -> candidate.getPassenger() != null
                        && candidate.getPassenger().getPhone().equals(principal.getName()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reserva no encontrada."));
        reservationService.cancelReservation(reservation.getId(), "PASSENGER");
        return ResponseEntity.noContent().build();
    }

    public record CreateReservationRequest(UUID passengerId, LocalDate travelDate,
            String pickupLocality, String pickupAddress, String destination,
            BigDecimal amount, int passengerCount, String departureSchedule) {
        Reservation toDomain(BigDecimal officialAmount) {
            return Reservation.builder(passengerId, pickupLocality, destination)
                    .travelDate(travelDate).pickupAddress(pickupAddress).amount(officialAmount)
                    .passengerCount(passengerCount).departureSchedule(departureSchedule)
                    .status(ReservationStatus.PENDING_PAYMENT).paymentVerified(false).build();
        }
    }

    public record ReservationResponse(UUID id, UUID passengerId, LocalDate travelDate,
            String pickupLocality, String destination, BigDecimal amount,
            ReservationStatus status, boolean paymentVerified) {
        static ReservationResponse from(Reservation reservation) {
            return new ReservationResponse(reservation.id(), reservation.passengerId(), reservation.travelDate(),
                    reservation.pickupLocality(), reservation.destination(), reservation.amount(),
                    reservation.status(), reservation.paymentVerified());
        }
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverController.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanDriverController {
    public record CheckInRequest(String token, UUID tripId) {
        @Override public String toString() { return "CheckInRequest[token=REDACTED,tripId=" + tripId + "]"; }
    }
    private final DriverRouteSheetQuery routes;
    private final CheckInService checkIn;

    public InterurbanDriverController(DriverRouteSheetQuery routes, CheckInService checkIn) {
        this.routes = routes;
        this.checkIn = checkIn;
    }

    @GetMapping("/api/v1/driver/route-sheet")
    public ResponseEntity<DriverRouteSheetQuery.RouteSheet> routeSheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication, CsrfToken csrf) {
        var sheet = routes.find(date, authentication);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(csrf.getHeaderName(), csrf.getToken()).body(sheet);
    }

    @PostMapping("/api/v1/checkin/verify")
    public ResponseEntity<CheckInService.Result> verify(@RequestBody CheckInRequest request, Authentication authentication) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(checkIn.verify(request.token(), request.tripId(), authentication));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverExceptionHandler.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = InterurbanDriverController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanDriverExceptionHandler {
    @ExceptionHandler(QrExpiredException.class)
    public ResponseEntity<Map<String, String>> expired() { return error(410, "QR_EXPIRED"); }
    @ExceptionHandler(QrAlreadyConsumedException.class)
    public ResponseEntity<Map<String, String>> consumed() { return error(409, "QR_ALREADY_CONSUMED"); }
    @ExceptionHandler(InvalidTripException.class)
    public ResponseEntity<Map<String, String>> trip() { return error(409, "INVALID_TRIP"); }
    @ExceptionHandler(InvalidQrException.class)
    public ResponseEntity<Map<String, String>> qr() { return error(400, "INVALID_QR"); }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> denied() { return error(403, "DRIVER_ACCESS_DENIED"); }
    @ExceptionHandler({org.springframework.dao.DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public ResponseEntity<Map<String, String>> storage() { return error(503, "CHECKIN_RETRY_REQUIRED"); }

    private ResponseEntity<Map<String, String>> error(int status, String code) {
        return ResponseEntity.status(status).cacheControl(org.springframework.http.CacheControl.noStore()).body(Map.of("code", code));
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanDriverSecurityConfiguration.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/** Cadena anterior a /api/**, limitada exactamente a los dos endpoints nuevos. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "lunaris.interurban.enabled", havingValue = "true", matchIfMissing = false)
public class InterurbanDriverSecurityConfiguration {
    @Bean @Order(0)
    public SecurityFilterChain interurbanDriverSecurity(HttpSecurity http,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        return http.securityMatcher("/api/v1/driver/route-sheet", "/api/v1/checkin/verify")
                .cors(config -> config.configurationSource(request -> {
                    CorsConfiguration original = cors.getCorsConfiguration(request);
                    if (original == null) return null;
                    var copy = new CorsConfiguration(original);
                    var headers = new ArrayList<>(original.getExposedHeaders() == null ? java.util.List.<String>of() : original.getExposedHeaders());
                    headers.add("X-CSRF-TOKEN");
                    copy.setExposedHeaders(headers);
                    return copy;
                }))
                .csrf(csrf -> {}) // Sesión/cookies: mantener CSRF sólo en esta cadena.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/driver/route-sheet").hasAnyRole("CHOFER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/checkin/verify").hasAnyRole("CHOFER", "ADMIN")
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .httpBasic(basic -> {})
                .build();
    }
}
```


## Archivo: `src/main/java/com/lunaris/ansenuza/service/interurban/InterurbanPaymentExceptionHandler.java`

```java
package com.lunaris.ansenuza.service.interurban;

import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MercadoPagoWebhookController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.payments.enabled"}, havingValue = "true", matchIfMissing = false)
public class InterurbanPaymentExceptionHandler {
    @ExceptionHandler(InterurbanPaymentException.class)
    public ResponseEntity<Map<String, String>> domain(InterurbanPaymentException exception) {
        int status = switch (exception.code()) {
            case INVALID_SIGNATURE -> 401;
            case INVALID_NOTIFICATION -> 400;
            case PROVIDER_UNAVAILABLE, QR_FAILURE -> 503;
        };
        return ResponseEntity.status(status).body(Map.of("code", exception.code().name()));
    }

    @ExceptionHandler({DataAccessException.class, org.springframework.transaction.TransactionException.class})
    public ResponseEntity<Map<String, String>> storage(Exception exception) {
        return ResponseEntity.status(503).body(Map.of("code", "RETRY_REQUIRED"));
    }
}
```
