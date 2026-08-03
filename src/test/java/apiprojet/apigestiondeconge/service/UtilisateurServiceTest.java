package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.Exceptions.EntityAlreadyExistsException;
import apiprojet.apigestiondeconge.Exceptions.ResourceNotFoundException;
import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.entity.Role;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Test
    void testCreer_EmailAlreadyExists_ThrowsException() {
        UtilisateurDto.Request request = new UtilisateurDto.Request();
        request.setEmail("test@email.com");

        when(utilisateurRepository.findByEmail("test@email.com")).thenReturn(Optional.of(new Utilisateur()));

        assertThrows(EntityAlreadyExistsException.class, () -> {
            utilisateurService.creer(request);
        });
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void testCreer_Success() {
        UtilisateurDto.Request request = new UtilisateurDto.Request();
        request.setEmail("new@email.com");
        request.setMotDePasse("password123");
        request.setNom("Nom");
        request.setPrenom("Prenom");
        request.setRole(Role.EMPLOYE);

        Utilisateur saved = Utilisateur.builder()
                .id(10L)
                .email("new@email.com")
                .motDePasse("hashedPassword")
                .nom("Nom")
                .prenom("Prenom")
                .role(Role.EMPLOYE)
                .actif(true)
                .build();

        when(utilisateurRepository.findByEmail("new@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(saved);

        UtilisateurDto.Response response = utilisateurService.creer(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("hashedPassword", saved.getMotDePasse());
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testListerTous_Empty_ThrowsException() {
        when(utilisateurRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> {
            utilisateurService.listerTous();
        });
    }

    @Test
    void testDesactiver_Success() {
        Utilisateur u = Utilisateur.builder().id(1L).actif(true).build();
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(u));

        utilisateurService.desactiver(1L);

        assertFalse(u.getActif());
        verify(utilisateurRepository, times(1)).save(u);
    }
}
