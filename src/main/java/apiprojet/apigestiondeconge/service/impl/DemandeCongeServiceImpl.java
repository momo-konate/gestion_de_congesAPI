package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import apiprojet.apigestiondeconge.entity.TypeConge;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
import apiprojet.apigestiondeconge.service.DemandeCongeService;
import apiprojet.apigestiondeconge.service.SoldeCongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * J'implémente les règles métier relatives à la gestion des demandes de congé.
 * Je respecte le principe SRP (Single Responsibility Principle) en me concentrant uniquement sur la logique métier,
 * et le DIP en implémentant l'interface DemandeCongeService. J'utilise l'injection par constructeur via @RequiredArgsConstructor.
 */
@Service
@RequiredArgsConstructor
public class DemandeCongeServiceImpl implements DemandeCongeService {

    // J'injecte mes dépendances de manière immuable grâce à des attributs final et au constructeur Lombok
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeService soldeCongeService;

    // Je crée une demande de congé après avoir validé les dates et vérifié l'existence des entités rattachées
    @Override
    @Transactional
    public DemandeCongeResponse creerDemande(DemandeCongeRequest request) {
        // Je m'assure que la date de fin n'est pas antérieure à la date de début
        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        // Je récupère l'employé émetteur de la demande ou je lève une exception 404
        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + request.getEmployeId()));

        // Je récupère le type de congé demandé ou je lève une exception 404
        TypeConge typeConge = typeCongeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de congé introuvable avec l'ID : " + request.getTypeId()));

        // Je calcule la durée effective du congé en nombre inclusif de jours
        long nombreJours = ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin()) + 1;

        // Je construis la nouvelle entité de demande avec un statut par défaut à EN_ATTENTE
        DemandeConge demande = DemandeConge.builder()
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .nombreJours((int) nombreJours)
                .motif(request.getMotif())
                .statut(StatutDemande.EN_ATTENTE)
                .employe(employe)
                .typeConge(typeConge)
                .build();

        // Je sauvegarde en base de données et je retourne la réponse sous forme de DTO
        DemandeConge saved = demandeCongeRepository.save(demande);
        return toResponse(saved);
    }

    // Je téléverse et j'attache un fichier justificatif (médical ou justificatif d'absence) à la demande
    @Override
    @Transactional
    public DemandeCongeResponse uploadJustificatif(Long demandeId, MultipartFile file) {
        // Je recherche la demande de congé concernée
        DemandeConge demande = findEntityOrThrow(demandeId);

        // Je valide le format MIME du fichier (acceptation des images et des documents PDF uniquement)
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf") &&
                 !contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Format non supporté. Veuillez joindre un PDF ou une image.");
        }

        // Je valide que le fichier ne dépasse pas la limite de 5 Mo
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (maximum 5 Mo).");
        }

        try {
            // J'enregistre les métadonnées et le contenu binaire du fichier justificatif dans l'entité
            demande.setJustificatifNom(file.getOriginalFilename());
            demande.setJustificatifType(contentType);
            demande.setJustificatifData(file.getBytes());
            DemandeConge saved = demandeCongeRepository.save(demande);
            return toResponse(saved);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier binaire.", e);
        }
    }

    // Je fournis l'accès direct à l'entité interne pour les traitements inter-services
    @Override
    public DemandeConge getDemandeEntity(Long id) {
        return findEntityOrThrow(id);
    }

    // Je liste toutes les demandes de congé sous forme de DTOs
    @Override
    public List<DemandeCongeResponse> listerToutes() {
        return demandeCongeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je récupère une demande par son identifiant unique sous forme de DTO
    @Override
    public DemandeCongeResponse getById(Long id) {
        return toResponse(findEntityOrThrow(id));
    }

    // Je liste les demandes d'un employé précis
    @Override
    public List<DemandeCongeResponse> listerParEmploye(Long employeId) {
        return demandeCongeRepository.findByEmployeId(employeId).stream()
                .map(this::toResponse)
                .toList();
    }

    // Je gère le changement de statut d'une demande et la mise à jour réactive des soldes
    @Override
    @Transactional
    public DemandeCongeResponse changerStatut(Long id, StatutDemande nouveauStatut) {
        DemandeConge demande = findEntityOrThrow(id);
        StatutDemande ancienStatut = demande.getStatut();

        // Si le statut ne change pas, je n'exécute aucun traitement inutile
        if (ancienStatut == nouveauStatut) {
            return toResponse(demande);
        }

        Integer annee = demande.getDateDebut().getYear();
        Long employeId = demande.getEmploye().getId();

        // Si la demande devient APPROUVÉE, je débite le solde de jours de l'employé
        if (nouveauStatut == StatutDemande.APPROUVEE && ancienStatut != StatutDemande.APPROUVEE) {
            soldeCongeService.debiter(employeId, annee, demande.getNombreJours());
        }

        // Si une demande APPROUVÉE change de statut (ex: refusée ou annulée), je réintroduis les jours dans le solde
        if (ancienStatut == StatutDemande.APPROUVEE && nouveauStatut != StatutDemande.APPROUVEE) {
            soldeCongeService.crediter(employeId, annee, demande.getNombreJours());
        }

        demande.setStatut(nouveauStatut);
        DemandeConge saved = demandeCongeRepository.save(demande);
        return toResponse(saved);
    }

    // J'annule une demande de congé si elle est encore au statut EN_ATTENTE
    @Override
    @Transactional
    public void annuler(Long id) {
        DemandeConge demande = findEntityOrThrow(id);

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalStateException("Impossible d'annuler une demande déjà traitée (validée ou refusée).");
        }

        demande.setStatut(StatutDemande.ANNULEE);
        demandeCongeRepository.save(demande);
    }

    // Je centralise la recherche d'une demande avec levée systématique d'une exception métrique claire
    private DemandeConge findEntityOrThrow(Long id) {
        return demandeCongeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé introuvable avec l'ID : " + id));
    }

    // Je mappe de manière étanche mon entité JPA vers le DTO de réponse exposé au client
    private DemandeCongeResponse toResponse(DemandeConge d) {
        String managerNom = (d.getValidation() != null && d.getValidation().getManager() != null)
                ? d.getValidation().getManager().getUtilisateur().getPrenom() + " " + d.getValidation().getManager().getUtilisateur().getNom()
                : null;

        return DemandeCongeResponse.builder()
                .id(d.getId())
                .dateDemande(d.getDateDemande())
                .dateDebut(d.getDateDebut())
                .dateFin(d.getDateFin())
                .nombreJours(d.getNombreJours())
                .motif(d.getMotif())
                .statut(d.getStatut())
                .employeId(d.getEmploye().getId())
                .employeNomComplet(d.getEmploye().getUtilisateur().getPrenom() + " " + d.getEmploye().getUtilisateur().getNom())
                .typeId(d.getTypeConge().getId())
                .typeLibelle(d.getTypeConge().getLibelle())
                .commentaireValidation(d.getValidation() != null ? d.getValidation().getCommentaire() : null)
                .validationManagerNom(managerNom)
                .justificatifNom(d.getJustificatifNom())
                .justificatifType(d.getJustificatifType())
                .build();
    }
}
