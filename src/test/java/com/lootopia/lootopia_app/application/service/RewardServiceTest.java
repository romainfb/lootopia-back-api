package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardServiceTest {

    private final RewardPersistencePort rewardPersistencePort = mock(RewardPersistencePort.class);
    private final RewardService rewardService = new RewardService(rewardPersistencePort);

    @Test
    void getRewardsByUserIdReturnsRewards() {
        Long userId = 1L;
        List<Reward> expectedRewards = List.of(new Reward());
        when(rewardPersistencePort.findByUserId(userId)).thenReturn(expectedRewards);

        List<Reward> rewards = rewardService.getRewardsByUserId(userId);

        assertEquals(expectedRewards, rewards);
    }

    @Test
    void getRewardsByUserIdReturnsEmptyListWhenNoRewards() {
        Long userId = 1L;
        when(rewardPersistencePort.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Reward> rewards = rewardService.getRewardsByUserId(userId);

        assertTrue(rewards.isEmpty());
    }

    @Test
    void getRewardsByUserIdThrowsExceptionForNullUserId() {
        assertThrows(InvalidParameterException.class, () -> rewardService.getRewardsByUserId(null));
    }

    @Test
    void createRewardWithoutHuntSucceeds() {
        // Create a reward without a hunt (chasseId is null)
        Reward rewardWithoutHunt = Reward.builder()
                .type("Test Type")
                .valeur(java.math.BigDecimal.valueOf(100))
                .description("Test Description")
                .imageUrl("test-image.jpg")
                .rarity("Common")
                .build();

        // Mock the persistence port to return the reward
        when(rewardPersistencePort.save(rewardWithoutHunt)).thenReturn(rewardWithoutHunt);

        // Call the service method
        Reward createdReward = rewardService.createReward(rewardWithoutHunt);

        // Verify the reward was created successfully
        assertEquals(rewardWithoutHunt, createdReward);
    }
}
