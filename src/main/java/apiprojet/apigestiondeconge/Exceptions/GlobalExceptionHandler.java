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

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validation des champs DTO (@NotBlank, @Email...) -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. Problème JSON / Enum invalide -> HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Format JSON invalide ou valeur de rôle non reconnue.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. Doublons métier (ex: Email déjà pris) -> HTTP 409 CONFLICT
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAlreadyExists(EntityAlreadyExistsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 4. Ressources non trouvées (ex: ID inexistant) -> HTTP 404 NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 5. Autres erreurs Runtime non capturées -> HTTP 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Erreur serveur : " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 6. Filet de sécurité pour toutes les autres exceptions non prévues -> HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", "Une erreur inattendue est survenue : " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 7. Gestion des violations de contraintes de base de données (ex: doublons, clés étrangères) -> HTTP 409 CONFLICT
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {


            Map<String, String> response = new HashMap<>();

            // Message personnalisé clair
            response.put("erreur", "Violation de contrainte d'unicité ou de clé étrangère. L'utilisateur ou la ressource est peut-être déjà lié(e).");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
