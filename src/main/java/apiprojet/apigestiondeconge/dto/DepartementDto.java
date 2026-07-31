package apiprojet.apigestiondeconge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class DepartementDto {

    @Getter
    @Setter
    public static class Request {
        @NotBlank
        private String nom;
        private String description;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String nom;
        private String description;
    }
}