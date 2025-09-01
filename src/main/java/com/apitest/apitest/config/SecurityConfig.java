package com.apitest.apitest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Allow public access to Swagger UI and API docs
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Allow public access to GitHub login endpoint
                .requestMatchers("/api/github/login", "/api/github/login/oauth2/code/github").permitAll()
                // Allow public access to actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                // All other requests can be accessed without authentication for now
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .oauth2Login(oauth2 -> oauth2.disable());

        return http.build();
    }
}
