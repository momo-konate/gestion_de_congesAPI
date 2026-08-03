package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.ValidationDto;
import apiprojet.apigestiondeconge.entity.*;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.ValidationRepository;
import apiprojet.apigestiondeconge.service.DemandeCongeService;
import apiprojet.apigestiondeconge.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente les fonctionnalités de validation et de refu des demandes de congé par les managers.
 * Je mets à jour la décision, le commentaire de validation et le statut global de la demande.
 */
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    // J'injecte les repositories et services requis via le constructeur
    private final ValidationRepository validationRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final DemandeCongeService demandeCongeService;

    // J'enregistre la décision de validation ou de refus d'une demande par un manager
    @Override
    @Transactional
    public ValidationDto.Response valider(ValidationDto.Request request) {

        // Je vérifie si la demande a déjà fait l'objet d'une décision
        validationRepository.findByDemandeCongeId(request.getDemandeId()).ifPresent(v -> {
            throw new IllegalArgumentException("Cette demande a déjà été validée (id validation : " + v.getId() + ")");
        });

        // Je recherche la demande de congé associée
        DemandeConge demande = demandeCongeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + request.getDemandeId()));

        // Je m'assure que la demande est bien au statut EN_ATTENTE avant toute décision
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalArgumentException(
                    "Seule une demande EN_ATTENTE peut être validée (statut actuel : " + demande.getStatut() + ")");
        }

        // Je recherche le manager réalisant la validation
        Employe manager = employeRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'ID : " + request.getManagerId()));

        // Je construis l'entité Validation
        Validation validation = Validation.builder()
                .decision(request.getDecision())
                .commentaire(request.getCommentaire())
                .demandeConge(demande)
                .manager(manager)
                .build();

        Validation saved = validationRepository.save(validation);

        // Je mets à jour le statut de la demande et je déclenche l'ajustement du solde de jours
        StatutDemande nouveauStatut = request.getDecision() == DecisionType.APPROUVEE
                ? StatutDemande.APPROUVEE
                : StatutDemande.REFUSEE;
        demandeCongeService.changerStatut(demande.getId(), nouveauStatut);

        return toResponse(saved);
    }

    // Je liste toutes les validations enregistrées
    @Override
    public List<ValidationDto.Response> listerToutes() {
        return validationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je récupère une validation par son identifiant unique
    @Override
    public ValidationDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je recherche la décision de validation associée à une demande spécifique
    @Override
    public ValidationDto.Response getByDemandeId(Long demandeId) {
        Validation validation = validationRepository.findByDemandeCongeId(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune validation trouvée pour la demande ID : " + demandeId));
        return toResponse(validation);
    }

    // Je cherche la validation par son ID ou je lève une exception 404
    private Validation findOrThrow(Long id) {
        return validationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Validation introuvable avec l'ID : " + id));
    }

    // Je convertis l'entité Validation vers son DTO de réponse
    private ValidationDto.Response toResponse(Validation v) {
        return ValidationDto.Response.builder()
                .id(v.getId())
                .decision(v.getDecision())
                .commentaire(v.getCommentaire())
                .dateValidation(v.getDateValidation())
                .demandeId(v.getDemandeConge().getId())
                .managerId(v.getManager().getId())
                .managerNomComplet(v.getManager().getUtilisateur().getPrenom() + " " + v.getManager().getUtilisateur().getNom())
                .build();
    }
}
