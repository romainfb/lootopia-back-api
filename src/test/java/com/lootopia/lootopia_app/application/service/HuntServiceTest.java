package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.out.persistance.HuntRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuntServiceTest {

    @Mock
    private HuntRepository huntRepository;

    @InjectMocks
    private HuntService huntService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createHunt_ShouldSaveAndReturnHunt() {
        Hunt hunt = new Hunt();
        when(huntRepository.save(hunt)).thenReturn(hunt);

        Hunt result = huntService.createHunt(hunt);

        assertNotNull(result);
        assertEquals(hunt, result);
        verify(huntRepository, times(1)).save(hunt);
    }
}
