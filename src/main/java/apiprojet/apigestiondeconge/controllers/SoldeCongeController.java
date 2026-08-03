package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import apiprojet.apigestiondeconge.service.SoldeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère la couche web REST relative aux soldes de congés annuels.
 * Je m'appuie sur l'interface SoldeCongeService pour appliquer l'inversion de dépendance (DIP).
 */
@RestController
@RequestMapping("/api/soldes-conge")
@RequiredArgsConstructor
public class SoldeCongeController {

    // J'injecte l'interface de service des soldes de congé
    private final SoldeCongeService soldeCongeService;

    // Je crée une nouvelle fiche de solde de congé
    @PostMapping
    public ResponseEntity<SoldeCongeDto.Response> creer(@Valid @RequestBody SoldeCongeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soldeCongeService.creer(request));
    }

    // Je retourne tous les soldes enregistrés
    @GetMapping
    public ResponseEntity<List<SoldeCongeDto.Response>> listerTous() {
        return ResponseEntity.ok(soldeCongeService.listerTous());
    }

    // Je récupère un solde par son identifiant unique
    @GetMapping("/{id}")
    public ResponseEntity<SoldeCongeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(soldeCongeService.getById(id));
    }

    // Je recherche le solde d'un employé pour une année donnée
    @GetMapping("/employe/{employeId}/annee/{annee}")
    public ResponseEntity<SoldeCongeDto.Response> getByEmployeEtAnnee(
            @PathVariable Long employeId, @PathVariable Integer annee) {
        return ResponseEntity.ok(soldeCongeService.getByEmployeEtAnnee(employeId, annee));
    }

    // Je modifie un solde de congé existant
    @PutMapping("/{id}")
    public ResponseEntity<SoldeCongeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody SoldeCongeDto.Request request) {
        return ResponseEntity.ok(soldeCongeService.modifier(id, request));
    }

    // Je supprime un solde de congé
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        soldeCongeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
