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
@ConditionalOnProperty(name = {"lunaris.interurban.enabled", "lunaris.interurban.driver.enabled"}, havingValue = "true")
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
