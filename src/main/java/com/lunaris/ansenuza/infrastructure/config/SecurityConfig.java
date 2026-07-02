package com.lunaris.ansenuza.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User; // 👈 AGREGÁ ESTA LÍNEA QUE FALTABA
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
            .csrf(csrf -> csrf.disable()) // Desactivado para facilitar WebSockets sin tokens complejos
            .authorizeHttpRequests(auth -> auth
                // 🛑 Solo VOS (ADMIN) vas a poder tocar el interruptor global de jornada o configuraciones críticas
                .requestMatchers("/admin/bot/toggle-bot").hasRole("ADMIN")
                .requestMatchers("/admin/bot/configurar-jornada").hasRole("ADMIN")
                
                // 💼 Tanto Vos como Martín (OPERATOR) manejan el monitor, chats y la agenda diaria
                .requestMatchers("/admin/bot/monitor/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/admin/chat/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/agenda/**").hasAnyRole("ADMIN", "OPERATOR")
                
                // 🛠️ Permitir recursos estáticos para que no se rompa el diseño visual de Bootstrap
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // Todo lo demás requiere login obligatorio
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .defaultSuccessUrl("/admin/bot/monitor", true) // Al loguearse van derecho a la torre de control
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // 👤 Tu usuario con control absoluto del bot y de las variables
        UserDetails admin = User.builder()
                .username("ignacio")
                .password(passwordEncoder.encode("Ignacio2026!")) 
                .roles("ADMIN")
                .build();

        // 👤 El usuario de Martín (y futuros operadores) para la gestión diaria
        UserDetails operator = User.builder()
                .username("martin")
                .password(passwordEncoder.encode("MartinLunaris2026")) 
                .roles("OPERATOR")
                .build();

        return new InMemoryUserDetailsManager(admin, operator);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}