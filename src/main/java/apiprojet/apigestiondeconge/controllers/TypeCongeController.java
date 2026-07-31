package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import apiprojet.apigestiondeconge.service.TypeCongeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-conge")
@RequiredArgsConstructor
public class TypeCongeController {

    private final TypeCongeService typeCongeService;

    @PostMapping
    public ResponseEntity<TypeCongeDto.Response> creer(@Valid @RequestBody TypeCongeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeCongeService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<TypeCongeDto.Response>> listerTous() {
        return ResponseEntity.ok(typeCongeService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeCongeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(typeCongeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TypeCongeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody TypeCongeDto.Request request) {
        return ResponseEntity.ok(typeCongeService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        typeCongeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

