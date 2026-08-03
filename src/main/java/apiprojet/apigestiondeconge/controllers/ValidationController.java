package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.ValidationDto;
import apiprojet.apigestiondeconge.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère les points de terminaison REST dédiés à l'approbation et au refus des demandes de congé par les managers.
 * J'injecte l'interface ValidationService pour respecter le principe DIP de SOLID.
 */
@RestController
@RequestMapping("/api/validations")
@RequiredArgsConstructor
public class ValidationController {

    // J'injecte l'interface du service de validation
    private final ValidationService validationService;

    // Je réceptionne la décision d'un manager sur une demande
    @PostMapping
    public ResponseEntity<ValidationDto.Response> valider(@Valid @RequestBody ValidationDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(validationService.valider(request));
    }

    // Je retourne la liste de toutes les décisions de validation
    @GetMapping
    public ResponseEntity<List<ValidationDto.Response>> listerToutes() {
        return ResponseEntity.ok(validationService.listerToutes());
    }

    // Je consulte une validation par son identifiant unique
    @GetMapping("/{id}")
    public ResponseEntity<ValidationDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(validationService.getById(id));
    }

    // Je cherche la décision de validation associée à une demande spécifique
    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<ValidationDto.Response> getByDemandeId(@PathVariable Long demandeId) {
        return ResponseEntity.ok(validationService.getByDemandeId(demandeId));
    }
}
