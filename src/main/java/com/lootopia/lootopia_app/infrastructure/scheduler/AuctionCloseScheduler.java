package com.lootopia.lootopia_app.infrastructure.scheduler;

import com.lootopia.lootopia_app.application.port.in.CloseExpiredAuctionsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionCloseScheduler {
    private final CloseExpiredAuctionsUseCase closeUseCase;

    @Scheduled(fixedDelayString = "PT2S")  // toutes les 2s
    public void closeExpiredAuctions() {
        try {
            int n = closeUseCase.closeExpired();
            if (n > 0) log.info("Closed {} expired auction(s)", n);
        } catch (Exception e) {
            log.error("Auction close scheduler error", e);
        }
    }
}
