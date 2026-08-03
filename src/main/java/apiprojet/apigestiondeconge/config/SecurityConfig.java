package apiprojet.apigestiondeconge.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactiver la protection CSRF & Activer le CORS
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Mode STATELESS (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Gestion personnalisée des erreurs Security (401 & 403)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                )

                // 4. Protection des routes (RBAC)
                .authorizeHttpRequests(auth -> auth
                        // --- ACCÈS PUBLIC (Swagger UI + OpenAPI Docs + Auth + H2) ---
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/auth/**",
                                "/h2-console/**"
                        ).permitAll()

                        // --- GESTION DES COMPTES (ADMIN SEULEMENT) ---
                        .requestMatchers(HttpMethod.POST, "/api/utilisateurs/**", "/api/employes/**", "/api/departements/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/utilisateurs/**", "/api/employes/**", "/api/departements/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/utilisateurs/**", "/api/employes/**", "/api/departements/**").hasAuthority("ADMIN")

                        // --- LECTURE EMPLOYES & DEPARTEMENTS (tous authentifiés) ---
                        .requestMatchers(HttpMethod.GET, "/api/employes/**", "/api/departements/**").hasAnyAuthority("EMPLOYE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/utilisateurs/**").hasAuthority("ADMIN")

                        // --- ACTIONS EMPLOYÉS & ADMIN ---
                        .requestMatchers(HttpMethod.POST, "/api/conges/**").hasAnyAuthority("EMPLOYE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/soldes-conge/**", "/api/demandes-conge/**").hasAnyAuthority("EMPLOYE", "ADMIN")

                        // --- VALIDATION DES CONGÉS (ADMIN SEULEMENT) ---
                        .requestMatchers(HttpMethod.PUT, "/api/validations/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/validations/**").hasAuthority("ADMIN")

                        // --- TOUTE AUTRE REQUÊTE ---
                        .anyRequest().authenticated()
                )

                // 5. Autoriser les frames pour H2 Console
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // 6. Filtre JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}