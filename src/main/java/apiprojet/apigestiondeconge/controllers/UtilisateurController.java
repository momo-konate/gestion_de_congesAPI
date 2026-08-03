package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.config.JwtUtils;
import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.entity.Role;
import apiprojet.apigestiondeconge.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère les requêtes HTTP pour l'administration des utilisateurs et la génération de jetons d'accès JWT à la création.
 * J'injecte l'interface UtilisateurService et la classe Utilitaire JwtUtils.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    // J'injecte les dépendances par constructeur
    private final UtilisateurService utilisateurService;
    private final JwtUtils jwtUtils;

    // Je crée un nouvel utilisateur et je lui attribue immédiatement son premier jeton JWT
    @PostMapping
    public ResponseEntity<UtilisateurDto.CreateResponse> creer(@Valid @RequestBody UtilisateurDto.Request request) {
        // 1. Je crée l'utilisateur en base de données via le service métier
        UtilisateurDto.Response utilisateurCree = utilisateurService.creer(request);

        // 2. Je génère immédiatement le token JWT contenant son email et son rôle
        String token = jwtUtils.generateToken(utilisateurCree.getEmail(), utilisateurCree.getRole().name());

        // 3. Je retourne le DTO englobant l'utilisateur créé et son jeton
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UtilisateurDto.CreateResponse(utilisateurCree, token));
    }

    // Je retourne la liste complète des utilisateurs
    @GetMapping
    public ResponseEntity<List<UtilisateurDto.Response>> listerTous() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    // Je filtre et retourne les utilisateurs selon leur rôle (EMPLOYE / ADMIN)
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDto.Response>> listerParRole(@PathVariable Role role) {
        return ResponseEntity.ok(utilisateurService.listerParRole(role));
    }

    // Je consulte la fiche d'un utilisateur par son ID
    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    // Je mets à jour les informations d'un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurDto.Request request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    // Je désactive l'accès d'un utilisateur
    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    // Je supprime un compte utilisateur
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}