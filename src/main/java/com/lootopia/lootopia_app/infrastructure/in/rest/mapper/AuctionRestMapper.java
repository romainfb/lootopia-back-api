package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.application.port.in.CreateAuctionUseCase;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AuctionResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.BidResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CreateAuctionRequest;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuctionRestMapper {

    private final UserPersistencePort userPort;

    public CreateAuctionUseCase.CreateAuctionCommand toCommand(CreateAuctionRequest request) {
        return new CreateAuctionUseCase.CreateAuctionCommand(
                request.getArtifactId(),
                request.getStartPrice(),
                request.getMinIncrement(),
                request.getStartsAt(),
                request.getEndsAt()
        );
    }

    public AuctionResponse toResponse(Auction auction) {
        if (auction==null) {
            return null;
        }

        String sellerUsername = userPort.findById(auction.getSellerId())
                .map(UserEntity::getUsername)
                .orElse("Unknown");

        String currentWinnerUsername = Optional.ofNullable(auction.getCurrentWinnerId())
                .flatMap(userPort::findById)
                .map(UserEntity::getUsername)
                .orElse(null);

        return AuctionResponse.builder()
                .id(auction.getId())
                .artifactId(auction.getArtifactId())
                .sellerId(auction.getSellerId())
                .sellerUsername(sellerUsername)
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .minIncrement(auction.getMinIncrement())
                .currentWinnerId(auction.getCurrentWinnerId())
                .currentWinnerUsername(currentWinnerUsername)
                .startsAt(auction.getStartsAt())
                .endsAt(auction.getEndsAt())
                .status(auction.getStatus().name())
                .build();
    }

    public BidResponse toBidResponse(Bid bid) {
        if (bid==null) {
            return null;
        }

        String bidderUsername = userPort.findById(bid.getBidderId())
                .map(UserEntity::getUsername)
                .orElse("Unknown");

        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuctionId())
                .bidderId(bid.getBidderId())
                .bidderUsername(bidderUsername)
                .amount(bid.getAmount())
                .placedAt(bid.getPlacedAt())
                .build();
    }
}
