package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.TypeConge;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
import apiprojet.apigestiondeconge.service.TypeCongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente les règles relatives à la gestion des types de congés (libellé, quota de jours, description).
 * Je respecte les principes SOLID, l'injection par constructeur et la conversion d'entités en DTO.
 */
@Service
@RequiredArgsConstructor
public class TypeCongeServiceImpl implements TypeCongeService {

    // J'injecte les répertoires nécessaires
    private final TypeCongeRepository typeCongeRepository;
    private final DemandeCongeRepository demandeCongeRepository;

    // Je crée une nouvelle catégorie de congé
    @Override
    @Transactional
    public TypeCongeDto.Response creer(TypeCongeDto.Request request) {
        TypeConge typeConge = TypeConge.builder()
                .libelle(request.getLibelle())
                .nombreJours(request.getNombreJours())
                .description(request.getDescription())
                .build();
        return toResponse(typeCongeRepository.save(typeConge));
    }

    // Je liste tous les types de congés sous forme de DTOs
    @Override
    public List<TypeCongeDto.Response> listerTous() {
        return typeCongeRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // Je cherche un type de congé par son identifiant
    @Override
    public TypeCongeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je modifie un type de congé existant
    @Override
    @Transactional
    public TypeCongeDto.Response modifier(Long id, TypeCongeDto.Request request) {
        TypeConge typeConge = findOrThrow(id);
        typeConge.setLibelle(request.getLibelle());
        typeConge.setNombreJours(request.getNombreJours());
        typeConge.setDescription(request.getDescription());
        return toResponse(typeCongeRepository.save(typeConge));
    }

    // Je supprime un type de congé en réassignant les demandes vers un type équivalent si nécessaire
    @Override
    @Transactional
    public void supprimer(Long id) {
        TypeConge typeToDelete = findOrThrow(id);

        // Je recherche s'il existe un doublon par libellé pour préserver l'intégrité des demandes en cours
        List<TypeConge> duplicates = typeCongeRepository.findByLibelle(typeToDelete.getLibelle());
        TypeConge otherType = duplicates.stream()
                .filter(t -> !t.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (otherType != null) {
            // Si un type identique existe, je rattache les demandes associées à cet autre type avant la suppression
            List<DemandeConge> demandes = demandeCongeRepository.findByTypeCongeId(id);
            for (DemandeConge d : demandes) {
                d.setTypeConge(otherType);
            }
            demandeCongeRepository.saveAll(demandes);
        }

        typeCongeRepository.delete(typeToDelete);
    }

    // Je recherche l'entité TypeConge ou je déclenche une exception 404
    private TypeConge findOrThrow(Long id) {
        return typeCongeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de congé introuvable avec l'ID : " + id));
    }

    // Je transforme l'entité TypeConge en DTO de réponse
    private TypeCongeDto.Response toResponse(TypeConge t) {
        return TypeCongeDto.Response.builder()
                .id(t.getId())
                .libelle(t.getLibelle())
                .nombreJours(t.getNombreJours())
                .description(t.getDescription())
                .build();
    }
}
