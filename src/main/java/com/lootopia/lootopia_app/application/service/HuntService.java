package com.lootopia.lootopia_app.application.service;



import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;

import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HuntService implements FetchHuntUseCase {

    private final HuntPersistencePort huntPersistencePort;
    private final ParticipationPersistencePort participationPersistencePort;

    @Override
    public List<Hunt> fetchAllHunts() {
        log.debug("Fetching all hunts");
        return huntPersistencePort.findAll();
    }

    @Override
    public Hunt fetchHuntDetail(Long id) {
        log.debug("Fetching hunt detail for id: {}", id);
        return huntPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hunt", "id", id));
    }

    @Override
    public List<Hunt> fetchHuntsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice > maxPrice) {
            throw new InvalidParameterException("Le prix minimum ne peut pas être supérieur au prix maximum");
        }
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getParticipationFees() != null
                        && h.getParticipationFees() >= minPrice
                        && h.getParticipationFees() <= maxPrice)
                .collect(Collectors.toList());
    }

    @Override
    public List<Hunt> fetchHuntsByComposition(boolean full) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> {
                    long nbParticipants = participationPersistencePort.countByHuntId(h.getId());

                    if (h.getNumberOfParticipants() == null) {
                        return false;
                    }

                    if (full) {
                        return nbParticipants >= h.getNumberOfParticipants();
                    } else {
                        return nbParticipants < h.getNumberOfParticipants();
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Hunt> fetchHuntsByDuration(Integer duration) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getDuration() <= duration)
                .collect(Collectors.toList());
    }

    @Override
    public List<Hunt> fetchHuntsByMode(String mode) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getMode() != null && h.getMode().equalsIgnoreCase(mode))
                .collect(Collectors.toList());
    }

    @Override
    public List<Hunt> fetchHuntsByWorld(String world) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getWorld() != null && h.getWorld().equalsIgnoreCase(world))
                .collect(Collectors.toList());
    }

}
