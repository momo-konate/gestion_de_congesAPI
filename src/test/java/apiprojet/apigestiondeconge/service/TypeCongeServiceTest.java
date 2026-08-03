package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.TypeCongeDto;
import apiprojet.apigestiondeconge.entity.DemandeConge;
import apiprojet.apigestiondeconge.entity.TypeConge;
import apiprojet.apigestiondeconge.repository.DemandeCongeRepository;
import apiprojet.apigestiondeconge.repository.TypeCongeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeCongeServiceTest {

    @Mock
    private TypeCongeRepository typeCongeRepository;

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    @InjectMocks
    private TypeCongeService typeCongeService;

    @Test
    void testCreer_Success() {
        TypeCongeDto.Request request = new TypeCongeDto.Request();
        request.setLibelle("RTT");
        request.setNombreJours(10);
        request.setDescription("RTT mensuel");

        TypeConge mockSaved = TypeConge.builder()
                .id(1L)
                .libelle("RTT")
                .nombreJours(10)
                .description("RTT mensuel")
                .build();

        when(typeCongeRepository.save(any(TypeConge.class))).thenReturn(mockSaved);

        TypeCongeDto.Response response = typeCongeService.creer(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("RTT", response.getLibelle());
        verify(typeCongeRepository, times(1)).save(any(TypeConge.class));
    }

    @Test
    void testSupprimer_SansDoublon() {
        TypeConge existing = TypeConge.builder().id(1L).libelle("RTT").build();
        when(typeCongeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(typeCongeRepository.findByLibelle("RTT")).thenReturn(Collections.singletonList(existing));

        typeCongeService.supprimer(1L);

        verify(typeCongeRepository, times(1)).delete(existing);
        verifyNoInteractions(demandeCongeRepository);
    }

    @Test
    void testSupprimer_AvecDoublon_ReassigneDemandes() {
        TypeConge existing = TypeConge.builder().id(1L).libelle("RTT").build();
        TypeConge duplicate = TypeConge.builder().id(2L).libelle("RTT").build();
        
        DemandeConge d1 = DemandeConge.builder().id(100L).typeConge(existing).build();
        DemandeConge d2 = DemandeConge.builder().id(101L).typeConge(existing).build();

        when(typeCongeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(typeCongeRepository.findByLibelle("RTT")).thenReturn(Arrays.asList(existing, duplicate));
        when(demandeCongeRepository.findByTypeCongeId(1L)).thenReturn(Arrays.asList(d1, d2));

        typeCongeService.supprimer(1L);

        // Doit réassigner au doublon et sauvegarder
        assertEquals(duplicate, d1.getTypeConge());
        assertEquals(duplicate, d2.getTypeConge());
        verify(demandeCongeRepository, times(1)).saveAll(Arrays.asList(d1, d2));
        verify(typeCongeRepository, times(1)).delete(existing);
    }
}
