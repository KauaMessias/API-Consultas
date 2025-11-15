package com.example.consultas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity()
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    public SecurityConfigurations(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/medicos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/clientes").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/enderecos").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/enderecos/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/enderecos/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/enderecos/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/consultas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/**").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/medicos/**").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/medicos/**").hasRole("MEDICO")
                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
