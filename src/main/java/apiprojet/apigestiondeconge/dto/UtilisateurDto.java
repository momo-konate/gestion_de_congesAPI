package apiprojet.apigestiondeconge.dto;

import apiprojet.apigestiondeconge.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UtilisateurDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank(message = "Le nom est obligatoire")
        private String nom;
        @NotBlank(message = "Le prénom est obligatoire")
        private String prenom;
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        private String email;
        // MVP sans sécurité : mot de passe en clair pour l'instant, à hasher (BCrypt) dès qu'on branche Spring Security
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 64, message = "Le mot de passe doit contenir entre 8 et 64 caractères")
        private String motDePasse;
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        private String telephone;
        @NotNull(message = "L'état actif est obligatoire")
        private Role role;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private Boolean actif;
        private Role role;
    }
}

