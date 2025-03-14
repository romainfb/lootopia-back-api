package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Hunt;

import java.util.List;

public interface FetchHuntUseCase {
    List<Hunt> fetchAllHunts();
    Hunt fetchHuntDetail(Long id);
    List<Hunt> fetchHuntsByPriceRange(Double minPrice, Double maxPrice);
    List<Hunt> fetchHuntsByComposition(boolean full);
    List<Hunt> fetchHuntsByDuration(Integer duration);
    List<Hunt> fetchHuntsByMode(String mode);
    List<Hunt> fetchHuntsByWorld(String world);
}