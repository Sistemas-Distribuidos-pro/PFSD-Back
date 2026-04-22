package com.monolito.ecommerce.config;

import com.monolito.ecommerce.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                    .csrf(csrf -> csrf.disable())
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) -> {
                                writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "No autenticado o token inválido");
                            })
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                writeErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                                        "No tienes permisos para acceder a este recurso");
                            }))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/products").permitAll()
                            .requestMatchers("/api/cart/**").authenticated()
                            .requestMatchers("/api/orders/**").authenticated()
                            .anyRequest().permitAll())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .httpBasic(Customizer.withDefaults());
            return http.build();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo construir SecurityFilterChain", ex);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = new ErrorResponse(status, message, LocalDateTime.now());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo escribir la respuesta de error", ex);
        }
    }
}
