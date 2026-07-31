package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.ValidationDto;
import apiprojet.apigestiondeconge.entity.*;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.ValidationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ValidationRepository validationRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final DemandeCongeService demandeCongeService;

    /**
     * Enregistre la décision d'un manager sur une demande, et répercute
     * automatiquement le changement de statut (+ débit/crédit du solde
     * via DemandeCongeService.changerStatut).
     */
    @Transactional
    public ValidationDto.Response valider(ValidationDto.Request request) {

        validationRepository.findByDemandeCongeId(request.getDemandeId()).ifPresent(v -> {
            throw new IllegalArgumentException("Cette demande a déjà été validée (id validation : " + v.getId() + ")");
        });

        DemandeConge demande = demandeCongeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + request.getDemandeId()));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new IllegalArgumentException(
                    "Seule une demande EN_ATTENTE peut être validée (statut actuel : " + demande.getStatut() + ")");
        }

        Employe manager = employeRepository.findById(request.getManagerId())
                .orElseThrow(() -> new IllegalArgumentException("Manager introuvable : " + request.getManagerId()));

        Validation validation = Validation.builder()
                .decision(request.getDecision())
                .commentaire(request.getCommentaire())
                .demandeConge(demande)
                .manager(manager)
                .build();

        Validation saved = validationRepository.save(validation);

        // Répercute la décision sur le statut de la demande (gère aussi le débit du solde si APPROUVEE)
        StatutDemande nouveauStatut = request.getDecision() == DecisionType.APPROUVEE
                ? StatutDemande.APPROUVEE
                : StatutDemande.REFUSEE;
        demandeCongeService.changerStatut(demande.getId(), nouveauStatut);

        return toResponse(saved);
    }

    public List<ValidationDto.Response> listerToutes() {
        return validationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ValidationDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public ValidationDto.Response getByDemandeId(Long demandeId) {
        Validation validation = validationRepository.findByDemandeCongeId(demandeId)
                .orElseThrow(() -> new IllegalArgumentException("Aucune validation pour la demande : " + demandeId));
        return toResponse(validation);
    }

    private Validation findOrThrow(Long id) {
        return validationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Validation introuvable : " + id));
    }

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
