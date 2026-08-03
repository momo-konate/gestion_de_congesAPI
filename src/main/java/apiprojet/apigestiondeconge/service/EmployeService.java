package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.EmployeDto;
import java.util.List;

/**
 * J'ai défini l'interface EmployeService pour abstraire toutes les opérations métier
 * liées à la gestion des employés et respecter le principe DIP (Dependency Inversion Principle).
 */
public interface EmployeService {

    /**
     * Je crée un nouvel employé et j'associe son compte utilisateur, son département et éventuellement son manager.
     */
    EmployeDto.Response creer(EmployeDto.Request request);

    /**
     * Je retourne la liste de tous les employés inscrits dans l'entreprise.
     */
    List<EmployeDto.Response> listerTous();

    /**
     * Je recherche un employé spécifique à partir de son identifiant unique.
     */
    EmployeDto.Response getById(Long id);

    /**
     * Je lister la totalité des collaborateurs sous la responsabilité d'un manager donné.
     */
    List<EmployeDto.Response> listerParManager(Long managerId);

    /**
     * Je mets à jour la fiche et le profil professionnel d'un employé existant.
     */
    EmployeDto.Response modifier(Long id, EmployeDto.Request request);

    /**
     * Je supprime un employé du système par son identifiant.
     */
    void supprimer(Long id);
}
