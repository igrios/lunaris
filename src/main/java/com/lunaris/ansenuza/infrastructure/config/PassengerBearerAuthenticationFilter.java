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
