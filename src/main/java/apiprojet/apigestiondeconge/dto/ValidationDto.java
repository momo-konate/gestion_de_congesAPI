package apiprojet.apigestiondeconge.dto;


import apiprojet.apigestiondeconge.entity.DecisionType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class ValidationDto {

    @Getter
    @Setter
    public static class Request {
        @NotNull(message = "L'ID de la demande est obligatoire")
        private Long demandeId;
        @NotNull(message = "L'ID du manager est obligatoire")
        private Long managerId;
        @NotNull(message = "La décision est obligatoire")
        private DecisionType decision;
        @NotNull(message = "Le commentaire est obligatoire")
        private String commentaire;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private DecisionType decision;
        private String commentaire;
        private LocalDate dateValidation;
        private Long demandeId;
        private Long managerId;
        private String managerNomComplet;
    }
}