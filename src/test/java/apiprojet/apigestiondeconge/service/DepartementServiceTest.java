package apiprojet.apigestiondeconge.service;

import apiprojet.apigestiondeconge.dto.DepartementDto;
import apiprojet.apigestiondeconge.entity.Departement;
import apiprojet.apigestiondeconge.repository.DepartementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartementServiceTest {

    @Mock
    private DepartementRepository departementRepository;

    @InjectMocks
    private DepartementService departementService;

    @Test
    void testCreer_Success() {
        // Arrange
        DepartementDto.Request request = new DepartementDto.Request();
        request.setNom("RH");
        request.setDescription("Ressources Humaines");

        Departement mockSaved = Departement.builder()
                .id(1L)
                .nom("RH")
                .description("Ressources Humaines")
                .build();

        when(departementRepository.save(any(Departement.class))).thenReturn(mockSaved);

        // Act
        DepartementDto.Response response = departementService.creer(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("RH", response.getNom());
        assertEquals("Ressources Humaines", response.getDescription());
        verify(departementRepository, times(1)).save(any(Departement.class));
    }

    @Test
    void testListerTous_Success() {
        // Arrange
        Departement d1 = Departement.builder().id(1L).nom("RH").description("RH").build();
        Departement d2 = Departement.builder().id(2L).nom("IT").description("IT").build();

        when(departementRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        // Act
        List<DepartementDto.Response> list = departementService.listerTous();

        // Assert
        assertEquals(2, list.size());
        assertEquals("RH", list.get(0).getNom());
        assertEquals("IT", list.get(1).getNom());
        verify(departementRepository, times(1)).findAll();
    }

    @Test
    void testGetById_NotFound_ThrowsException() {
        // Arrange
        when(departementRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departementService.getById(1L);
        });
        assertEquals("Département introuvable : 1", exception.getMessage());
    }

    @Test
    void testModifier_Success() {
        // Arrange
        Departement original = Departement.builder().id(1L).nom("RH").description("Old").build();
        DepartementDto.Request updateRequest = new DepartementDto.Request();
        updateRequest.setNom("RH Modifié");
        updateRequest.setDescription("New description");

        when(departementRepository.findById(1L)).thenReturn(Optional.of(original));
        when(departementRepository.save(any(Departement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DepartementDto.Response response = departementService.modifier(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("RH Modifié", response.getNom());
        assertEquals("New description", response.getDescription());
        verify(departementRepository, times(1)).save(original);
    }

    @Test
    void testSupprimer_Success() {
        // Arrange
        Departement existing = Departement.builder().id(1L).nom("RH").build();
        when(departementRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Act
        departementService.supprimer(1L);

        // Assert
        verify(departementRepository, times(1)).delete(existing);
    }
}
