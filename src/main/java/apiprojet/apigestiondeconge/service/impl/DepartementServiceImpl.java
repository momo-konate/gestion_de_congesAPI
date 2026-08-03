package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.DepartementDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import apiprojet.apigestiondeconge.service.DepartementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente les fonctionnalités du service de gestion des départements.
 * Je respecte le principe DIP en implémentant l'interface DepartementService et SRP en gérant uniquement la logique métier.
 */
@Service
@RequiredArgsConstructor
public class DepartementServiceImpl implements DepartementService {

    // J'injecte le dépôt de données des départements
    private final DepartementRepository departementRepository;

    // Je crée un département à partir du DTO de requête
    @Override
    @Transactional
    public DepartementDto.Response creer(DepartementDto.Request request) {
        Departement departement = Departement.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .build();
        return toResponse(departementRepository.save(departement));
    }

    // Je liste tous les départements sous forme de DTOs
    @Override
    public List<DepartementDto.Response> listerTous() {
        return departementRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je cherche un département par son ID
    @Override
    public DepartementDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je mets à jour les informations d'un département
    @Override
    @Transactional
    public DepartementDto.Response modifier(Long id, DepartementDto.Request request) {
        Departement departement = findOrThrow(id);
        departement.setNom(request.getNom());
        departement.setDescription(request.getDescription());
        return toResponse(departementRepository.save(departement));
    }

    // Je supprime un département par son ID
    @Override
    @Transactional
    public void supprimer(Long id) {
        departementRepository.delete(findOrThrow(id));
    }

    // Je cherche l'entité Departement ou je déclenche une exception 404
    private Departement findOrThrow(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département introuvable avec l'ID : " + id));
    }

    // Je convertis l'entité Departement vers un DTO Response
    private DepartementDto.Response toResponse(Departement d) {
        return DepartementDto.Response.builder()
                .id(d.getId())
                .nom(d.getNom())
                .description(d.getDescription())
                .build();
    }
}
