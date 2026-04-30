package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchParticipationUseCase;
import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.UnauthorizedAccessException;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.ParticipationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationService implements CreateParticipationUseCase, DeleteParticipationUseCase, FetchParticipationUseCase
{

    private final ParticipationPersistencePort participationPersistencePort;
    private final ParticipationMapper participationMapper;
    private final FetchHuntUseCase fetchHuntUseCase;

    @Override
        public Participation createParticipation(ParticipationRequestDto participationRequest) {
        log.info("Creating participation for user {} in hunt {}", participationRequest.getUserId(), participationRequest.getHuntId());
        Hunt hunt = fetchHuntUseCase.fetchHuntDetail(Long.valueOf(participationRequest.getHuntId()));

        if (hunt == null) {
            throw new ResourceNotFoundException("Hunt","huntId", participationRequest.getHuntId());
        }

        if (participationPersistencePort.findByHuntIdAndUserId(Long.valueOf(participationRequest.getHuntId()), userId).isPresent()) {
            throw new UnauthorizedAccessException("L'utilisateur est déjà inscrit à cette chasse.");
        }

        long currentParticipations = participationPersistencePort.countByHuntId(Long.valueOf(participationRequest.getHuntId()));
        if (hunt.getNumberOfParticipants() != null && currentParticipations >= hunt.getNumberOfParticipants()) {
            throw new UnauthorizedAccessException("La chasse " + hunt.getTitle() + " a atteint sa capacité maximale.");
        }

        return participationPersistencePort.saveParticipation(participationMapper.ParticipationRequestToParticipation(participationRequest, userId, hunt.getOrganizerId()));
    }

    @Override
        public void deleteParticipation(Long participationId) {
        log.info("Deleting participation with id: {}", participationId);
        if (participationPersistencePort.findById(participationId).isEmpty()) {
            throw new ResourceNotFoundException("Participation","participationId", participationId);
        }
        participationPersistencePort.deleteParticipationById(participationId);
    }

    @Override
    public List<Participation> findByHuntId(Integer huntId) {
        return participationPersistencePort.findByHuntId(huntId);
    }
}
