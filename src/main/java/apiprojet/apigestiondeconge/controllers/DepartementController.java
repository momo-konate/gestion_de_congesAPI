package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.DepartementDto;
import apiprojet.apigestiondeconge.service.DepartementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
@RequiredArgsConstructor
public class DepartementController {

    private final DepartementService departementService;

    @PostMapping
    public ResponseEntity<DepartementDto.Response> creer(@Valid @RequestBody DepartementDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departementService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<DepartementDto.Response>> listerTous() {
        return ResponseEntity.ok(departementService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartementDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departementService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartementDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody DepartementDto.Request request) {
        return ResponseEntity.ok(departementService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        departementService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

