package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DepartementDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;

    @Transactional
    public DepartementDto.Response creer(DepartementDto.Request request) {
        Departement departement = Departement.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .build();
        return toResponse(departementRepository.save(departement));
    }

    public List<DepartementDto.Response> listerTous() {
        return departementRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DepartementDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public DepartementDto.Response modifier(Long id, DepartementDto.Request request) {
        Departement departement = findOrThrow(id);
        departement.setNom(request.getNom());
        departement.setDescription(request.getDescription());
        return toResponse(departementRepository.save(departement));
    }

    @Transactional
    public void supprimer(Long id) {
        departementRepository.delete(findOrThrow(id));
    }

    private Departement findOrThrow(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable : " + id));
    }

    private DepartementDto.Response toResponse(Departement d) {
        return DepartementDto.Response.builder()
                .id(d.getId())
                .nom(d.getNom())
                .description(d.getDescription())
                .build();
    }
}
