package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import apiprojet.apigestiondeconge.service.DemandeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Je gère l'exposition des endpoints REST relatifs aux demandes de congé.
 * En suivant le principe SRP, ce contrôleur s'occupe exclusivement de la couche HTTP (réception des requêtes,
 * validation des champs DTO et acheminement des réponses). Il s'appuie sur l'interface DemandeCongeService (DIP).
 */
@RestController
@RequestMapping("/api/demandes-conge")
@RequiredArgsConstructor
public class DemandeCongeController {

    // J'injecte l'interface du service de demande de congé (abstraction) via le constructeur Lombok
    private final DemandeCongeService demandeCongeService;

    // Je réceptionne la création d'une nouvelle demande de congé
    @PostMapping
    public ResponseEntity<DemandeCongeResponse> creer(@Valid @RequestBody DemandeCongeRequest request) {
        // Je délègue au service métier et je retourne une réponse HTTP 201 CREATED avec le DTO
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeCongeService.creerDemande(request));
    }

    // Je propose la consultation de toutes les demandes de congé de l'organisation
    @GetMapping
    public ResponseEntity<List<DemandeCongeResponse>> listerToutes() {
        return ResponseEntity.ok(demandeCongeService.listerToutes());
    }

    // Je permet la récupération d'une demande par son identifiant unique
    @GetMapping("/{id}")
    public ResponseEntity<DemandeCongeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(demandeCongeService.getById(id));
    }

    // Je liste l'ensemble des demandes faites par un employé particulier
    @GetMapping("/employe/{employeId}")
    public ResponseEntity<List<DemandeCongeResponse>> listerParEmploye(@PathVariable Long employeId) {
        return ResponseEntity.ok(demandeCongeService.listerParEmploye(employeId));
    }

    // Je réceptionne la demande de changement de statut d'une demande de congé
    @PutMapping("/{id}/statut")
    public ResponseEntity<DemandeCongeResponse> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutDemande statut) {
        return ResponseEntity.ok(demandeCongeService.changerStatut(id, statut));
    }

    // Je permet à un employé d'annuler sa demande de congé
    @PutMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        demandeCongeService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    // Je réceptionne le téléversement d'un justificatif pour une demande donnée
    @PostMapping("/{id}/justificatif")
    public ResponseEntity<DemandeCongeResponse> uploadJustificatif(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(demandeCongeService.uploadJustificatif(id, file));
    }

    // Je télécharge le fichier justificatif binaire (PDF ou image) rattaché à une demande de congé
    @GetMapping("/{id}/justificatif")
    public ResponseEntity<byte[]> downloadJustificatif(@PathVariable Long id) {
        DemandeConge demande = demandeCongeService.getDemandeEntity(id);

        if (demande.getJustificatifData() == null || demande.getJustificatifData().length == 0) {
            return ResponseEntity.notFound().build();
        }

        String mediaType = demande.getJustificatifType() != null ? demande.getJustificatifType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String filename = demande.getJustificatifNom() != null ? demande.getJustificatifNom() : "justificatif.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(mediaType))
                .body(demande.getJustificatifData());
    }
}
