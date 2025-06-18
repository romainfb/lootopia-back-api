package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.HuntPersistencePort;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.HuntEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.HuntPersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.HuntRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HuntPersistenceAdapter implements HuntPersistencePort {

    private final HuntRepository huntRepository;

    @Override
    public Hunt saveHunt(Hunt hunt) {
        HuntEntity entity = HuntPersistenceMapper.toEntity(hunt);
        HuntEntity savedEntity = huntRepository.save(entity);
        return HuntPersistenceMapper.toModel(savedEntity);
    }

    @Override
    public Optional<Hunt> findById(Long id) {
        return huntRepository.findById(id)
                .map(HuntPersistenceMapper::toModel);
    }

    @Override
    public void deleteHunt(Hunt hunt) {
        HuntEntity entity = HuntPersistenceMapper.toEntity(hunt);
        huntRepository.delete(entity);
    }

    @Override
    public List<Hunt> findAll() {
        List<HuntEntity> entities = huntRepository.findAll();
        return entities.stream()
                .map(HuntPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }
}
