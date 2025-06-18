package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ArtefactServiceTest {

    private final ArtifactPersistencePort artefactPersistencePort = mock(ArtifactPersistencePort.class);
    private final ArtefactService artefactService = new ArtefactService(artefactPersistencePort);

    @Test
    void getArtifactsByUserIdReturnsArtifacts() {
        Long userId = 1L;
        List<Artifact> expectedArtifacts = List.of(new Artifact());
        when(artefactPersistencePort.findByUserId(userId)).thenReturn(expectedArtifacts);

        List<Artifact> artifacts = artefactService.getArtifactsByUserId(userId);

        assertEquals(expectedArtifacts, artifacts);
    }

    @Test
    void getArtifactsByUserIdReturnsEmptyListWhenNoArtifacts() {
        Long userId = 1L;
        when(artefactPersistencePort.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Artifact> artifacts = artefactService.getArtifactsByUserId(userId);

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void getArtifactsByUserIdThrowsExceptionForNullUserId() {
        assertThrows(InvalidParameterException.class, () -> artefactService.getArtifactsByUserId(null));
    }
}
