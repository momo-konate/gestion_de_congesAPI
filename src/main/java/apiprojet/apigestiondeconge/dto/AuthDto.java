package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        private String motDePasse;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private String token;
        private String email;
        private String role;
        private Long id;
        private String prenom;
        private String nom;
        private Long employeId;
    }
}