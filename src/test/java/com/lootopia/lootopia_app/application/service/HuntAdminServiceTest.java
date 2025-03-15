package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuntAdminServiceTest {

    @Mock
    private HuntPersistencePort huntPersistencePort;

    @InjectMocks
    private HuntAdminService huntAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createHunt_ShouldSaveAndReturnHunt() {
        Hunt hunt = Hunt.builder()
                .title("Nouvelle chasse")
                .description("Description")
                .build();

        when(huntPersistencePort.saveHunt(hunt)).thenReturn(hunt);

        Hunt result = huntAdminService.createHunt(hunt);

        assertNotNull(result);
        assertEquals(hunt, result);
        verify(huntPersistencePort, times(1)).saveHunt(hunt);
    }

    @Test
    void updateHunt_ShouldUpdateAndReturnUpdatedHunt() {
        Long id = 1L;
        Hunt existingHunt = Hunt.builder()
                .id(id)
                .title("Ancien titre")
                .description("Ancienne description")
                .mode("Solo")
                .build();

        Hunt update = Hunt.builder()
                .title("Nouveau titre")
                .build();

        when(huntPersistencePort.findById(id)).thenReturn(Optional.of(existingHunt));
        when(huntPersistencePort.saveHunt(existingHunt)).thenReturn(existingHunt);

        Hunt result = huntAdminService.updateHunt(id, update);

        assertNotNull(result);
        assertEquals("Nouveau titre", result.getTitle());
        assertEquals("Ancienne description", result.getDescription());
        assertEquals("Solo", result.getMode());
        verify(huntPersistencePort, times(1)).findById(id);
        verify(huntPersistencePort, times(1)).saveHunt(existingHunt);
    }

    @Test
    void updateHunt_ShouldThrowException_WhenHuntNotFound() {
        Long id = 1L;
        Hunt update = Hunt.builder()
                .title("Nouveau titre")
                .build();

        when(huntPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> huntAdminService.updateHunt(id, update));
        verify(huntPersistencePort, times(1)).findById(id);
    }

    @Test
    void deleteHunt_ShouldDeleteHunt() {
        Long id = 1L;
        Hunt hunt = Hunt.builder()
                .id(id)
                .title("Chasse à supprimer")
                .build();

        when(huntPersistencePort.findById(id)).thenReturn(Optional.of(hunt));

        huntAdminService.deleteHunt(id);

        verify(huntPersistencePort, times(1)).deleteHunt(hunt);
    }

    @Test
    void deleteHunt_ShouldThrowException_WhenHuntNotFound() {
        Long id = 1L;
        when(huntPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> huntAdminService.deleteHunt(id));
        verify(huntPersistencePort, times(1)).findById(id);
    }
}
