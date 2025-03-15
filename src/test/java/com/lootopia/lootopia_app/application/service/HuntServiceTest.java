package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuntServiceTest {

    @Mock
    private HuntPersistencePort huntPersistencePort;

    @Mock
    private ParticipationPersistencePort participationPersistencePort;

    @InjectMocks
    private HuntService huntService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fetchAllHunts_ShouldReturnAllHunts() {
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt de baisé").build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt de malade").build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);

        List<Hunt> result = huntService.fetchAllHunts();

        assertEquals(2, result.size());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntDetail_WhenHuntExists_ShouldReturnHunt() {
        Long id = 1L;
        Hunt hunt = Hunt.builder().id(id).title("BLABLABLABLA  TEST DE MORT").build();
        when(huntPersistencePort.findById(id)).thenReturn(Optional.of(hunt));

        Hunt result = huntService.fetchHuntDetail(id);

        assertNotNull(result);
        assertEquals("BLABLABLABLA  TEST DE MORT", result.getTitle());
        verify(huntPersistencePort).findById(id);
    }

    @Test
    void fetchHuntDetail_WhenHuntDoesNotExist_ShouldThrowResourceNotFoundException() {
        Long id = 1L;
        when(huntPersistencePort.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> huntService.fetchHuntDetail(id));
        assertTrue(exception.getMessage().contains("Hunt not found"));
        verify(huntPersistencePort).findById(id);
    }

    @Test
    void fetchHuntsByPriceRange_WhenValidRange_ShouldReturnFilteredHunts() {
        Double minPrice = 10.0;
        Double maxPrice = 50.0;
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt A").participationFees(30).build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt B").participationFees(60).build(); // hors range
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);

        List<Hunt> result = huntService.fetchHuntsByPriceRange(minPrice, maxPrice);

        assertEquals(1, result.size());
        assertEquals(30, result.getFirst().getParticipationFees());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByPriceRange_WhenMinPriceGreaterThanMaxPrice_ShouldThrowInvalidParameterException() {
        Double minPrice = 60.0;
        Double maxPrice = 50.0;

        InvalidParameterException exception = assertThrows(InvalidParameterException.class,
                () -> huntService.fetchHuntsByPriceRange(minPrice, maxPrice));
        assertEquals("Le prix minimum ne peut pas être supérieur au prix maximum", exception.getMessage());
    }

    @Test
    void fetchHuntsByComposition_WhenFullTrue_ShouldReturnHuntsCompleted() {

        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt FULL").numberOfParticipants(5).build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt Not FULL").numberOfParticipants(10).build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);
        when(participationPersistencePort.countByHuntId(1L)).thenReturn(5L);
        when(participationPersistencePort.countByHuntId(2L)).thenReturn(8L);

        List<Hunt> result = huntService.fetchHuntsByComposition(true);

        assertEquals(1, result.size());
        assertEquals("Hunt FULL", result.get(0).getTitle());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByComposition_WhenFullFalse_ShouldReturnHuntsNotCompleted() {
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt FULL").numberOfParticipants(5).build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt Not FULL").numberOfParticipants(10).build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);
        when(participationPersistencePort.countByHuntId(1L)).thenReturn(5L);
        when(participationPersistencePort.countByHuntId(2L)).thenReturn(8L);

        List<Hunt> result = huntService.fetchHuntsByComposition(false);

        assertEquals(1, result.size());
        assertEquals("Hunt Not FULL", result.getFirst().getTitle());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByComposition_WhenNumberOfParticipantsIsNull_ShouldExcludeHunt() {
        Hunt hunt = Hunt.builder().id(1L).title("Hunt with null nb").numberOfParticipants(null).build();
        when(huntPersistencePort.findAll()).thenReturn(Collections.singletonList(hunt));

        List<Hunt> result = huntService.fetchHuntsByComposition(true);

        assertTrue(result.isEmpty());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByDuration_ShouldReturnHuntsWithinDuration() {
        Integer durationParam = 120;
        Hunt hunt1 = Hunt.builder().id(1L).title("Short Hunt").duration(90).build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Long Hunt").duration(150).build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);

        List<Hunt> result = huntService.fetchHuntsByDuration(durationParam);

        assertEquals(1, result.size());
        assertEquals("Short Hunt", result.getFirst().getTitle());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByMode_ShouldReturnHuntsMatchingModeIgnoringCase() {
        String mode = "Adventure";
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt A").mode("adventure").build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt B").mode("Survival").build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);

        List<Hunt> result = huntService.fetchHuntsByMode(mode);

        assertEquals(1, result.size());
        assertEquals("Hunt A", result.getFirst().getTitle());
        verify(huntPersistencePort).findAll();
    }

    @Test
    void fetchHuntsByWorld_ShouldReturnHuntsMatchingWorldIgnoringCase() {
        String world = "AR";
        Hunt hunt1 = Hunt.builder().id(1L).title("Hunt AR").world("AR").build();
        Hunt hunt2 = Hunt.builder().id(2L).title("Hunt MAP").world("MAP").build();
        List<Hunt> hunts = Arrays.asList(hunt1, hunt2);
        when(huntPersistencePort.findAll()).thenReturn(hunts);

        List<Hunt> result = huntService.fetchHuntsByWorld(world);

        assertEquals(1, result.size());
        assertEquals("Hunt AR", result.getFirst().getTitle());
        verify(huntPersistencePort).findAll();
    }
}
