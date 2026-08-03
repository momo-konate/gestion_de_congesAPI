package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.EmployeDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.Utilisateur;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeServiceTest {

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private DepartementRepository departementRepository;

    @InjectMocks
    private EmployeService employeService;

    private Utilisateur mockUtilisateur;
    private Departement mockDepartement;
    private Employe mockManager;

    @BeforeEach
    void setUp() {
        mockUtilisateur = Utilisateur.builder()
                .id(1L)
                .nom("Diallo")
                .prenom("Moussa")
                .email("moussa@email.com")
                .build();

        mockDepartement = Departement.builder()
                .id(10L)
                .nom("IT")
                .build();

        mockManager = Employe.builder()
                .id(100L)
                .utilisateur(Utilisateur.builder().id(9L).prenom("Cheikh").nom("Gueye").build())
                .build();
    }

    @Test
    void testCreer_UtilisateurNotFound_ThrowsException() {
        EmployeDto.Request request = new EmployeDto.Request();
        request.setUtilisateurId(1L);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            employeService.creer(request);
        });
        verify(employeRepository, never()).save(any());
    }

    @Test
    void testCreer_Success_AvecManager() {
        EmployeDto.Request request = new EmployeDto.Request();
        request.setUtilisateurId(1L);
        request.setDepartementId(10L);
        request.setManagerId(100L);
        request.setPoste("Développeur Java");
        request.setSexe("Homme");
        request.setDateEmbauche(LocalDate.of(2026, 1, 1));
        request.setDateNaissance(LocalDate.of(1995, 5, 5));

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(mockUtilisateur));
        when(departementRepository.findById(10L)).thenReturn(Optional.of(mockDepartement));
        when(employeRepository.findById(100L)).thenReturn(Optional.of(mockManager));
        
        when(employeRepository.save(any(Employe.class))).thenAnswer(invocation -> {
            Employe e = invocation.getArgument(0);
            e.setId(500L);
            return e;
        });

        EmployeDto.Response response = employeService.creer(request);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("Développeur Java", response.getPoste());
        assertEquals("Moussa Diallo", response.getNomComplet());
        assertEquals(10L, response.getDepartementId());
        assertEquals("IT", response.getDepartementNom());
        assertEquals(100L, response.getManagerId());
        assertEquals("Cheikh Gueye", response.getManagerNomComplet());

        verify(employeRepository, times(1)).save(any(Employe.class));
    }

    @Test
    void testListerParManager() {
        Employe sub1 = Employe.builder().id(1L).utilisateur(mockUtilisateur).departement(mockDepartement).manager(mockManager).build();
        when(employeRepository.findByManagerId(100L)).thenReturn(Collections.singletonList(sub1));

        List<EmployeDto.Response> list = employeService.listerParManager(100L);

        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(100L, list.get(0).getManagerId());
        verify(employeRepository, times(1)).findByManagerId(100L);
    }
}
