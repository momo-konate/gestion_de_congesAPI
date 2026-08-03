package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.ValidationDto;
import java.util.List;

/**
 * J'ai créé cette interface pour abstraire les règles d'approbation et de refus des demandes de congé par les managers.
 */
public interface ValidationService {

    /**
     * J'enregistre la décision (Approuvée / Refusée) d'un manager sur une demande de congé.
     */
    ValidationDto.Response valider(ValidationDto.Request request);

    /**
     * Je liste l'historique complet des validations effectuées.
     */
    List<ValidationDto.Response> listerToutes();

    /**
     * Je récupère une validation par son identifiant.
     */
    ValidationDto.Response getById(Long id);

    /**
     * Je cherche la décision de validation associée à une demande de congé spécifique.
     */
    ValidationDto.Response getByDemandeId(Long demandeId);
}
