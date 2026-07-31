package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.SoldeConge;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.SoldeCongeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoldeCongeService {

    private final SoldeCongeRepository soldeCongeRepository;
    private final EmployeRepository employeRepository;

    @Transactional
    public SoldeCongeDto.Response creer(SoldeCongeDto.Request request) {
        soldeCongeRepository.findByEmployeIdAndAnnee(request.getEmployeId(), request.getAnnee())
                .ifPresent(s -> {
                    throw new IllegalArgumentException(
                            "Un solde existe déjà pour cet employé sur l'année " + request.getAnnee());
                });

        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable : " + request.getEmployeId()));

        SoldeConge solde = SoldeConge.builder()
                .annee(request.getAnnee())
                .joursAcquis(request.getJoursAcquis())
                .joursUtilises(0)
                .joursRestants(request.getJoursAcquis())
                .employe(employe)
                .build();

        return toResponse(soldeCongeRepository.save(solde));
    }

    public List<SoldeCongeDto.Response> listerTous() {
        return soldeCongeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SoldeCongeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public SoldeCongeDto.Response getByEmployeEtAnnee(Long employeId, Integer annee) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun solde trouvé pour l'employé " + employeId + " sur l'année " + annee));
        return toResponse(solde);
    }

    @Transactional
    public SoldeCongeDto.Response modifier(Long id, SoldeCongeDto.Request request) {
        SoldeConge solde = findOrThrow(id);

        // On recalcule joursRestants proprement à partir du nouveau joursAcquis,
        // en conservant joursUtilises tel quel (l'historique de consommation ne change pas).
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

    /**
     * Débite le solde de l'employé pour l'année de la date de début de la demande.
     * Appelé lors du passage d'une demande à APPROUVEE.
     * Lève une exception si le solde est insuffisant ou inexistant.
     */
    @Transactional
    public void debiter(Long employeId, Integer annee, Integer nombreJours) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun solde de congé configuré pour l'employé " + employeId + " sur l'année " + annee));

        if (solde.getJoursRestants() < nombreJours) {
            throw new IllegalArgumentException(
                    "Solde insuffisant : reste " + solde.getJoursRestants() + " jour(s), demande de " + nombreJours + " jour(s)");
        }

        solde.setJoursUtilises(solde.getJoursUtilises() + nombreJours);
        solde.setJoursRestants(solde.getJoursRestants() - nombreJours);
        soldeCongeRepository.save(solde);
    }

    /**
     * Recrédite le solde (ex: une demande APPROUVEE est finalement ANNULEE).
     */
    @Transactional
    public void crediter(Long employeId, Integer annee, Integer nombreJours) {
        SoldeConge solde = soldeCongeRepository.findByEmployeIdAndAnnee(employeId, annee)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun solde de congé configuré pour l'employé " + employeId + " sur l'année " + annee));

        solde.setJoursUtilises(solde.getJoursUtilises() - nombreJours);
        solde.setJoursRestants(solde.getJoursRestants() + nombreJours);
        soldeCongeRepository.save(solde);
    }

    @Transactional
    public void supprimer(Long id) {
        soldeCongeRepository.delete(findOrThrow(id));
    }

    private SoldeConge findOrThrow(Long id) {
        return soldeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solde introuvable : " + id));
    }

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