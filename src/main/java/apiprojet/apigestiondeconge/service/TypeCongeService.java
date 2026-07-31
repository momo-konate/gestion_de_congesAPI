package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import apiprojet.apigestiondeconge.entity.TypeConge;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeCongeService {

    private final TypeCongeRepository typeCongeRepository;

    @Transactional
    public TypeCongeDto.Response creer(TypeCongeDto.Request request) {
        TypeConge typeConge = TypeConge.builder()
                .libelle(request.getLibelle())
                .nombreJours(request.getNombreJours())
                .description(request.getDescription())
                .build();
        return toResponse(typeCongeRepository.save(typeConge));
    }

    public List<TypeCongeDto.Response> listerTous() {
        return typeCongeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TypeCongeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public TypeCongeDto.Response modifier(Long id, TypeCongeDto.Request request) {
        TypeConge typeConge = findOrThrow(id);
        typeConge.setLibelle(request.getLibelle());
        typeConge.setNombreJours(request.getNombreJours());
        typeConge.setDescription(request.getDescription());
        return toResponse(typeCongeRepository.save(typeConge));
    }

    @Transactional
    public void supprimer(Long id) {
        typeCongeRepository.delete(findOrThrow(id));
    }

    private TypeConge findOrThrow(Long id) {
        return typeCongeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Type de congé introuvable : " + id));
    }

    private TypeCongeDto.Response toResponse(TypeConge t) {
        return TypeCongeDto.Response.builder()
                .id(t.getId())
                .libelle(t.getLibelle())
                .nombreJours(t.getNombreJours())
                .description(t.getDescription())
                .build();
    }
}

