package apiprojet.apigestiondeconge.dto;


import apiprojet.apigestiondeconge.entity.StatutDemande;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DemandeCongeResponse {

    private Long id;
    private LocalDate dateDemande;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;
    private String motif;
    private StatutDemande statut;

    private Long employeId;
    private String employeNomComplet;

    private Long typeId;
    private String typeLibelle;
}
