package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.out.persistance.HuntRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HuntService implements CreateHuntUseCase {

    private final HuntRepository huntRepository;

    public Hunt createHunt(Hunt hunt) {
        return huntRepository.save(hunt);
    }
}
