package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class TypeCongeDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank
        private String libelle;
        @NotNull
        private Integer nombreJours;
        private String description;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String libelle;
        private Integer nombreJours;
        private String description;
    }
}

