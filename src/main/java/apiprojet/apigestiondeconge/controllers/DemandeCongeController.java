package apiprojet.apigestiondeconge.controllers;


import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import apiprojet.apigestiondeconge.service.DemandeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes-conge")
@RequiredArgsConstructor
public class DemandeCongeController {

    private final DemandeCongeService demandeCongeService;

    @PostMapping
    public ResponseEntity<DemandeCongeResponse> creer(@Valid @RequestBody DemandeCongeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeCongeService.creerDemande(request));
    }

    @GetMapping
    public ResponseEntity<List<DemandeCongeResponse>> listerToutes() {
        return ResponseEntity.ok(demandeCongeService.listerToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeCongeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.getById(id));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<DemandeCongeResponse>> listerParEmploye(@PathVariable Long employeId) {
        return ResponseEntity.ok(demandeCongeService.listerParEmploye(employeId));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<DemandeCongeResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutDemande statut) {
        return ResponseEntity.ok(demandeCongeService.changerStatut(id, statut));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        demandeCongeService.annuler(id);
        return ResponseEntity.noContent().build();
    }
}

