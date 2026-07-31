package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.DemandeConge;

import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.StatutDemande;
import apiprojet.apigestiondeconge.entity.TypeConge;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeCongeService {

    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final SoldeCongeService soldeCongeService;

    @Transactional
    public DemandeCongeResponse creerDemande(DemandeCongeRequest request) {

        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable : " + request.getEmployeId()));

        TypeConge typeConge = typeCongeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Type de congé introuvable : " + request.getTypeId()));

        // Calcul simple du nombre de jours (inclusif). A affiner plus tard si tu veux exclure les week-ends/jours fériés.
        long nombreJours = ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin()) + 1;

        DemandeConge demande = DemandeConge.builder()
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .nombreJours((int) nombreJours)
                .motif(request.getMotif())
                .statut(StatutDemande.EN_ATTENTE)
                .employe(employe)
                .typeConge(typeConge)
                .build();

        DemandeConge saved = demandeCongeRepository.save(demande);
        return toResponse(saved);
    }

    public List<DemandeCongeResponse> listerToutes() {
        return demandeCongeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DemandeCongeResponse getById(Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));
        return toResponse(demande);
    }

    public List<DemandeCongeResponse> listerParEmploye(Long employeId) {
        return demandeCongeRepository.findByEmployeId(employeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DemandeCongeResponse changerStatut(Long id, StatutDemande nouveauStatut) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));

        StatutDemande ancienStatut = demande.getStatut();

        if (ancienStatut == nouveauStatut) {
            return toResponse(demande); // rien à faire
        }

        Integer annee = demande.getDateDebut().getYear();
        Long employeId = demande.getEmploye().getId();

        // Passage vers APPROUVEE : on débite le solde (uniquement si elle ne l'était pas déjà)
        if (nouveauStatut == StatutDemande.APPROUVEE && ancienStatut != StatutDemande.APPROUVEE) {
            soldeCongeService.debiter(employeId, annee, demande.getNombreJours());
        }

        // Une demande qui était APPROUVEE et qui change de statut (refusée a posteriori, annulée...) : on recrédite
        if (ancienStatut == StatutDemande.APPROUVEE && nouveauStatut != StatutDemande.APPROUVEE) {
            soldeCongeService.crediter(employeId, annee, demande.getNombreJours());
        }

        demande.setStatut(nouveauStatut);
        DemandeConge saved = demandeCongeRepository.save(demande);
        return toResponse(saved);
    }

    @Transactional
    public void annuler(Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable : " + id));

        // Si la demande avait déjà été approuvée (donc déjà débitée), on recrédite le solde avant d'annuler
        if (demande.getStatut() == StatutDemande.APPROUVEE) {
            soldeCongeService.crediter(
                    demande.getEmploye().getId(),
                    demande.getDateDebut().getYear(),
                    demande.getNombreJours());
        }

        demande.setStatut(StatutDemande.ANNULEE);
        demandeCongeRepository.save(demande);
    }

    private DemandeCongeResponse toResponse(DemandeConge d) {
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
                .build();
    }
}
