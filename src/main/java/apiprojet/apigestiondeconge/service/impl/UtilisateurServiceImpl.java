package apiprojet.apigestiondeconge.service.impl;

import apiprojet.apigestiondeconge.Exceptions.EntityAlreadyExistsException;
import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.entity.Role;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import apiprojet.apigestiondeconge.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * J'implémente les règles relatives à la gestion des utilisateurs, à l'encodage des mots de passe
 * et à la sécurité de l'application. Je respecte les principes SOLID et j'utilise l'injection par constructeur.
 */
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    // J'injecte le répertoire utilisateur et l'encodeur de mot de passe BCrypt
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    // Je crée un nouvel utilisateur en vérifiant l'unicité de son email et en hachant son mot de passe
    @Override
    @Transactional
    public UtilisateurDto.Response creer(UtilisateurDto.Request request) {
        // Je vérifie qu'aucun compte n'existe déjà avec cette adresse email
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException("Un utilisateur existe déjà avec cet email : " + request.getEmail());
        }

        String telephone = (request.getTelephone() != null && request.getTelephone().isBlank())
                ? null
                : request.getTelephone();

        // Je hache le mot de passe de manière sécurisée via PasswordEncoder (BCrypt)
        String motDePasseHache = passwordEncoder.encode(request.getMotDePasse());

        // Je construis la nouvelle entité Utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(motDePasseHache)
                .telephone(telephone)
                .role(request.getRole())
                .actif(true)
                .build();

        return toResponse(utilisateurRepository.save(utilisateur));
    }

    // Je liste la totalité des utilisateurs inscrits
    @Override
    public List<UtilisateurDto.Response> listerTous() {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        if (utilisateurs.isEmpty()) {
            throw new ResourceNotFoundException("Aucun utilisateur trouvé dans la base de données.");
        }

        return utilisateurs.stream().map(this::toResponse).toList();
    }

    // Je filtre les utilisateurs selon leur rôle (EMPLOYE ou ADMIN)
    @Override
    public List<UtilisateurDto.Response> listerParRole(Role role) {
        return utilisateurRepository.findByRole(role).stream().map(this::toResponse).toList();
    }

    // Je cherche un utilisateur par son identifiant unique
    @Override
    public UtilisateurDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // Je mets à jour les informations d'un utilisateur
    @Override
    @Transactional
    public UtilisateurDto.Response modifier(Long id, UtilisateurDto.Request request) {
        Utilisateur utilisateur = findOrThrow(id);

        // Je m'assure que le nouvel email ne crée pas de conflit d'unicité avec un autre utilisateur
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

    // Je désactive un compte utilisateur sans le supprimer définitivement
    @Override
    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = findOrThrow(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    // Je supprime un compte utilisateur de la base de données
    @Override
    @Transactional
    public void supprimer(Long id) {
        utilisateurRepository.delete(findOrThrow(id));
    }

    // Je centralise la recherche d'utilisateur avec gestion d'exception 404
    private Utilisateur findOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + id));
    }

    // Je mappe l'entité Utilisateur vers un DTO Response pour isoler la couche de persistance
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
