package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class SoldeCongeDto {

    @Getter
    @Setter
    public static class Request {
        @NotNull
        private Long employeId;
        @NotNull
        private Integer annee;
        @NotNull
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