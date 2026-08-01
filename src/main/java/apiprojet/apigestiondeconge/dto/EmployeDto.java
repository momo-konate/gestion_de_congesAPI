package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class EmployeDto {

    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "Le poste est obligatoire")
        @Size(max = 100, message = "Le poste ne doit pas dépasser 100 caractères")
        private String poste;

        @NotBlank(message = "Le sexe est obligatoire")
        @Pattern(
                regexp = "Homme|Femme",
                message = "Le sexe doit être Homme ou Femme"
        )
        private String sexe;

        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        private LocalDate dateNaissance;

        @NotNull(message = "La date d'embauche est obligatoire")
        @PastOrPresent(message = "La date d'embauche ne peut pas être dans le futur")
        private LocalDate dateEmbauche;

        @NotNull(message = "L'utilisateur est obligatoire")
        private Long utilisateurId;

        @NotNull(message = "Le département est obligatoire")
        private Long departementId;

        // Optionnel : un employé peut ne pas avoir de manager (DG par exemple)
        private Long managerId;
    }

    @Getter
    @Builder
    public static class Response {

        private Long id;

        private String poste;
        private String sexe;
        private LocalDate dateNaissance;
        private LocalDate dateEmbauche;

        private Long utilisateurId;
        private String nomComplet;

        private Long departementId;
        private String departementNom;

        private Long managerId;
        private String managerNomComplet;
    }
}