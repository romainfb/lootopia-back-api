package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateHuntUseCase;
import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HuntAdminService implements CreateHuntUseCase, UpdateHuntUseCase, DeleteHuntUseCase {

    private final HuntPersistencePort huntPersistencePort;

    @Override
    public Hunt createHunt(Hunt hunt) {
        log.info("Creating hunt: {}", hunt.getTitle());
        Hunt saved = huntPersistencePort.saveHunt(hunt);
        log.info("Hunt created with id: {}", saved.getId());
        return saved;
    }

    @Override
    public Hunt updateHunt(Long id, Hunt hunt) {
        log.info("Updating hunt with id: {}", id);
        Hunt existingHunt = huntPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hunt", "id", id));

        if (hunt.getTitle() != null) {
            existingHunt.setTitle(hunt.getTitle());
        }
        if (hunt.getDescription() != null) {
            existingHunt.setDescription(hunt.getDescription());
        }
        if (hunt.getMode() != null) {
            existingHunt.setMode(hunt.getMode());
        }
        if (hunt.getDifficulty() != null) {
            existingHunt.setDifficulty(hunt.getDifficulty());
        }
        if (hunt.getParticipationFees() != null) {
            existingHunt.setParticipationFees(hunt.getParticipationFees());
        }
        if (hunt.getChatEnabled() != null) {
            existingHunt.setChatEnabled(hunt.getChatEnabled());
        }
        if (hunt.getOrganizerId() != null) {
            existingHunt.setOrganizerId(hunt.getOrganizerId());
        }
        return huntPersistencePort.saveHunt(existingHunt);
    }

    @Override
    public void deleteHunt(Long id) {
        log.info("Deleting hunt with id: {}", id);
        Hunt existingHunt = huntPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hunt", "id", id));
        huntPersistencePort.deleteHunt(existingHunt);
    }
}
