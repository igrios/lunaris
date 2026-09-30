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
