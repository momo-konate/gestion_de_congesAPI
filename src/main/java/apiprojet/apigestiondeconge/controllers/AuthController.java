package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.config.JwtUtils;
import apiprojet.apigestiondeconge.dto.AuthDto;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.Request request) {
        // 1. Chercher l'utilisateur (Admin ou Employé) par son email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElse(null);

        // 2. Vérifier si l'utilisateur existe et si le mot de passe concorde
        if (utilisateur == null || !passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erreur", "Email ou mot de passe incorrect"));
        }

        // 3. Vérifier que le compte n'est pas désactivé
        if (Boolean.FALSE.equals(utilisateur.getActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erreur", "Ce compte est désactivé"));
        }

        // 4. Générer le Token JWT contenant l'email et le rôle exact (ADMIN ou EMPLOYE)
        String token = jwtUtils.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());

        // 5. Renvoyer le Token
        return ResponseEntity.ok(new AuthDto.Response(token, utilisateur.getEmail(), utilisateur.getRole().name()));
    }
}