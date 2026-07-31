package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.ValidationDto;
import apiprojet.apigestiondeconge.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validations")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping
    public ResponseEntity<ValidationDto.Response> valider(@Valid @RequestBody ValidationDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(validationService.valider(request));
    }

    @GetMapping
    public ResponseEntity<List<ValidationDto.Response>> listerToutes() {
        return ResponseEntity.ok(validationService.listerToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValidationDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(validationService.getById(id));
    }

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<ValidationDto.Response> getByDemandeId(@PathVariable Long demandeId) {
        return ResponseEntity.ok(validationService.getByDemandeId(demandeId));
    }
}
