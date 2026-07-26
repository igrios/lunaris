package com.lunaris.ansenuza.infrastructure.config;

import com.lunaris.ansenuza.application.usecase.PassengerOtpService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            otpService.resolvePhone(token).ifPresent(phone -> {
                var authentication = new UsernamePasswordAuthenticationToken(
                        phone, token, List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }
}
