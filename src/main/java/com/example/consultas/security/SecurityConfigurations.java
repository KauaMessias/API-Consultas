package com.example.consultas.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity()
@EnableMethodSecurity(prePostEnabled = true)
@SecurityScheme(name = SecurityConfigurations.SECURITY, type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SecurityConfigurations {

    @Value("${FRONTEND_URL}")
    private String FRONTEND_URL;
    private final SecurityFilter securityFilter;

    public static final String SECURITY = "bearerAuth";

    public SecurityConfigurations(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/medicos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/clientes").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/enderecos").hasAnyRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/enderecos/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/enderecos/**").hasAnyRole("MEDICO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/enderecos/**").hasAnyRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/v1/consultas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/consultas/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/consultas/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/consultas/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**").hasAnyRole("MEDICO", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/{id}/horarios").hasRole("MEDICO")
                                .requestMatchers(HttpMethod.GET, "/api/v1/medicos/horarios/{id}").hasRole("MEDICO")
                                .requestMatchers(HttpMethod.POST, "/api/v1/medicos/horarios").hasRole("MEDICO")
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/medicos/horarios/{id}").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/{id}/horarios/disponiveis").hasAnyRole("MEDICO", "CLIENTE")


                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/medicos/**").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/medicos/**").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicos/perfil").hasRole("MEDICO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/perfil").hasRole("CLIENTE")
                        .requestMatchers( "/", "/error", "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/swagger-ui/index.html").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
   CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:4173", FRONTEND_URL));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
