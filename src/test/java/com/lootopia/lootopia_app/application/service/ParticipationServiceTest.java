package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;
import com.lootopia.lootopia_app.domain.ParticipationStatut;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.UnauthorizedAccessException;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.ParticipationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ParticipationServiceTest {

    @Mock
    private ParticipationPersistencePort participationPersistencePort;
    @Mock
    private ParticipationMapper participationMapper;
    @Mock
    private FetchHuntUseCase fetchHuntUseCase;

    @InjectMocks
    private ParticipationService participationService;

    private ParticipationRequestDto requestDto;
    private Hunt sampleHunt;
    private Participation participation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDto = ParticipationRequestDto.builder()
                .huntId(37)
                .statut(ParticipationStatut.PARTICIPANT)
                .build();

        sampleHunt = Hunt.builder()
                .id(37L)
                .title("Chasse A")
                .numberOfParticipants(5)
                .organizerId(99)
                .build();


        participation = Participation.builder()
                .userId(1)
                .huntId(37)
                .statut(ParticipationStatut.PARTICIPANT)
                .dateInscription(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    void createParticipation_success() {
        when(fetchHuntUseCase.fetchHuntDetail(eq(37L))).thenReturn(sampleHunt);
        when(participationPersistencePort.findByHuntIdAndUserId(eq(37L), eq(1))).thenReturn(Optional.empty());
        when(participationPersistencePort.countByHuntId(eq(37L))).thenReturn(2L);
        when(participationMapper.ParticipationRequestToParticipation(eq(requestDto), eq(1), eq(sampleHunt.getOrganizerId())))
                .thenReturn(participation);
        when(participationPersistencePort.saveParticipation(eq(participation))).thenReturn(participation);

        Participation result = participationService.createParticipation(requestDto, 1);
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(37, result.getHuntId());
        assertEquals(ParticipationStatut.PARTICIPANT, result.getStatut());
    }

    @Test
    void createParticipation_huntNotFound() {
        when(fetchHuntUseCase.fetchHuntDetail(eq(37L))).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                participationService.createParticipation(requestDto, 1)
        );
        assertTrue(exception.getMessage().contains("Hunt"));
    }

    @Test
    void createParticipation_userAlreadyRegistered() {
        when(fetchHuntUseCase.fetchHuntDetail(eq(37L))).thenReturn(sampleHunt);
        when(participationPersistencePort.findByHuntIdAndUserId(eq(37L), eq(1)))
                .thenReturn(Optional.of(participation));

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () ->
                participationService.createParticipation(requestDto, 1)
        );
        assertTrue(exception.getMessage().contains("déjà inscrit"));
    }

    @Test
    void createParticipation_capacityReached() {
        when(fetchHuntUseCase.fetchHuntDetail(eq(37L))).thenReturn(sampleHunt);
        when(participationPersistencePort.findByHuntIdAndUserId(eq(37L), eq(1)))
                .thenReturn(Optional.empty());
        when(participationPersistencePort.countByHuntId(eq(37L))).thenReturn(5L);

        UnauthorizedAccessException exception = assertThrows(UnauthorizedAccessException.class, () ->
                participationService.createParticipation(requestDto, 1)
        );
        assertTrue(exception.getMessage().contains("atteint sa capacité maximale"));
    }

    @Test
    void deleteParticipation_success() {
        Long participationId = 10L;
        when(participationPersistencePort.findById(eq(participationId))).thenReturn(Optional.of(participation));
        doNothing().when(participationPersistencePort).deleteParticipationById(eq(participationId));

        assertDoesNotThrow(() -> participationService.deleteParticipation(participationId));
        verify(participationPersistencePort).deleteParticipationById(eq(participationId));
    }

    @Test
    void deleteParticipation_notFound() {
        Long participationId = 10L;
        when(participationPersistencePort.findById(eq(participationId))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                participationService.deleteParticipation(participationId)
        );
        assertTrue(exception.getMessage().contains("Participation"));
    }

    @Test
    void fetchParticipationsByHunt_success() {
        Integer huntId = 37;
        List<Participation> list = new ArrayList<>();
        list.add(participation);

        when(participationPersistencePort.findByHuntId(eq(huntId))).thenReturn(list);

        List<Participation> result = participationService.findByHuntId(huntId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
    }
}
