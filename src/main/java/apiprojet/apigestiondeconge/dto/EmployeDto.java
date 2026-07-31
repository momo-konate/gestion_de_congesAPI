package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class EmployeDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank

        private String poste;
        private String sexe;
        private LocalDate dateNaissance;
        private LocalDate dateEmbauche;

        @NotNull
        private Long utilisateurId;
        @NotNull
        private Long departementId;
        // Optionnel : un employé peut ne pas avoir de manager (ex : le DG)
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

