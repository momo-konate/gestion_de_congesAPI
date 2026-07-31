package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.entity.Role;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public UtilisateurDto.Response creer(UtilisateurDto.Request request) {
        utilisateurRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Un utilisateur existe déjà avec cet email : " + request.getEmail());
        });

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(request.getMotDePasse()) // TODO: hasher avec BCrypt une fois la sécurité branchée
                .telephone(request.getTelephone())
                .role(request.getRole())
                .actif(true)
                .build();

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    public List<UtilisateurDto.Response> listerTous() {
        return utilisateurRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<UtilisateurDto.Response> listerParRole(Role role) {
        return utilisateurRepository.findByRole(role).stream().map(this::toResponse).toList();
    }

    public UtilisateurDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public UtilisateurDto.Response modifier(Long id, UtilisateurDto.Request request) {
        Utilisateur utilisateur = findOrThrow(id);
        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setRole(request.getRole());
        // Mot de passe volontairement non modifié ici ; prévoir un endpoint dédié plus tard
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = findOrThrow(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void supprimer(Long id) {
        utilisateurRepository.delete(findOrThrow(id));
    }

    private Utilisateur findOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : " + id));
    }

    private UtilisateurDto.Response toResponse(Utilisateur u) {
        return UtilisateurDto.Response.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .telephone(u.getTelephone())
                .actif(u.getActif())
                .role(u.getRole())
                .build();
    }
}
