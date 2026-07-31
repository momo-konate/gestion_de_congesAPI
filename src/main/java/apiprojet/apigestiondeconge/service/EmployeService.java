package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.EmployeDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;

    @Transactional
    public EmployeDto.Response creer(EmployeDto.Request request) {
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + request.getUtilisateurId()));

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable : " + request.getDepartementId()));

        Employe manager = null;
        if (request.getManagerId() != null) {
            manager = employeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager introuvable : " + request.getManagerId()));
        }

        Employe employe = Employe.builder()
                .poste(request.getPoste())
                .sexe(request.getSexe())
                .dateNaissance(request.getDateNaissance())
                .dateEmbauche(request.getDateEmbauche())
                .utilisateur(utilisateur)
                .departement(departement)
                .manager(manager)
                .build();

        return toResponse(employeRepository.save(employe));
    }

    public List<EmployeDto.Response> listerTous() {
        return employeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public EmployeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<EmployeDto.Response> listerParManager(Long managerId) {
        return employeRepository.findByManagerId(managerId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public EmployeDto.Response modifier(Long id, EmployeDto.Request request) {
        Employe employe = findOrThrow(id);

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable : " + request.getDepartementId()));

        Employe manager = null;
        if (request.getManagerId() != null) {
            manager = employeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager introuvable : " + request.getManagerId()));
        }


        employe.setPoste(request.getPoste());
        employe.setSexe(request.getSexe());
        employe.setDateNaissance(request.getDateNaissance());
        employe.setDateEmbauche(request.getDateEmbauche());
        employe.setDepartement(departement);
        employe.setManager(manager);

        return toResponse(employeRepository.save(employe));
    }

    @Transactional
    public void supprimer(Long id) {
        employeRepository.delete(findOrThrow(id));
    }

    private Employe findOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable : " + id));
    }

    private EmployeDto.Response toResponse(Employe e) {
        return EmployeDto.Response.builder()
                .id(e.getId())
                .poste(e.getPoste())
                .sexe(e.getSexe())
                .dateNaissance(e.getDateNaissance())
                .dateEmbauche(e.getDateEmbauche())
                .utilisateurId(e.getUtilisateur().getId())
                .nomComplet(e.getUtilisateur().getPrenom() + " " + e.getUtilisateur().getNom())
                .departementId(e.getDepartement().getId())
                .departementNom(e.getDepartement().getNom())
                .managerId(e.getManager() != null ? e.getManager().getId() : null)
                .managerNomComplet(e.getManager() != null
                        ? e.getManager().getUtilisateur().getPrenom() + " " + e.getManager().getUtilisateur().getNom()
                        : null)
                .build();
    }
}
