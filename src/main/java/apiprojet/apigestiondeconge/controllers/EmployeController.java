package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.EmployeDto;
import apiprojet.apigestiondeconge.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Je gère les points d'entrée HTTP REST relatifs à l'administration des employés.
 * Je m'assure de valider les DTOs entrants et de déléguer la logique métier à l'interface EmployeService (DIP).
 */
@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
public class EmployeController {

    // J'injecte l'interface du service d'employés via le constructeur Lombok
    private final EmployeService employeService;

    // Je réceptionne la création d'un nouvel employé
    @PostMapping
    public ResponseEntity<EmployeDto.Response> creer(@Valid @RequestBody EmployeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.creer(request));
    }

    // Je retourne la liste globale de tous les employés
    @GetMapping
    public ResponseEntity<List<EmployeDto.Response>> listerTous() {
        return ResponseEntity.ok(employeService.listerTous());
    }

    // Je cherche et retourne le profil d'un employé via son identifiant
    @GetMapping("/{id}")
    public ResponseEntity<EmployeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.getById(id));
    }

    // Je retourne la liste des employés gérés par un manager donné
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeDto.Response>> listerParManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeService.listerParManager(managerId));
    }

    // Je mets à jour la fiche d'un employé existant
    @PutMapping("/{id}")
    public ResponseEntity<EmployeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody EmployeDto.Request request) {
        return ResponseEntity.ok(employeService.modifier(id, request));
    }

    // Je supprime un employé du système
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
