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

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<UtilisateurDto.CreateResponse> creer(@Valid @RequestBody UtilisateurDto.Request request) {
        // 1. Création de l'utilisateur
        UtilisateurDto.Response utilisateurCree = utilisateurService.creer(request);

        // 2. Génération immédiate du Token JWT avec son rôle
        String token = jwtUtils.generateToken(utilisateurCree.getEmail(), utilisateurCree.getRole().name());

        // 3. Retourne l'utilisateur + le token JWT
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UtilisateurDto.CreateResponse(utilisateurCree, token));
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurDto.Response>> listerTous() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDto.Response>> listerParRole(@PathVariable Role role) {
        return ResponseEntity.ok(utilisateurService.listerParRole(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurDto.Request request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    @PutMapping("/{id}/desactiver")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}