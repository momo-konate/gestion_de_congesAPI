package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.Exceptions.EntityAlreadyExistsException;
import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
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
        // Utilisation d'une exception métier -> Déclenchera une erreur 409 CONFLICT
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException("Un utilisateur existe déjà avec cet email : " + request.getEmail());
        }

        // Nettoyage du téléphone : si vide "", on enregistre null en BDD
        String telephone = (request.getTelephone() != null && request.getTelephone().isBlank())
                ? null
                : request.getTelephone();

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(request.getMotDePasse()) // TODO: hasher avec BCrypt dès que Security est actif
                .telephone(telephone)
                .role(request.getRole())
                .actif(true)
                .build();

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    public List<UtilisateurDto.Response> listerTous() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        if (utilisateurs.isEmpty()) {
            throw new ResourceNotFoundException("Aucun utilisateur trouvé dans la base de données.");
        }

        return utilisateurs.stream().map(this::toResponse).toList();
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

        // Vérifier si le nouvel email n'appartient pas déjà à un AUTRE utilisateur
        utilisateurRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new EntityAlreadyExistsException("L'email " + request.getEmail() + " est déjà utilisé.");
                });

        String telephone = (request.getTelephone() != null && request.getTelephone().isBlank())
                ? null
                : request.getTelephone();

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(telephone);
        utilisateur.setRole(request.getRole());

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

    // Utilisation d'une exception métier -> Déclenchera une erreur 404 NOT FOUND
    private Utilisateur findOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + id));
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