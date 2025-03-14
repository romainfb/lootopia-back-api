package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.FetchHuntUseCase;
import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HuntService implements FetchHuntUseCase {

    private final HuntPersistencePort huntPersistencePort;
    @Override
    public List<Hunt> fetchAllHunts() {
        return huntPersistencePort.findAll();
    }

    @Override
    public Hunt fetchHuntDetail(Long id) {
        return huntPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hunt", "id", id));
    }

    @Override
    public List<Hunt> fetchHuntsByPriceRange(Double minPrice, Double maxPrice) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getParticipationFees() != null
                        && h.getParticipationFees() >= minPrice
                        && h.getParticipationFees() <= maxPrice)
                .collect(Collectors.toList());
    }

    @Override
    public List<Hunt> fetchHuntsByComposition(boolean complete) {
        return huntPersistencePort.findAll().stream()
                .filter(h -> h.getCompositionComplete() != null && h.getCompositionComplete() == complete)
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
