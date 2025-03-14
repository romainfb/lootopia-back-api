package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Hunt;

import java.util.Optional;

public interface HuntPersistencePort {
    Hunt saveHunt(Hunt hunt);
    Optional<Hunt> findById(Long id);
    void deleteHunt(Hunt hunt);
}
