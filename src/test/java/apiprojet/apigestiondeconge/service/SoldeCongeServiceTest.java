package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.SoldeCongeDto;
import apiprojet.apigestiondeconge.entity.Employe;
import apiprojet.apigestiondeconge.entity.SoldeConge;
import apiprojet.apigestiondeconge.repository.EmployeRepository;
import apiprojet.apigestiondeconge.repository.SoldeCongeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SoldeCongeServiceTest {

    @Mock
    private SoldeCongeRepository soldeCongeRepository;

    @Mock
    private EmployeRepository employeRepository;

    @InjectMocks
    private SoldeCongeService soldeCongeService;

    @Test
    void testCreer_SoldeAlreadyExists_ThrowsException() {
        SoldeCongeDto.Request request = new SoldeCongeDto.Request();
        request.setEmployeId(1L);
        request.setAnnee(2026);

        when(soldeCongeRepository.findByEmployeIdAndAnnee(1L, 2026)).thenReturn(Optional.of(new SoldeConge()));

        assertThrows(IllegalArgumentException.class, () -> {
            soldeCongeService.creer(request);
        });
        verify(soldeCongeRepository, never()).save(any());
    }

    @Test
    void testDebiter_Success() {
        SoldeConge solde = SoldeConge.builder()
                .id(100L)
                .annee(2026)
                .joursAcquis(30)
                .joursUtilises(5)
                .joursRestants(25)
                .build();

        when(soldeCongeRepository.findByEmployeIdAndAnnee(1L, 2026)).thenReturn(Optional.of(solde));

        soldeCongeService.debiter(1L, 2026, 10);

        assertEquals(15, solde.getJoursRestants());
        assertEquals(15, solde.getJoursUtilises());
        verify(soldeCongeRepository, times(1)).save(solde);
    }

    @Test
    void testDebiter_SoldeInsuffisant_ThrowsException() {
        SoldeConge solde = SoldeConge.builder()
                .id(100L)
                .annee(2026)
                .joursAcquis(30)
                .joursUtilises(25)
                .joursRestants(5)
                .build();

        when(soldeCongeRepository.findByEmployeIdAndAnnee(1L, 2026)).thenReturn(Optional.of(solde));

        assertThrows(IllegalArgumentException.class, () -> {
            soldeCongeService.debiter(1L, 2026, 10);
        });
        verify(soldeCongeRepository, never()).save(solde);
    }

    @Test
    void testCrediter_Success() {
        SoldeConge solde = SoldeConge.builder()
                .id(100L)
                .annee(2026)
                .joursAcquis(30)
                .joursUtilises(15)
                .joursRestants(15)
                .build();

        when(soldeCongeRepository.findByEmployeIdAndAnnee(1L, 2026)).thenReturn(Optional.of(solde));

        soldeCongeService.crediter(1L, 2026, 5);

        assertEquals(20, solde.getJoursRestants());
        assertEquals(10, solde.getJoursUtilises());
        verify(soldeCongeRepository, times(1)).save(solde);
    }
}
