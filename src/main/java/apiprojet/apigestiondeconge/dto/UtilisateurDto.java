package apiprojet.apigestiondeconge.dto;

import apiprojet.apigestiondeconge.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UtilisateurDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank
        private String nom;
        @NotBlank
        private String prenom;
        @NotBlank
        @Email
        private String email;
        // MVP sans sécurité : mot de passe en clair pour l'instant, à hasher (BCrypt) dès qu'on branche Spring Security
        @NotBlank
        private String motDePasse;
        private String telephone;
        @NotNull
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

