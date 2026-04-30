package com.lootopia.lootopia_app.application.port.in;

public interface OpenAdChestUseCase {

    StartAdChestResult startAdChest(Long userId);

    OpenAdChestResult openAdChest(Long userId, String adWatchToken);

    record StartAdChestResult(String adWatchToken, long minWatchSeconds) {}

    record OpenAdChestResult(int amount, int newBalance) {}
}
