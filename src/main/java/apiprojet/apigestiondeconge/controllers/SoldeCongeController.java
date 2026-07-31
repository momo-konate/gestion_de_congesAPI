package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import apiprojet.apigestiondeconge.service.SoldeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soldes-conge")
@RequiredArgsConstructor
public class SoldeCongeController {

    private final SoldeCongeService soldeCongeService;

    @PostMapping
    public ResponseEntity<SoldeCongeDto.Response> creer(@Valid @RequestBody SoldeCongeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(soldeCongeService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<SoldeCongeDto.Response>> listerTous() {
        return ResponseEntity.ok(soldeCongeService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SoldeCongeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(soldeCongeService.getById(id));
    }

    @GetMapping("/employe/{employeId}/annee/{annee}")
    public ResponseEntity<SoldeCongeDto.Response> getByEmployeEtAnnee(
            @PathVariable Long employeId, @PathVariable Integer annee) {
        return ResponseEntity.ok(soldeCongeService.getByEmployeEtAnnee(employeId, annee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SoldeCongeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody SoldeCongeDto.Request request) {
        return ResponseEntity.ok(soldeCongeService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        soldeCongeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
