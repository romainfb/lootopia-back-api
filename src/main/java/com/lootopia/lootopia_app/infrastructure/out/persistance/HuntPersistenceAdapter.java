package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HuntPersistenceAdapter implements HuntPersistencePort {

    private final HuntRepository huntRepository;

    @Override
    public Hunt saveHunt(Hunt hunt) {
        return huntRepository.save(hunt);
    }

    @Override
    public Optional<Hunt> findById(Long id) {
        return huntRepository.findById(id);
    }

    @Override
    public void deleteHunt(Hunt hunt) {
        huntRepository.delete(hunt);
    }

    @Override
    public List<Hunt> findAll() {
        return huntRepository.findAll();
    }
}
