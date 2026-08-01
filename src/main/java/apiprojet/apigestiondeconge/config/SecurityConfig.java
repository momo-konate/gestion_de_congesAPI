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
                // 1. Désactiver la protection CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Activation du CORS pour Angular
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Gestion des sessions en mode STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Gestion personnalisée des exceptions HTTP Security
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler) // Rejet des permissions insuffisantes (403)
                        .authenticationEntryPoint(authenticationEntryPoint) // Rejet de l'absence de token valide (401)
                )
                // 5. Protection des routes par Rôle (RBAC)
                .authorizeHttpRequests(auth -> auth
                        // ---  ACCÈS PUBLIC (Uniquement l'authentification et la console H2) ---
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()

                        // ---  CRÉATION & GESTION DES COMPTES (STRICTEMENT RESERVE A L'ADMIN) ---
                        // Seul l'ADMIN peut faire des POST, GET, PUT, DELETE sur les utilisateurs et employés
                        .requestMatchers("/api/utilisateurs/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/employes/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/departements/**").hasAuthority("ADMIN")

                        // --- 👤 ACTIONS RÉSERVÉES AUX EMPLOYÉS (ET ADMIN) ---
                        // 1. Soumettre une demande de congé
                        .requestMatchers(HttpMethod.POST, "/api/conges/**").hasAnyAuthority("EMPLOYE", "ADMIN")
                        // 2. Consulter son propre solde et ses propres demandes
                        .requestMatchers(HttpMethod.GET, "/api/soldes-conge/**", "/api/demandes-conge/**")
                        .hasAnyAuthority("EMPLOYE", "ADMIN")

                        // ---  VALIDATION DES CONGÉS (ADMIN SEULEMENT) ---
                        .requestMatchers(HttpMethod.PUT, "/api/validations/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/validations/**").hasAuthority("ADMIN")

                        // ---  TOUTE AUTRE REQUÊTE ---
                        .anyRequest().authenticated()
                )

                // 6. Autoriser les frames pour H2
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // 7. Ajouter le filtre JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}