package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class SoldeCongeDto {

    @Getter
    @Setter
    public static class Request {
        @NotNull(message = "L'ID de l'employé est obligatoire")
        private Long employeId;
        @NotNull(message = "L'année est obligatoire")
        private Integer annee;
        @NotNull(message = "Les jours acquis sont obligatoires")
        private Integer joursAcquis;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Integer annee;
        private Integer joursAcquis;
        private Integer joursUtilises;
        private Integer joursRestants;
        private Long employeId;
    }
}