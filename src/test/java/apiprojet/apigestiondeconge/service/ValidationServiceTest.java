package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.ValidationDto;
import apiprojet.apigestiondeconge.entity.*;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.ValidationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private ValidationRepository validationRepository;

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private DemandeCongeService demandeCongeService;

    @InjectMocks
    private ValidationService validationService;

    private Employe mockManager;
    private Utilisateur mockUtilisateur;
    private DemandeConge mockDemande;

    @BeforeEach
    void setUp() {
        mockUtilisateur = Utilisateur.builder()
                .id(2L)
                .nom("Sow")
                .prenom("Awa")
                .email("awa.sow@entreprise.com")
                .role(Role.ADMIN)
                .build();

        mockManager = Employe.builder()
                .id(2L)
                .utilisateur(mockUtilisateur)
                .poste("Responsable RH")
                .build();

        mockDemande = DemandeConge.builder()
                .id(10L)
                .statut(StatutDemande.EN_ATTENTE)
                .dateDebut(LocalDate.of(2026, 8, 1))
                .dateFin(LocalDate.of(2026, 8, 5))
                .nombreJours(5)
                .build();
    }

    @Test
    void testValider_AlreadyValidated_ThrowsException() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.APPROUVEE);

        Validation existingValidation = Validation.builder().id(100L).build();
        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.of(existingValidation));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validationService.valider(request);
        });
        assertTrue(exception.getMessage().contains("Cette demande a déjà été validée"));
        verifyNoInteractions(demandeCongeRepository, employeRepository, demandeCongeService);
    }

    @Test
    void testValider_DemandeNotFound_ThrowsException() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.APPROUVEE);

        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.empty());
        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validationService.valider(request);
        });
        assertEquals("Demande introuvable : 10", exception.getMessage());
        verifyNoInteractions(employeRepository, demandeCongeService);
    }

    @Test
    void testValider_DemandeNotPending_ThrowsException() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.APPROUVEE);

        mockDemande.setStatut(StatutDemande.APPROUVEE); // Déjà approuvée

        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.empty());
        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(mockDemande));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validationService.valider(request);
        });
        assertTrue(exception.getMessage().contains("Seule une demande EN_ATTENTE peut être validée"));
        verifyNoInteractions(employeRepository, demandeCongeService);
    }

    @Test
    void testValider_ManagerNotFound_ThrowsException() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.APPROUVEE);

        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.empty());
        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(mockDemande));
        when(employeRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            validationService.valider(request);
        });
        assertEquals("Manager introuvable : 2", exception.getMessage());
        verifyNoInteractions(demandeCongeService);
    }

    @Test
    void testValider_DecisionApprouvee_Success() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.APPROUVEE);
        request.setCommentaire("Bonnes vacances !");

        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.empty());
        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(mockDemande));
        when(employeRepository.findById(2L)).thenReturn(Optional.of(mockManager));
        
        when(validationRepository.save(any(Validation.class))).thenAnswer(invocation -> {
            Validation v = invocation.getArgument(0);
            v.setId(50L);
            return v;
        });

        // Act
        ValidationDto.Response response = validationService.valider(request);

        // Assert
        assertNotNull(response);
        assertEquals(50L, response.getId());
        assertEquals(DecisionType.APPROUVEE, response.getDecision());
        assertEquals("Bonnes vacances !", response.getCommentaire());
        assertEquals(10L, response.getDemandeId());
        assertEquals(2L, response.getManagerId());
        assertEquals("Awa Sow", response.getManagerNomComplet());

        // Doit changer le statut de la demande en APPROUVEE
        verify(demandeCongeService, times(1)).changerStatut(10L, StatutDemande.APPROUVEE);
        verify(validationRepository, times(1)).save(any(Validation.class));
    }

    @Test
    void testValider_DecisionRefusee_Success() {
        // Arrange
        ValidationDto.Request request = new ValidationDto.Request();
        request.setDemandeId(10L);
        request.setManagerId(2L);
        request.setDecision(DecisionType.REFUSEE);
        request.setCommentaire("Surcharge de travail.");

        when(validationRepository.findByDemandeCongeId(10L)).thenReturn(Optional.empty());
        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(mockDemande));
        when(employeRepository.findById(2L)).thenReturn(Optional.of(mockManager));
        
        when(validationRepository.save(any(Validation.class))).thenAnswer(invocation -> {
            Validation v = invocation.getArgument(0);
            v.setId(60L);
            return v;
        });

        // Act
        ValidationDto.Response response = validationService.valider(request);

        // Assert
        assertNotNull(response);
        assertEquals(60L, response.getId());
        assertEquals(DecisionType.REFUSEE, response.getDecision());
        assertEquals("Surcharge de travail.", response.getCommentaire());

        // Doit changer le statut de la demande en REFUSEE
        verify(demandeCongeService, times(1)).changerStatut(10L, StatutDemande.REFUSEE);
        verify(validationRepository, times(1)).save(any(Validation.class));
    }
}
