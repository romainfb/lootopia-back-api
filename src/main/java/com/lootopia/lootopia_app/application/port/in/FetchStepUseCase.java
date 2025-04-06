package com.lootopia.lootopia_app.application.port.in;


public interface FetchStepUseCase {
    List<Step> fetchStepsByHunt(Long huntId);
}
