package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.EmployeDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import apiprojet.apigestiondeconge.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente le service métier pour la gestion des employés.
 * Je sépare la logique métier de l'accès aux données, j'injecte mes dépendances via le constructeur
 * et je garantis que seules des représentations DTO sont renvoyées aux contrôleurs HTTP.
 */
@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements EmployeService {

    // J'injecte les répertoires nécessaires pour la persistance et les liaisons d'entités
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DepartementRepository departementRepository;

    // Je crée une fiche employé en liant son compte utilisateur, son département et son manager hiérarchique
    @Override
    @Transactional
    public EmployeDto.Response creer(EmployeDto.Request request) {
        // Je vérifie l'existence de l'utilisateur rattaché
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + request.getUtilisateurId()));

        // Je vérifie l'existence du département de rattachement
        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'ID : " + request.getDepartementId()));

        // Je vérifie le manager s'il est spécifié dans la requête
        Employe manager = null;
        if (request.getManagerId() != null) {
            manager = employeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'ID : " + request.getManagerId()));
        }

        // Je construis l'entité Employe à partir des données transmises
        Employe employe = Employe.builder()
                .poste(request.getPoste())
                .sexe(request.getSexe())
                .dateNaissance(request.getDateNaissance())
                .dateEmbauche(request.getDateEmbauche())
                .utilisateur(utilisateur)
                .departement(departement)
                .manager(manager)
                .build();

        // Je sauvegarde l'employé et je retourne la vue DTO associée
        return toResponse(employeRepository.save(employe));
    }

    // Je liste l'ensemble des employés enregistrés
    @Override
    public List<EmployeDto.Response> listerTous() {
        return employeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je cherche un employé par son identifiant unique
    @Override
    public EmployeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je liste tous les employés placés sous l'autorité d'un manager spécifique
    @Override
    public List<EmployeDto.Response> listerParManager(Long managerId) {
        return employeRepository.findByManagerId(managerId).stream()
                .map(this::toResponse)
                .toList();
    }

    // Je modifie la fiche d'un employé existant
    @Override
    @Transactional
    public EmployeDto.Response modifier(Long id, EmployeDto.Request request) {
        Employe employe = findOrThrow(id);

        Departement departement = departementRepository.findById(request.getDepartementId())
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'ID : " + request.getDepartementId()));

        Employe manager = null;
        if (request.getManagerId() != null) {
            manager = employeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager introuvable avec l'ID : " + request.getManagerId()));
        }

        // Je mets à jour les champs de l'employé
        employe.setPoste(request.getPoste());
        employe.setSexe(request.getSexe());
        employe.setDateNaissance(request.getDateNaissance());
        employe.setDateEmbauche(request.getDateEmbauche());
        employe.setDepartement(departement);
        employe.setManager(manager);

        return toResponse(employeRepository.save(employe));
    }

    // Je supprime la fiche d'un employé
    @Override
    @Transactional
    public void supprimer(Long id) {
        employeRepository.delete(findOrThrow(id));
    }

    // Je recherche l'entité Employe ou je déclenche une exception 404 si introuvable
    private Employe findOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé introuvable avec l'ID : " + id));
    }

    // Je mappe l'entité Employe vers le DTO de réponse pour protéger la structure de ma base de données
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
