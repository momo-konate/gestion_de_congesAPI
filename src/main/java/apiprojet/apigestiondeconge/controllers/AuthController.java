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

/**
 * Je gère le point d'entrée REST de connexion et d'authentification des utilisateurs (JWT).
 * J'utilise l'injection par constructeur via Lombok @RequiredArgsConstructor.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // J'injecte mes dépendances sécuritaires et d'accès aux données
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // Je traite la tentative d'authentification par email et mot de passe
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.Request request) {
        // 1. Je cherche l'utilisateur (Admin ou Employé) par son email en base
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElse(null);

        // 2. Je valide les identifiants et la correspondance du mot de passe hashé
        if (utilisateur == null || !passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erreur", "Email ou mot de passe incorrect"));
        }

        // 3. Je m'assure que le compte de l'utilisateur n'a pas été désactivé
        if (Boolean.FALSE.equals(utilisateur.getActif())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erreur", "Ce compte est désactivé"));
        }

        // 4. Je génère le jeton JWT cryptographique avec l'email et le rôle de l'utilisateur
        String token = jwtUtils.generateToken(utilisateur.getEmail(), utilisateur.getRole().name());

        // 5. Je retourne le DTO d'authentification complet contenant le token et le profil
        Long employeId = utilisateur.getEmploye() != null ? utilisateur.getEmploye().getId() : null;
        return ResponseEntity.ok(new AuthDto.Response(
                token,
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getId(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                employeId
        ));
    }
}
