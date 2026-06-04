package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdChestStartResponse {
    private String adWatchToken;
    private long minWatchSeconds;
    private String adVideoUrl;
    private String videoType;
}
