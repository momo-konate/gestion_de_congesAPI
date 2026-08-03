package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * J'ai défini ici le contrat d'interface pour le service de gestion des demandes de congé.
 * En suivant le principe de l'inversion des dépendances (DIP - SOLID), les contrôleurs et autres
 * composants dépendent de cette abstraction et non d'une implémentation concrète.
 */
public interface DemandeCongeService {

    /**
     * Je crée une nouvelle demande de congé pour un employé après vérifications métier.
     */
    DemandeCongeResponse creerDemande(DemandeCongeRequest request);

    /**
     * J'attache un fichier justificatif (PDF ou image) à une demande de congé existante.
     */
    DemandeCongeResponse uploadJustificatif(Long demandeId, MultipartFile file);

    /**
     * Je récupère l'entité interne DemandeConge pour les besoins de traitement interne.
     */
    DemandeConge getDemandeEntity(Long id);

    /**
     * Je liste l'ensemble des demandes de congé enregistrées dans le système.
     */
    List<DemandeCongeResponse> listerToutes();

    /**
     * Je recherche et retourne une demande de congé précise par son identifiant unique.
     */
    DemandeCongeResponse getById(Long id);

    /**
     * Je liste toutes les demandes de congé associées à un employé spécifique.
     */
    List<DemandeCongeResponse> listerParEmploye(Long employeId);

    /**
     * Je modifie le statut d'une demande de congé (ex: validation/refus) et j'ajuste le solde de jours en conséquence.
     */
    DemandeCongeResponse changerStatut(Long id, StatutDemande nouveauStatut);

    /**
     * J'annule une demande de congé en attente.
     */
    void annuler(Long id);
}
