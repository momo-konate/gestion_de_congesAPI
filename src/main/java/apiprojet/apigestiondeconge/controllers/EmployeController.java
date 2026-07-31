package apiprojet.apigestiondeconge.controllers;

import apiprojet.apigestiondeconge.dto.EmployeDto;
import apiprojet.apigestiondeconge.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService employeService;

    @PostMapping
    public ResponseEntity<EmployeDto.Response> creer(@Valid @RequestBody EmployeDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeService.creer(request));
    }

    @GetMapping
    public ResponseEntity<List<EmployeDto.Response>> listerTous() {
        return ResponseEntity.ok(employeService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeService.getById(id));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<EmployeDto.Response>> listerParManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeService.listerParManager(managerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeDto.Response> modifier(@PathVariable Long id, @Valid @RequestBody EmployeDto.Request request) {
        return ResponseEntity.ok(employeService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        employeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
