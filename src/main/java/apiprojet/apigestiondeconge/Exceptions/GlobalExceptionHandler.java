package apiprojet.apigestiondeconge.Exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * J'ai configuré ce gestionnaire d'exceptions global avec @RestControllerAdvice.
 * Il intercepte toutes les exceptions levées par les contrôleurs et les services pour construire des réponses HTTP
 * cohérentes, claires et adaptées (400, 404, 409, 500), évitant la fuite de détails d'implémentation vers le frontend.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Je gère la validation des champs DTO (@NotBlank, @Email...) et je renvoie un statut HTTP 400 (BAD REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. Je traite les erreurs de désérialisation JSON ou d'Enums invalides et je renvoie un statut HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Format JSON invalide ou valeur de rôle non reconnue.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. Je gère les conflits de doublons métier (ex: email déjà existant) et je renvoie un statut HTTP 409 (CONFLICT)
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(EntityAlreadyExistsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 4. Je gère la non-existence des ressources (ex: ID inexistant) et je renvoie un statut HTTP 404 (NOT FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 5. Je gère les exceptions d'erreurs d'exécution (RuntimeException) non attrapées et je renvoie un statut HTTP 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Erreur serveur : " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 6. Je fournis un filet de sécurité pour toutes les autres exceptions inattendues -> HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Une erreur inattendue est survenue : " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 7. Je gère les violations de contraintes d'intégrité de la base de données -> HTTP 409 (CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Violation de contrainte d'unicité ou de clé étrangère dans la base de données.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
