package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import java.util.List;

/**
 * J'ai créé l'interface TypeCongeService pour abstraire les règles liées aux types de congés
 * (ex: Congé Payé, RTT, Congé Maladie).
 */
public interface TypeCongeService {

    /**
     * Je crée une nouvelle catégorie/type de congé.
     */
    TypeCongeDto.Response creer(TypeCongeDto.Request request);

    /**
     * Je liste tous les types de congés disponibles dans le système.
     */
    List<TypeCongeDto.Response> listerTous();

    /**
     * Je récupère les informations détaillées d'un type de congé par son ID.
     */
    TypeCongeDto.Response getById(Long id);

    /**
     * Je mets à jour la configuration d'un type de congé.
     */
    TypeCongeDto.Response modifier(Long id, TypeCongeDto.Request request);

    /**
     * Je supprime un type de congé et réassigne proprement les demandes si nécessaire.
     */
    void supprimer(Long id);
}
