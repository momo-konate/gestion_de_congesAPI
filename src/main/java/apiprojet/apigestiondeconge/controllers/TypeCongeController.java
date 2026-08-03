package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import apiprojet.apigestiondeconge.service.TypeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère l'exposition des API REST relatives à la configuration des types de congés.
 * J'injecte l'interface TypeCongeService afin de dépendre d'abstractions (DIP).
 */
@RestController
@RequestMapping("/api/types-conge")
@RequiredArgsConstructor
public class TypeCongeController {

    // J'injecte le service d'abstractions des types de congé par constructeur
    private final TypeCongeService typeCongeService;

    // Je crée un nouveau type de congé
    @PostMapping
    public ResponseEntity<TypeCongeDto.Response> creer(@Valid @RequestBody TypeCongeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeCongeService.creer(request));
    }

    // Je liste tous les types de congé existants
    @GetMapping
    public ResponseEntity<List<TypeCongeDto.Response>> listerTous() {
        return ResponseEntity.ok(typeCongeService.listerTous());
    }

    // Je récupère les détails d'un type de congé par son identifiant
    @GetMapping("/{id}")
    public ResponseEntity<TypeCongeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(typeCongeService.getById(id));
    }

    // Je mets à jour la définition d'un type de congé
    @PutMapping("/{id}")
    public ResponseEntity<TypeCongeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody TypeCongeDto.Request request) {
        return ResponseEntity.ok(typeCongeService.modifier(id, request));
    }

    // Je supprime un type de congé
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        typeCongeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
