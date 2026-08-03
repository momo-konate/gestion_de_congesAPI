package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.entity.Role;
import java.util.List;

/**
 * J'ai créé l'interface UtilisateurService pour séparer le contrat d'opérations sur les utilisateurs de son implémentation.
 */
public interface UtilisateurService {

    /**
     * Je crée un nouvel utilisateur avec encodage sécurisé de son mot de passe.
     */
    UtilisateurDto.Response creer(UtilisateurDto.Request request);

    /**
     * Je liste tous les utilisateurs du système.
     */
    List<UtilisateurDto.Response> listerTous();

    /**
     * Je filtre les utilisateurs selon leur rôle (EMPLOYE, ADMIN).
     */
    List<UtilisateurDto.Response> listerParRole(Role role);

    /**
     * Je recherche un utilisateur à partir de son ID.
     */
    UtilisateurDto.Response getById(Long id);

    /**
     * Je modifie les informations personnelles et les accès d'un utilisateur.
     */
    UtilisateurDto.Response modifier(Long id, UtilisateurDto.Request request);

    /**
     * Je désactive le compte d'un utilisateur sans le supprimer de la base.
     */
    void desactiver(Long id);

    /**
     * Je supprime définitivement un compte utilisateur.
     */
    void supprimer(Long id);
}