package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.SoldeConge;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.SoldeCongeRepository;
import apiprojet.apigestiondeconge.service.SoldeCongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente la logique de calcul et de suivi du solde de congé des employés.
 * Je gère les crédits, les débits, l'initialisation automatique et les ajustements manuels.
 */
@Service
@RequiredArgsConstructor
public class SoldeCongeServiceImpl implements SoldeCongeService {

    // J'injecte les repositories nécessaires via le constructeur
    private final SoldeCongeRepository soldeCongeRepository;
    private final EmployeRepository employeRepository;

    // Je crée manuellement un solde de congé annuel pour un employé s'il n'existe pas déjà
    @Override
    @Transactional
    public SoldeCongeDto.Response creer(SoldeCongeDto.Request request) {
        soldeCongeRepository.findByEmployeIdAndAnnee(request.getEmployeId(), request.getAnnee())
                .ifPresent(s -> {
                    throw new IllegalArgumentException(
                            "Un solde existe déjà pour cet employé sur l'année " + request.getAnnee());
                });

        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + request.getEmployeId()));

        SoldeConge solde = SoldeConge.builder()
                .annee(request.getAnnee())
                .joursAcquis(request.getJoursAcquis())
                .joursUtilises(0)
                .joursRestants(request.getJoursAcquis())
                .employe(employe)
                .build();

        return toResponse(soldeCongeRepository.save(solde));
    }

    // Je liste tous les soldes de congés enregistrés
    @Override
    public List<SoldeCongeDto.Response> listerTous() {
        return soldeCongeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je récupère un solde par son identifiant unique
    @Override
    public SoldeCongeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je récupère le solde d'un employé pour une année donnée, ou je l'initialise par défaut à 30 jours s'il n'existe pas
    @Override
    @Transactional
    public SoldeCongeDto.Response getByEmployeEtAnnee(Long employeId, Integer annee) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseGet(() -> {
                    Employe employe = employeRepository.findById(employeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + employeId));
                    SoldeConge newSolde = SoldeConge.builder()
                            .annee(annee)
                            .joursAcquis(30)
                            .joursUtilises(0)
                            .joursRestants(30)
                            .employe(employe)
                            .build();
                    return soldeCongeRepository.save(newSolde);
                });
        return toResponse(solde);
    }

    // Je modifie la quantité de jours acquis et je réactualise le nombre de jours restants
    @Override
    @Transactional
    public SoldeCongeDto.Response modifier(Long id, SoldeCongeDto.Request request) {
        SoldeConge solde = findOrThrow(id);

        int nouveauJoursAcquis = request.getJoursAcquis();
        int joursRestants = nouveauJoursAcquis - solde.getJoursUtilises();

        if (joursRestants < 0) {
            throw new IllegalArgumentException(
                    "Impossible : jours acquis (" + nouveauJoursAcquis + ") inférieur aux jours déjà utilisés (" + solde.getJoursUtilises() + ")");
        }

        solde.setAnnee(request.getAnnee());
        solde.setJoursAcquis(nouveauJoursAcquis);
        solde.setJoursRestants(joursRestants);

        return toResponse(soldeCongeRepository.save(solde));
    }

    // Je débite les jours du solde d'un employé lors de la validation d'une demande de congé
    @Override
    @Transactional
    public void debiter(Long employeId, Integer annee, Integer nombreJours) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseGet(() -> {
                    Employe employe = employeRepository.findById(employeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + employeId));
                    SoldeConge newSolde = SoldeConge.builder()
                            .annee(annee)
                            .joursAcquis(30)
                            .joursUtilises(0)
                            .joursRestants(30)
                            .employe(employe)
                            .build();
                    return soldeCongeRepository.save(newSolde);
                });

        if (solde.getJoursRestants() < nombreJours) {
            throw new IllegalArgumentException(
                    "Solde insuffisant : reste " + solde.getJoursRestants() + " jour(s), demande de " + nombreJours + " jour(s)");
        }

        solde.setJoursUtilises(solde.getJoursUtilises() + nombreJours);
        solde.setJoursRestants(solde.getJoursRestants() - nombreJours);
        soldeCongeRepository.save(solde);
    }

    // Je recrédite les jours dans le solde d'un employé en cas d'annulation ou d'invalidation
    @Override
    @Transactional
    public void crediter(Long employeId, Integer annee, Integer nombreJours) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucun solde de congé configuré pour l'employé " + employeId + " sur l'année " + annee));

        solde.setJoursUtilises(solde.getJoursUtilises() - nombreJours);
        solde.setJoursRestants(solde.getJoursRestants() + nombreJours);
        soldeCongeRepository.save(solde);
    }

    // Je supprime un solde de congé
    @Override
    @Transactional
    public void supprimer(Long id) {
        soldeCongeRepository.delete(findOrThrow(id));
    }

    // Je cherche le solde par ID ou je lève une exception 404
    private SoldeConge findOrThrow(Long id) {
        return soldeCongeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solde introuvable avec l'ID : " + id));
    }

    // Je convertis le SoldeConge en DTO de réponse
    private SoldeCongeDto.Response toResponse(SoldeConge s) {
        return SoldeCongeDto.Response.builder()
                .id(s.getId())
                .annee(s.getAnnee())
                .joursAcquis(s.getJoursAcquis())
                .joursUtilises(s.getJoursUtilises())
                .joursRestants(s.getJoursRestants())
                .employeId(s.getEmploye().getId())
                .build();
    }
}
