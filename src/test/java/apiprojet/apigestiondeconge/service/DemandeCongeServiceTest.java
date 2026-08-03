package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DemandeCongeRequest;
import apiprojet.apigestiondeconge.dto.DemandeCongeResponse;
import apiprojet.apigestiondeconge.entity.*;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
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
class DemandeCongeServiceTest {

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private TypeCongeRepository typeCongeRepository;

    @Mock
    private SoldeCongeService soldeCongeService;

    @InjectMocks
    private DemandeCongeService demandeCongeService;

    private Employe mockEmploye;
    private TypeConge mockTypeConge;
    private Utilisateur mockUtilisateur;

    @BeforeEach
    void setUp() {
        mockUtilisateur = Utilisateur.builder()
                .id(1L)
                .nom("Diallo")
                .prenom("Moussa")
                .email("moussa.diallo@entreprise.com")
                .role(Role.EMPLOYE)
                .build();

        mockEmploye = Employe.builder()
                .id(1L)
                .utilisateur(mockUtilisateur)
                .poste("Développeur")
                .sexe("Homme")
                .build();

        mockTypeConge = TypeConge.builder()
                .id(1L)
                .libelle("Congé Annuel")
                .nombreJours(30)
                .build();
    }

    @Test
    void testCreerDemande_DateFinAvantDateDebut_ThrowsException() {
        // Arrange
        DemandeCongeRequest request = new DemandeCongeRequest();
        request.setEmployeId(1L);
        request.setTypeId(1L);
        request.setDateDebut(LocalDate.of(2026, 8, 10));
        request.setDateFin(LocalDate.of(2026, 8, 5)); // Fin avant début

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            demandeCongeService.creerDemande(request);
        });
        assertEquals("La date de fin doit être après la date de début", exception.getMessage());
        verifyNoInteractions(demandeCongeRepository);
    }

    @Test
    void testCreerDemande_Success() {
        // Arrange
        DemandeCongeRequest request = new DemandeCongeRequest();
        request.setEmployeId(1L);
        request.setTypeId(1L);
        request.setDateDebut(LocalDate.of(2026, 8, 1));
        request.setDateFin(LocalDate.of(2026, 8, 5)); // 5 jours inclusifs
        request.setMotif("Vacances");

        when(employeRepository.findById(1L)).thenReturn(Optional.of(mockEmploye));
        when(typeCongeRepository.findById(1L)).thenReturn(Optional.of(mockTypeConge));
        
        // Simuler le retour du repository avec un ID défini
        when(demandeCongeRepository.save(any(DemandeConge.class))).thenAnswer(invocation -> {
            DemandeConge saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        // Act
        DemandeCongeResponse response = demandeCongeService.creerDemande(request);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(5, response.getNombreJours());
        assertEquals(StatutDemande.EN_ATTENTE, response.getStatut());
        assertEquals("Vacances", response.getMotif());
        verify(demandeCongeRepository, times(1)).save(any(DemandeConge.class));
    }

    @Test
    void testChangerStatut_ToApprouvee_DebitsSolde() {
        // Arrange
        DemandeConge demande = DemandeConge.builder()
                .id(10L)
                .dateDebut(LocalDate.of(2026, 8, 1))
                .dateFin(LocalDate.of(2026, 8, 5))
                .nombreJours(5)
                .statut(StatutDemande.EN_ATTENTE)
                .employe(mockEmploye)
                .typeConge(mockTypeConge)
                .build();

        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(demande));
        when(demandeCongeRepository.save(any(DemandeConge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DemandeCongeResponse response = demandeCongeService.changerStatut(10L, StatutDemande.APPROUVEE);

        // Assert
        assertNotNull(response);
        assertEquals(StatutDemande.APPROUVEE, response.getStatut());
        // Doit appeler soldeCongeService.debiter(employeId=1, annee=2026, jours=5)
        verify(soldeCongeService, times(1)).debiter(1L, 2026, 5);
        verify(soldeCongeService, never()).crediter(anyLong(), anyInt(), anyInt());
        verify(demandeCongeRepository, times(1)).save(demande);
    }

    @Test
    void testChangerStatut_FromApprouveeToRefusee_CreditsSolde() {
        // Arrange
        DemandeConge demande = DemandeConge.builder()
                .id(10L)
                .dateDebut(LocalDate.of(2026, 8, 1))
                .dateFin(LocalDate.of(2026, 8, 5))
                .nombreJours(5)
                .statut(StatutDemande.APPROUVEE) // déjà approuvée
                .employe(mockEmploye)
                .typeConge(mockTypeConge)
                .build();

        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(demande));
        when(demandeCongeRepository.save(any(DemandeConge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DemandeCongeResponse response = demandeCongeService.changerStatut(10L, StatutDemande.REFUSEE);

        // Assert
        assertNotNull(response);
        assertEquals(StatutDemande.REFUSEE, response.getStatut());
        // Doit recréditer le solde car elle était approuvée et passe à un autre statut
        verify(soldeCongeService, times(1)).crediter(1L, 2026, 5);
        verify(soldeCongeService, never()).debiter(anyLong(), anyInt(), anyInt());
        verify(demandeCongeRepository, times(1)).save(demande);
    }

    @Test
    void testChangerStatut_SameStatut_DoesNothing() {
        // Arrange
        DemandeConge demande = DemandeConge.builder()
                .id(10L)
                .dateDebut(LocalDate.of(2026, 8, 1))
                .dateFin(LocalDate.of(2026, 8, 5))
                .nombreJours(5)
                .statut(StatutDemande.APPROUVEE)
                .employe(mockEmploye)
                .typeConge(mockTypeConge)
                .build();

        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(demande));

        // Act
        DemandeCongeResponse response = demandeCongeService.changerStatut(10L, StatutDemande.APPROUVEE);

        // Assert
        assertNotNull(response);
        assertEquals(StatutDemande.APPROUVEE, response.getStatut());
        verifyNoInteractions(soldeCongeService);
        verify(demandeCongeRepository, never()).save(any());
    }

    @Test
    void testAnnuler_Success() {
        // Arrange
        DemandeConge demande = DemandeConge.builder()
                .id(10L)
                .statut(StatutDemande.EN_ATTENTE)
                .build();

        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(demande));

        // Act
        demandeCongeService.annuler(10L);

        // Assert
        assertEquals(StatutDemande.ANNULEE, demande.getStatut());
        verify(demandeCongeRepository, times(1)).save(demande);
    }

    @Test
    void testAnnuler_AlreadyProcessed_ThrowsException() {
        // Arrange
        DemandeConge demande = DemandeConge.builder()
                .id(10L)
                .statut(StatutDemande.APPROUVEE) // déjà traitée
                .build();

        when(demandeCongeRepository.findById(10L)).thenReturn(Optional.of(demande));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            demandeCongeService.annuler(10L);
        });
        assertEquals("Impossible d'annuler une demande déjà traitée (validée ou refusée).", exception.getMessage());
        verify(demandeCongeRepository, never()).save(any());
    }
}
