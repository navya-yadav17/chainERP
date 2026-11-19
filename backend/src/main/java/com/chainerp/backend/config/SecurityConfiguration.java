package com.chainerp.backend.config;

import com.chainerp.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/treasury/**").hasAnyRole("FINANCE", "ADMIN")
                .requestMatchers("/api/v1/payroll/**").hasAnyRole("HR", "FINANCE", "ADMIN")
                .requestMatchers("/api/v1/subscriptions/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/v1/events/**").hasAnyRole("USER", "SERVICE", "ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
            User.withUsername("admin")
                .password(passwordEncoder.encode(
                    System.getenv().getOrDefault("CHAINERP_ADMIN_PASSWORD", "change-me")))
                .roles("ADMIN")
                .build(),
            User.withUsername("finance")
                .password(passwordEncoder.encode(
                    System.getenv().getOrDefault("CHAINERP_FINANCE_PASSWORD", "change-me")))
                .roles("FINANCE")
                .build(),
            User.withUsername("hr")
                .password(passwordEncoder.encode(
                    System.getenv().getOrDefault("CHAINERP_HR_PASSWORD", "change-me")))
                .roles("HR")
                .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
