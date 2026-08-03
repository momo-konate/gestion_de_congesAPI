package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DepartementDto;
import java.util.List;

/**
 * J'ai créé cette interface pour déclarer les opérations de gestion des départements de l'entreprise.
 * Cela permet de découpler les consommateurs (Contrôleurs HTTP) des détails d'implémentation.
 */
public interface DepartementService {

    /**
     * Je crée un nouveau département organisationnel.
     */
    DepartementDto.Response creer(DepartementDto.Request request);

    /**
     * Je récupère la liste complète des départements.
     */
    List<DepartementDto.Response> listerTous();

    /**
     * Je cherche un département à partir de son identifiant unique.
     */
    DepartementDto.Response getById(Long id);

    /**
     * Je modifie le libellé ou la description d'un département.
     */
    DepartementDto.Response modifier(Long id, DepartementDto.Request request);

    /**
     * Je supprime un département par son identifiant.
     */
    void supprimer(Long id);
}
