package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.DepartementDto;
import apiprojet.apigestiondeconge.service.DepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère les routes API pour l'administration des départements de l'entreprise.
 * J'utilise l'interface DepartementService pour interagir avec la couche métier sans dépendre de son implémentation.
 */
@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
public class DepartementController {

    // J'injecte l'interface du service de département par constructeur
    private final DepartementService departementService;

    // Je crée un nouveau département
    @PostMapping
    public ResponseEntity<DepartementDto.Response> creer(@Valid @RequestBody DepartementDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departementService.creer(request));
    }

    // Je retourne la liste de tous les départements
    @GetMapping
    public ResponseEntity<List<DepartementDto.Response>> listerTous() {
        return ResponseEntity.ok(departementService.listerTous());
    }

    // Je récupère un département à partir de son identifiant
    @GetMapping("/{id}")
    public ResponseEntity<DepartementDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.getById(id));
    }

    // Je modifie un département existant
    @PutMapping("/{id}")
    public ResponseEntity<DepartementDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody DepartementDto.Request request) {
        return ResponseEntity.ok(departementService.modifier(id, request));
    }

    // Je supprime un département par son identifiant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        departementService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
